package com.example.adagiom.bepim;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class SectorFragment extends Fragment implements InterfazAsyntask{

    private ClienteHTTP_POST threadCliente_Post;
    private String ruta;
    ListView listSector;
    FloatingActionButton addSector;
    private SectorListAdapter sectorAdapter;
    private ArrayList<Sector> sectorArrayList;
    JSONObject json;
    private String chipid;
    SharedPreferences sharedPreferences;

    public SectorFragment() {
        // Required empty public constructor
    }


    public static SectorFragment newInstance(String param1, String param2) {
        SectorFragment fragment = new SectorFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sector, container, false);
        addSector = (FloatingActionButton) v.findViewById(R.id.addSector);
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        Plataforma plataforma = (Plataforma) getArguments().getSerializable("plataforma");
        chipid = plataforma.getChipid();

        json = new JSONObject();
        refreshSector();
        listSector = (ListView) v.findViewById(R.id.listAddSector);
        sectorAdapter = new SectorListAdapter(getActivity());
        addSector.setOnClickListener(agregarSector);
        return v;
    }

    View.OnClickListener agregarSector = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.addSector:
                    IntentIntegrator.forSupportFragment(SectorFragment.this).initiateScan();
                break;
            }
        }
    };
    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void VerificarMensaje(JSONObject msg)  throws JSONException{
        Gson gson = new Gson();
        try{
            Response_Sectores mensaje = gson.fromJson(msg.getString("respuesta"),Response_Sectores.class);
            Log.i("Sector",mensaje.getOpcion().toString());
            if(mensaje.getOpcion().equals("SECTORES")) {
                sectorArrayList = mensaje.getSectores();
                sectorAdapter.setData(sectorArrayList);
                listSector.setAdapter(sectorAdapter);
            }else if(mensaje.getOpcion().contains("DUPLICADO")){

                mostrarToastMake("Plataforma duplicada");

            }else if(mensaje.getOpcion().contains("OK")){

                refreshSector();
                mostrarToastMake("Sector registrado correctamente");
            }else{
                mostrarToastMake("ERROR DE CONEXIÓN");
            }

        }catch (Exception e){
            mostrarToastMake("NO PRESENTA SECTORES REGISTRADOS");
        }
    }
    public void refreshSector(){
        json = new JSONObject();
        String mensaje =Integer.toString(ClienteHTTP_POST.SECTORES);
        try {
            json.put("url",ruta);
            json.put("OPCION",mensaje);
            json.put("ID",chipid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(intentResult.getContents() != null){
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_plataforma,null);
            final EditText nombreSector = (EditText) view.findViewById(R.id.nameplataforma);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            json = new JSONObject();
                            String mensaje =Integer.toString(ClienteHTTP_POST.ASOCIAR_SECTOR);

                            try {
                                json.put("url",ruta);
                                json.put("OPCION",mensaje);
                                json.put("ID",chipid);
                                json.put("MAC",intentResult.getContents());
                                if(nombreSector.getText().toString() != ""){
                                    json.put("NOMBRE",nombreSector.getText().toString());
                                }else{
                                    json.put("NOMBRE",intentResult.getContents());
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            threadCliente_Post =  new ClienteHTTP_POST(SectorFragment.this);
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


}
