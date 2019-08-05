package com.example.adagiom.bepim;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegistroActivity extends AppCompatActivity  implements InterfazAsyntask{

    Button ingresar;
    Button registrar;
    EditText user;
    EditText pass;
    EditText confpass;
    private ClienteHTTP_POST threadCliente_Post;
    String ruta;
    JSONObject json;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        ingresar = (Button) findViewById(R.id.btn_ingresar);
        registrar = (Button) findViewById(R.id.btn_registrar);
        user = (EditText) findViewById(R.id.user);
        pass = (EditText) findViewById(R.id.pass);
        confpass = (EditText) findViewById(R.id.confpass);
        ingresar.setOnClickListener(onClickListener);
        registrar.setOnClickListener(onClickListener);
        sharedPreferences = getSharedPreferences(getString(R.string.key_preference),MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
    }

    @Override
    public void VerificarMensaje(JSONObject msj) throws JSONException {
        Gson gson = new Gson();
        try{
            Response_Conexion mensaje = gson.fromJson(msj.getString("respuesta"),Response_Conexion.class);
            Log.i("JSON", mensaje.getOpcion().toString());
            if(mensaje.getOpcion().equals("USUARIO REGISTRADO")) {
                try {

                    Thread.sleep(1000);
                    Intent intent = new Intent(this, ListPlataforma.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                //ip.setEnabled(true);
                mostrarToastMake("ERROR DE CONEXIÓN");
            }
        }catch (Exception e){
            mostrarToastMake("ERROR EN SERVIDOR");
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.btn_registrar:
                    json = new JSONObject();
                    String mensaje =Integer.toString(ClienteHTTP_POST.REG_USUARIO);
                    try {
                        json.put("url",ruta);
                        json.put("OPCION",mensaje);
                        json.put("user",user.getText().toString());
                        json.put("pass",pass.getText().toString());
                        json.put("confpass",confpass.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    threadCliente_Post =  new ClienteHTTP_POST(RegistroActivity.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                    break;
                case R.id.btn_ingresar:
                    Intent intent = new Intent(RegistroActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Desea salir de la aplicacion?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Si",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


}
