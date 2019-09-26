package com.example.adagiom.bepim;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TabsActivity extends AppCompatActivity implements InterfazAsyntask{
    FragmentManager fm;
    Fragment active;
    final private TrainingFragment trainingFragment = new TrainingFragment();
    final private MainFragment mainFragment =  new MainFragment();
    final private SectorFragment sectorFragment = new SectorFragment();
    final private ConfigFragment configFragment = new ConfigFragment();
    String ruta;
    Bundle bundle;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    BroadcastReceiver broadcastReceiver;
    private ClienteHTTP_POST threadCliente_Post;
    private String chipid;
    JSONObject json;
    Plataforma plataforma;
    SharedPreferences sharedPreferences;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i("Fragment","Entre");
            drawerLayout = (DrawerLayout) findViewById(R.id.drawner_nav);
            switch (v.getId()) {
                case R.id.btn_principal:
                    Log.i("Fragment","Principal");
                    try {
                        mainFragment.setArguments(bundle);
                    }catch(Exception e){

                    }
                    fm.beginTransaction().replace(R.id.container_ly,mainFragment).commit();
                    active = mainFragment;
                    drawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.btn_entrenar:
                    Log.i("Fragment","Entrenar");
                    try {
                        trainingFragment.setArguments(bundle);
                    }catch(Exception e){

                    }
                    fm.beginTransaction().replace(R.id.container_ly,trainingFragment).commit();
                    active = trainingFragment;
                    drawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.btn_sectores:
                    Log.i("Fragment","Sectores");
                    try {
                        sectorFragment.setArguments(bundle);
                    }catch(Exception e){

                    }
                    fm.beginTransaction().replace(R.id.container_ly,sectorFragment).commit();
                    active = sectorFragment;
                    drawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.btn_plataforma:
                    String uri = ClienteHTTP_POST.LIBERAR;
                    try {
                        json.put("url",ruta + uri);
                        json.put("ID",plataforma.getChipid());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    threadCliente_Post =  new ClienteHTTP_POST(TabsActivity.this);
                    threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                    startActivity(new Intent(TabsActivity.this,ListPlataforma.class));
                    finish();
                    break;
                case R.id.btn_configuracion:
                    Log.i("Fragment","Configuración");
                    try {
                        configFragment.setArguments(bundle);

                    }catch(Exception e){

                    }
                    fm.beginTransaction().replace(R.id.container_ly,configFragment).commit();
                    active = configFragment;
                    drawerLayout.closeDrawer(Gravity.START);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        navigationView =(NavigationView) findViewById(R.id.navigationView);
        navigationView.getHeaderView(0).findViewById(R.id.btn_principal).setOnClickListener(onClickListener);
        navigationView.getHeaderView(0).findViewById(R.id.btn_entrenar).setOnClickListener(onClickListener);
        navigationView.getHeaderView(0).findViewById(R.id.btn_sectores).setOnClickListener(onClickListener);
        navigationView.getHeaderView(0).findViewById(R.id.btn_plataforma).setOnClickListener(onClickListener);
        navigationView.getHeaderView(0).findViewById(R.id.btn_configuracion).setOnClickListener(onClickListener);
        sharedPreferences = getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        plataforma = (Plataforma) getIntent().getSerializableExtra("plataforma");
        bundle = new Bundle();
        json = new JSONObject();
        if(plataforma != null) {
            bundle.putSerializable("plataforma", plataforma);
            fm = getSupportFragmentManager();
            //fm.beginTransaction().add(R.id.container_ly,trainingFragment).hide(trainingFragment).commit();
            mainFragment.setArguments(bundle);
            fm.beginTransaction().add(R.id.container_ly, mainFragment).commit();
            active = mainFragment;
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

                        String uri = ClienteHTTP_POST.LIBERAR;
                        try {
                            json.put("url",ruta + uri);
                            json.put("ID",plataforma.getChipid());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        threadCliente_Post =  new ClienteHTTP_POST(TabsActivity.this);
                        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);

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

        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    public String getIp() {
        return ruta;
    }

    @Override
    public void mostrarToastMake(String msg) {

    }

    @Override
    public void VerificarMensaje(JSONObject msg) throws JSONException {

    }
}
