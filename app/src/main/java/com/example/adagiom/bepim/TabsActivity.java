package com.example.adagiom.bepim;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionProvider;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

public class TabsActivity extends AppCompatActivity {
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i("Fragment","Entre");
            drawerLayout = (DrawerLayout) findViewById(R.id.drawner_nav);
            switch (v.getId()) {
                case R.id.btn_principal:
                    Log.i("Fragment","Principal");
                    mainFragment.setArguments(bundle);
                    fm.beginTransaction().replace(R.id.container_ly,mainFragment).commit();
                    active = mainFragment;
                    drawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.btn_entrenar:
                    Log.i("Fragment","Entrenar");
                    trainingFragment.setArguments(bundle);
                    fm.beginTransaction().replace(R.id.container_ly,trainingFragment).commit();
                    active = trainingFragment;
                    drawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.btn_sectores:
                    Log.i("Fragment","Sectores");
                    sectorFragment.setArguments(bundle);
                    fm.beginTransaction().replace(R.id.container_ly,sectorFragment).commit();
                    active = sectorFragment;
                    drawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.btn_configuracion:
                    Log.i("Fragment","Configuración");
                    configFragment.setArguments(bundle);
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
        navigationView.getHeaderView(0).findViewById(R.id.btn_configuracion).setOnClickListener(onClickListener);

        fm = getSupportFragmentManager();
        //fm.beginTransaction().add(R.id.container_ly,trainingFragment).hide(trainingFragment).commit();
        fm.beginTransaction().add(R.id.container_ly, mainFragment).commit();
        active = mainFragment;
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

        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    public String getIp() {
        return ruta;
    }
}
