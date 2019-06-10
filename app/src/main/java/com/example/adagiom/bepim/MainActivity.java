package com.example.adagiom.bepim;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements InterfazAsyntask{

    private static Button connect;
    private static EditText ip;
    //private ClienteHTTP_POST threadCliente_Post;
    private String ruta = "http://";
    static TextView mensaje;
    static int CONECTAR = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect = (Button) findViewById(R.id.btnConnect);
        ip = (EditText) findViewById(R.id.txtIP);
        mensaje = (TextView) findViewById(R.id.textView);
        connect.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.btnConnect:

                    ip.setEnabled(false);
                    String uri = ruta + ip.getText() + "/";
                    String mensaje =Integer.toString(MainActivity.CONECTAR);
                    new Conexion().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,uri,mensaje);
                    break;
                default:
                    Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    public void VerificarMensaje(String msj){

        if(msj.equals("USUARIO CONECTADO")) {
            try {

                Thread.sleep(1000);
                Intent intent = new Intent(this, RegistroActivity.class);
                startActivity(intent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            ip.setEnabled(true);
            mostrarToastMake("ERROR DE CONEXIÓN");
        }
    }


    public class Conexion extends AsyncTask<String , String ,String> {
         private Exception mException=null;

        private String POST (String uri, String mensaje) {
            HttpURLConnection urlConnection = null;

            try {

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

                if(responseCode != HttpURLConnection.HTTP_OK) {
                    return "NO_OK";
                }else{
                    return result.toString();
                }
            } catch (Exception e) {
                mException=e;
            }
            return "NO_OK";
        }

        @Override
        protected void onProgressUpdate(String... strings) {
            super.onProgressUpdate(strings);
            VerificarMensaje(strings[0]);
        }

        @Override
        protected String doInBackground(String... params) {
            publishProgress(POST(params[0],params[1]));
            return POST(params[0],params[1]);
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
        }
    }

}
