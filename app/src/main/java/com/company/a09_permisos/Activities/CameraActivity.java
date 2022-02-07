package com.company.a09_permisos.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.company.a09_permisos.R;
import com.company.a09_permisos.databinding.ActivityCameraBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {


    private ActivityCameraBinding binding;
    private final int CAMERA_MINIATURA = 1;
    private final int GALERIA_ACTION = 2;
    private final int CAMERA_FULL = 3;

    private ActivityResultLauncher<Intent>launcherMiniatura;
    private ActivityResultLauncher<Intent>launcherGaleria;
    private ActivityResultLauncher<Intent>launcherCameraFull;

    private String imgPath;

    /**
     * 1. Path de ficheros: archivo que marca la estructura de carpetas inicial para esta App
     * - ARCHIVO XML:Que contiene el nombre de la carpeta inicial y un nombre para asignar esa ruta
     * 2. FileProvider: Posicionar a la App en esas carpetas L/E archivos en ellas
     * - Proveedor que se conecta al path y ofrece los servicios para los ficheros
     * 3.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.btnMiniaturaCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT <Build.VERSION_CODES.M){
                    cameraMiniaturaAction();
                }else{
                    if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        cameraMiniaturaAction();
                    }else{
                        requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_MINIATURA);
                    }
                }
            }
        });
        launcherMiniatura = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK && result.getData() !=null){
                            Bitmap imagenBMP = (Bitmap) result.getData().getExtras().get("data");
                            binding.imgPhotoCamera.setImageBitmap(imagenBMP);
                        }
                    }
                }
        );

        binding.btnGaleriaCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    galeriaAction();
                }else{
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                        galeriaAction();
                    }else{
                        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, GALERIA_ACTION);
                    }
                }
            }
        });

        launcherGaleria = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
              if(result.getResultCode()==RESULT_OK){
                  Uri imagenSeleccionada = result.getData().getData();
                  binding.imgPhotoCamera.setImageURI(imagenSeleccionada);
              }

            }
        });

        binding.btnFicheroCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    camaraFullAction();
                }else{
                    if (checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        camaraFullAction();
                    }else{
                        String[]permisos = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
                        requestPermissions(permisos, CAMERA_FULL);
                    }
                }
            }
        });

        launcherCameraFull = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                   if(result.getResultCode()==RESULT_OK){
                     Uri uriImagen = Uri.parse(imgPath);
                     binding.imgPhotoCamera.setImageURI(uriImagen);
                     //Agregar a la galeria del archivo
                       Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                       intent.setData(uriImagen);
                       sendBroadcast(intent);

                   }
              }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_MINIATURA){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                cameraMiniaturaAction();
            }else{
                finish();
            }
        }

        if(requestCode == GALERIA_ACTION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                galeriaAction();
            }else{
                finish();
            }
        }
        if(requestCode == CAMERA_FULL){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                camaraFullAction();
            }else{
                finish();
            }
        }
    }


    /**
     * 1.Crear el fichero para pasar el lienzo a la cámara
     * 2.Abrir la cámara
     */
    private void camaraFullAction() {
        try{
            File ficheroImg = crearFichero();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uriImagen = FileProvider.getUriForFile(this,
                    "com.company.a09_permisos", ficheroImg);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagen);
            launcherCameraFull.launch(intent);
        }catch(IOException exception){
            exception.printStackTrace();
            Toast.makeText(this, "ERROR AL CREAR EL FICHERO", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Crea el fichero
     * 1. Nombre Fichero
     * 2. Extension para el fichero
     * 3. Ruta donde se almacena
     * @return -> fichero
     */
    private File crearFichero() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "JPG_"+timeStamp+"_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(
                fileName,
                ".jpg",
                dir
        );
        imgPath = imagen.getAbsolutePath();
        return imagen;
    }


    private void galeriaAction() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        launcherGaleria.launch(intent);
    }

    private void cameraMiniaturaAction(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        launcherMiniatura.launch(intent);
    }
}