package com.example.adagiom.bepim.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.adagiom.bepim.REST.ClienteHTTP_POST;
import com.example.adagiom.bepim.interfaz.InterfazAsyntask;
import com.example.adagiom.bepim.R;
import com.example.adagiom.bepim.response.Response_Plataforma;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistroActivity extends AppCompatActivity  implements InterfazAsyntask {

    Button ingresar;
    Button registrar;
    EditText user;
    EditText pass;
    EditText confpass;
    private ClienteHTTP_POST threadCliente_Post;
    String ruta;
    JSONObject json;
    SharedPreferences sharedPreferences;
    private static String TAG = "FirebaseLogin";
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        ingresar = (Button) findViewById(R.id.btn_ingresar);
        registrar = (Button) findViewById(R.id.btn_registrar);
        user = (EditText) findViewById(R.id.user);
        pass = (EditText) findViewById(R.id.pass);
        confpass = (EditText) findViewById(R.id.confpass);
        ingresar.setOnClickListener(onClickListener);
        registrar.setOnClickListener(onClickListener);
        sharedPreferences = getSharedPreferences(getString(R.string.key_preference),MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        mAuth = FirebaseAuth.getInstance();
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.i("DynamicLink",deepLink.toString());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    @Override
    public void VerificarMensaje(JSONObject msj)  {
        Gson gson = new Gson();
        try{
            Response_Plataforma mensaje = gson.fromJson(msj.getString("respuesta"),Response_Plataforma.class);
            if(mensaje.getOpcion().equals("USER")) {
                sharedPreferences.edit()
                        .putString(getString(R.string.token_pass),pass.getText().toString())
                        .commit();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                currentUser.sendEmailVerification();
                Intent intent = new Intent(RegistroActivity.this,LoginActivity.class);
                intent.putExtra("verificar",true);
                startActivity(intent);
                finish();

            }else{
            }
        }catch (Exception e){
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.btn_registrar:
                    if(!user.getText().toString().isEmpty() && !pass.getText().toString().isEmpty()) {
                        if(pass.getText().toString().equals(confpass.getText().toString())) {
                            mAuth.createUserWithEmailAndPassword(user.getText().toString(), pass.getText().toString())
                                    .addOnCompleteListener(RegistroActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d(TAG, "createUserWithEmail:success");
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                updateUI(user);
                                                json = new JSONObject();
                                                String uri = ClienteHTTP_POST.REG_USER_APP;
                                                String token = FirebaseInstanceId.getInstance().getToken();
                                                try {
                                                    json.put("url", ruta + uri);
                                                    json.put("USER_ID", user.getUid());
                                                    json.put("EMAIL", user.getEmail());
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                threadCliente_Post = new ClienteHTTP_POST(RegistroActivity.this);
                                                threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, json);
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                mostrarToastMake(task.getException().getMessage().toString());
                                                updateUI(null);
                                            }
                                        }
                                    });
                        }else {
                            mostrarToastMake("Error en confirmación de contraseña");
                        }
                    }
                    break;
                case R.id.btn_ingresar:
                    Intent intent = new Intent(RegistroActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
    //bepim.page.link
    ActionCodeSettings actionCodeSettings =
            ActionCodeSettings.newBuilder()
                    // URL you want to redirect back to. The domain (www.example.com) for this
                    // URL must be whitelisted in the Firebase Console.
                    .setUrl("https://bepim-54fc0.firebaseapp.com")
                    // This must be true
                    .setHandleCodeInApp(true)
                    .setIOSBundleId("com.example.ios")
                    .setAndroidPackageName(
                            "bepim.page.link/logIn",
                            true, /* installIfNotAvailable */
                            "12"    /* minimumVersion */)
                    .build();


    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }
    private void updateUI(FirebaseUser currentUser) {
        if(currentUser != null){
            user.setText(currentUser.getEmail());

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


}
