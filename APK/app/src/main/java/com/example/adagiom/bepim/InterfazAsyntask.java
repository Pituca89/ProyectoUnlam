package com.example.adagiom.bepim;

import org.json.JSONException;
import org.json.JSONObject;

public interface InterfazAsyntask {
    void mostrarToastMake(String msg);
    void VerificarMensaje(JSONObject msg) throws JSONException;
}
