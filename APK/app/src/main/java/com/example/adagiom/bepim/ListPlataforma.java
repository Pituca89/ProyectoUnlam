package com.example.adagiom.bepim;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListPlataforma extends AppCompatActivity implements InterfazAsyntask{
    private ClienteHTTP_POST threadCliente_Post;
    private String ruta;
    ListView listPlataforma;
    private PlataformaAdapter plataformaAdapter;
    private ArrayList<Plataforma> plataformaArrayList;
    FloatingActionButton addPlataforma;
    SharedPreferences sharedPreferences;
    static int RESPONSE_QR = 2;
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
            json.put("USER",0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);

        listPlataforma = (ListView) findViewById(R.id.listPlataforma);
        addPlataforma = (FloatingActionButton) findViewById(R.id.addPlataforma);
        addPlataforma.setOnClickListener(agregarPlataforma);
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
            intent.putExtra("plataforma",s);

            startActivity(intent);
            finish();
        }
    };

    View.OnClickListener agregarPlataforma = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.addPlataforma:
                    //startActivityForResult(new Intent(ListPlataforma.this,LecturaQR.class),RESPONSE_QR);
                    new IntentIntegrator(ListPlataforma.this).initiateScan();
                    break;
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(intentResult.getContents() != null){
            LayoutInflater inflater = getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(inflater.inflate(R.layout.dialog_plataforma, null))
                    // Add action buttons
                    .setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // sign in the user ...
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else{
            Log.i("QR","Error al obtener QR");
        }
    }

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
