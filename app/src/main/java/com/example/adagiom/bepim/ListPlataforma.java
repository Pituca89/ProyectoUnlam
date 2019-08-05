package com.example.adagiom.bepim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListPlataforma extends AppCompatActivity implements InterfazAsyntask{
    private ClienteHTTP_POST threadCliente_Post;
    private String ruta;
    private ListView listPlataforma;
    private PlataformaAdapter plataformaAdapter;
    private ArrayList<Plataforma> plataformaArrayList;
    SharedPreferences sharedPreferences;
    JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_plataforma);
        sharedPreferences = getSharedPreferences(getString(R.string.key_preference),MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        json = new JSONObject();
        String mensaje =Integer.toString(ClienteHTTP_POST.PLATAFORMA);
        try {
            json.put("url",ruta);
            json.put("OPCION",mensaje);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);

        listPlataforma = (ListView) findViewById(R.id.listPlataforma);
        plataformaAdapter = new PlataformaAdapter(this);
    }

    @Override
    public void mostrarToastMake(String msg) {

    }
    private PlataformaAdapter.OnSelectPlataforma onEnviarPlataforma = new PlataformaAdapter.OnSelectPlataforma() {
        @Override
        public void selectPlataforma(int position) {

            Plataforma s = (Plataforma) plataformaAdapter.getItem(position);
            Intent intent = new Intent(ListPlataforma.this,TabsActivity.class);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.key_plataforma),s.getChipid());
            editor.putInt(getString(R.string.sector_actual),s.getSectoract());
            editor.commit();
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void VerificarMensaje(JSONObject msg) throws JSONException {
        Gson gson = new Gson();
        try{
            Response_Plataforma mensaje = gson.fromJson(msg.getString("respuesta"),Response_Plataforma.class);
            if(mensaje.getOpcion().equals("PLATAFORMA")) {
                plataformaArrayList = mensaje.getPlataforma();
                plataformaAdapter.setData(plataformaArrayList);
                plataformaAdapter.setListener(onEnviarPlataforma);
                listPlataforma.setAdapter(plataformaAdapter);
            }else{
                //ip.setEnabled(true);
                mostrarToastMake("ERROR DE CONEXIÓN");
            }
        }catch (Exception e){
            mostrarToastMake("ERROR EN SERVIDOR");
        }

    }
}
