package com.company.a09_permisos.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.company.a09_permisos.R;
import com.company.a09_permisos.databinding.ActivityPhoneBinding;

public class PhoneActivity extends AppCompatActivity {

    private final int CALL_RESQUEST = 3567;
    private ActivityPhoneBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnActionCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.txtNumeroCall.getText().toString().isEmpty()){
                    Toast.makeText(PhoneActivity.this, "No hay teléfono al que llamar", Toast.LENGTH_SHORT);
                }else{
                    //Compruebo la versión de Android en la que estoy
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                        callAction();
                    }else{
                        if(checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                            callAction();
                        }else{
                            requestPermissions(new String[] {Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA}, CALL_RESQUEST);
                        }
                    }
                }
            }
        });
    }

    /**
     * Se dispara solo al finalizar todas las solicitudes de permisos
     * @param requestCode -> es el identificador de la petición que se ha procesado
     * @param permissions -> es el conjunto de permisos que se han pedido
     * @param grantResults -> es el conjunto de resultados(Autorizaciones o no) de los permisos pedidos
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == CALL_RESQUEST){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                callAction();
            }else{
                finish();
            }
        }
    }

    private void callAction(){
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel: "+binding.txtNumeroCall.getText().toString()));
        startActivity(intent);
    }
}