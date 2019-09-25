package com.example.adagiom.bepim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


public class MainFragment extends Fragment implements InterfazAsyntask{

    private ClienteHTTP_POST threadCliente_Post;
    private String ruta;
    private ListView listSector;
    private static TextView actual;
    private String idactual;
    private SectorAdapter sectorAdapter;
    private ArrayList<Sector> sectorArrayList;
    JSONObject json;
    private String chipid;
    SharedPreferences sharedPreferences;
    ArrayList<Sector> sectors;

    Plataforma plataforma;
    static Sector destino;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main, container, false);
        actual = (TextView) v.findViewById(R.id.sectoract);
        listSector = (ListView) v.findViewById(R.id.listSector);
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        plataforma = (Plataforma) getArguments().getSerializable("plataforma");
        chipid = plataforma.getChipid();
        FirebaseMessaging.getInstance().subscribeToTopic(plataforma.getChipid().toString());
        json = new JSONObject();
        sectorAdapter = new SectorAdapter(getActivity());
        actualizarSector();

        return v;
    }

    private SectorAdapter.OnEnviarPlataforma onEnviarPlataforma = new SectorAdapter.OnEnviarPlataforma() {
        @Override
        public void enviarPlataformaClick(int position) {
            json = new JSONObject();
            destino = (Sector) sectorAdapter.getItem(position);
            String uri = ClienteHTTP_POST.ENVIAR_RUTA;
            try {
                json.put("url",ruta + uri);
                json.put("USER",1);
                json.put("ID", chipid);
                json.put("DESDE",plataforma.getIdsector());
                json.put("HASTA",destino.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            threadCliente_Post =  new ClienteHTTP_POST(MainFragment.this);
            threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);

            Log.i("HTTPRequest",json.toString());
        }
    };

    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void VerificarMensaje(JSONObject msg) throws JSONException {
        Gson gson = new Gson();

        try{
            Response_Sectores mensaje = gson.fromJson(msg.getString("respuesta"),Response_Sectores.class);
            if(mensaje.getOpcion().equals("SECTORES")) {
                sectorArrayList = mensaje.getSectores();
                if(!sectorArrayList.isEmpty()) {
                    Iterator<Sector> sectorIterator = sectorArrayList.iterator();
                    while (sectorIterator.hasNext()) {
                        Sector sector = sectorIterator.next();
                        if (sector.getActual() == 1) {
                            actual.setText(sector.getNombre());
                            sectorIterator.remove();
                        }
                    }
                    sectorAdapter.setData(sectorArrayList);
                    listSector.setAdapter(sectorAdapter);
                    sectorAdapter.setListener(onEnviarPlataforma);
                }else{
                    mostrarToastMake("Debe registrar el sector de carga");
                }

            }else if(mensaje.getOpcion().equals("OK")) {
                mostrarToastMake("Atendiendo peticion...");
                actual.setText("EN VIAJE...");
                Intent intent = new Intent(getActivity(),ListPlataforma.class);
                startActivity(intent);
                getActivity().finish();
            }else if(mensaje.getOpcion().equals("OCUPADO")) {
                mostrarToastMake("Plataforma en uso");
            }else if(mensaje.getOpcion().equals("ACTUAL")) {
                actualizarSector();
            }else {
                mostrarToastMake("ERROR DE CONEXIÓN");
            }

        }catch (Exception e){
            mostrarToastMake("ERROR EN SERVIDOR");
        }
    }

    public void actualizarSector(){
        String uri = ClienteHTTP_POST.SECTORES;
        try {
            json.put("url",ruta + uri);
            json.put("ID",chipid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(MainFragment.this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
    }

}
