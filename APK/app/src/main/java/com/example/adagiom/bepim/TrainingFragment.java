package com.example.adagiom.bepim;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;


import io.github.controlwear.virtual.joystick.android.JoystickView;


public class TrainingFragment extends Fragment implements InterfazAsyntask{

    private String ruta;
    private String ruta_esp;
    private String chipid;
    private ClienteHTTP_POST threadCliente_Post;
    SharedPreferences sharedPreferences;
    Button comenzar;
    Button deshacer;
    Button confirmar;
    Button up;
    Button left;
    Button right;
    JSONObject json;
    public static int FRENTE = 0;
    public static int STOP = 1;
    public static int DERECHA = 2;
    public static int IZQUIERDA = 3;
    public static int ESTADO_INICIAL = -1;
    int estado_anterior = ESTADO_INICIAL;
    String ipPlataforma;
    AlertDialog.Builder builder;
    AlertDialog.Builder builderDevice;
    AlertDialog alertDialog;
    AlertDialog alertDialogDevice;
    ListView listSector;
    FloatingActionButton addSector;
    private SectorTrainingAdapter sectorAdapter;
    private ArrayList<Sector> sectorArrayList;
    TextView lblsector;
    TextView lblsectortitle;
    static TextView lblsectoractual;
    static String actual;
    Sector destino;
    Sector origen;
    private ProgressDialog mProgressDlg;
    private ProgressDialog mProgressDlg1;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private String [] datos;
    private ConnectedThread mConnectedThread;
    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.
    private DeviceListAdapter mAdapter;
    private int posicionListBluethoot;
    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message
    private ListView mListView;
    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address del Hc05
    private static String address = null;
    //se crea un array de String con los permisos a solicitar en tiempo de ejecucion
    //Esto se debe realizar a partir de Android 6.0, ya que con versiones anteriores
    //con solo solicitarlos en el Manifest es suficiente
    String[] permissions= new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public TrainingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (checkPermissions())
        {
            enableComponent();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_preference),Context.MODE_PRIVATE);
        ruta = sharedPreferences.getString(getString(R.string.path_plataforma),"");
        Plataforma plataforma = (Plataforma) getArguments().getSerializable("plataforma");
        ipPlataforma = plataforma.getIp().toString();
        chipid = plataforma.getChipid();
        json = new JSONObject();

        View v = inflater.inflate(R.layout.fragment_training, container, false);
        comenzar = (Button) v.findViewById(R.id.btn_comenzar);
        deshacer = (Button) v.findViewById(R.id.btn_deshacer);
        confirmar = (Button) v.findViewById(R.id.btn_confirmar);
        up = (Button) v.findViewById(R.id.btn_frente);
        left = (Button) v.findViewById(R.id.btn_izquierda);
        right = (Button) v.findViewById(R.id.btn_derecha);
        lblsectoractual = (TextView) v.findViewById(R.id.lbl_sector_actual);

        comenzar.setOnClickListener(onClickTraining);
        deshacer.setOnClickListener(onClickTraining);
        confirmar.setOnClickListener(onClickTraining);
        left.setOnClickListener(onActionButton);
        up.setOnClickListener(onActionButton);
        right.setOnClickListener(onActionButton);
        View viewSector = inflater.inflate(R.layout.fragment_sector,null);
        View viewDevice = inflater.inflate(R.layout.activity_paired_devices,null);

        builder = new AlertDialog.Builder(getContext());
        builderDevice = new AlertDialog.Builder(getContext());

        listSector = (ListView) viewSector.findViewById(R.id.listAddSector);
        mListView = (ListView) viewDevice.findViewById(R.id.lv_paired);

        addSector = (FloatingActionButton) viewSector.findViewById(R.id.addSector);
        lblsector = (TextView) viewSector.findViewById(R.id.lblsector);
        lblsectortitle = (TextView) viewSector.findViewById(R.id.lblsectortitle);
        lblsectortitle.setText("Destinos disponibles");
        lblsector.setText("Registrar destino");

        sectorAdapter = new SectorTrainingAdapter(getActivity());
        addSector.setOnClickListener(agregarSector);

        builder.setView(viewSector)
                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        alertDialog = builder.create();

        builderDevice.setView(viewDevice)
                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        alertDialogDevice = builderDevice.create();


        refreshSector();


        /**
         * IP/training
         * opcion: INST
         * sentido: F-D-I-R
         * mac: mac registrada en cada sector
         * confirma: SI - NO
         *
         * IP/mode
         * codigo: MODO
         * dato: MOD_O - MOD_E -> Al seleccionar COMENZAR: MOD_E - Al seleccionar CONFIRMAR: MOD_O
         * **/
        //Se crea un adaptador para podermanejar el bluethoot del celular
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter = new DeviceListAdapter(getContext());
        //Se Crea la ventana de dialogo que indica que se esta buscando dispositivos bluethoot
        mProgressDlg = new ProgressDialog(getContext());
        mProgressDlg1 = new ProgressDialog(getContext());
        mProgressDlg.setMessage("Buscando dispositivos...");
        mProgressDlg.setCancelable(false);
        mProgressDlg1.setMessage("Escaneando Beacon...");
        mProgressDlg1.setCancelable(false);
        bluetoothIn = Handler_Msg_Hilo_Principal();
        //se asocia un listener al boton cancelar para la ventana de dialogo ue busca los dispositivos bluethoot
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", btnCancelarDialogListener);
        //
        return v;
    }

    View.OnClickListener onActionButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                switch (view.getId()){
                    case R.id.btn_frente:
                        Log.i("DatoBT","F");
                        mConnectedThread.write("F");
                        break;
                    case R.id.btn_derecha:
                        Log.i("DatoBT","D");
                        mConnectedThread.write("D");
                        break;
                    case R.id.btn_izquierda:
                        Log.i("DatoBT","I");
                        mConnectedThread.write("I");
                        break;
                }
            }catch (Exception e){
                mostrarToastMake("Plataforma desconectada");
            }

        }
    };
    @Override
    public void mostrarToastMake(String msg) {
        Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void VerificarMensaje(JSONObject msg) throws JSONException {
        Gson gson = new Gson();
        try{
            Response_Sectores mensaje = gson.fromJson(msg.getString("respuesta"),Response_Sectores.class);
            Log.i("Sector",mensaje.getOpcion().toString());
            if(mensaje.getOpcion().equals("SECTORES")) {
                sectorArrayList = mensaje.getSectores();

                Iterator<Sector> sectorIterator = sectorArrayList.iterator();
                while(sectorIterator.hasNext()){
                    Sector sector = sectorIterator.next();
                    if(sector.getActual() == 1) {
                        origen = sector;
                        lblsectoractual.setText("Partiendo del sector: " + sector.getNombre());
                        sectorIterator.remove();
                    }
                }
                sectorAdapter.setData(sectorArrayList);
                listSector.setAdapter(sectorAdapter);
                sectorAdapter.setListener(onSelectFinDestino);

            }else if(mensaje.getOpcion().contains("DUPLICADO")){
                mostrarToastMake("Plataforma duplicada");
            }else if(mensaje.getOpcion().contains("ACTUAL")){
                refreshSector();
                mProgressDlg1.dismiss();
            }else if(mensaje.getOpcion().contains("RUTA")){
                //refreshSector();
                actualizarSectorActual();
            }else{
                //mostrarToastMake("ERROR DE CONEXIÓN");
            }
        }catch (Exception e){
            mProgressDlg1.dismiss();
            mostrarToastMake("ERROR DE SERVIDOR");
        }
    }
    View.OnClickListener onClickTraining;

    {
        onClickTraining = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ruta_esp = "http://" + ipPlataforma + "/mode";
                switch (view.getId()) {
                    case R.id.btn_comenzar:
                        mBluetoothAdapter.startDiscovery();
                        break;
                    case R.id.btn_deshacer:

                        break;
                    case R.id.btn_confirmar:
                        alertDialog.show();
                    break;
                }
            }
        };
    }

    public void refreshSector(){
        json = new JSONObject();
        String uri = ClienteHTTP_POST.SECTORES;
        try {
            json.put("url",getString(R.string.url) + uri);
            json.put("ID",chipid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
    }

    View.OnClickListener agregarSector = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.addSector:
                    IntentIntegrator.forSupportFragment(TrainingFragment.this).initiateScan();
                    break;
            }
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(intentResult.getContents() != null){
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.dialog_plataforma,null);
            final EditText nombreSector = (EditText) view.findViewById(R.id.nameplataforma);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            json = new JSONObject();
                            String uri = ClienteHTTP_POST.ASOCIAR_SECTOR;

                            try {
                                json.put("url",getString(R.string.url) + uri);
                                json.put("ID",chipid);
                                json.put("MAC",intentResult.getContents());
                                if(nombreSector.getText().toString() != ""){
                                    json.put("NOMBRE",nombreSector.getText().toString());
                                }else{
                                    json.put("NOMBRE",intentResult.getContents());
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.i("JSONT",json.toString());
                            threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
                            threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog1 = builder.create();

            alertDialog1.show();
        }else{
            Log.i("QR","Error al obtener QR");
        }
    }
    SectorTrainingAdapter.OnSelectFinDestino onSelectFinDestino = new SectorTrainingAdapter.OnSelectFinDestino() {
        @Override
        public void enviarPlataformaClick(final int position) {
            alertDialog.dismiss();
            destino = sectorArrayList.get(position);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    // Add action buttons
                    .setTitle("")
                    .setMessage("Desea confirmar el destino?")
                    .setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                mConnectedThread.write("P"+destino.getMac()+"#");
                                actual = sectorArrayList.get(position).getNombre();
                                mProgressDlg1.show();
                            }catch (Exception e){
                                mostrarToastMake("Plataforma desconectada");
                            }
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            alertDialog.show();
                        }
                    });
            AlertDialog alertDialog2 = builder.create();
            alertDialog2.show();
        }
    };

    public void actualizarSectorActual(){
        String uri = ClienteHTTP_POST.ACTUALIZAR_SECTOR_ACTUAL;
        try {
            json.put("url",getString(R.string.url) + uri);
            json.put("ID",chipid);
            json.put("ACTUAL",destino.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
    }

    public void registrarRuta( String ruta,int desde, int hasta,int potencia,int costo){
        String uri = ClienteHTTP_POST.REG_RUTA;
        try {
            json.put("url",getString(R.string.url) + uri);
            json.put("DESDE",desde);
            json.put("HASTA",hasta);
            json.put("RUTA",ruta);
            json.put("POTENCIA",potencia);
            json.put("COSTO",costo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        threadCliente_Post =  new ClienteHTTP_POST(TrainingFragment.this);
        threadCliente_Post.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,json);
    }
    protected  void enableComponent()
    {

        //se definen un broadcastReceiver que captura el broadcast del SO cuando captura los siguientes eventos:
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //Cambia el estado del Bluethoot (Acrtivado /Desactivado)
        filter.addAction(BluetoothDevice.ACTION_FOUND); //Se encuentra un dispositivo bluethoot al realizar una busqueda
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //Cuando se comienza una busqueda de bluethoot
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //cuando la busqueda de bluethoot finaliza

        //se define (registra) el handler que captura los broadcast anterirmente mencionados.
        getActivity().registerReceiver(mReceiver, filter);

        //mBluetoothAdapter.startDiscovery();
    }

    @Override
    //Cuando se llama al metodo OnPausa se cancela la busqueda de dispositivos bluethoot
    public void onPause()
    {

        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        super.onPause();
    }
    @Override
    //Cuando se detruye la Acivity se quita el registro de los brodcast. Apartir de este momento no se
    //recibe mas broadcast del SO. del bluethoot
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }



    //Handler que captura los brodacast que emite el SO al ocurrir los eventos del bluethoot
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            //Atraves del Intent obtengo el evento de Bluethoot que informo el broadcast del SO
            String action = intent.getAction();

            //Si cambio de estado el Bluethoot(Activado/desactivado)
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                //Obtengo el parametro, aplicando un Bundle, que me indica el estado del Bluethoot
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                //Si esta activado
                if (state == BluetoothAdapter.STATE_ON)
                {
                    mostrarToastMake("Activar");
                }
            }
            //Si se inicio la busqueda de dispositivos bluethoot
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                //Creo la lista donde voy a mostrar los dispositivos encontrados
                mDeviceList = new ArrayList<BluetoothDevice>();

                //muestro el cuadro de dialogo de busqueda
                mProgressDlg.show();
            }
            //Si finalizo la busqueda de dispositivos bluethoot
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                //se cierra el cuadro de dialogo de busqueda
                mProgressDlg.dismiss();

                //asocio el listado de los dispositovos pasado en el bundle al adaptador del Listview
                mAdapter.setData(mDeviceList);
                //defino un listener en el boton emparejar del listview
                mAdapter.setListener(listenerBotonEmparejar);
                mAdapter.setmListenerPlay(onPlayButtonClickListener);
                mListView.setAdapter(mAdapter);
                //se definen un broadcastReceiver que captura el broadcast del SO cuando captura los siguientes eventos:
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); //Cuando se empareja o desempareja el bluethoot
                //se define (registra) el handler que captura los broadcast anterirmente mencionados.
                getActivity().registerReceiver(mPairReceiver, filter);
                alertDialogDevice.show();
            }
            //si se encontro un dispositivo bluethoot
            else if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //Se lo agregan sus datos a una lista de dispositivos encontrados
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                mostrarToastMake("Dispositivo Encontrado:" + device.getName());
            }
        }
    };


    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    private View.OnClickListener btnEmparejarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices == null || pairedDevices.size() == 0)
            {
                mostrarToastMake("No se encontraron dispositivos emparejados");
            }
            else
            {
                ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                list.addAll(pairedDevices);

                //Intent intent = new Intent(getActivity(), DeviceListActivity.class);

                //intent.putParcelableArrayListExtra("device.list", list);

                //startActivity(intent);
            }
        }
    };

    private View.OnClickListener btnBuscarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBluetoothAdapter.startDiscovery();
        }
    };


    private View.OnClickListener btnActivarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();

            } else {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(intent, 1000);
            }
        }
    };


    private DialogInterface.OnClickListener btnCancelarDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            mBluetoothAdapter.cancelDiscovery();
        }
    };


    //Metodo que chequea si estan habilitados los permisos
    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        //Se chequea si la version de Android es menor a la 6
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }


        for (String p:permissions) {
            result = ActivityCompat.checkSelfPermission(getContext(),p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                    enableComponent(); // Now you call here what ever you want :)
                } else {
                    String perStr = "";
                    for (String per : permissions) {
                        perStr += "\n" + per;
                    }
                    // permissions list of don't granted permission
                    Toast.makeText(getContext(),"ATENCION: La aplicacion no funcionara " +
                            "correctamente debido a la falta de Permisos", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    private DeviceListAdapter.OnPairButtonClickListener listenerBotonEmparejar = new DeviceListAdapter.OnPairButtonClickListener() {
        @Override
        public void onPairButtonClick(int position) {
            //Obtengo los datos del dispostivo seleccionado del listview por el usuario
            BluetoothDevice device = mDeviceList.get(position);

            //Se checkea si el sipositivo ya esta emparejado
            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
            {
                //Si esta emparejado,quiere decir que se selecciono desemparjar y entonces se le desempareja
                unpairDevice(device);
            }
            else
            {
                //Si no esta emparejado,quiere decir que se selecciono emparjar y entonces se le empareja
                mostrarToastMake("Emparejando");
                posicionListBluethoot = position;
                pairDevice(device);
            }
        }
    };

    DeviceListAdapter.OnPlayButtonClickListener onPlayButtonClickListener = new DeviceListAdapter.OnPlayButtonClickListener() {
        @Override
        public void onPlayButtonClick(int position) {
            BluetoothDevice device = mDeviceList.get(position);
            //se inicia el Activity de comunicacion con el bluethoot, para transferir los datos.
            //Para eso se le envia como parametro la direccion(MAC) del bluethoot Arduino
            address = device.getAddress();
            iniciarComunicacion();
            alertDialogDevice.dismiss();
        }
    };
    //Handler que captura los brodacast que emite el SO al ocurrir los eventos del bluethoot
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            //Atraves del Intent obtengo el evento de Bluethoot que informo el broadcast del SO
            String action = intent.getAction();

            //si el SO detecto un emparejamiento o desemparjamiento de bulethoot
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
            {
                //Obtengo los parametro, aplicando un Bundle, que me indica el estado del Bluethoot
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                //se analiza si se puedo emparejar o no
                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING)
                {
                    //Si se detecto que se puedo emparejar el bluethoot
                    mostrarToastMake("Emparejado");

                }  //si se detrecto un desaemparejamiento
                else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    mostrarToastMake("No emparejado");
                }

                mAdapter.notifyDataSetChanged();
            }
        }
    };

    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC05
            while (true)
            {
                try
                {
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }


        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                mostrarToastMake("La conexion fallo");
                //finish();
            }
        }
    }

    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    //voy concatenando el msj
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("\r\n");

                    //cuando recibo toda una linea la muestro en el layout
                    if (endOfLineIndex > 0)
                    {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        //txtPotenciometro.setText(dataInPrint);
                        Log.i("BT",dataInPrint.toString());
                        if(dataInPrint.contains("P")) {
                            //actualizarSectorActual();
                            //progressBar.setVisibility(View.INVISIBLE);

                            datos = dataInPrint.split("P\\|");
                            int costo = obtenerCosto(datos[0]);
                            Log.i("Costo",Integer.toString(costo));
                            try{
                                registrarRuta(datos[0].toString(),origen.getId(),destino.getId(),Integer.parseInt(datos[1].toString()),costo);
                            }catch (Exception e){
                                mostrarToastMake("ERROR DE PROCESAMIENTO");
                            }

                        }
                        if(dataInPrint.contains("ERROR")) {
                            mProgressDlg1.dismiss();
                        }
                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }

    public int obtenerCosto(String ruta){
        int costoTotal = 0;

        try {
            String[] costos = ruta.split("\\|");
            for (String costo : costos) {
                if (costo.contains("F")) {
                    costoTotal += Integer.parseInt(costo.substring(1, costo.length()));
                }
            }
        }catch (Exception e){
            mostrarToastMake("ERROR AL OBTENER EL COSTO DE LA RUTA");
        }
        return costoTotal;
    }
    public void iniciarComunicacion() {

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e)
        {
            mostrarToastMake( "La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        }
        catch (IOException e)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
                //insert code to deal with this
            }
        }

        //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("X");
    }

}
