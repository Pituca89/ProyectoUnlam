package com.example.adagiom.bepim.REST;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.example.adagiom.bepim.interfaz.InterfazAsyntask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClienteHTTP_POST extends AsyncTask<JSONObject , JSONObject ,JSONObject>
{
    public static String ENVIAR_RUTA = "/enviar_peticion";
    public static String REG_RUTA = "/registrar_ruta";
    public static String OCUPAR_PLATAFORMA = "/ocupar_plataforma";
    public static String VERIFICAR_CONEXION = "/conexion";
    public static String SECTORES = "/sectores";
    public static String PLATAFORMA = "/plataformas";
    public static String ENVIAR_TOKEN = "/token";
    public static String ASOCIAR_PLATAFORMA = "/asociar_plataforma";
    public static String ASOCIAR_SECTOR = "/asociar_sector";
    public static String ACTUALIZAR_SECTOR_ACTUAL = "/actual";
    public static String LIBERAR = "/cancelar";
    public static String REG_USER_APP = "/register_user_app";
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
            URL mUrl = new URL(jsonData.getString("url").toString());

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

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
            caller.mostrarToastMake("Error en POST:\n" + mException.toString());
            //caller.mostrarToastMake("En estos momentos no es posible procesar la solicitud, por favor espero unos minutos e intente nuevamente");
            return;
        }
        try {
            if (result.get("respuesta") == "NO_OK") {
                caller.mostrarToastMake("Error de solicitud, por favor comuníquese con el administrador");
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
