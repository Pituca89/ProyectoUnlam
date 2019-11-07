package com.example.adagiom.bepim.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.adagiom.bepim.REST.ClienteHTTP_POST;
import com.example.adagiom.bepim.fragment.ConfigFragment;
import com.example.adagiom.bepim.interfaz.InterfazAsyntask;
import com.example.adagiom.bepim.fragment.MainFragment;
import com.example.adagiom.bepim.model.Plataforma;
import com.example.adagiom.bepim.R;
import com.example.adagiom.bepim.fragment.SectorFragment;
import com.example.adagiom.bepim.fragment.TrainingFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class DrawerPrincipal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, InterfazAsyntask {
    FragmentManager fm;
    Fragment active;
    final private TrainingFragment trainingFragment = new TrainingFragment();
    final private MainFragment mainFragment =  new MainFragment();
    final private SectorFragment sectorFragment = new SectorFragment();
    final private ConfigFragment configFragment = new ConfigFragment();
    String ruta;
    Bundle bundle;
    NavigationView navigationView;
    private ClienteHTTP_POST threadCliente_Post;
    JSONObject json;
    Plataforma plataforma;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sharedPreferences = getSharedPreferences(getString(R.string.key_preference), Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        plataforma = (Plataforma) getIntent().getSerializableExtra("plataforma");
        bundle = new Bundle();
        json = new JSONObject();
        fm = getSupportFragmentManager();
        if(plataforma != null) {
            bundle.putSerializable("plataforma", plataforma);
            //fm.beginTransaction().add(R.id.container_ly,trainingFragment).hide(trainingFragment).commit();
            mainFragment.setArguments(bundle);
            fm.beginTransaction().add(R.id.container_ly, mainFragment).commit();
            active = mainFragment;
        }
    }

    @Override
    public void onBackPressed() {

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
                        threadCliente_Post =  new ClienteHTTP_POST(DrawerPrincipal.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            active.setArguments(bundle);
            fm.beginTransaction()
                    .detach(active)
                    .attach(active)
                    .commit();
            Log.i("Refresh","Presione Refresh");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int title;
        if (id == R.id.nav_home) {
            if(active != mainFragment) {
                active = mainFragment;
                mainFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.container_ly, mainFragment).commit();
            }
        } else if (id == R.id.nav_training) {
            if(active != trainingFragment) {
                active = trainingFragment;
                trainingFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.container_ly, trainingFragment).commit();
            }
        } else if (id == R.id.nav_sectors) {
            if(active != sectorFragment) {
                active = sectorFragment;
                sectorFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.container_ly, sectorFragment).commit();
            }
        }else if (id == R.id.action_refresh) {
                active.setArguments(bundle);
                fm.beginTransaction().replace(R.id.container_ly, active).commit();
        } else if (id == R.id.nav_plataform) {
            String uri = ClienteHTTP_POST.LIBERAR;
            try {
                json.put("url",ruta + uri);
                json.put("ID",plataforma.getChipid());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            threadCliente_Post =  new ClienteHTTP_POST(DrawerPrincipal.this);
            threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
            startActivity(new Intent(DrawerPrincipal.this,ListPlataforma.class));
            finish();
        } else if (id == R.id.nav_send) {
            String[] address = {"Bepim.soporte@gmail.com"};
            String subject = "Reclamo";
            composeEmail(address,subject);
            //active = configFragment;
            //configFragment.setArguments(bundle);
            //fm.beginTransaction().replace(R.id.container_ly,configFragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void mostrarToastMake(String msg) {

    }

    @Override
    public void VerificarMensaje(JSONObject msg) throws JSONException {

    }

}
