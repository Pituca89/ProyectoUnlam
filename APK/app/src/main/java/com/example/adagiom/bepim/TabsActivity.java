package com.example.adagiom.bepim;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.List;

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
        Plataforma plataforma = (Plataforma) getIntent().getSerializableExtra("plataforma");
        bundle = new Bundle();
        bundle.putSerializable("plataforma",plataforma);
        fm = getSupportFragmentManager();
        //fm.beginTransaction().add(R.id.container_ly,trainingFragment).hide(trainingFragment).commit();
        mainFragment.setArguments(bundle);
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

    public static class SectorListAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;
        private List<Sector> mData;
        private OnEnviarPlataforma enviarPlataforma;

        public SectorListAdapter(Context context){
            layoutInflater = LayoutInflater.from(context);
        }

        public void setData(List<Sector> data){
            mData = data;
        }

        public List<Sector> getData() {
            return mData;
        }

        public void setListener(OnEnviarPlataforma listener){
            enviarPlataforma = listener;
        }

        @Override
        public int getCount() {
            return (mData == null) ? 0: mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewSector viewSector = null;
            if(convertView == null){
                viewSector = new ViewSector();
                convertView = layoutInflater.inflate(R.layout.item_sector,null);

                viewSector.sector_name = (TextView) convertView.findViewById(R.id.sector_name);
                viewSector.sector_enviar = (Button) convertView.findViewById(R.id.sector_envar);
                convertView.setTag(viewSector);
            }else{
                viewSector = (ViewSector) convertView.getTag();
            }

            Sector sector = mData.get(position);
            viewSector.sector_name.setText(sector.getNombre());
            viewSector.sector_name.setTag(sector.getId());
            viewSector.sector_enviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(enviarPlataforma != null){
                        enviarPlataforma.enviarPlataformaClick(position);
                    }
                }
            });
            return convertView;
        }
        static class ViewSector {
            TextView sector_name;
            Button sector_enviar;
        }
        public interface OnEnviarPlataforma{
            public abstract void enviarPlataformaClick(int position);
        }
    }
}
