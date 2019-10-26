package com.example.adagiom.bepim;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
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
    String iduser;
    ListenerThread listenerThread;
    Handler handler;
    private static int HANDLER_MESSAGE_ON = 1;
    private static int HANDLER_MESSAGE_OFF = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_plataforma);
        sharedPreferences = getSharedPreferences(getString(R.string.key_preference),MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        iduser = sharedPreferences.getString(getString(R.string.token_user),"");
        refreshPlataforma();
        listPlataforma = (ListView) findViewById(R.id.listPlataforma);
        addPlataforma = (FloatingActionButton) findViewById(R.id.addPlataforma);

        handler = handler_espera_notificacion();
        listenerThread = new ListenerThread();
        listenerThread.start();

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
            Intent intent = new Intent(ListPlataforma.this,DrawerPrincipal.class);
            intent.putExtra("plataforma",s);
            String uri = ClienteHTTP_POST.OCUPAR_PLATAFORMA;
            try {
                json.put("url",ruta + uri);
                json.put("ID",s.getChipid());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            threadCliente_Post =  new ClienteHTTP_POST(ListPlataforma.this);
            threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
            //listenerThread.stop();
            startActivity(intent);
            finish();
        }
    };


    View.OnClickListener agregarPlataforma = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.addPlataforma:
                    new IntentIntegrator(ListPlataforma.this).initiateScan();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(intentResult.getContents() != null){

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_plataforma,null);
            final EditText nombreplataforma = (EditText) view.findViewById(R.id.nameplataforma);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            json = new JSONObject();
                            String uri = ClienteHTTP_POST.ASOCIAR_PLATAFORMA;

                            try {
                                json.put("url",ruta + uri);
                                json.put("USER",iduser);
                                json.put("ID",intentResult.getContents());
                                if(nombreplataforma.getText().toString() != ""){
                                    json.put("NOMBRE",nombreplataforma.getText().toString());
                                }else{
                                    json.put("NOMBRE",intentResult.getContents());
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            FirebaseMessaging.getInstance().subscribeToTopic(intentResult.getContents().toString());
                            threadCliente_Post =  new ClienteHTTP_POST(ListPlataforma.this);
                            threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
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
        try {
            Response_Plataforma mensaje = gson.fromJson(msg.getString("respuesta"), Response_Plataforma.class);
            if (mensaje.getOpcion().equals("PLATAFORMA")) {
                plataformaArrayList = mensaje.getPlataforma();
                plataformaAdapter.setData(plataformaArrayList);
                plataformaAdapter.setListener(onEnviarPlataforma);
                listPlataforma.setAdapter(plataformaAdapter);
            }else if(mensaje.getOpcion().contains("EXISTS")){

                mostrarToastMake("Plataforma no registrada");

            }else if(mensaje.getOpcion().contains("DUPLICADO")){

                mostrarToastMake("Plataforma duplicada");

            }else if(mensaje.getOpcion().contains("OK")){

                refreshPlataforma();
                mostrarToastMake("Plataforma registrada correctamente");

            }else{
                //ip.setEnabled(true);
                mostrarToastMake("ERROR DE CONEXIÓN");
            }
        }catch (Exception e){
            mostrarToastMake("ERROR EN SERVIDOR");
        }

    }
    public void refreshPlataforma(){
        json = new JSONObject();
        String uri = ClienteHTTP_POST.PLATAFORMA;
        try {
            json.put("url",ruta + uri);
            json.put("USER",iduser);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
    }

    public Handler handler_espera_notificacion(){
        return new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == HANDLER_MESSAGE_ON){
                    mostrarToastMake("Su plataforma llego a destino");
                    Log.i("Notificacion","Recibi mensaje");
                    try {
                        Thread.sleep(1000);
                        refreshPlataforma();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private class ListenerThread extends Thread{
        NotificationSingleton singleton = new NotificationSingleton().getInstance();

        @Override
        public void run() {
            super.run();

            while(!singleton.isNotification()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            singleton.setNotificationFalse();
            handler.obtainMessage(HANDLER_MESSAGE_ON).sendToTarget();

        }
    }
}
