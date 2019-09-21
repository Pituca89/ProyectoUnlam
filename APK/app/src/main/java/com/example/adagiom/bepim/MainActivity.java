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

    private ClienteHTTP_POST threadCliente_Post;
    JSONObject json;
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;
    String uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBarInicial);
        progressBar.setIndeterminate(true);
        sharedPreferences = getSharedPreferences(getString(R.string.key_preference), MODE_PRIVATE);
        json = new JSONObject();
        uri = getString(R.string.url);//sharedPreferences.getString(getString(R.string.path_plataforma),"");
        String mensaje =Integer.toString(ClienteHTTP_POST.VERIFICAR_CONEXION);
        try {
            json.put("url",uri);
            json.put("OPCION",mensaje);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(MainActivity.this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


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
                progressBar.setIndeterminate(false);
                Intent intent = new Intent(this, LoginActivity.class);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.path_plataforma),uri);
                editor.commit();
                startActivity(intent);
                finish();
            }else{
                mostrarToastMake("ERROR DE CONEXIÓN");
            }
        }catch (Exception e){
            mostrarToastMake("ERROR EN SERVIDOR");
        }
    }

}
