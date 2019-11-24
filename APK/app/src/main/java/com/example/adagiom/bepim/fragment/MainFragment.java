package com.example.adagiom.bepim.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adagiom.bepim.REST.ClienteHTTP_BATERIA;
import com.example.adagiom.bepim.REST.ClienteHTTP_POST;
import com.example.adagiom.bepim.activity.DrawerPrincipal;
import com.example.adagiom.bepim.interfaz.InterfazAsyntask;
import com.example.adagiom.bepim.interfaz.InterfazBateria;
import com.example.adagiom.bepim.model.Plataforma;
import com.example.adagiom.bepim.R;
import com.example.adagiom.bepim.response.Response_Sectores;
import com.example.adagiom.bepim.model.Sector;
import com.example.adagiom.bepim.adapter.SectorAdapter;
import com.example.adagiom.bepim.activity.ListPlataforma;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


public class MainFragment extends Fragment implements InterfazAsyntask,InterfazBateria {

    private ClienteHTTP_POST threadCliente_Post;
    private ClienteHTTP_BATERIA thread_bateria;
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
    private TextView namePlataforma;
    Plataforma plataforma;
    private Sector destino;
    private Sector origen;
    private Sector carga;
    private Button bateria;
    private ImageView imgBateria;
    private TextView txtBateria;
    private static String TAG = MainFragment.class.getSimpleName();
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
        namePlataforma = (TextView) v.findViewById(R.id.namePlataform);
        listSector = (ListView) v.findViewById(R.id.listSector);
        imgBateria = (ImageView) v.findViewById(R.id.imgBate);
        bateria = (Button) v.findViewById(R.id.imgBateria);
        txtBateria = (TextView) v.findViewById(R.id.textBateria);
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        plataforma = (Plataforma) getArguments().getSerializable("plataforma");
        chipid = plataforma.getChipid();
        namePlataforma.setText(plataforma.getNombre());
        actual.setOnClickListener(onMoveOnSector);
        bateria.setOnClickListener(enviarZonaCarga);
        json = new JSONObject();
        sectorAdapter = new SectorAdapter(getActivity());
        actualizarSector();
        try {
            Thread.sleep(200);
            getNivelBateria();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(thread_bateria!=null)
            thread_bateria.cancel(true);
    }

    View.OnClickListener enviarZonaCarga = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(origen.getId() != carga.getId()) {
                json = new JSONObject();
                String uri = ClienteHTTP_POST.ENVIAR_RUTA;
                try {
                    json.put("url", ruta + uri);
                    json.put("USER", 1);
                    json.put("ID", chipid);
                    json.put("DESDE", origen.getId());
                    json.put("HASTA", carga.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                threadCliente_Post = new ClienteHTTP_POST(MainFragment.this);
                threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, json);

                Log.i("HTTPRequest", json.toString());
            }else{
                mostrarToastMake("La plataforma se encuentra en la zona de carga");
            }
        }
    };
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
                json.put("DESDE",origen.getId());
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
        Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();
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
                            origen = sector;
                            actual.setText(origen.getNombre());
                            sectorIterator.remove();
                        }
                        if (sector.getCarga() == 2) {
                            carga = sector;
                            Log.i(TAG,sector.getNombre());
                        }
                    }
                    sectorAdapter.setData(sectorArrayList);
                    listSector.setAdapter(sectorAdapter);
                    sectorAdapter.setListener(onEnviarPlataforma);
                }else{
                    mostrarToastMake("Debe registrar al menos un Beacon");
                }

            }else if(mensaje.getOpcion().equals("OK")) {
                mostrarToastMake("Atendiendo peticion...");
                actual.setText("EN VIAJE...");
                Intent intent = new Intent(getActivity(),ListPlataforma.class);
                startActivity(intent);
                getActivity().finish();
            }else if(mensaje.getOpcion().equals("OCUPADO")) {
                mostrarToastMake("Existe una peticion pendiente de atención, por favor espere unos minutos e intente nuevamente");
            }else if(mensaje.getOpcion().equals("ACTUAL")) {
                actualizarSector();
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

    public void getNivelBateria(){
        String uri = ClienteHTTP_BATERIA.BATERIA;
        try {
            json.put("url",ruta + uri + "/" + chipid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        thread_bateria =  new ClienteHTTP_BATERIA(MainFragment.this);
        thread_bateria.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
    }

    @Override
    public void actualizarBateria(JSONObject valor) throws JSONException {
        String nivel = valor.getString("respuesta");
        txtBateria.setText(nivel + " %");
        int n = Integer.parseInt(nivel);
        if(n >= 90){
            imgBateria.setImageResource(R.drawable.battery_2);
        }else if(n < 90 && n >= 70){
            imgBateria.setImageResource(R.drawable.battery_1);
        }else if(n < 70 && n >= 50){
            imgBateria.setImageResource(R.drawable.battery_4);
        }else if(n < 50 ){
            imgBateria.setImageResource(R.drawable.battery_3);
        }
    }

    View.OnClickListener onMoveOnSector = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DrawerPrincipal.moveOnSector();
        }
    };
}
