package com.example.adagiom.bepim;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Bateria extends AsyncTask<JSONObject,JSONObject,String> {

    static String BATERIA = "/bateria";
    private InterfazBateria caller;
    private Exception mException=null;
    private JSONObject resp;

    public Bateria(android.support.v4.app.Fragment a){this.caller = (InterfazBateria) a;}

    private JSONObject getBateria (JSONObject jsonData)
    {
        HttpURLConnection urlConnection = null;
        resp = new JSONObject();
        try
        {
            URL mUrl = new URL(jsonData.getString("url").toString());

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setRequestMethod("GET");

            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            Log.i("Respuesta URL",Integer.toString(responseCode));
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line);
            }
            urlConnection.disconnect();

            if(responseCode != HttpURLConnection.HTTP_OK)
            {
                resp.put("respuesta","NO_OK");

                return resp;
            }else{
                resp.put("respuesta",result);
                return resp;
            }
        } catch (Exception e)
        {
            mException=e;
        }
        try {
            resp.put("respuesta","NO_OK");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resp;
    }

    @Override
    protected void onProgressUpdate(JSONObject... values) {
        super.onProgressUpdate(values);
        try {
            caller.actualizarBateria(values[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(JSONObject... jsonObjects) {

        if(!isCancelled()) {
            try {
                publishProgress(getBateria(jsonObjects[0]));
                Thread.sleep(60000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
