package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.util.services.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.RefreshDataAddressTask;
import com.gc.materialdesign.views.ButtonRectangle;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by Elder on 28/02/2015.
 */
public class EditProfileActivity extends ActionBarActivity {

    private String objectId;
    private ParseFile parseFile;
    private ParseUser currentUser;
    private int avatarRequestCode = 1;
    private String rutaImagen;

    //UI
    private ImageView imgAvatar;
    private EditText txtUsername;
    private EditText txtEstado;
    private EditText txtEmail;
    private EditText txtCiudad;
    private EditText txtNombres;

    private ProgressDialog progressDialog;
    private GlobalApplication globalApplication;
    private byte[] byteImage;

    private Activity activity;
    private Toolbar toolbar;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private Uri mImageCaptureUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        setContentView(R.layout.activity_edit_profile);

        globalApplication = (GlobalApplication) getApplicationContext();
        currentUser = globalApplication.getCurrentUser();
        setTitle(currentUser.getUsername());

        inicializarCompUI();

        loadDatos();
        //new EditProfileTask(this, getResources().getString(R.string.msgLoading)).execute(currentUser);
    }

    private void inicializarCompUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        imgAvatar = (ImageView) findViewById(R.id.imgUserAvatar);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtEstado = (EditText) findViewById(R.id.txtUserEstado);
        txtNombres = (EditText) findViewById(R.id.txtUserNombres);
        txtEmail = (EditText) findViewById(R.id.txtUserEmail);
        txtCiudad = (EditText) findViewById(R.id.txtUserCity);
        //txtCiudad.setText(!globalApplication.getNameLocation().contains("no disponible") ? globalApplication.getNameLocation() : null);//Sugerir ubicación

        //LongClick
        imgAvatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onLongClickAvatar(view);
                return false;
            }
        });

        ButtonRectangle btnSend = (ButtonRectangle) findViewById(R.id.btnUpdateProfile);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDatos();
            }
        });
    }

    private void loadDatos() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.msgLoading));
        progressDialog.show();
        if (currentUser != null) {
            if (currentUser.getParseFile("avatar") != null) {
                imgAvatar.setImageDrawable(GlobalApplication.getAvatar(currentUser));
            }
            txtNombres.setText(currentUser.getString("name"));
            txtEstado.setText(currentUser.getString("wall"));
            txtCiudad.setText(currentUser.getString("city"));
            txtEmail.setText(currentUser.getEmail());
        }
        progressDialog.dismiss();
        if (txtCiudad.getText() == null || txtCiudad.getText().toString().isEmpty()) { //Sugerir Ubicación
            //get Name Ubicacion
            ManagerGPS managerGPS = new ManagerGPS(getApplicationContext());
            if (managerGPS.isEnableGetLocation()) {
                new RefreshDataAddressTask(managerGPS, txtCiudad, true).execute();
            } else {
                managerGPS.gpsShowSettingsAlert();
            }
        }
    }

    /**
     * Guarda los datos del perfil
     */
    private void saveDatos() {
        //Validacion
        int error = 0;
        if (txtNombres.getText().length() == 0) {
            txtNombres.setError(getResources().getString(R.string.msgProfiNameEmty));
            error++;
        }
        if (txtEmail.getText().length() == 0) {
            txtEmail.setError(getResources().getString(R.string.msgSignUpEmailEmpty));
            error++;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail.getText().toString()).matches()) {
            txtEmail.setError(getResources().getString(R.string.msgEmailInvalid));
            error++;
        }

        if (error != 0) {
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.msgSaving));
        progressDialog.show();

        if (currentUser != null) {
            //Imagen cambiada desde la activity
            if (imgAvatar.getTag() != null) {
                byteImage = getByteAvatar((Bitmap) imgAvatar.getTag());
            }

            parseFile = new ParseFile("ParseZAvatar", byteImage != null ? byteImage : new byte[0]);
            parseFile.saveInBackground();

            ParseUser parseUser = currentUser;
            if (byteImage != null) {
                parseUser.put("avatar", parseFile);
            }
            parseUser.put("name", txtNombres.getText().toString());
            parseUser.put("wall", txtEstado.getText().toString());
            parseUser.put("city", txtCiudad.getText().toString());
            parseUser.setEmail(txtEmail.getText().toString());

            parseUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        progressDialog.dismiss();
                        Intent intent = new Intent();
                        intent.putExtra("editprofileOk", true);
                        activity.setResult(Activity.RESULT_OK, intent);
                        activity.finish();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msgProfileUpdate), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msgProfileUpdateError), Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }

    private byte[] getByteAvatar(Bitmap photo) {
        if (photo == null)
            return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    /**
     * Cambiar imagen
     *
     * @param view
     */
    public void onLongClickAvatar(View view) {
        if (view.getId() == R.id.imgUserAvatar) {
            Intent intent = new Intent();
            //Verificar plataforma Android
            if (Build.VERSION.SDK_INT < 20) {
                //Android Jelly Bean 4.3 y Anteriores
                intent.setAction(Intent.ACTION_GET_CONTENT);
            } else {//if (Build.VERSION.SDK_INT > 20) {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            }
            intent.setType("image/*");
            startActivityForResult(intent, PICK_FROM_FILE);
        }
    }

    /**
     * Corta la imagen
     */
    private void cropImage() {
        if (mImageCaptureUri == null) {
            return;
        }
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(mImageCaptureUri, "image/*");

            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("crop", true);
            intent.putExtra("return-data", true);

            startActivityForResult(intent, CROP_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Tu dispositivo no soporta la acción CORTAR!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        switch (requestCode) {
            case PICK_FROM_FILE:
                mImageCaptureUri = data.getData();
                cropImage();
                break;
            case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    imgAvatar.setImageBitmap(photo);
                    imgAvatar.setTag(photo);//Para activar el cambio.
                }

                File f = new File(mImageCaptureUri.getPath());
                if (f.exists()) f.delete();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
