package com.example.adagiom.bepim.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.adagiom.bepim.REST.ClienteHTTP_POST;
import com.example.adagiom.bepim.interfaz.InterfazAsyntask;
import com.example.adagiom.bepim.R;
import com.example.adagiom.bepim.response.Response_Conexion;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements InterfazAsyntask {

    private ClienteHTTP_POST threadCliente_Post;
    JSONObject json;
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;
    ProgressDialog progressDialog;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBarInicial);
        progressBar.setIndeterminate(true);
        sharedPreferences = getSharedPreferences(getString(R.string.key_preference), MODE_PRIVATE);
        json = new JSONObject();
        url = getString(R.string.url);//sharedPreferences.getString(getString(R.string.path_plataforma),"");
        String uri = ClienteHTTP_POST.VERIFICAR_CONEXION;
        try {
            json.put("url",url + uri);
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
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
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
                editor.putString(getString(R.string.path_plataforma),url);
                editor.commit();
                startActivity(intent);
                finish();
            }else{
                mostrarToastMake("No es posible establecer la conexión en este momento, por favor espere unos minutos e inténtelo nuevamente");
            }
        }catch (Exception e){

        }
    }

}
