package com.ecp.gsy.dcs.zirkapp.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.util.images.RoundedImageView;
import com.ecp.gsy.dcs.zirkapp.app.util.locations.ManagerGPS;
import com.ecp.gsy.dcs.zirkapp.app.util.task.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.util.task.NameLocationTask;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Elder on 28/02/2015.
 */
public class EditProfileActivity extends Activity {

    private String objectId;
    private ParseFile parseFile;
    private ParseUser currentUser;
    private int avatarRequestCode = 1;
    private String rutaImagen;

    //UI
    private RoundedImageView imgAvatar;
    private EditText txtUsername;
    private EditText txtEstado;
    private EditText txtEmail;
    private EditText txtCiudad;
    private EditText txtNombres;

    private ProgressDialog progressDialog;
    private GlobalApplication globalApplication;
    private byte[] byteImage;

    private Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        setContentView(R.layout.activity_edit_profile);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        globalApplication = (GlobalApplication) getApplicationContext();
        currentUser = globalApplication.getCurrentUser();
        setTitle(currentUser.getUsername());

        inicializarCompUI();

        //get Name Ubicacion
        ManagerGPS managerGPS = new ManagerGPS(getApplicationContext());
        managerGPS.obtenertUbicacion();
        new NameLocationTask(getApplicationContext(), managerGPS, txtCiudad).execute();

//        loadDatos();
        new EditProfileTask(this, getResources().getString(R.string.msgLoading)).execute(currentUser);
    }

    private void inicializarCompUI() {
        imgAvatar = (RoundedImageView) findViewById(R.id.imgUserAvatar);
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

        Button btnSend = (Button) findViewById(R.id.btnUpdateProfile);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDatos();
            }
        });
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

        if (currentUser != null) {
            //Imagen
            parseFile = null;
            if (imgAvatar.getTag() != null) {
                byteImage = getByteAvatar((Uri) imgAvatar.getTag());
            } else if (byteImage == null) {
                byteImage = new byte[0];
            }

            parseFile = new ParseFile("ParseZAvatar", byteImage);
            parseFile.saveInBackground();

            //Buscar si existe
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZProfile");
            query.whereEqualTo("user", currentUser);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        if (parseObjects.size() > 0) {
                            objectId = parseObjects.get(0).getObjectId();
                        }
                    }
                    if (objectId != null) {//Existe [ACTUALIZAR]
                        query.getInBackground(objectId, new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if (e == null) {
                                    createOrUpdateProfile(parseFile, parseObject); //Actualiza
                                }
                            }
                        });
                    } else { //No Existe [CREAR]
                        createOrUpdateProfile(parseFile, null); //Crea
                    }
                }
            });
        }
    }

    /**
     * Crea o actualiza los datos del perfil
     *
     * @param parseFile
     * @param inParseObject
     */
    private void createOrUpdateProfile(ParseFile parseFile, ParseObject inParseObject) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.msgSaving));
        progressDialog.show();
        //Datos del Perfil
        ParseObject parseObject = inParseObject;
        if (parseObject == null) {
            parseObject = new ParseObject("ParseZProfile");
        }
        parseObject.put("user", currentUser);
        parseObject.put("name", txtNombres.getText().toString());
        parseObject.put("wall", txtEstado.getText().toString());
        parseObject.put("city", txtCiudad.getText().toString());
        parseObject.put("avatar", parseFile);
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //Actualizar email
                    currentUser.setEmail(txtEmail.getText().toString());
                    currentUser.saveInBackground();
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

    private byte[] getByteAvatar(Uri filePath) {
        InputStream fileInputStream = null;
        try {
            fileInputStream = getApplicationContext().getContentResolver().openInputStream(filePath);
        } catch (FileNotFoundException e) {
            Log.e("FileNotFound", e.getMessage());
            Toast.makeText(getApplicationContext(), "Problemas con tu Avatar...", Toast.LENGTH_SHORT).show();
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
        return stream.toByteArray();
    }

    public void onLongClickAvatar(View view) {
        if (view.getId() == R.id.imgUserAvatar) {
            Intent intent = null;
            //Verificar plataforma Android
            if (Build.VERSION.SDK_INT < 19) {
                //Android Jelly Bean 4.3 y Anteriores
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
            } else {
                //Android Kitkat 4.4 +
                intent = new Intent();
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
            }

            intent.setType("image/*");
            startActivityForResult(intent, avatarRequestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && this.avatarRequestCode == requestCode) {
            imgAvatar.setImageURI(data.getData());
            imgAvatar.setTag(data.getData());
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class EditProfileTask extends AsyncTask<ParseUser, Void, String> {

        private Context context;
        private String messageDialog;
        private List<ParseObject> parseObjects;

        private EditProfileTask(Context context, String messageDialog) {
            this.context = context;
            this.messageDialog = messageDialog;
        }

        @Override
        protected String doInBackground(ParseUser... parseUsers) {
            //Buscamos datos en Parse
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseZProfile");
            query.whereEqualTo("user", parseUsers[0]);
            query.include("user");
            parseObjects = null;
            try {
                parseObjects = query.find();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return "finish";
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(messageDialog);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            if (parseObjects.size() > 0) {
                txtEmail.setText(parseObjects.get(0).getParseUser("user").getEmail().toString());
                txtNombres.setText(parseObjects.get(0).get("name").toString());
                txtEstado.setText(parseObjects.get(0).get("wall").toString());
                txtCiudad.setText(parseObjects.get(0).get("city").toString());

                txtEstado.setSelection(txtEstado.getText().length());

                //Setter Imagen
                byteImage = new byte[0];
                try {
                    byteImage = parseObjects.get(0).getParseFile("avatar").getData();
                    Bitmap bmp = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                    imgAvatar.setImageBitmap(bmp);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            } else {
                if (byteImage == null) {
                    imgAvatar.setImageResource(R.drawable.ic_user_male);
                }
            }
            progressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            if (byteImage == null) {
                imgAvatar.setImageResource(R.drawable.ic_user_male);
            }
        }
    }
}
