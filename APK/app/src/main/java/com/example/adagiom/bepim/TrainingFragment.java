package com.example.adagiom.bepim;

import android.annotation.SuppressLint;
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
        Plataforma plataforma = (Plataforma) getArguments().getSerializable("plataforma");
        ruta = "http://"+plataforma.getIp().toString()+"/mode";
        Log.i("ruta",ruta);
        chipid = plataforma.getChipid();
        json = new JSONObject();
        View v = inflater.inflate(R.layout.fragment_training, container, false);
        comenzar = v.findViewById(R.id.btn_comenzar);
        deshacer = v.findViewById(R.id.btn_deshacer);
        confirmar = v.findViewById(R.id.btn_confirmar);

        comenzar.setOnClickListener(onClickTraining);
        deshacer.setOnClickListener(onClickTraining);
        confirmar.setOnClickListener(onClickTraining);

        final JoystickView joystickRight = (JoystickView) v.findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onMove(int angle, int strength) {
                //mTextViewCoordinateRight.setText(
                //       String.format("x%03d:y%03d",
                //                joystickRight.getNormalizedX(),
                //                joystickRight.getNormalizedY())
                //);
                if(strength == 0 && angle == 0){
                        try {
                            json.put("opcion","INST");
                            json.put("sentido","S");
                            //json.put("mac","00:BB");
                            //json.put("angulo",Integer.toString(angle));
                            //json.put("confirma","no");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
                if(strength != 0 && angle < 135 && angle > 45){
                    try {
                        json.put("opcion","INST");
                        json.put("sentido","F");
                        //json.put("mac","00:BB");
                        //json.put("angulo",Integer.toString(angle));
                        //json.put("confirma","no");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(strength != 0 && angle > 135 && angle < 225){
                    try {
                        json.put("opcion","INST");
                        json.put("sentido","I");
                        //json.put("mac","00:BB");
                        //json.put("angulo",Integer.toString(angle));
                        //json.put("confirma","no");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if(strength != 0 && angle < 315 && angle < 45){
                    try {
                        json.put("opcion","INST");
                        json.put("sentido","I");
                        //json.put("mac","00:BB");
                        //json.put("angulo",Integer.toString(angle));
                        //json.put("confirma","no");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
                threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
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

    }
    View.OnClickListener onClickTraining = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_comenzar:
                    try {
                        json.put("url",ruta);
                        json.put("codigo","MODO");
                        json.put("dato","MOD_E");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                    break;
                case R.id.btn_deshacer:

                    break;
                case R.id.btn_confirmar:
                    try {
                        json.put("url",ruta);
                        json.put("codigo","MODO");
                        json.put("dato","MOD_O");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                    break;
            }
        }
    };
}
