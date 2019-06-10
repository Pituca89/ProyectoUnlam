package com.example.adagiom.bepim;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClienteHTTP_POST extends AsyncTask<String , String ,String>
{
    static int CONECTAR = 0;
    static int ENVIAR_RUTA = 1;
    static int REG_PLATAFORMA = 2;
    static int REG_RUTA = 3;
    static int REG_USUARIO = 4;
    static int PLATAFORMA_DISPONIBLE = 5;
    static int OCUPAR_PLATAFORMA = 6;
    static int LOGIN_USUARIO = 7;
    private InterfazAsyntask caller;
    private Exception mException=null;

    public ClienteHTTP_POST(Activity a)
    {
        this.caller=(InterfazAsyntask)a;
    }

    private String POST (String uri, String mensaje)
    {
        HttpURLConnection urlConnection = null;

        try
        {

            URL mUrl = new URL(uri);

            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");

            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

            JSONObject obj = new JSONObject();
            obj.put("OPCION" , mensaje);

            wr.writeBytes(obj.toString());
            Log.i("JSON Input", obj.toString());

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
                return "NO_OK";
            }else{
                return result.toString();
            }



        } catch (Exception e)
        {
            mException=e;
        }
        return "NO_OK";
    }

    @Override
    protected void onProgressUpdate(String... strings) {
        super.onProgressUpdate(strings);
        //MainActivity.VerificarMensaje(strings[0]);
    }

    @Override

    protected String doInBackground(String... params)
    {

        publishProgress(POST(params[0],params[1]));
        return POST(params[0],params[1]);
    }

    @Override
    protected void onPostExecute(String result) {

        super.onPostExecute(result);
        if (mException != null) {
            caller.mostrarToastMake("Error en POST:\n" + mException.toString());
            return;
        }
        if (result == "NO_OK") {
            caller.mostrarToastMake("Error en POST, se recibio response NO_OK");
            return;
        }

        if(result != "NO_OK" && mException == null){
            caller.mostrarToastMake(result);
        }

    }
}
