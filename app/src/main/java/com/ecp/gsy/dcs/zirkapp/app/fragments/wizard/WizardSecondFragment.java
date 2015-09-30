package com.ecp.gsy.dcs.zirkapp.app.fragments.wizard;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ecp.gsy.dcs.zirkapp.app.GlobalApplication;
import com.ecp.gsy.dcs.zirkapp.app.R;
import com.ecp.gsy.dcs.zirkapp.app.activities.ManagerWizard;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by ecanaveras on 22/09/2015.
 */
public class WizardSecondFragment extends Fragment {

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private Uri mImageCaptureUri;
    private ImageView imgAvatar;
    private ParseUser currentUser;
    private EditText txtWall;
    private boolean changeAvatar = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_second, container, false);

        currentUser = ParseUser.getCurrentUser();

        inicializarCompUI(view);

        loadDataUser();

        return view;
    }

    private void inicializarCompUI(View view) {
        Button btnNext = (Button) view.findViewById(R.id.btnNext);
        Button btnBack = (Button) view.findViewById(R.id.btnBack);

        txtWall = (EditText) view.findViewById(R.id.txtUserEstado);
        imgAvatar = (ImageView) view.findViewById(R.id.imgUserAvatar);
        imgAvatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onLongClickAvatar(view);
                return false;
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cambia el fragment
                if (saveDataUser()) {
                    ManagerWizard managerWizard = (ManagerWizard) getActivity();
                    managerWizard.mViewPager.setCurrentItem(managerWizard.mViewPager.getCurrentItem() + 1);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Cambia el fragment
                ManagerWizard managerWizard = (ManagerWizard) getActivity();
                managerWizard.mViewPager.setCurrentItem(managerWizard.mViewPager.getCurrentItem() - 1);
            }
        });
    }

    private boolean saveDataUser() {
        if (currentUser != null) {
            ParseFile parseFile;

            String estado = txtWall.getText().toString();
            if (!changeAvatar) {
                Toast.makeText(getActivity(), "Sube una foto de perfil", Toast.LENGTH_SHORT).show();
                return false;
            } else if (imgAvatar.getTag() != null) {
                byte[] byteImage = getByteAvatar((Bitmap) imgAvatar.getTag());
                parseFile = new ParseFile("ParseZAvatar", byteImage != null ? byteImage : new byte[0]);
                parseFile.saveInBackground();
                currentUser.put("avatar", parseFile);

            }
            if (!estado.isEmpty()) {
                currentUser.put("wall", estado);
            }

            currentUser.saveInBackground();
        }

        return true;
    }

    private void loadDataUser() {
        if (currentUser != null) {
            txtWall.setText(currentUser.getString("wall"));
            if (currentUser.getParseFile("avatar") != null) {
                GlobalApplication app = (GlobalApplication) getActivity().getApplicationContext();
                app.setAvatarRounded(currentUser.getParseFile("avatar"), imgAvatar);
                changeAvatar = true;
            }

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
            Toast.makeText(getActivity(), "No es posible CORTAR en tu dispositivo!", Toast.LENGTH_SHORT).show();
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
                    changeAvatar = true;
                }

//                File f = new File(mImageCaptureUri.getPath());
//                if (f.exists()) f.delete();
                break;
        }
    }

}
