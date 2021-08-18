package com.florencia.simascotas;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.florencia.simascotas.activities.ComprobanteActivity;
import com.florencia.simascotas.activities.ConfigActivity;
import com.florencia.simascotas.activities.PedidoActivity;
import com.florencia.simascotas.activities.PedidoInventarioActivity;
import com.florencia.simascotas.activities.RecepcionActivity;
import com.florencia.simascotas.activities.TransferenciaActivity;
import com.florencia.simascotas.activities.actLogin;
import com.florencia.simascotas.fragments.ClienteFragment;
import com.florencia.simascotas.fragments.MascotaFragment;
import com.florencia.simascotas.fragments.PrincipalFragment;
import com.florencia.simascotas.interfaces.ClienteInterface;
import com.florencia.simascotas.interfaces.ComprobanteInterface;
import com.florencia.simascotas.interfaces.ProductoInterface;
import com.florencia.simascotas.interfaces.UsuarioInterface;
import com.florencia.simascotas.models.Cliente;
import com.florencia.simascotas.models.Comprobante;
import com.florencia.simascotas.models.Consulta;
import com.florencia.simascotas.models.FodaPropiedad;
import com.florencia.simascotas.models.Foto;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.models.Medicamento;
import com.florencia.simascotas.models.MedicamentoMascota;
import com.florencia.simascotas.models.Pedido;
import com.florencia.simascotas.models.PedidoInventario;
import com.florencia.simascotas.models.Permiso;
import com.florencia.simascotas.models.Producto;
import com.florencia.simascotas.models.Propiedad;
import com.florencia.simascotas.models.Ubicacion;
import com.florencia.simascotas.models.UsoSuelo;
import com.florencia.simascotas.models.Usuario;
import com.florencia.simascotas.services.GPSTracker;
import com.florencia.simascotas.services.JobServiceGPS;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.CheckInternet;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.shasin.notificationbanner.Banner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    private DrawerLayout drawerLayout;
    public Fragment fragment;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    public static List<String> listaFragments = new ArrayList<String>();
    private SharedPreferences sPreferencesSesion;
    private TextView txt_Usuario, txtInfo, txtUltimaConexion, txtSucursal, txtEmpresa, txtPerfil;
    private NavigationView navigation;
    private Gson gson = new Gson();
    private ProgressDialog pbProgreso;
    private OkHttpClient okHttpClient;
    private final static int ID_SERVICE_LOCATION = 1000;
    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Inicio");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        rootView = findViewById(android.R.id.content);

        sPreferencesSesion = getSharedPreferences("DatosSesion", MODE_PRIVATE);

        navigation = findViewById(R.id.navigation_view);
        txt_Usuario = navigation.getHeaderView(0).findViewById(R.id.txt_Usuario);
        txtInfo = navigation.getHeaderView(0).findViewById(R.id.txtInfo);
        txtSucursal = navigation.getHeaderView(0).findViewById(R.id.txtSucursal);
        txtEmpresa = navigation.getHeaderView(0).findViewById(R.id.txtEmpresa);
        txtPerfil = navigation.getHeaderView(0).findViewById(R.id.txtPerfil);
        txtUltimaConexion = navigation.findViewById(R.id.txtUltimoAcceso);
        try {
            Log.d("TAG", SQLite.configuracion.urlbase);
            if (sPreferencesSesion != null) {
                String ultimoacceso = sPreferencesSesion.getString("ultimaconexion", "");
                String url = (SQLite.configuracion.hasSSL ? Constants.HTTPs : Constants.HTTP) + SQLite.configuracion.urlbase;
                txtUltimaConexion.setText((ultimoacceso.length() > 0 ? "Último acceso: " + ultimoacceso + "\n" : "") + url);
            }
            txtEmpresa.setText(SQLite.usuario.sucursal.RazonSocial.toUpperCase());
            txt_Usuario.setText(SQLite.usuario.RazonSocial);
            txtPerfil.setText("Perfil: " + SQLite.usuario.nombrePerfil);
            txtInfo.setText("Establecimiento: " + SQLite.usuario.sucursal.CodigoEstablecimiento
                    + " - Punto Emisión: " + SQLite.usuario.sucursal.PuntoEmision);
            txtSucursal.setText(SQLite.usuario.sucursal.NombreSucursal);


        } catch (Exception e) {
            Log.d("TAGNAV", e.getMessage());
        }
        navigation.inflateMenu(R.menu.menu_navigation);
        initNavigationDrawer();
        VerificarPermisos();

        fragmentManager = getSupportFragmentManager();
        fragment = new PrincipalFragment();
        String backStateName = fragment.getClass().getName();
        //agregaFragment(backStateName);
        fragment.setArguments(getIntent().getExtras());
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment)
                .commit();
        listaFragments.add(backStateName);

        pbProgreso = new ProgressDialog(this);

        Utils.verificarPermisos(this);

        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        if (SQLite.gpsTracker == null)
            SQLite.gpsTracker = new GPSTracker(this);

        if (!SQLite.gpsTracker.checkGPSEnabled()) {
            SQLite.gpsTracker.showSettingsAlert(this);
        }

        //SUBIR LA LISTA DE UBICACIONES PENDIENTES
        Thread th = new Thread() {
            @Override
            public void run() {
                loadUbicacion();
            }
        };
        th.start();
        IniciarServicio();
    }

    public void agregaFragment(String backStateName) {
        fragment.setArguments(getIntent().getExtras());
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment)
                .addToBackStack(null)
                .commit();
        if (!listaFragments.contains(backStateName)) {
            /*fragmentTransaction.replace(R.id.fragment, fragment)
                    .addToBackStack(backStateName)
                    .commit();*/
            listaFragments.add(backStateName);
        } else {
            /*fragmentTransaction.replace(R.id.fragment, fragment)
                    .addToBackStack(null)
                    .commit();*/
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void initNavigationDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                boolean retorno = true;
                int id = menuItem.getItemId();
                //getSupportActionBar().setTitle(menuItem.getTitle());
                String backStateName;
                Intent i;

                switch (id) {
                    case R.id.nav_home:
                    case R.id.nav_cliente:
                        menuItem.setChecked(true);
                        if (id == R.id.nav_home)
                            fragment = new PrincipalFragment();
                        else if (id == R.id.nav_cliente)
                            fragment = new ClienteFragment();

                        backStateName = fragment.getClass().getName();
                        agregaFragment(backStateName);
                        break;
                    case R.id.nav_factura:
                        i = new Intent(MainActivity.this, ComprobanteActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_pedido:
                        i = new Intent(MainActivity.this, PedidoActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_recepcion:
                        i = new Intent(MainActivity.this, RecepcionActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_transferencia:
                        i = new Intent(MainActivity.this, TransferenciaActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_pedidoinv:
                        i = new Intent(MainActivity.this, PedidoInventarioActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_config:
                        i = new Intent(MainActivity.this, ConfigActivity.class);
                        startActivity(i);
                        break;
                    case R.id.nav_cerrarsesion:
                        if (SQLite.usuario != null) {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
                            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_confirmation_dialog,
                                    (ConstraintLayout) findViewById(R.id.lyDialogContainer));
                            builder.setView(view);
                            ((TextView) view.findViewById(R.id.lblTitle)).setText("Salir");
                            ((TextView) view.findViewById(R.id.lblMessage)).setText("¿Está seguro que desea cerrar sesión?");
                            ((ImageView) view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_exit);
                            ((Button) view.findViewById(R.id.btnCancel)).setText(getResources().getString(R.string.Cancel));
                            ((Button) view.findViewById(R.id.btnConfirm)).setText(getResources().getString(R.string.Confirm));
                            final android.app.AlertDialog alertDialog = builder.create();
                            view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (SQLite.usuario.CerrarSesionLocal(getApplicationContext())) {
                                        DetenerServicio();
                                        Intent i = new Intent(MainActivity.this, actLogin.class);
                                        startActivity(i);
                                        alertDialog.dismiss();
                                        finish();
                                    }
                                }
                            });

                            view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });

                            if (alertDialog.getWindow() != null)
                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                            alertDialog.show();
                        }
                        break;

                }
                return retorno;
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        menu.findItem(R.id.option_descargaproductos).setVisible(false);
        menu.findItem(R.id.option_sincronizacomprobantes)
                .setVisible(SQLite.usuario.VerificaPermiso(this, Constants.PUNTO_VENTA, "escritura"));
        menu.findItem(R.id.option_sincronizapedidos)
                .setVisible(SQLite.usuario.VerificaPermiso(this, Constants.PEDIDO, "escritura"));
        menu.findItem(R.id.option_sincronizapedidos_inv)
                .setVisible(SQLite.usuario.VerificaPermiso(this, Constants.PEDIDO_INVENTARIO, "escritura"));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.option_descargaproductos:
                descargaProductos(getApplicationContext());
                break;
            case R.id.option_sincronizaclientes:
                sincronizaClientes(getApplicationContext());
                break;
            case R.id.option_sincronizacomprobantes:
                sincronizaComprobantes(getApplicationContext());
                break;
            case R.id.option_sincronizapedidos:
                sincronizaPedidos(getApplicationContext());
                break;
            case R.id.option_sincronizapedidos_inv:
                sincronizaPedidos_Inv(getApplicationContext());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sincronizaClientes(final Context context) {
        try {
            /*if(!Utils.isOnlineNet(SQLite.configuracion.urlbase)) {
                Banner.make(rootView, MainActivity.this,Banner.ERROR, Constants.MSG_COMPROBAR_CONEXION_INTERNET, Banner.BOTTOM, 3000).show();
                return;
            }*/

            List<Cliente> listClientes = Cliente.getClientesSC(SQLite.usuario.IdUsuario);
            if (listClientes == null) {
                listClientes = new ArrayList<>();
            }
            if (listClientes.size() > 0) {
                for (Cliente ganadero : listClientes) {
                    if (ganadero.fotos == null)
                        continue;
                    for (int i = 0; i < ganadero.fotos.size(); i++) {
                        try {
                            String ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;
                            File miFile = new File(ExternalDirectory, ganadero.fotos.get(i).name);
                            Uri path = Uri.fromFile(miFile);
                            ganadero.fotos.get(i).bitmap = MediaStore.Images.Media.getBitmap(
                                    MainActivity.this.getContentResolver(),
                                    path);
                            ganadero.fotos.get(i).image_base = Utils.convertImageToString(ganadero.fotos.get(i).bitmap);
                        } catch (IOException e) {
                            Log.d("TAGMAINACTIVITY", "NotFound(): " + e.getMessage());
                        }
                    }

                    //CONVERTIR A STRING LAS IMAGENES DE LAS PROPIEDAS
                    if (ganadero.propiedades.size() > 0) {
                        for (Propiedad propiedad : ganadero.propiedades) {
                            for (int i = 0; i < propiedad.fotos.size(); i++) {
                                try {
                                    String ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;
                                    File miFile = new File(ExternalDirectory, propiedad.fotos.get(i).name);
                                    Uri path = Uri.fromFile(miFile);
                                    propiedad.fotos.get(i).bitmap = MediaStore.Images.Media.getBitmap(
                                            MainActivity.this.getContentResolver(),
                                            path);
                                    propiedad.fotos.get(i).image_base = Utils.convertImageToString(propiedad.fotos.get(i).bitmap);
                                } catch (IOException e) {
                                    Log.d("TAGMAINACTIVITY", "NotFound(): " + e.getMessage());
                                }
                            }
                        }
                    }
                    //CONVERTIR A STRING LAS IMAGENES DE LAS MASCOTAS
                    if (ganadero.mascotas.size() > 0) {
                        for (Mascota mascota : ganadero.mascotas) {
                            for (int i = 0; i < mascota.fotos.size(); i++) {
                                try {
                                    String ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;
                                    File miFile = new File(ExternalDirectory, mascota.fotos.get(i).name);
                                    Uri path = Uri.fromFile(miFile);
                                    mascota.fotos.get(i).bitmap = MediaStore.Images.Media.getBitmap(
                                            MainActivity.this.getContentResolver(),
                                            path);
                                    mascota.fotos.get(i).image_base = Utils.convertImageToString(mascota.fotos.get(i).bitmap);
                                } catch (IOException e) {
                                    Log.d("TAGMAINACTIVITY", "NotFound(): " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }

            pbProgreso.setTitle("Sincronizando datos");
            pbProgreso.setMessage("Espere un momento...");
            pbProgreso.setCancelable(false);
            pbProgreso.show();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SQLite.configuracion.url_ws)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            ClienteInterface miInterface = retrofit.create(ClienteInterface.class);

            Map<String, Object> post = new HashMap<>();
            post.put("usuario", SQLite.usuario.Usuario);
            post.put("clave", SQLite.usuario.Clave);
            post.put("ganaderos", listClientes);
            post.put("modomascota", true);
            Call<JsonObject> call = miInterface.LoadCliente2(post);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        Banner.make(rootView, MainActivity.this, Banner.ERROR, "Error: " + response.code() + " - " + response.message(), Banner.BOTTOM, 3000).show();
                        pbProgreso.dismiss();
                        return;
                    }
                    try {

                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (!obj.get("haserror").getAsBoolean()) {
                                JsonArray jsonClientesUpdate = obj.getAsJsonArray("ganaderosupdate");
                                if (jsonClientesUpdate != null) {
                                    for (JsonElement ele : jsonClientesUpdate) {
                                        JsonObject cli = ele.getAsJsonObject();
                                        ContentValues values = new ContentValues();
                                        values.put("codigosistema", cli.get("codigosistema").getAsInt());
                                        values.put("actualizado", 0);
                                        Cliente.Update(cli.get("idpersona").getAsInt(), values);

                                        JsonArray jsonPropiedadUpdate = cli.getAsJsonArray("propiedades_update");
                                        if (jsonPropiedadUpdate != null) {
                                            for (JsonElement elep : jsonPropiedadUpdate) {
                                                JsonObject prop = elep.getAsJsonObject();
                                                values = new ContentValues();
                                                values.put("codigosistema", prop.get("codigosistema").getAsInt());
                                                values.put("actualizado", 0);
                                                Propiedad.Update(prop.get("idpropiedad").getAsInt(), values);
                                            }
                                        }

                                        JsonArray jsonMascotaUpdate = cli.getAsJsonArray("mascotas_update");
                                        if (jsonMascotaUpdate != null) {
                                            for (JsonElement elep : jsonMascotaUpdate) {
                                                JsonObject prop = elep.getAsJsonObject();
                                                values = new ContentValues();
                                                values.put("codigosistema", prop.get("codigosistema").getAsInt());
                                                values.put("actualizado", 0);
                                                Mascota.Update(prop.get("idmascota").getAsInt(), values);

                                                if (prop.has("arrayCons")) {
                                                    JsonArray jConsultas = prop.getAsJsonArray("arrayCons");
                                                    if (jConsultas != null) {
                                                        for (JsonElement jCon : jConsultas) {
                                                            JsonObject oCons = jCon.getAsJsonObject();
                                                            values = new ContentValues();
                                                            values.put("codigosistema", oCons.get("codigosistema").getAsInt());
                                                            values.put("actualizado", 0);
                                                            Consulta.Update(oCons.get("idconsulta").getAsInt(), values);
                                                        }
                                                    }
                                                }

                                                if (prop.has("arrayMedi")) {
                                                    JsonArray jVacunas = prop.getAsJsonArray("arrayMedi");
                                                    if (jVacunas != null) {
                                                        for (JsonElement jVac : jVacunas) {
                                                            JsonObject oVac = jVac.getAsJsonObject();
                                                            values = new ContentValues();
                                                            values.put("codigosistema", oVac.get("codigosistema").getAsInt());
                                                            values.put("actualizado", 0);
                                                            MedicamentoMascota.Update(oVac.get("idmedicamento").getAsInt(), values);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                JsonArray jsonClientes = obj.getAsJsonArray("ganaderos");
                                if (jsonClientes != null) {
                                    int numClientUpdate = 0;
                                    if (jsonClientes.size() > 0) {
                                        Cliente.removeClientes(SQLite.usuario.IdUsuario);
                                        Propiedad.removePropiedades(SQLite.usuario.IdUsuario);
                                        Mascota.removeMascotas(SQLite.usuario.IdUsuario);
                                    }
                                    for (JsonElement ele : jsonClientes) {
                                        JsonObject clie = ele.getAsJsonObject();
                                        Cliente miCliente = new Cliente();
                                        miCliente.codigosistema = clie.has("idpersona") ? clie.get("idpersona").getAsInt() : 0;
                                        miCliente.tiponip = clie.get("tiponip").isJsonNull() ? "00" : clie.get("tiponip").getAsString();
                                        miCliente.nip = clie.get("nip").isJsonNull() ? "" : clie.get("nip").getAsString();
                                        miCliente.razonsocial = clie.get("razonsocial").isJsonNull() ? "" : clie.get("razonsocial").getAsString();
                                        miCliente.nombrecomercial = clie.get("nombrecomercial").isJsonNull() ? "" : clie.get("nombrecomercial").getAsString();
                                        miCliente.direccion = clie.get("direccion").isJsonNull() ? "" : clie.get("direccion").getAsString();
                                        miCliente.lat = clie.get("lat").isJsonNull() ? 0 : clie.get("lat").getAsDouble();
                                        miCliente.lon = clie.get("lon").isJsonNull() ? 0 : clie.get("lon").getAsDouble();
                                        miCliente.fono1 = clie.get("fono1").isJsonNull() ? "" : clie.get("fono1").getAsString();
                                        miCliente.fono2 = clie.get("fono2").isJsonNull() ? "" : clie.get("fono2").getAsString();
                                        miCliente.usuarioid = SQLite.usuario.IdUsuario;//clie.get("usuarioid").isJsonNull()?SQLite.usuario.IdUsuario:clie.get("usuarioid").getAsInt();
                                        miCliente.categoria = clie.get("categoria").isJsonNull() ? "" : clie.get("categoria").getAsString();
                                        miCliente.email = clie.get("email").isJsonNull() ? "" : clie.get("email").getAsString();
                                        miCliente.observacion = clie.get("observacion").isJsonNull() ? "" : clie.get("observacion").getAsString();
                                        miCliente.ruc = clie.get("ruc").isJsonNull() ? "" : clie.get("ruc").getAsString();
                                        miCliente.parroquiaid = clie.get("parroquiaid").isJsonNull() ? 0 : clie.get("parroquiaid").getAsInt();
                                        miCliente.fecharegistro = clie.has("fechareg") ? clie.get("fechareg").getAsString() : "";
                                        miCliente.longdater = Utils.longDate(miCliente.fecharegistro);
                                        miCliente.nombrecategoria = clie.has("nombrecategoria") ? clie.get("nombrecategoria").getAsString() : "";

                                        JsonArray jsonFotos;
                                        if (clie.has("fotos")) {
                                            jsonFotos = clie.get("fotos").isJsonNull() ? null : clie.getAsJsonArray("fotos");
                                            if (jsonFotos != null) {
                                                for (JsonElement eleFoto : jsonFotos) {
                                                    JsonObject jFoto = eleFoto.isJsonObject() ? eleFoto.getAsJsonObject() : null;
                                                    if (jFoto == null)
                                                        continue;
                                                    Foto miFoto = new Foto();
                                                    miFoto.name = jFoto.get("name").isJsonNull() ? "" : jFoto.get("name").getAsString();
                                                    miFoto.path = jFoto.get("path_phone").isJsonNull() ? "" : jFoto.get("path_phone").getAsString();
                                                    miFoto.tipo = "G";
                                                    miCliente.fotos.add(miFoto);
                                                }
                                            }
                                        }

                                        if (miCliente.Save() || miCliente.nip.equals("9999999999999")) {
                                            JsonArray jsonPropiedades = clie.get("propiedades").isJsonNull() ? null : clie.getAsJsonArray("propiedades");
                                            if (jsonPropiedades != null) {
                                                for (JsonElement elePro : jsonPropiedades) {
                                                    JsonObject pro = elePro.isJsonObject() ? elePro.getAsJsonObject() : null;
                                                    if (pro == null)
                                                        continue;
                                                    Propiedad miPropiedad = new Propiedad();
                                                    miPropiedad.codigosistema = pro.get("idpropiedad").isJsonNull() ? 0 : pro.get("idpropiedad").getAsInt();
                                                    miPropiedad.nombrepropiedad = pro.get("nombrepropiedad").isJsonNull() ? "" : pro.get("nombrepropiedad").getAsString();
                                                    miPropiedad.propietarioid = miCliente.idcliente;
                                                    miPropiedad.administrador.idcliente = pro.get("administradorid").isJsonNull() ? miCliente.idcliente : pro.get("administradorid").getAsInt();
                                                    miPropiedad.fecha_adquisicion = pro.get("fecha_adquisicion").isJsonNull() ? "" : pro.get("fecha_adquisicion").getAsString();
                                                    miPropiedad.area = pro.get("area").isJsonNull() ? 0d : pro.get("area").getAsDouble();
                                                    miPropiedad.caracteristicas_fisograficas = pro.get("caracteristicas_fisograficas").isJsonNull() ? "" : pro.get("caracteristicas_fisograficas").getAsString();
                                                    miPropiedad.descripcion_usos_suelo = pro.get("descripcion_usos_suelo").isJsonNull() ? "" : pro.get("descripcion_usos_suelo").getAsString();
                                                    miPropiedad.condiciones_accesibilidad = pro.get("condiciones_accesibilidad").isJsonNull() ? "" : pro.get("condiciones_accesibilidad").getAsString();
                                                    miPropiedad.caminos_principales = pro.get("caminos_principales").isJsonNull() ? "" : pro.get("caminos_principales").getAsString();
                                                    miPropiedad.caminos_secundarios = pro.get("caminos_secundarios").isJsonNull() ? "" : pro.get("caminos_secundarios").getAsString();
                                                    miPropiedad.fuentes_agua = pro.get("fuentes_agua").isJsonNull() ? "" : pro.get("fuentes_agua").getAsString();
                                                    miPropiedad.norte = pro.get("norte").isJsonNull() ? "" : pro.get("norte").getAsString();
                                                    miPropiedad.sur = pro.get("sur").isJsonNull() ? "" : pro.get("sur").getAsString();
                                                    miPropiedad.este = pro.get("este").isJsonNull() ? "" : pro.get("este").getAsString();
                                                    miPropiedad.oeste = pro.get("oeste").isJsonNull() ? "" : pro.get("oeste").getAsString();
                                                    miPropiedad.cobertura_forestal = pro.get("cobertura_forestal").isJsonNull() ? "" : pro.get("cobertura_forestal").getAsString();
                                                    miPropiedad.razas_ganado = pro.get("razas_ganado").isJsonNull() ? "" : pro.get("razas_ganado").getAsString();
                                                    miPropiedad.num_vacas_paridas = pro.get("num_vacas_paridas").isJsonNull() ? 0 : pro.get("num_vacas_paridas").getAsInt();
                                                    miPropiedad.num_vacas_preñadas = pro.get("num_vacas_prenadas").isJsonNull() ? 0 : pro.get("num_vacas_prenadas").getAsInt();
                                                    miPropiedad.num_vacas_solteras = pro.get("num_vacas_solteras").isJsonNull() ? 0 : pro.get("num_vacas_solteras").getAsInt();
                                                    miPropiedad.num_terneros = pro.get("num_terneros").isJsonNull() ? 0 : pro.get("num_terneros").getAsInt();
                                                    miPropiedad.num_toros = pro.get("num_toros").isJsonNull() ? 0 : pro.get("num_toros").getAsInt();
                                                    miPropiedad.num_equinos = pro.get("num_equinos").isJsonNull() ? 0 : pro.get("num_equinos").getAsInt();
                                                    miPropiedad.num_aves = pro.get("num_aves").isJsonNull() ? 0 : pro.get("num_aves").getAsInt();
                                                    miPropiedad.num_cerdos = pro.get("num_cerdos").isJsonNull() ? 0 : pro.get("num_cerdos").getAsInt();
                                                    miPropiedad.num_mascotas = pro.get("num_mascotas").isJsonNull() ? 0 : pro.get("num_mascotas").getAsInt();
                                                    miPropiedad.otros = pro.get("otros").isJsonNull() ? "" : pro.get("otros").getAsString();
                                                    miPropiedad.parroquiaid = pro.get("parroquiaid").isJsonNull() ? 0 : pro.get("parroquiaid").getAsInt();
                                                    miPropiedad.usuarioid = pro.get("usuarioid").isJsonNull() ? 0 : pro.get("usuarioid").getAsInt();
                                                    miPropiedad.nip_administrador = pro.get("nip_administrador").isJsonNull() ? miCliente.nip : pro.get("nip_administrador").getAsString();
                                                    miPropiedad.direccion = pro.get("direccion").isJsonNull() ? "" : pro.get("direccion").getAsString();

                                                    JsonArray jsonUsoSuelo = pro.get("usosuelo").isJsonArray() ? pro.getAsJsonArray("usosuelo") : null;
                                                    if (jsonUsoSuelo != null) {
                                                        if (jsonUsoSuelo.size() > 0) {
                                                            for (JsonElement eleUso : jsonUsoSuelo) {
                                                                JsonObject uso = eleUso.isJsonNull() ? null : eleUso.getAsJsonObject();
                                                                if (uso == null)
                                                                    continue;
                                                                UsoSuelo miUso = new UsoSuelo();
                                                                miUso.propiedadid = uso.get("propiedadid").getAsInt();
                                                                miUso.tipo_cultivo.codigocatalogo = uso.get("tipo_cultivo").getAsString();
                                                                miUso.area_cultivo = uso.get("area_cultivo").getAsDouble();
                                                                miUso.variedad_sembrada = uso.get("variedad_sembrada").getAsString();
                                                                miUso.observacion = uso.get("observacion").getAsString();
                                                                miUso.orden = uso.get("orden").getAsInt();
                                                                miPropiedad.listaUsoSuelo.add(miUso);
                                                            }
                                                        }
                                                    }

                                                    JsonArray jsonFoda = pro.get("foda").isJsonArray() ? pro.getAsJsonArray("foda") : null;
                                                    if (jsonFoda != null) {
                                                        if (jsonFoda.size() > 0) {
                                                            for (JsonElement eleFoda : jsonFoda) {
                                                                JsonObject foda = eleFoda.isJsonNull() ? null : eleFoda.getAsJsonObject();
                                                                if (foda == null)
                                                                    continue;
                                                                FodaPropiedad miFoda = new FodaPropiedad();
                                                                miFoda.ganaderoid = foda.get("ganaderoid").getAsInt();
                                                                miFoda.propiedadid = foda.get("propiedadid").getAsInt();
                                                                miFoda.tipo = foda.get("tipo").getAsInt();
                                                                miFoda.descripcion = foda.get("descripcion").getAsString();
                                                                miFoda.causas = foda.get("causas").getAsString();
                                                                miFoda.solucion_1 = foda.get("solucion_1").getAsString();
                                                                miFoda.solucion_2 = foda.get("solucion_2").getAsString();
                                                                miFoda.observacion = foda.get("observacion").getAsString();
                                                                miPropiedad.listaFoda.add(miFoda);
                                                            }
                                                        }
                                                    }
                                                    if (pro.has("fotos")) {
                                                        jsonFotos = pro.get("fotos").isJsonNull() ? null : pro.getAsJsonArray("fotos");
                                                        if (jsonFotos != null) {
                                                            for (JsonElement eleFoto : jsonFotos) {
                                                                JsonObject jFoto = eleFoto.isJsonObject() ? eleFoto.getAsJsonObject() : null;
                                                                if (jFoto == null)
                                                                    continue;
                                                                Foto miFoto = new Foto();
                                                                miFoto.name = jFoto.get("name").isJsonNull() ? "" : jFoto.get("name").getAsString();
                                                                miFoto.path = jFoto.get("path_phone").isJsonNull() ? "" : jFoto.get("path_phone").getAsString();
                                                                miFoto.tipo = "P";
                                                                miPropiedad.fotos.add(miFoto);
                                                            }
                                                        }
                                                    }

                                                    miPropiedad.Save();
                                                }
                                            } else {
                                                Log.d("TAG", "no hay propiedades");
                                            }

                                            JsonArray jsonMascotas = clie.get("mascotas").isJsonNull() ? null : clie.getAsJsonArray("mascotas");
                                            if (jsonMascotas != null) {
                                                for (JsonElement elePro : jsonMascotas) {
                                                    JsonObject pro = elePro.isJsonObject() ? elePro.getAsJsonObject() : null;
                                                    if (pro == null)
                                                        continue;
                                                    Mascota miMascota = new Mascota();
                                                    miMascota.codigosistema = pro.get("idmascota").isJsonNull() ? 0 : pro.get("idmascota").getAsInt();
                                                    miMascota.nombre = pro.get("nombre").isJsonNull() ? "" : pro.get("nombre").getAsString();
                                                    miMascota.duenoid = miCliente.idcliente;
                                                    miMascota.color1 = pro.get("color1").isJsonNull() ? "" : pro.get("color1").getAsString();
                                                    miMascota.color2 = pro.get("color2").isJsonNull() ? "" : pro.get("color2").getAsString();
                                                    miMascota.especie.codigocatalogo = pro.get("especieid").isJsonNull() ? "" : pro.get("especieid").getAsString();
                                                    miMascota.raza.codigocatalogo = pro.get("razaid").isJsonNull() ? "" : pro.get("razaid").getAsString();
                                                    miMascota.fechanacimiento = pro.get("fechanacimiento").isJsonNull() ? "1900-01-01" : pro.get("fechanacimiento").getAsString();
                                                    miMascota.lat = pro.get("lat").isJsonNull() ? 0d : pro.get("lat").getAsDouble();
                                                    miMascota.lon = pro.get("lon").isJsonNull() ? 0d : pro.get("lon").getAsDouble();
                                                    miMascota.usuarioid = pro.get("usuarioid").isJsonNull() ? SQLite.usuario.IdUsuario : pro.get("usuarioid").getAsInt();
                                                    miMascota.sexo = pro.get("sexo").isJsonNull() ? "M" : pro.get("sexo").getAsString();
                                                    miMascota.observacion = pro.get("observacion").isJsonNull() ? "" : pro.get("observacion").getAsString();
                                                    miMascota.peso = pro.get("peso").isJsonNull() ? 0d : pro.get("peso").getAsDouble();
                                                    miMascota.longdaten = Utils.longDate(miMascota.fechanacimiento);
                                                    miMascota.nipdueno = miCliente.nip;
                                                    miMascota.fechacelular = pro.get("fechareg").isJsonNull() ? Utils.getDateFormat("yyyy-MM-dd HH:mm:ss") : pro.get("fechareg").getAsString();
                                                    miMascota.codigomascota = pro.get("codigomascota").getAsString();

                                                    if (pro.has("fotos")) {
                                                        jsonFotos = pro.get("fotos").isJsonNull() ? null : pro.getAsJsonArray("fotos");
                                                        if (jsonFotos != null) {
                                                            for (JsonElement eleFoto : jsonFotos) {
                                                                JsonObject jFoto = eleFoto.isJsonObject() ? eleFoto.getAsJsonObject() : null;
                                                                if (jFoto == null)
                                                                    continue;
                                                                Foto miFoto = new Foto();
                                                                miFoto.name = jFoto.get("name").isJsonNull() ? "" : jFoto.get("name").getAsString();
                                                                miFoto.path = jFoto.get("path_phone").isJsonNull() ? "" : jFoto.get("path_phone").getAsString();
                                                                miFoto.tipo = "M";
                                                                miMascota.fotos.add(miFoto);
                                                            }
                                                        }
                                                    }

                                                    if (pro.has("consultas")) {
                                                        JsonArray jConsultas = pro.get("consultas").isJsonNull() ? null : pro.get("consultas").getAsJsonArray();
                                                        if (jConsultas != null) {
                                                            Consulta miCons;
                                                            for (JsonElement jCons : jConsultas) {
                                                                JsonObject oCons = jCons.getAsJsonObject();
                                                                miCons = new Consulta();
                                                                miCons.codigosistema = oCons.get("idconsulta").getAsInt();
                                                                miCons.codigoconsulta = oCons.get("codigoconsulta").getAsString();
                                                                miCons.usuarioid = oCons.get("usuarioid").getAsInt();
                                                                miCons.nombreusuario = oCons.get("nombreusuario").getAsString();
                                                                miCons.diagnostico = oCons.get("diagnostico").getAsString();
                                                                miCons.receta = oCons.get("receta").getAsString();
                                                                miCons.prescripcion = oCons.get("prescripcion").getAsString();
                                                                miCons.fechaconsulta = oCons.get("fechaconsulta").getAsString();
                                                                miCons.fechacelular = oCons.get("fechacelular").getAsString();
                                                                miCons.longdatec = Utils.longDateF(miCons.fechacelular, "yyyy-MM-dd HH:mm:ss");
                                                                miCons.lat = oCons.get("lat").getAsDouble();
                                                                miCons.lon = oCons.get("lon").getAsDouble();

                                                                miMascota.consultas.add(miCons);
                                                            }
                                                        }
                                                    }

                                                    if (pro.has("vacunas")) {
                                                        JsonArray jVacunas = pro.get("vacunas").isJsonNull() ? null : pro.get("vacunas").getAsJsonArray();
                                                        if (jVacunas != null) {
                                                            MedicamentoMascota miVac;
                                                            for (JsonElement jVac : jVacunas) {
                                                                JsonObject oVac = jVac.getAsJsonObject();
                                                                miVac = new MedicamentoMascota();
                                                                miVac.codigosistema = oVac.get("idmascotamedicamento").getAsInt();
                                                                miVac.codigo = oVac.get("codigo").getAsString();
                                                                miVac.tipo = oVac.get("tipo").getAsString();
                                                                miVac.observacion = oVac.get("observacion").getAsString();
                                                                miVac.medicamento.idmedicamento = oVac.get("medicamentoid").getAsInt();
                                                                miVac.fechaaplicacion = oVac.get("fechaaplicacion").getAsString();
                                                                miVac.proximaaplicacion = oVac.get("proximaaplicacion").getAsString();
                                                                miVac.fechacelular = oVac.get("fechacelular").getAsString();
                                                                miVac.longdate = Utils.longDateF(miVac.fechacelular, "yyyy-MM-dd HH:mm:ss");
                                                                miVac.lat = oVac.get("lat").getAsDouble();
                                                                miVac.lon = oVac.get("lon").getAsDouble();
                                                                miVac.usuarioid = oVac.get("usuarioid").getAsInt();
                                                                miVac.nombreusuario = oVac.get("nombreusuario").getAsString();

                                                                miMascota.vacunas.add(miVac);
                                                            }
                                                        }
                                                    }

                                                    miMascota.Save();
                                                }
                                            } else {
                                                Log.d("TAG", "no hay mascotas");
                                            }
                                            numClientUpdate++;
                                        }
                                    }
                                    if (numClientUpdate == jsonClientes.size()) {
                                        Banner.make(rootView, MainActivity.this, Banner.SUCCESS, Constants.MSG_PROCESO_COMPLETADO + "\nSe sincronizó " + numClientUpdate + " registro(s). " + obj.get("message").getAsString(), Banner.BOTTOM, 3000).show();
                                        try {
                                            List<Fragment> fragments = fragmentManager.getFragments();
                                            if (fragments != null) {
                                                for (Fragment f : fragments) {
                                                    Log.d("TAGMAIN", "Fls: " + f.getClass().getSimpleName());
                                                    if (f.getClass().getSimpleName().equalsIgnoreCase("clientefragment") && f.isVisible()) {
                                                        fragment = f;
                                                        break;
                                                    } else if (f.getClass().getSimpleName().equalsIgnoreCase("principalfragment") && f.isVisible()) {
                                                        fragment = f;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (fragment != null) {
                                                if (fragment.getClass().getSimpleName().equalsIgnoreCase("clientefragment") && fragment.isVisible())
                                                    ((ClienteFragment) fragment).CargarDatos(false);
                                                else if (fragment.getClass().getSimpleName().equalsIgnoreCase("principalfragment") && fragment.isVisible())
                                                    ((PrincipalFragment) fragment).BuscaResumen("");
                                                else if (fragment.getClass().getSimpleName().equalsIgnoreCase("mascotafragment") && fragment.isVisible())
                                                    ((MascotaFragment) fragment).CargarDatos(false);
                                            }
                                        } catch (Exception e) {
                                            Log.d("TAGMAIN", e.getMessage());
                                        }
                                    } else
                                        Banner.make(rootView, MainActivity.this, Banner.WARNING, Constants.MSG_PROCESO_NO_COMPLETADO + "\nSe sincronizó " + numClientUpdate + "/" + jsonClientes.size() + " registro(s). " + obj.get("message").getAsString(), Banner.BOTTOM, 3500).show();
                                }

                                JsonArray jsonMedicamentos = obj.has("medicamentos")?obj.getAsJsonArray("medicamentos"):null;
                                if(jsonMedicamentos != null){
                                    Type listType = new TypeToken<List<Medicamento>>(){}.getType();
                                    List<Medicamento> lstMed = new Gson().fromJson(jsonMedicamentos, listType);
                                    Medicamento.SaveLista(lstMed);
                                }
                            } else
                                Utils.showErrorDialog(MainActivity.this, "Error", obj.get("message").getAsString());
                        } else {
                            Banner.make(rootView, MainActivity.this, Banner.ERROR, Constants.MSG_USUARIO_CLAVE_INCORRECTO, Banner.BOTTOM, 3000).show();
                        }
                    } catch (JsonParseException ex) {
                        Log.d("TAG", ex.getMessage());
                    }
                    pbProgreso.dismiss();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Utils.showErrorDialog(MainActivity.this, "Error", t.getMessage());
                    Log.d("TAG", t.getMessage());
                    pbProgreso.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG", e.getMessage());
            Utils.showErrorDialog(MainActivity.this, "Error", e.getMessage());
            pbProgreso.dismiss();
        }
    }

    private void sincronizaComprobantes(final Context context) {
        try {
            if (!Utils.isOnlineNet(SQLite.configuracion.urlbase)) {
                Banner.make(rootView, this, Banner.ERROR, Constants.MSG_COMPROBAR_CONEXION_INTERNET, Banner.BOTTOM, 3000).show();
                return;
            }

            List<Comprobante> listComprobantes = Comprobante.getPorSincronizar(SQLite.usuario.IdUsuario);
            if (listComprobantes == null)
                listComprobantes = new ArrayList<>();

            if (listComprobantes.size() == 0) {
                Banner.make(rootView, this, Banner.INFO, "No hay comprobantes por sincronizar.", Banner.BOTTOM, 2000).show();
                return;
            }

            pbProgreso.setTitle("Sincronizando comprobantes");
            pbProgreso.setMessage("Espere un momento...");
            pbProgreso.setCancelable(false);
            pbProgreso.show();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SQLite.configuracion.url_ws)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            ComprobanteInterface miInterface = retrofit.create(ComprobanteInterface.class);

            Map<String, Object> post = new HashMap<>();
            post.put("usuario", SQLite.usuario.Usuario);
            post.put("clave", SQLite.usuario.Clave);
            post.put("comprobantes", listComprobantes);
            post.put("establecimientoid", SQLite.usuario.sucursal.IdEstablecimiento);
            String json = post.toString();
            Log.d("TAGJSON", json);
            Call<JsonObject> call = null;
            call = miInterface.LoadComprobantes(post);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        Banner.make(rootView, MainActivity.this, Banner.ERROR, "Código:" + response.code() + " - " + response.message(), Banner.BOTTOM, 2500).show();
                        pbProgreso.dismiss();
                        return;
                    }
                    try {

                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (!obj.get("haserror").getAsBoolean()) {
                                JsonArray jsonComprobantesUpdate = obj.getAsJsonArray("comprobantesupdate");

                                if (obj.has("productos")) {
                                    JsonArray jsonProductos = obj.getAsJsonArray("productos");
                                    if (jsonProductos != null) {
                                        Producto.Delete(SQLite.usuario.sucursal.IdEstablecimiento);
                                        int num = 1;
                                        for (JsonElement pro : jsonProductos) {
                                            JsonObject prod = pro.getAsJsonObject();
                                            Producto miProducto = new Gson().fromJson(prod, Producto.class);
                                            if (miProducto != null) {
                                                if (miProducto.Save()) {
                                                    num++;
                                                    Log.d("TAG", prod.get("nombreproducto").getAsString());
                                                }
                                            }
                                        }
                                    }
                                }

                                if (jsonComprobantesUpdate != null) {
                                    int numUpdate = 0;
                                    ContentValues values;
                                    for (JsonElement ele : jsonComprobantesUpdate) {
                                        JsonObject upd = ele.getAsJsonObject();
                                        //ACTUALIZAR EL CLIENTE
                                        values = new ContentValues();
                                        values.put("codigosistema", upd.get("codigosistema_cliente").getAsInt());
                                        values.put("actualizado", 0);
                                        Cliente.Update(upd.get("idcliente").getAsInt(), values);

                                        values = new ContentValues();
                                        values.put("codigosistema", upd.get("codigosistema_comprobante").getAsInt());
                                        values.put("estado", 1);
                                        if (Comprobante.Update(upd.get("idcomprobante").getAsInt(), values))
                                            numUpdate++;
                                    }

                                    if (numUpdate == jsonComprobantesUpdate.size()) {
                                        Banner.make(rootView, MainActivity.this, Banner.SUCCESS, Constants.MSG_PROCESO_COMPLETADO
                                                + "\nSe sincronizó " + numUpdate + " comprobante(s)."
                                                + "\n" + obj.get("message").getAsString(), Banner.BOTTOM, 3000).show();

                                        List<Fragment> fragments = fragmentManager.getFragments();
                                        if (fragments != null) {
                                            for (Fragment f : fragments) {
                                                Log.d("TAGMAIN", "Fls: " + f.getClass().getSimpleName());
                                                if (f.getClass().getSimpleName().equalsIgnoreCase("principalfragment") && f.isVisible()) {
                                                    fragment = f;
                                                    break;
                                                }
                                            }
                                        }
                                        if (fragment != null) {
                                            if (fragment.getClass().getSimpleName().equalsIgnoreCase("principalfragment") ||
                                                    (listaFragments.size() > 0 && listaFragments.get(listaFragments.size() - 1).toLowerCase().contains("principalfragment"))) {
                                                ((PrincipalFragment) fragment).BuscaResumen("");
                                            }
                                        }
                                    } else
                                        Banner.make(rootView, MainActivity.this, Banner.ERROR, Constants.MSG_PROCESO_NO_COMPLETADO
                                                + "\nSe sincronizó " + numUpdate + "/" + jsonComprobantesUpdate.size() + " comprobante(s)."
                                                + "\n" + obj.get("message").getAsString(), Banner.BOTTOM, 3500).show();


                                }

                            } else
                                Utils.showErrorDialog(MainActivity.this, "Error", obj.get("message").getAsString());
                        } else {
                            Banner.make(rootView, MainActivity.this, Banner.ERROR, Constants.MSG_USUARIO_CLAVE_INCORRECTO, Banner.BOTTOM, 3000).show();
                        }
                    } catch (JsonParseException ex) {
                        Log.d("TAG", ex.getMessage());
                    }
                    pbProgreso.dismiss();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Utils.showErrorDialog(MainActivity.this, "Error", t.getMessage());
                    Log.d("TAG", t.getMessage());
                    pbProgreso.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG", e.getMessage());
            Utils.showErrorDialog(this, "Error", e.getMessage());
            pbProgreso.dismiss();
        }
    }

    private void sincronizaPedidos(final Context context) {
        try {
            if (!Utils.isOnlineNet(SQLite.configuracion.urlbase)) {
                Banner.make(rootView, this, Banner.ERROR, Constants.MSG_COMPROBAR_CONEXION_INTERNET, Banner.BOTTOM, 3000).show();
                return;
            }

            List<Pedido> listPedidos = Pedido.getPorSincronizar(SQLite.usuario.IdUsuario);
            if (listPedidos == null)
                listPedidos = new ArrayList<>();

            if (listPedidos.size() == 0) {
                Banner.make(rootView, this, Banner.INFO, "No hay pedidos por sincronizar.", Banner.BOTTOM, 2000).show();
                return;
            }

            pbProgreso.setTitle("Sincronizando pedidos");
            pbProgreso.setMessage("Espere un momento...");
            pbProgreso.setCancelable(false);
            pbProgreso.show();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SQLite.configuracion.url_ws)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            ComprobanteInterface miInterface = retrofit.create(ComprobanteInterface.class);

            Map<String, Object> post = new HashMap<>();
            post.put("usuario", SQLite.usuario.Usuario);
            post.put("clave", SQLite.usuario.Clave);
            post.put("pedidos", listPedidos);
            String json = post.toString();
            Log.d("TAGJSON", json);
            Call<JsonObject> call = null;
            call = miInterface.LoadPedidos(post);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        Banner.make(rootView, MainActivity.this, Banner.ERROR, "Código: " + response.code() + " - " + response.message(), Banner.BOTTOM, 3000).show();
                        pbProgreso.dismiss();
                        return;
                    }
                    try {

                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (!obj.get("haserror").getAsBoolean()) {
                                JsonArray jsonPedidosUpdate = obj.getAsJsonArray("pedidosupdate");
                                if (jsonPedidosUpdate != null) {
                                    int numUpdate = 0;
                                    ContentValues values;
                                    for (JsonElement ele : jsonPedidosUpdate) {
                                        JsonObject upd = ele.getAsJsonObject();
                                        //ACTUALIZAR EL CLIENTE
                                        values = new ContentValues();
                                        values.put("codigosistema", upd.get("codigosistema_cliente").getAsInt());
                                        values.put("actualizado", 0);
                                        Cliente.Update(upd.get("idcliente").getAsInt(), values);

                                        values = new ContentValues();
                                        values.put("codigosistema", upd.get("codigosistema_pedido").getAsInt());
                                        values.put("estado", upd.get("codigosistema_pedido").getAsInt());
                                        values.put("secuencialsistema", upd.get("secuencialsistema").getAsString());
                                        if (Pedido.Update(upd.get("idpedido").getAsInt(), values))
                                            numUpdate++;
                                    }

                                    if (obj.has("secuencial_pe")) {
                                        Integer secuencial_pe = obj.get("secuencial_pe").getAsInt();
                                        Comprobante comprobante = new Comprobante();
                                        comprobante.secuencial = secuencial_pe;
                                        comprobante.establecimientoid = SQLite.usuario.sucursal.IdEstablecimiento;
                                        comprobante.codigoestablecimiento = SQLite.usuario.sucursal.CodigoEstablecimiento;
                                        comprobante.puntoemision = SQLite.usuario.sucursal.PuntoEmision;
                                        comprobante.tipotransaccion = "PC";
                                        comprobante.actualizasecuencial();
                                    }
                                    if (numUpdate == jsonPedidosUpdate.size()) {
                                        Banner.make(rootView, MainActivity.this, Banner.SUCCESS, Constants.MSG_PROCESO_COMPLETADO
                                                + "\nSe sincronizó " + numUpdate + " pedido(s)."
                                                + "\n" + obj.get("message").getAsString(), Banner.BOTTOM, 3000).show();

                                        try {
                                            List<Fragment> fragments = fragmentManager.getFragments();
                                            if (fragments != null) {
                                                for (Fragment f : fragments) {
                                                    Log.d("TAGMAIN", "Fls: " + f.getClass().getSimpleName());
                                                    if (f.getClass().getSimpleName().equalsIgnoreCase("principalfragment") && f.isVisible()) {
                                                        fragment = f;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (fragment != null) {
                                                Log.d("TAGMAIN1", fragment.getClass().getSimpleName());
                                                if (fragment.getClass().getSimpleName().equalsIgnoreCase("principalfragment") ||
                                                        (listaFragments.size() > 0 && listaFragments.get(listaFragments.size() - 1).toLowerCase().contains("principalfragment"))) {
                                                    ((PrincipalFragment) fragment).BuscaResumen("");
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.d("TAGMAIN", e.getMessage());
                                        }
                                    } else {
                                        Banner.make(rootView, MainActivity.this, Banner.SUCCESS, Constants.MSG_PROCESO_NO_COMPLETADO
                                                + "\nSe sincronizó " + numUpdate + "/" + jsonPedidosUpdate.size() + " pedido(s)."
                                                + "\n" + obj.get("message").getAsString(), Banner.BOTTOM, 3500).show();
                                    }
                                }

                            } else
                                Utils.showErrorDialog(MainActivity.this, "Error", obj.get("message").getAsString());
                        } else {
                            Banner.make(rootView, MainActivity.this, Banner.ERROR, Constants.MSG_USUARIO_CLAVE_INCORRECTO, Banner.BOTTOM, 3000).show();
                        }
                    } catch (JsonParseException ex) {
                        Log.d("TAG", ex.getMessage());
                    }
                    pbProgreso.dismiss();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Utils.showErrorDialog(MainActivity.this, "Error", t.getMessage());
                    Log.d("TAG", t.getMessage());
                    pbProgreso.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG", e.getMessage());
            Utils.showErrorDialog(this, "Error", e.getMessage());
            pbProgreso.dismiss();
        }
    }

    private void contarproductos(Context context) {
        try {
            Log.d("TAG", String.valueOf(Producto.getAll(SQLite.usuario.sucursal.IdEstablecimiento).size()));
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
    }

    private void descargaProductos(final Context context) {
        try {
            if (!Utils.isOnlineNet(SQLite.configuracion.urlbase)) {
                Banner.make(rootView, MainActivity.this, Banner.ERROR, Constants.MSG_COMPROBAR_CONEXION_INTERNET, Banner.BOTTOM, 3000).show();
                return;
            }

            pbProgreso.setTitle("Descargando productos");
            pbProgreso.setMessage("Espere un momento...");
            pbProgreso.setCancelable(false);
            pbProgreso.show();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SQLite.configuracion.url_ws)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            ProductoInterface miInterface = retrofit.create(ProductoInterface.class);

            Call<JsonObject> call = null;
            call = miInterface.GetProductos(SQLite.usuario.Usuario, SQLite.usuario.Clave, SQLite.usuario.sucursal.IdEstablecimiento);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        Banner.make(rootView, MainActivity.this, Banner.ERROR, "Código: " + response.code() + " - " + response.message(), Banner.BOTTOM, 3000).show();
                        pbProgreso.dismiss();
                        return;
                    }
                    try {
                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (!obj.get("haserror").getAsBoolean()) {
                                JsonArray jsonProductos = obj.getAsJsonArray("productos");
                                if (jsonProductos != null) {
                                    int numProd = 0;
                                    Producto.Delete(SQLite.usuario.sucursal.IdEstablecimiento);
                                    for (JsonElement ele : jsonProductos) {
                                        JsonObject prod = ele.getAsJsonObject();
                                        Producto miProducto = new Gson().fromJson(prod, Producto.class);
                                    /*miProducto.idproducto = prod.get("idproducto").getAsInt();
                                    miProducto.codigoproducto = prod.get("codigoproducto").getAsString();
                                    miProducto.nombreproducto = prod.get("nombreproducto").getAsString();
                                    miProducto.pvp = prod.get("pvp").getAsDouble();
                                    miProducto.unidadid = prod.get("unidadid").getAsInt();
                                    miProducto.unidadesporcaja = prod.get("unidadesporcaja").getAsInt();
                                    miProducto.iva = prod.get("iva").getAsInt();
                                    miProducto.ice = prod.get("ice").getAsInt();
                                    miProducto.factorconversion = prod.get("factorconversion").getAsDouble();
                                    miProducto.pvp1 = prod.get("pvp1").getAsDouble();
                                    miProducto.pvp2 = prod.get("pvp2").getAsDouble();
                                    miProducto.pvp3 = prod.get("pvp3").getAsDouble();
                                    miProducto.pvp4 = prod.get("pvp4").getAsDouble();
                                    miProducto.pvp5 = prod.get("pvp5").getAsDouble();
                                    miProducto.stock = prod.get("stock").getAsDouble();
                                    miProducto.porcentajeiva = prod.get("porcentajeiva").getAsDouble();
                                    JsonArray jsonLotes = prod.has("lotes")?prod.get("lotes").getAsJsonArray():null;
                                    if(jsonLotes != null){
                                        miProducto.lotes = new Gson().fromJson(jsonLotes,miProducto.lotes.getClass());
                                    }*/
                                        if (miProducto != null) {
                                            if (miProducto.Save())
                                                numProd++;
                                            Log.d("TAG", prod.get("nombreproducto").getAsString());
                                        }
                                    }
                                    if (numProd == jsonProductos.size())
                                        Banner.make(rootView, MainActivity.this, Banner.SUCCESS, Constants.MSG_PROCESO_COMPLETADO + (numProd > 0 ? "\nSe descargó " + numProd + " producto(s)" : ""), Banner.BOTTOM, 3000).show();
                                    else
                                        Banner.make(rootView, MainActivity.this, Banner.ERROR, Constants.MSG_PROCESO_NO_COMPLETADO, Banner.BOTTOM, 3000).show();
                                }
                            } else
                                Banner.make(rootView, MainActivity.this, Banner.ERROR, obj.get("message").getAsString(), Banner.BOTTOM, 3000).show();
                        } else
                            Banner.make(rootView, MainActivity.this, Banner.ERROR, Constants.MSG_USUARIO_CLAVE_INCORRECTO, Banner.BOTTOM, 3000).show();
                    } catch (JsonParseException ex) {
                        Log.d("TAG", ex.getMessage());
                    }
                    pbProgreso.dismiss();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Utils.showErrorDialog(MainActivity.this, "Error", t.getMessage());
                    Log.d("TAG", t.getMessage());
                    pbProgreso.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG", e.getMessage());
            Utils.showErrorDialog(this, "Error", e.getMessage());
            pbProgreso.dismiss();
        }
    }

    private void sincronizaPedidos_Inv(final Context context) {
        try {
            if (!Utils.isOnlineNet(SQLite.configuracion.urlbase)) {
                Banner.make(rootView, this, Banner.ERROR, Constants.MSG_COMPROBAR_CONEXION_INTERNET, Banner.BOTTOM, 3000).show();
                return;
            }

            List<PedidoInventario> listPedidos = PedidoInventario.getPorSincronizar(SQLite.usuario.IdUsuario);
            if (listPedidos == null)
                listPedidos = new ArrayList<>();

            if (listPedidos.size() == 0) {
                Banner.make(rootView, this, Banner.INFO, "No hay pedidos por sincronizar.", Banner.BOTTOM, 2000).show();
                return;
            }

            pbProgreso.setTitle("Sincronizando pedidos");
            pbProgreso.setMessage("Espere un momento...");
            pbProgreso.setCancelable(false);
            pbProgreso.show();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SQLite.configuracion.url_ws)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            ComprobanteInterface miInterface = retrofit.create(ComprobanteInterface.class);

            Map<String, Object> post = new HashMap<>();
            post.put("usuario", SQLite.usuario.Usuario);
            post.put("clave", SQLite.usuario.Clave);
            post.put("pedidos", listPedidos);
            post.put("periodo", SQLite.usuario.sucursal.periodo.toString() + SQLite.usuario.sucursal.mesactual);
            post.put("codigoestablecimiento", SQLite.usuario.sucursal.IdSucursal);
            Call<JsonObject> call = miInterface.LoadPedidosInv(post);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        Banner.make(rootView, MainActivity.this, Banner.ERROR, "Código: " + response.code() + " - " + response.message(), Banner.BOTTOM, 3000).show();
                        pbProgreso.dismiss();
                        return;
                    }
                    try {

                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (!obj.get("haserror").getAsBoolean()) {
                                JsonArray jsonPedidosUpdate = obj.getAsJsonArray("pedidosupdate");
                                if (jsonPedidosUpdate != null) {
                                    int numUpdate = 0;
                                    ContentValues values;
                                    for (JsonElement ele : jsonPedidosUpdate) {
                                        JsonObject upd = ele.getAsJsonObject();

                                        values = new ContentValues();
                                        values.put("codigosistema", upd.get("codigosistema_pedido").getAsInt());
                                        values.put("estadomovil", upd.get("codigosistema_pedido").getAsInt());
                                        if (PedidoInventario.Update(upd.get("idpedido").getAsInt(), values))
                                            numUpdate++;
                                    }

                                    if (obj.has("secuencial_pi")) {
                                        Integer secuencial_pi = obj.get("secuencial_pi").getAsInt();
                                        PedidoInventario pedido = new PedidoInventario();
                                        pedido.secuencial = secuencial_pi;
                                        pedido.establecimientoid = SQLite.usuario.sucursal.IdEstablecimiento;
                                        pedido.tipotransaccion = "PI";
                                        pedido.actualizasecuencial();
                                    }
                                    if (numUpdate == jsonPedidosUpdate.size()) {
                                        Banner.make(rootView, MainActivity.this, Banner.SUCCESS, Constants.MSG_PROCESO_COMPLETADO
                                                + "\nSe sincronizó " + numUpdate + " pedido(s)."
                                                + "\n" + obj.get("message").getAsString(), Banner.BOTTOM, 3000).show();

                                        try {
                                            List<Fragment> fragments = fragmentManager.getFragments();
                                            if (fragments != null) {
                                                for (Fragment f : fragments) {
                                                    Log.d("TAGMAIN", "Fls: " + f.getClass().getSimpleName());
                                                    if (f.getClass().getSimpleName().equalsIgnoreCase("principalfragment") && f.isVisible()) {
                                                        fragment = f;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (fragment != null) {
                                                Log.d("TAGMAIN1", fragment.getClass().getSimpleName());
                                                if (fragment.getClass().getSimpleName().equalsIgnoreCase("principalfragment") ||
                                                        (listaFragments.size() > 0 && listaFragments.get(listaFragments.size() - 1).toLowerCase().contains("principalfragment"))) {
                                                    ((PrincipalFragment) fragment).BuscaResumen("");
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.d("TAGMAIN", e.getMessage());
                                        }
                                    } else {
                                        Banner.make(rootView, MainActivity.this, Banner.SUCCESS, Constants.MSG_PROCESO_NO_COMPLETADO
                                                + "\nSe sincronizó " + numUpdate + "/" + jsonPedidosUpdate.size() + " pedido(s)."
                                                + "\n" + obj.get("message").getAsString(), Banner.BOTTOM, 3500).show();
                                    }
                                }

                            } else
                                Utils.showErrorDialog(MainActivity.this, "Error", obj.get("message").getAsString());
                        } else {
                            Banner.make(rootView, MainActivity.this, Banner.ERROR, Constants.MSG_USUARIO_CLAVE_INCORRECTO, Banner.BOTTOM, 3000).show();
                        }
                    } catch (JsonParseException ex) {
                        Log.d("TAG", ex.getMessage());
                    }
                    pbProgreso.dismiss();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Utils.showErrorDialog(MainActivity.this, "Error", t.getMessage());
                    Log.d("TAG", t.getMessage());
                    pbProgreso.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TAG", e.getMessage());
            Utils.showErrorDialog(this, "Error", e.getMessage());
            pbProgreso.dismiss();
        }
    }

    private void VerificarPermisos() {
        try {
            for (int i = 0; i < navigation.getMenu().size(); i++) {
                MenuItem menuItem = navigation.getMenu().getItem(i);
                if (menuItem.hasSubMenu()) {
                    /*for (int j = 0; j < menuItem.getSubMenu().size(); j++) {
                        MenuItem menuSubItem = menuItem.getSubMenu().getItem(j);
                        menuSubItem.setVisible(SQLite.usuario.VerificaPermiso(this,menuItem.getTitleCondensed().toString(),"lectura"));
                        break;
                    }*/
                } else {
                    menuItem.setVisible(SQLite.usuario.VerificaPermiso(this, menuItem.getTitleCondensed().toString().toUpperCase(), "lectura"));
                }
            }
            navigation.getMenu().findItem(R.id.nav_home).setVisible(true);

        } catch (Exception e) {
            Log.d("TAGPERMISO", "VerificarPermisos(): " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
    }

    private static long presionado;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                try {
                    listaFragments.remove(listaFragments.size() - 1);
                    Log.d("TAGMAIN", "onBackPressed: " + fragment.getClass().getSimpleName());
                    List<Fragment> fragments = fragmentManager.getFragments();
                    if (fragments != null) {
                        for (Fragment f : fragments)
                            Log.d("TAGMAIN", "F: " + f.getClass().getSimpleName());
                    }
                    for (String n : listaFragments)
                        Log.d("TAGMAIN", "N: " + n);
                } catch (Exception e) {
                    Log.d("TAGMAIN", e.getMessage());
                }
                super.onBackPressed();
            } else {
                if (presionado + 2000 > System.currentTimeMillis())
                    super.onBackPressed();
                else
                    Utils.showMessage(this, "Vuelve a presionar para salir");
                presionado = System.currentTimeMillis();
            }
        }
    }

    private void IniciarServicio() {
        try {
            ComponentName componentName = new ComponentName(getApplicationContext(), JobServiceGPS.class);
            JobInfo info;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                info = new JobInfo.Builder(ID_SERVICE_LOCATION, componentName)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPersisted(true)
                        .setMinimumLatency(5 * 1000)
                        .build();
            } else {
                info = new JobInfo.Builder(ID_SERVICE_LOCATION, componentName)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPersisted(true)
                        .setPeriodic(5 * 1000)
                        .build();
            }
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            int result = scheduler.schedule(info);
            if (result == JobScheduler.RESULT_SUCCESS)
                Log.d("TAG", "Completado correctamente");
            else
                Log.d("TAG", "Ha ocurrido un error en el job!!!");
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
    }

    private void DetenerServicio() {
        try {
            JobScheduler schedule = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            schedule.cancel(ID_SERVICE_LOCATION);
            Log.d("TAG", "Job Cancelado por el usuario!");
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
    }

    private boolean VerificaServicio() {
        boolean result = false;
        try {
            JobScheduler schedule = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            JobInfo jInfo = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                jInfo = schedule.getPendingJob(ID_SERVICE_LOCATION);
            }
            if (jInfo != null) {
                result = true;
                Log.d("TAG", "EL SERVICIO ESTA INICIADO");
            } else
                Log.d("TAG", "EL SERVICIO NO ESTÀ INICIADO");
        } catch (Exception e) {
            Log.d("TAG", e.getMessage());
        }
        return result;
    }

    private void loadUbicacion() {
        try {
            Map<String, Object> datos = new HashMap<>();
            List<Ubicacion> ubicaciones = Ubicacion.getListSC(SQLite.usuario.IdUsuario);
            if (ubicaciones == null) {
                Log.d("TAGMAIN", "La lista es nula");
                return;
            }
            if (ubicaciones.size() == 0) {
                Log.d("TAGMAIN", "La lista está vacía");
                return;
            }
            datos.put("ubicaciones", ubicaciones);
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SQLite.configuracion.url_ws)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            UsuarioInterface miInterface = retrofit.create(UsuarioInterface.class);

            Call<JsonObject> call = null;
            call = miInterface.loadUbicacion(datos);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    try {
                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (!obj.get("haserror").getAsBoolean()) {
                                JsonArray jsonUpdate = obj.getAsJsonArray("ubicacionesupdate");
                                if (jsonUpdate != null) {
                                    int numUpdate = 0;
                                    ContentValues values;
                                    for (JsonElement ele : jsonUpdate) {
                                        JsonObject upd = ele.getAsJsonObject();

                                        values = new ContentValues();
                                        values.put("estado", upd.get("codigosistema").getAsInt());
                                        if (Ubicacion.Update(upd.get("idubicacion").getAsInt(), values))
                                            numUpdate++;
                                    }
                                    Log.d("TAGMAIN", "Se subieron " + numUpdate + "/" + jsonUpdate.size() + " ubicaciones");
                                } else
                                    Log.d("TAGMAIN", "Error: El webservice no devolvió valores");
                            } else
                                Log.d("TAGMAIN", "Error: " + obj.get("message").getAsString());
                        }
                    } catch (Exception e) {
                        Log.d("TAGMAIN1", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("TAGMAIN2", t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.d("TAGMAIN3", e.getMessage());
        }
    }
}
