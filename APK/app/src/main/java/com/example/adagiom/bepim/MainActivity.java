package com.example.adagiom.bepim;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements InterfazAsyntask{

    private static Button connect;
    private static EditText ip;
    private ClienteHTTP_POST threadCliente_Post;
    private String ruta = "http://";
    JSONObject json;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect = (Button) findViewById(R.id.btnConnect);
        ip = (EditText) findViewById(R.id.txtIP);
        connect.setOnClickListener(onClickListener);
        sharedPreferences = getSharedPreferences(getString(R.string.key_preference), MODE_PRIVATE);
        ip.setText(sharedPreferences.getString(getString(R.string.path_plataforma),""));
        new Firebase_ID_Service().onTokenRefresh();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.btnConnect:
                    json = new JSONObject();
                    ip.setEnabled(false);
                    String uri = ip.getText().toString();
                    String mensaje =Integer.toString(ClienteHTTP_POST.VERIFICAR_CONEXION);
                    try {
                        json.put("url",uri);
                        json.put("OPCION",mensaje);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    threadCliente_Post =  new ClienteHTTP_POST(MainActivity.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void VerificarMensaje(JSONObject msj) throws JSONException {
        Gson gson = new Gson();
        try{
            Response_Conexion mensaje = gson.fromJson(msj.getString("respuesta"),Response_Conexion.class);
            if(mensaje.getOpcion().equals("CONECTADO")) {
                Intent intent = new Intent(this, LoginActivity.class);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.path_plataforma),ip.getText().toString());
                editor.commit();
                startActivity(intent);
                finish();
            }else{
                ip.setEnabled(true);
                mostrarToastMake("ERROR DE CONEXIÓN");
            }
        }catch (Exception e){
            mostrarToastMake("ERROR EN SERVIDOR");
        }
    }

}
