package com.example.adagiom.bepim;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class TrainingFragment extends Fragment implements InterfazAsyntask{
    private String ruta;
    private int chipid;
    SharedPreferences sharedPreferences;
    public TrainingFragment() {
        // Required empty public constructor
    }

    public static TrainingFragment newInstance(String param1, String param2) {
        TrainingFragment fragment = new TrainingFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        chipid = sharedPreferences.getInt(getString(R.string.key_plataforma),0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        chipid = sharedPreferences.getInt(getString(R.string.key_plataforma),0);
        View v = inflater.inflate(R.layout.fragment_training, container, false);
        return v;
    }

    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void VerificarMensaje(JSONObject msg) throws JSONException {

    }
}
