package com.example.adagiom.bepim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private TextView actual;
    private int idactual;
    private SectorAdapter sectorAdapter;
    private ArrayList<Sector> sectorArrayList;
    JSONObject json;
    private String chipid;
    private String sectact;
    SharedPreferences sharedPreferences;
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

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        chipid = sharedPreferences.getString(getString(R.string.key_plataforma),"");
        idactual = sharedPreferences.getInt(getString(R.string.sector_actual), 0);
        json = new JSONObject();
        /**Envio de mensaje a servidor**/
        String mensaje =Integer.toString(ClienteHTTP_POST.SECTORES);
        try {
            json.put("url",ruta);
            json.put("OPCION",mensaje);
            json.put("ID",chipid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(MainFragment.this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
        /**FIN**/
        actual = (TextView) v.findViewById(R.id.sectoract);
        listSector = (ListView) v.findViewById(R.id.listSector);
        sectorAdapter = new SectorAdapter(getActivity());

        return v;
    }

    private SectorAdapter.OnEnviarPlataforma onEnviarPlataforma = new SectorAdapter.OnEnviarPlataforma() {
        @Override
        public void enviarPlataformaClick(int position) {
            json = new JSONObject();
            Sector s = (Sector) sectorAdapter.getItem(position);
            String mensaje =Integer.toString(ClienteHTTP_POST.ENVIAR_RUTA);
            try {
                json.put("url",ruta);
                json.put("OPCION",mensaje);
                json.put("USER",1);
                json.put("ID", chipid);
                json.put("DESDE",idactual);
                json.put("HASTA",s.getId());
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
            }else if(mensaje.getOpcion().equals("OK")) {
                idactual = mensaje.getActual();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.sector_actual),idactual);
                editor.commit();
            }else{
                mostrarToastMake("ERROR DE CONEXIÓN");
            }
            ArrayList<Sector> sectors = (ArrayList<Sector>) sectorArrayList.clone();
            actualizarSector(sectors);
        }catch (Exception e){
            mostrarToastMake("ERROR EN SERVIDOR");
        }
    }

    public void actualizarSector(ArrayList<Sector> sectors){
        Iterator<Sector> item = sectors.iterator();
        while(item.hasNext()){
            Sector sector = item.next();
            if(sector.getId() == idactual){
                sectact = sector.getNombre();
                item.remove();
            }
        }
        actual.setText(sectact);
        sectorAdapter.setData(sectors);
        sectorAdapter.setListener(onEnviarPlataforma);
        listSector.setAdapter(sectorAdapter);
    }
}
