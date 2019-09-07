package com.example.adagiom.bepim;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;



import io.github.controlwear.virtual.joystick.android.JoystickView;


public class TrainingFragment extends Fragment implements InterfazAsyntask{
    private String ruta;
    private String chipid;
    private ClienteHTTP_POST threadCliente_Post;
    SharedPreferences sharedPreferences;
    Button comenzar;
    Button deshacer;
    Button confirmar;
    JSONObject json;
    public static int FRENTE = 0;
    public static int STOP = 1;
    public static int DERECHA = 2;
    public static int IZQUIERDA = 3;
    public static int ESTADO_INICIAL = -1;
    int estado_anterior = ESTADO_INICIAL;
    String ipPlataforma;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    ListView listSector;
    FloatingActionButton addSector;
    private SectorTrainingAdapter sectorAdapter;
    private ArrayList<Sector> sectorArrayList;
    TextView lblsector;
    TextView lblsectortitle;
    TextView lblsectoractual;
    public TrainingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        Plataforma plataforma = (Plataforma) getArguments().getSerializable("plataforma");
        ipPlataforma = plataforma.getIp().toString();
        chipid = plataforma.getChipid();
        json = new JSONObject();

        View v = inflater.inflate(R.layout.fragment_training, container, false);
        comenzar = v.findViewById(R.id.btn_comenzar);
        deshacer = v.findViewById(R.id.btn_deshacer);
        confirmar = v.findViewById(R.id.btn_confirmar);
        lblsectoractual = v.findViewById(R.id.lbl_sector_actual);

        lblsectoractual.setText("Partiendo del sector: " + plataforma.getSectoract());
        comenzar.setOnClickListener(onClickTraining);
        deshacer.setOnClickListener(onClickTraining);
        confirmar.setOnClickListener(onClickTraining);

        View viewSector = inflater.inflate(R.layout.fragment_sector,null);
        builder = new AlertDialog.Builder(getContext());
        listSector = (ListView) viewSector.findViewById(R.id.listAddSector);
        addSector = (FloatingActionButton) viewSector.findViewById(R.id.addSector);
        lblsector = viewSector.findViewById(R.id.lblsector);
        lblsectortitle = viewSector.findViewById(R.id.lblsectortitle);
        lblsectortitle.setText("Destinos disponibles");
        lblsector.setText("Registrar destino");
        sectorAdapter = new SectorTrainingAdapter(getActivity());
        addSector.setOnClickListener(agregarSector);
        builder.setView(viewSector)
                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                             
                    }
                });

        alertDialog = builder.create();
        refreshSector();
        final JoystickView joystickRight = (JoystickView) v.findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {

            @SuppressLint("DefaultLocale")
            @Override
            public void onMove(int angle, int strength) {

                ruta = "http://"+ipPlataforma+"/training";
                if(strength == 0 && angle == 0){
                    if(estado_anterior != STOP) {
                        estado_anterior = STOP;
                        try {
                            json.put("url",ruta);
                            json.put("opcion", "INST");
                            json.put("sentido", "S");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
                        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                    }
                }
                if(strength != 0 && angle < 135 && angle > 45){
                    if(estado_anterior != FRENTE) {
                        estado_anterior = FRENTE;
                        try {
                            json.put("url",ruta);
                            json.put("opcion", "INST");
                            json.put("sentido", "F");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
                        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                    }
                }
                if(strength != 0 && angle > 135 && angle < 225){
                    if(estado_anterior != IZQUIERDA) {
                        estado_anterior = IZQUIERDA;
                        try {
                            json.put("url",ruta);
                            json.put("opcion", "INST");
                            json.put("sentido", "I");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
                        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                    }
                }
                if(strength != 0 && angle < 315 && angle < 45){
                    if(estado_anterior != DERECHA) {
                        estado_anterior = DERECHA;
                        try {
                            json.put("url",ruta);
                            json.put("opcion", "INST");
                            json.put("sentido", "D");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
                        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                    }
                }
            }
        });
        return v;
        /**
         * IP/training
         * opcion: INST
         * sentido: F-D-I-R
         * mac: mac registrada en cada sector
         * confirma: SI - NO
         *
         * IP/mode
         * codigo: MODO
         * dato: MOD_O - MOD_E -> Al seleccionar COMENZAR: MOD_E - Al seleccionar CONFIRMAR: MOD_O
         * **/
    }

    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void VerificarMensaje(JSONObject msg) throws JSONException {
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
    View.OnClickListener onClickTraining;

    {
        onClickTraining = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ruta = "http://" + ipPlataforma + "/mode";
                switch (view.getId()) {
                    case R.id.btn_comenzar:
                        try {
                            json.put("url", ruta);
                            json.put("codigo", "MODO");
                            json.put("dato", "MOD_E");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        threadCliente_Post = new ClienteHTTP_POST(TrainingFragment.this);
                        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, json);

                        break;
                    case R.id.btn_deshacer:

                        break;
                    case R.id.btn_confirmar:
                    /*
                    try {
                        json.put("url",ruta);
                        json.put("codigo","MODO");
                        json.put("dato","MOD_O");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                    */

                    alertDialog.show();
                    break;
                }
            }
        };
    }

    public void refreshSector(){
        json = new JSONObject();
        String mensaje =Integer.toString(ClienteHTTP_POST.SECTORES);
        try {
            json.put("url",getString(R.string.url));
            json.put("OPCION",mensaje);
            json.put("ID",chipid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
    }

    View.OnClickListener agregarSector = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.addSector:
                    IntentIntegrator.forSupportFragment(TrainingFragment.this).initiateScan();
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
                                json.put("url",getString(R.string.url));
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
                            Log.i("JSONT",json.toString());
                            threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
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
