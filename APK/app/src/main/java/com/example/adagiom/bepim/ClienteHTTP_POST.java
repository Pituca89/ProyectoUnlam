package com.example.adagiom.bepim;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.util.Log;

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

public class ClienteHTTP_POST extends AsyncTask<JSONObject , JSONObject ,JSONObject>
{
    static int CONECTAR = 0;
    static int ENVIAR_RUTA = 1;
    static int REG_PLATAFORMA = 2;
    static int REG_RUTA = 3;
    static int REG_USUARIO = 4;
    static int PLATAFORMA_DISPONIBLE = 5;
    static int OCUPAR_PLATAFORMA = 6;
    static int VERIFICAR_CONEXION = 7;
    static int RUTA_PRUEBA = 8;
    static int SECTORES = 9;
    static int PLATAFORMA = 10;
    private InterfazAsyntask caller;
    private Exception mException=null;
    private JSONObject resp;
    /**URL: http://gestiondenegocio.esy.es**/
    public ClienteHTTP_POST(Activity a)
    {
        this.caller=(InterfazAsyntask)a;
    }
    public ClienteHTTP_POST(android.support.v4.app.Fragment a)
    {
        this.caller=(InterfazAsyntask)a;
    }

    private JSONObject POST (JSONObject jsonData)
    {
        HttpURLConnection urlConnection = null;
        resp = new JSONObject();
        try
        {
            URL mUrl = new URL(jsonData.getString("url"));

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

            wr.writeBytes(jsonData.toString());
            Log.i("JSON Input", jsonData.toString());

            wr.flush();
            wr.close();

            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

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
        publishProgress(POST(params[0]));
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
