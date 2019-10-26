package com.example.adagiom.bepim;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ClienteHTTP_DELETE extends AsyncTask<JSONObject , JSONObject ,JSONObject>
{
    static String DELETE_SECTOR = "/sector";
    private InterfazAsyntask caller;
    private Exception mException=null;
    private JSONObject resp;
    /**URL: http://gestiondenegocio.esy.es**/
    public ClienteHTTP_DELETE(android.support.v4.app.Fragment a)
    {
        this.caller=(InterfazAsyntask)a;
    }
    private JSONObject DELETE (JSONObject jsonData)
    {
        HttpURLConnection urlConnection = null;
        resp = new JSONObject();
        try
        {
            URL mUrl = new URL(jsonData.getString("url").toString());

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

            wr.writeBytes(jsonData.toString());
            Log.i("JSON Input", jsonData.toString());

            wr.flush();
            wr.close();

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
                Map json = new HashMap();
                json.put("opcion","BORRADO");
                JSONObject jsonObject = new JSONObject(json);
                resp.put("respuesta",jsonObject);
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
    protected void onProgressUpdate(JSONObject... strings) {
        super.onProgressUpdate(strings);
        try {
            caller.VerificarMensaje(strings[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override

    protected JSONObject doInBackground(JSONObject... params)
    {
        publishProgress(DELETE(params[0]));
        return resp;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        if (mException != null) {
            //caller.mostrarToastMake("Error en POST:\n" + mException.toString());
            caller.mostrarToastMake("ERROR EN EL SERVIDOR");
            return;
        }
        try {
            if (result.get("respuesta") == "NO_OK") {
                caller.mostrarToastMake("Error en POST, se recibio response NO_OK");
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
