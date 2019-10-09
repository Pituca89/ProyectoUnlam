package com.example.adagiom.bepim;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class PlataformaFragment extends Fragment implements InterfazAsyntask{

    private OnFragmentInteractionListener mListener;
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
    PlataformaFragment.ListenerThread listenerThread;
    Handler handler;
    private static int HANDLER_MESSAGE_ON = 1;
    private static int HANDLER_MESSAGE_OFF = 0;
    public PlataformaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_plataforma, container, false);
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        iduser = sharedPreferences.getString(getString(R.string.token_user),"");
        refreshPlataforma();
        listPlataforma = (ListView) v.findViewById(R.id.listPlataforma);
        addPlataforma = (FloatingActionButton) v.findViewById(R.id.addPlataforma);
        /*
        if(getIntent().getBooleanExtra("ENVIO",false)){
            handler = handler_espera_notificacion();
            listenerThread = new PlataformaFragment.ListenerThread();
            listenerThread.start();
        }
        */
        addPlataforma.setOnClickListener(agregarPlataforma);
        plataformaAdapter = new PlataformaAdapter(getActivity());
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void mostrarToastMake(String msg) {

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private PlataformaAdapter.OnSelectPlataforma onEnviarPlataforma = new PlataformaAdapter.OnSelectPlataforma() {
        @Override
        public void selectPlataforma(int position) {

            Plataforma s = (Plataforma) plataformaAdapter.getItem(position);
            /**Guardar Plataforma como predeterminada**/
            String uri = ClienteHTTP_POST.OCUPAR_PLATAFORMA;
            try {
                json.put("url",ruta + uri);
                json.put("ID",s.getChipid());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            threadCliente_Post =  new ClienteHTTP_POST(PlataformaFragment.this);
            threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);

        }
    };

    View.OnClickListener agregarPlataforma = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.addPlataforma:
                    IntentIntegrator.forSupportFragment(PlataformaFragment.this).initiateScan();
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(intentResult.getContents() != null){

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_plataforma,null);
            final EditText nombreplataforma = (EditText) view.findViewById(R.id.nameplataforma);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                            threadCliente_Post =  new ClienteHTTP_POST(PlataformaFragment.this);
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
                    refreshPlataforma();
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
                    handler.obtainMessage(HANDLER_MESSAGE_OFF).sendToTarget();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            singleton.setNotification(false);
            handler.obtainMessage(HANDLER_MESSAGE_ON).sendToTarget();

        }
    }
}
