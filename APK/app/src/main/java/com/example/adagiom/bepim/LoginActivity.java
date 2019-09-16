package com.example.adagiom.bepim;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements InterfazAsyntask{
    Button ingresar;
    Button registrar;
    EditText user;
    EditText pass;
    private ClienteHTTP_POST threadCliente_Post;
    String ruta;
    JSONObject json;
    SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private static String TAG = "FirebaseLogin";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ingresar = (Button) findViewById(R.id.btn_ingresar);
        registrar = (Button) findViewById(R.id.btn_registrar);
        user = (EditText) findViewById(R.id.user);
        pass = (EditText) findViewById(R.id.pass);
        ingresar.setOnClickListener(onClickListener);
        registrar.setOnClickListener(onClickListener);
        sharedPreferences = getSharedPreferences(getString(R.string.key_preference),MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        mAuth = FirebaseAuth.getInstance();
        String psw = sharedPreferences.getString(getString(R.string.token_pass),"");
        pass.setText(psw);
        Log.i("pass",psw.toString());
        if(!psw.equals("")){
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
            json = new JSONObject();
            String mensaje =Integer.toString(ClienteHTTP_POST.ENVIAR_TOKEN);
            String token = FirebaseInstanceId.getInstance().getToken();
            try {
                json.put("url",ruta);
                json.put("OPCION",mensaje);
                json.put("TOKEN",token);
                json.put("USER",user.getUid());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            threadCliente_Post =  new ClienteHTTP_POST(LoginActivity.this);
            threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.btn_ingresar:

                    if(!user.getText().toString().isEmpty() && !pass.getText().toString().isEmpty()) {
                        mAuth.signInWithEmailAndPassword(user.getText().toString(), pass.getText().toString())
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            updateUI(user);
                                            json = new JSONObject();
                                            String mensaje =Integer.toString(ClienteHTTP_POST.ENVIAR_TOKEN);
                                            String token = FirebaseInstanceId.getInstance().getToken();
                                            try {
                                                json.put("url",ruta);
                                                json.put("OPCION",mensaje);
                                                json.put("TOKEN",token);
                                                json.put("USER",user.getUid());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            threadCliente_Post =  new ClienteHTTP_POST(LoginActivity.this);
                                            threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                                        } else {
                                            mostrarToastMake("El email o la contraseña son incorrectos");
                                            updateUI(null);
                                        }

                                    }
                                });
                    }
                    break;
                case R.id.btn_registrar:
                    Intent intent = new Intent(LoginActivity.this,RegistroActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    Toast.makeText(getApplicationContext(),"Error en Listener de botones",Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.}
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }


    private void updateUI(FirebaseUser currentUser) {
        if(currentUser != null){
            user.setText(currentUser.getEmail());
            sharedPreferences.edit()
                    .putString(getString(R.string.token_user),currentUser.getUid())
                    .commit();
        }
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Desea salir de la aplicacion?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Si",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void VerificarMensaje(JSONObject msg) {
        Gson gson = new Gson();
        try{
            Response_Plataforma mensaje = gson.fromJson(msg.getString("respuesta"),Response_Plataforma.class);
            if(mensaje.getOpcion().equals("TOKEN")) {
                Intent intent = new Intent(LoginActivity.this, ListPlataforma.class);
                sharedPreferences.edit()
                        .putString(getString(R.string.token_pass),pass.getText().toString())
                        .commit();
                startActivity(intent);
                finish();
            }else{
                //ip.setEnabled(true);
                mostrarToastMake("ERROR DE CONEXIÓN");
            }
        }catch (Exception e){
            mostrarToastMake("ERROR EN SERVIDOR");
        }
    }

}
