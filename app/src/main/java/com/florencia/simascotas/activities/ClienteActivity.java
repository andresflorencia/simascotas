package com.florencia.simascotas.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.florencia.simascotas.BuildConfig;
import com.florencia.simascotas.R;
import com.florencia.simascotas.adapters.ImageAdapter;
import com.florencia.simascotas.adapters.MascotaAdapter;
import com.florencia.simascotas.adapters.PropiedadAdapter;
import com.florencia.simascotas.models.Canton;
import com.florencia.simascotas.models.Cliente;
import com.florencia.simascotas.models.Foto;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.models.Parroquia;
import com.florencia.simascotas.models.Propiedad;
import com.florencia.simascotas.models.Provincia;
import com.florencia.simascotas.models.TipoIdentificacion;
import com.florencia.simascotas.services.GPSTracker;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.shasin.notificationbanner.Banner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClienteActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {

    Spinner cbTipoDocumento, cbProvincia, cbCanton, cbParroquia;;
    EditText txtNIP, txtRazonSocial, txtNombreComercial, txtLatitud, txtLongitud, txtDireccion,
            txtFono1, txtFono2, txtCorreo, txtObservacion;
    ImageButton btnObtenerDireccion;
    Cliente miCliente;
    View rootView;
    boolean isReturn = false, band = false;

    TextView lblMessage, lblTitle, lblInfoPersonal, lblInfoContacto, lblInfoAdicional, lblInfo,
            lblPropiedades, lblMascotas, lblFotos;
    LinearLayout lyInfoPersonal, lyInfoContacto, lyInfoAdicional, lyPropiedades, lyMascotas, lyFotos;
    BottomSheetDialog btsDialog;
    Button btnPositive, btnNegative, btnNewPropiedad, btnNewMascota;
    ImageButton btnNewFoto;
    View viewSeparator;
    String tipoAccion="";
    Toolbar toolbar;
    List<Provincia> provincias = new ArrayList<>();
    List<Canton> cantones = new ArrayList<>();
    List<Parroquia> parroquias = new ArrayList<>();
    Provincia provActual = new Provincia();
    Canton canActual = new Canton();
    CardView cvInfo, cvFoto, cvMascota;
    RecyclerView rvPropiedades, rvMascotas;
    PropiedadAdapter propiedadAdapter;
    MascotaAdapter mascotaAdapter;
    String path, nameFoto;
    File fileImage;
    Bitmap bitmap;

    RecyclerView rvFotos;
    ImageAdapter imageAdapter;

    List<Foto> listFoto = new ArrayList<>();
    String ExternalDirectory = "";

    public static final int REQUEST_NEW_PROPIEDAD = 10;
    private static final String TAG = "TAGGANADERO_ACTIVITY";
    private static final int REQUEST_NEW_FOTO = 20;
    private static final int REQUEST_SELECCIONA_FOTO = 30;
    public static final int REQUEST_NEW_MASCOTA = 40;

    public ClienteActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rootView = findViewById(android.R.id.content);
        init();

        if(getIntent().getExtras()!=null){
            Integer idcliente =  getIntent().getExtras().getInt("idcliente",0);
            if(idcliente>0){
                BuscarDatos(idcliente,"");
            }
            this.isReturn = getIntent().getExtras().getBoolean("nuevo_cliente",false);
        }
    }

    private void BuscarDatos(Integer id, String nip){
        try{
            Thread th = new Thread(){
                @Override
                public void run(){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(id>0)
                                miCliente = Cliente.get(id, true);
                            else if(!nip.equals(""))
                                miCliente = Cliente.get(nip, true);

                            if(miCliente != null){

                                band = true;
                                Parroquia miparroquia = Parroquia.get(miCliente.parroquiaid);
                                canActual = Canton.get(miparroquia.cantonid);
                                provActual = Provincia.get(canActual.provinciaid);

                                for(Provincia miP:provincias){
                                    if(provActual.idprovincia.equals(miP.idprovincia)){
                                        cbProvincia.setSelection(provincias.indexOf(miP));
                                        break;
                                    }
                                }
                                LlenarComboCantones(provActual.idprovincia, canActual.idcanton);
                                LlenarComboParroquias(canActual.idcanton, miparroquia.idparroquia);

                                txtNIP.setTag(miCliente.idcliente);
                                txtNIP.setText(miCliente.nip);
                                txtRazonSocial.setText(miCliente.razonsocial);
                                txtNombreComercial.setText(miCliente.nombrecomercial);
                                txtLatitud.setText(miCliente.lat.toString());
                                txtLongitud.setText(miCliente.lon.toString());
                                txtDireccion.setText(miCliente.direccion);
                                txtFono1.setText(miCliente.fono1);
                                txtFono2.setText(miCliente.fono2);
                                txtCorreo.setText(miCliente.email);
                                txtObservacion.setText(miCliente.observacion);
                                cvInfo.setVisibility(View.VISIBLE);
                                String _sInfo = getResources().getString(R.string.textFecha).concat(" ").concat(miCliente.fecharegistro);
                                _sInfo = _sInfo.concat("<br>").concat(getResources().getString(R.string.textCategoria)).concat("").concat(miCliente.nombrecategoria);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                    lblInfo.setText(Html.fromHtml(_sInfo, Html.FROM_HTML_MODE_COMPACT));
                                else
                                    lblInfo.setText(Html.fromHtml(_sInfo));

                                if(miCliente.tiponip != null){
                                    for(int i =0; i < cbTipoDocumento.getCount();i++){
                                        TipoIdentificacion ti = (TipoIdentificacion) cbTipoDocumento.getItemAtPosition(i);
                                        if(ti.getCodigo().equals(miCliente.tiponip)){
                                            cbTipoDocumento.setSelection(i,true);
                                            break;
                                        }
                                    }
                                }

                                cvFoto.setVisibility(View.VISIBLE);
                                cvMascota.setVisibility(View.VISIBLE);

                                if (miCliente.propiedades != null) {
                                    propiedadAdapter = new PropiedadAdapter(ClienteActivity.this, miCliente.propiedades);
                                    rvPropiedades.setAdapter(propiedadAdapter);
                                }
                                if(miCliente.mascotas != null){
                                    mascotaAdapter = new MascotaAdapter(ClienteActivity.this, miCliente.mascotas);
                                    rvMascotas.setAdapter(mascotaAdapter);
                                }
                                if(miCliente.fotos != null){
                                    for(int i = 0; i<miCliente.fotos.size(); i++){
                                        try {
                                            File miFile = new File(ExternalDirectory, miCliente.fotos.get(i).name);
                                            Uri path = Uri.fromFile(miFile);
                                            miCliente.fotos.get(i).bitmap = MediaStore.Images.Media.getBitmap(
                                                    ClienteActivity.this.getContentResolver(),
                                                    path);
                                            imageAdapter.listFoto.add(miCliente.fotos.get(i));
                                        } catch (IOException e) {
                                            Log.d(TAG, "NotFound(): " + e.getMessage());
                                        }
                                    }
                                    imageAdapter.notifyDataSetChanged();
                                }

                                btnNewPropiedad.setVisibility(View.VISIBLE);
                                btnNewMascota.setVisibility(View.VISIBLE);
                                btnNewFoto.setVisibility(View.VISIBLE);
                                toolbar.setTitle("Modificación");
                            }
                        }
                    });
                }
            };
            th.start();
        }catch (Exception e){
            Log.d("TAGCLIENTE", "BuscarDatos" + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btnPositive:
                if (tipoAccion.equals("MESSAGE")) {
                    btnNegative.setVisibility(View.VISIBLE);
                    viewSeparator.setVisibility(View.VISIBLE);
                    lblTitle.setVisibility(View.VISIBLE);
                    btsDialog.dismiss();
                }
                break;
            case R.id.btnNegative:
                btsDialog.dismiss();
                break;
            case R.id.btnObtenerDireccion:
                //SQLite.gpsTracker = new GPSTracker(v.getContext());
                ObtenerCoordenadas(true);
                break;
            case R.id.lblInfoPersonal:
                Utils.EfectoLayout(lyInfoPersonal, lblInfoPersonal);
                break;
            case R.id.lblInfoContacto:
                Utils.EfectoLayout(lyInfoContacto, lblInfoContacto);
                break;
            case R.id.lblInfoAdicional:
                Utils.EfectoLayout(lyInfoAdicional, lblInfoAdicional);
                break;
            case R.id.lblFotos:
                Utils.EfectoLayout(lyFotos, lblFotos);
                break;
            case R.id.lblMascotas:
                Utils.EfectoLayout(lyMascotas, lblMascotas);
                break;
            case R.id.lblPropiedades:
                Utils.EfectoLayout(lyPropiedades, lblPropiedades);
                break;
            case R.id.btnNewPropiedad:
                i = new Intent(ClienteActivity.this, PropiedadActivity.class);
                i.putExtra("idpropietario", miCliente.idcliente);
                startActivityForResult(i, REQUEST_NEW_PROPIEDAD);
                break;
            case R.id.btnNewMascota:
                i = new Intent(ClienteActivity.this, MascotaActivity.class);
                i.putExtra("idpropietario", miCliente.idcliente);
                startActivityForResult(i, REQUEST_NEW_MASCOTA);
                break;
            case R.id.btnNewFoto:
                if(imageAdapter.listFoto.size() == SQLite.configuracion.maxfotoganadero)
                    Utils.showMessageShort(ClienteActivity.this, Constants.MSG_MAX_IMAGES + "(");
                else
                    ElegirOpcionFoto();
                break;
        }
    }

    void init(){
        cbTipoDocumento = findViewById(R.id.spTipoDocumento);
        txtNIP = findViewById(R.id.txtNIP);
        txtRazonSocial = findViewById(R.id.txtRazonSocial);
        txtNombreComercial = findViewById(R.id.txtNombreComercial);
        btnObtenerDireccion = findViewById(R.id.btnObtenerDireccion);
        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtFono1 = findViewById(R.id.txtfono1);
        txtFono2 = findViewById(R.id.txtfono2);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtObservacion = findViewById(R.id.txtObservacion);
        lblInfoPersonal = findViewById(R.id.lblInfoPersonal);
        lblInfoContacto = findViewById(R.id.lblInfoContacto);
        lblInfoAdicional = findViewById(R.id.lblInfoAdicional);
        lyInfoPersonal = findViewById(R.id.lyInfoPersonal);
        lyInfoContacto = findViewById(R.id.lyInfoContacto);
        lyInfoAdicional = findViewById(R.id.lyInfoAdicional);
        cbProvincia = findViewById(R.id.cbProvincia);
        cbCanton = findViewById(R.id.cbCanton);
        cbParroquia = findViewById(R.id.cbParroquia);
        lblInfo = findViewById(R.id.lblInfo);
        cvInfo = findViewById(R.id.cvInfo);
        cvInfo.setVisibility(View.GONE);
        rvPropiedades = findViewById(R.id.rvPropiedades);
        rvMascotas = findViewById(R.id.rvMascotas);
        rvFotos = findViewById(R.id.rvFotos);
        btnNewPropiedad = findViewById(R.id.btnNewPropiedad);
        btnNewMascota = findViewById(R.id.btnNewMascota);
        btnNewFoto = findViewById(R.id.btnNewFoto);
        lblPropiedades = findViewById(R.id.lblPropiedades);
        lblMascotas = findViewById(R.id.lblMascotas);
        lyPropiedades = findViewById(R.id.lyPropiedades);
        lyMascotas = findViewById(R.id.lyMascotas);
        lblFotos = findViewById(R.id.lblFotos);
        lyFotos = findViewById(R.id.lyFotos);
        cvFoto = findViewById(R.id.cvFoto);
        cvMascota = findViewById(R.id.cvMascota);
        LlenarTipoNIP();
        txtNIP.setOnFocusChangeListener(this);
        btnObtenerDireccion.setOnClickListener(this::onClick);
        lblInfoPersonal.setOnClickListener(this::onClick);
        lblInfoContacto.setOnClickListener(this::onClick);
        lblInfoAdicional.setOnClickListener(this::onClick);
        lblPropiedades.setOnClickListener(this::onClick);
        lblMascotas.setOnClickListener(this::onClick);
        lblFotos.setOnClickListener(this::onClick);
        btnNewPropiedad.setOnClickListener(this::onClick);
        btnNewMascota.setOnClickListener(this::onClick);
        btnNewFoto.setOnClickListener(this::onClick);

        btnNewPropiedad.setVisibility(View.GONE);
        btnNewMascota.setVisibility(View.GONE);
        btnNewFoto.setVisibility(View.GONE);

        cvFoto.setVisibility(View.GONE);
        cvMascota.setVisibility(View.GONE);

        imageAdapter = new ImageAdapter(this, listFoto);
        rvFotos.setAdapter(imageAdapter);
        ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;


        if(SQLite.gpsTracker==null)
            SQLite.gpsTracker = new GPSTracker(this);
        if (!SQLite.gpsTracker.checkGPSEnabled())
            SQLite.gpsTracker.showSettingsAlert(ClienteActivity.this);

        ObtenerCoordenadas(false);

        LlenarComboProvincias(0);
        LlenarComboCantones(0,-1);
        LlenarComboParroquias(0,-1);

        cbProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(cbProvincia.getAdapter()!=null) {
                    Provincia provincia = ((Provincia)cbProvincia.getItemAtPosition(position));
                    if(provincia.idprovincia!=provActual.idprovincia)
                        band=false;
                    if(!band)
                        LlenarComboCantones(provincia.idprovincia,-1);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cbCanton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(cbCanton.getAdapter()!=null) {
                    Canton canton = ((Canton)cbCanton.getItemAtPosition(position));
                    if(!band)
                        LlenarComboParroquias(canton.idcanton,-1);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void ElegirOpcionFoto() {
        final CharSequence[] opciones = {"Desde cámara", "Desde galería"};
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Elija una opción");
            builder.setItems(opciones, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File miFileC = new File(getExternalMediaDirs()[0], Constants.FOLDER_FILES);
                    boolean exists = miFileC.exists();
                    if (!exists)
                        miFileC.mkdirs();

                    if (which == 0) { //DESDE LA CAMARA
                        openCamera();
                    } else if (which == 1) { //DESDE LA GALERIA
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        i.setType("image/*");
                        startActivityForResult(i.createChooser(i, "Seleccione"), REQUEST_SELECCIONA_FOTO);
                    } else {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        } catch (Exception e) {
            Log.d(TAG, "elegirOpcionFoto(): " + e.getMessage());
        }
    }

    private void openCamera() {
        try {
            boolean exists = false;
            File miFile = new File(getExternalMediaDirs()[0], Constants.FOLDER_FILES);
            exists = miFile.exists();
            if (!exists) {
                exists = miFile.mkdirs();
                Log.d(TAG, "NO EXISTE LA CARPETA: " + String.valueOf(exists) + " - " + miFile.canRead());
                //exists = true;
            }
            if (exists) {
                Long consecutivo = System.currentTimeMillis() / 1000;
                nameFoto = miCliente.nip + "_gana_" + Utils.getDateFormat("yyyyMMddHHmmss")+".jpg";
                //nameFoto = consecutivo.toString() + ".jpg";
                path = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES
                        + File.separator + nameFoto;
                fileImage = new File(path);
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(this,
                                BuildConfig.APPLICATION_ID + ".services.GenericFileProvider",
                                fileImage));
                startActivityForResult(i, REQUEST_NEW_FOTO);
            }
        } catch (Exception e) {
            Log.d(TAG, "openCamera(): " + e.getMessage());
        }
    }

    private void ObtenerCoordenadas(boolean alertar){
        try {
            if (SQLite.gpsTracker.checkGPSEnabled()) {
                SQLite.gpsTracker.updateGPSCoordinates();
                SQLite.gpsTracker.getLastKnownLocation();
                txtLatitud.setText(String.valueOf(SQLite.gpsTracker.getLatitude()));
                txtLongitud.setText(String.valueOf(SQLite.gpsTracker.getLongitude()));
            } else if(alertar)
                SQLite.gpsTracker.showSettingsAlert(ClienteActivity.this);
        }catch (Exception e){
            Log.d("TAG_CLIENTEACTIVITY", e.getMessage());
        }
    }

    void LlenarTipoNIP(){
        ArrayList<TipoIdentificacion> tipoIdentificaciones = new ArrayList<>();
        tipoIdentificaciones.add(new TipoIdentificacion("00", "SIN IDENTIFICACIÓN"));
        tipoIdentificaciones.add(new TipoIdentificacion("05", "CÉDULA"));
        tipoIdentificaciones.add(new TipoIdentificacion("04", "RUC"));
        tipoIdentificaciones.add(new TipoIdentificacion("06", "PASAPORTE"));
        tipoIdentificaciones.add(new TipoIdentificacion("07", "ID. EXTERIOR"));
        ArrayAdapter<TipoIdentificacion> adapter = new ArrayAdapter<TipoIdentificacion>(
                this, android.R.layout.simple_spinner_item, tipoIdentificaciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbTipoDocumento.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);
        menu.findItem(R.id.option_newdocument).setVisible(false);
        menu.findItem(R.id.option_reimprimir).setVisible(false);
        menu.findItem(R.id.option_listdocument).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.option_save:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(this).inflate(R.layout.layout_confirmation_dialog,
                        (ConstraintLayout) findViewById(R.id.lyDialogContainer));
                builder.setView(view);
                ((TextView)view.findViewById(R.id.lblTitle)).setText("Guardar cliente");
                ((TextView)view.findViewById(R.id.lblMessage)).setText("¿Está seguro que desea guardar los datos del cliente?");
                ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_save);
                ((Button)view.findViewById(R.id.btnCancel)).setText(getResources().getString(R.string.Cancel));
                ((Button)view.findViewById(R.id.btnConfirm)).setText(getResources().getString(R.string.Confirm));
                final AlertDialog alertDialog = builder.create();
                view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GuardarDatos();
                        alertDialog.dismiss();
                    }
                });

                view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { alertDialog.dismiss();}
                });

                if(alertDialog.getWindow()!=null)
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                alertDialog.show();
                break;
            case R.id.option_newclient:
                LimpiarDatos();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void GuardarDatos() {
        try {
            if(miCliente == null)
                miCliente = new Cliente();
            if(!ValidarDatos()) return;

            miCliente.tiponip = ((TipoIdentificacion)cbTipoDocumento.getSelectedItem()).getCodigo();
            miCliente.nip = txtNIP.getText().toString().trim();
            miCliente.razonsocial = txtRazonSocial.getText().toString().trim();
            miCliente.nombrecomercial = txtNombreComercial.getText().toString().trim();

            SQLite.gpsTracker.getLastKnownLocation();
            miCliente.lat = SQLite.gpsTracker.getLatitude();
            miCliente.lon = SQLite.gpsTracker.getLongitude();

            miCliente.direccion = txtDireccion.getText().toString().trim();
            miCliente.fono1 = txtFono1.getText().toString().trim();
            miCliente.fono2 = txtFono2.getText().toString().trim();
            miCliente.email = txtCorreo.getText().toString().trim();
            miCliente.observacion = txtObservacion.getText().toString().trim();
            miCliente.usuarioid = SQLite.usuario.IdUsuario;
            miCliente.actualizado = 1;
            miCliente.establecimientoid = SQLite.usuario.sucursal.IdEstablecimiento;
            miCliente.parroquiaid = ((Parroquia) cbParroquia.getSelectedItem()).idparroquia;
            if(miCliente.idcliente == 0) {
                miCliente.fecharegistro = Utils.getDateFormat("yyyy-MM-dd HH:mm:ss");
                miCliente.longdater = Utils.longDate(Utils.getDateFormat("yyyy-MM-dd"));
            }
            miCliente.fechamodificacion = Utils.getDateFormat("yyyy-MM-dd HH:mm:ss");
            miCliente.longdatem = Utils.longDate(Utils.getDateFormat("yyyy-MM-dd"));

            miCliente.fotos.clear();
            miCliente.fotos.addAll(imageAdapter.listFoto);

            if(miCliente.Save()) {
                Banner.make(rootView, this,
                        Banner.SUCCESS, Constants.MSG_DATOS_GUARDADOS.concat("\nSe habilitó la opción de «REGISTRO DE MASCOTAS»."),
                        Banner.BOTTOM, 3000).show();
                Integer idtemp = miCliente.idcliente;
                this.LimpiarDatos();
                this.BuscarDatos(idtemp,"");
                if(this.isReturn)
                    setResult(Activity.RESULT_OK,new Intent().putExtra("idcliente",miCliente.idcliente));
                //finish();
            }else
                Banner.make(rootView,this,Banner.ERROR,Constants.MSG_DATOS_NO_GUARDADOS, Banner.BOTTOM,3500).show();

        }catch (Exception e){
            Log.d("TAGCLIENTE", e.getMessage());
            Utils.showErrorDialog(this,"Error: ", e.getMessage());
        }
    }

    private boolean ValidarDatos() throws Exception{
        if(miCliente.idcliente==0
            && !SQLite.usuario.VerificaPermiso(this,Constants.REGISTRO_CLIENTE, "escritura")){
                Banner.make(rootView, this, Banner.ERROR,"No tiene permisos para registrar nuevos clientes.", Banner.BOTTOM, 3000).show();
                return false;
        }else if(miCliente.idcliente>0
            && !SQLite.usuario.VerificaPermiso(this,Constants.REGISTRO_CLIENTE, "modificacion")){
            Banner.make(rootView, this, Banner.ERROR,"No tiene permisos para modificar datos.", Banner.BOTTOM, 3000).show();
                return false;
        }
        if(((TipoIdentificacion)cbTipoDocumento.getSelectedItem()).getCodigo().equals("00")){
            Banner.make(rootView,this, Banner.ERROR,"Especifique el tipo de identificación.", Banner.BOTTOM, 3000).show();
            return false;
        }
        if(txtNIP.getText().toString().trim().equals("")){
            txtNIP.setError("Ingrese una identificación.");
            Banner.make(rootView,this, Banner.ERROR,"Ingrese una identificación.", Banner.BOTTOM, 3000).show();
            return false;
        }
        if(txtRazonSocial.getText().toString().trim().equals("")){
            txtRazonSocial.setError("Ingrese el nombre del cliente.");
            Banner.make(rootView, this, Banner.ERROR,"Ingrese el nombre del cliente.", Banner.BOTTOM, 3000).show();
            return false;
        }
        if(txtDireccion.getText().toString().trim().equals("")){
            txtRazonSocial.setError("Ingrese la dirección del cliente.");
            Banner.make(rootView, this, Banner.ERROR,"Ingrese la dirección del cliente.",Banner.BOTTOM, 3000).show();
            return false;
        }
        if(txtFono1.getText().toString().trim().equals("") && txtFono2.getText().toString().trim().equals("")){
            Banner.make(rootView,this,Banner.ERROR,"Especifique al menos un número de contacto.", Banner.BOTTOM, 3000).show();
            return false;
        }

        if(Double.parseDouble(txtLatitud.getText().toString()) == 0 && Double.parseDouble(txtLongitud.getText().toString()) == 0){
            Banner.make(rootView,this,Banner.ERROR,"Debe obtener las coordenadas. Verifique si está activado el GPS.", Banner.BOTTOM, 3000).show();
            return false;
        }
        return true;
    }

    private void LimpiarDatos(){
        try {
            miCliente = new Cliente();
            cbTipoDocumento.setSelection(0, true);
            txtNIP.setText("");
            txtNIP.setTag(0);
            txtRazonSocial.setText("");
            txtNombreComercial.setText("");
            txtLatitud.setText("");
            txtLongitud.setText("");
            txtDireccion.setText("");
            txtFono1.setText("");
            txtFono2.setText("");
            txtCorreo.setText("");
            txtDireccion.setText("");
            txtObservacion.setText("");
            propiedadAdapter.listPropiedad.clear();
            mascotaAdapter.listMascotas.clear();
            listFoto.clear();
            toolbar.setTitle("Nuevo Registro");
            cvInfo.setVisibility(View.GONE);
            cvFoto.setVisibility(View.GONE);
            cvMascota.setVisibility(View.GONE);
        }catch (Exception e){
            Log.d("TAGCLIENTE", "LimpiarDatos(): " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            Foto mifoto;
            switch (requestCode){
                case REQUEST_NEW_PROPIEDAD:
                    miCliente.propiedades.clear();
                    miCliente.propiedades = Propiedad.getByPropietario(miCliente.idcliente);
                    propiedadAdapter.listPropiedad.clear();
                    propiedadAdapter.listPropiedad.addAll(miCliente.propiedades);
                    propiedadAdapter.notifyDataSetChanged();
                    break;
                case REQUEST_NEW_MASCOTA:
                    miCliente.mascotas.clear();
                    miCliente.mascotas = Mascota.getByPropietario(miCliente.idcliente, false);
                    mascotaAdapter.listMascotas.clear();
                    mascotaAdapter.listMascotas.addAll(miCliente.mascotas);
                    mascotaAdapter.notifyDataSetChanged();
                    break;
                case REQUEST_SELECCIONA_FOTO:
                    mifoto = new Foto();
                    Uri miPath = data.getData();
                    mifoto.uriFoto = miPath;
                    String[] projection = { MediaStore.Images.Media.DATA };
                    Cursor cursor = managedQuery(miPath, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path1= cursor.getString(column_index);

                    try {
                        mifoto.bitmap = MediaStore.Images.Media.getBitmap(ClienteActivity.this.getContentResolver(),miPath);
                        Long consecutivo = System.currentTimeMillis()/1000;
                        String nombre = miCliente.nip + "_gana_" + Utils.getDateFormat("yyyyMMddHHmmss")+".jpg";
                        path = getExternalMediaDirs()[0]+File.separator+Constants.FOLDER_FILES
                                +File.separator+nombre;

                        Utils.insert_image(mifoto.bitmap, nombre, ExternalDirectory, getContentResolver());
                        mifoto.path = path;
                        mifoto.name = nombre;
                        mifoto.tipo = "G";
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    imageAdapter.listFoto.add(mifoto);
                    imageAdapter.notifyDataSetChanged();
                    //imgFoto1.setImageURI(miPath);
                    break;
                case REQUEST_NEW_FOTO:
                    MediaScannerConnection.scanFile(ClienteActivity.this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.d(TAG, path);
                                }
                            });
                    bitmap = BitmapFactory.decodeFile(path);
                    Utils.insert_image(bitmap, nameFoto, ExternalDirectory, getContentResolver());
                    mifoto = new Foto();
                    mifoto.bitmap = bitmap;
                    mifoto.path = path;
                    mifoto.name = nameFoto;
                    mifoto.tipo = "G";
                    imageAdapter.listFoto.add(mifoto);
                    imageAdapter.notifyDataSetChanged();
                    //imgFoto1.setImageBitmap(bitmap);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        String titulo=miCliente == null? "Nuevo registro" : "Modificación";
        toolbar.setTitle(titulo);
        toolbar.setTitleTextColor(Color.WHITE);
        //toolbar.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        //toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_confirmation_dialog,
                    (ConstraintLayout) findViewById(R.id.lyDialogContainer));
            builder.setView(view);
            ((TextView)view.findViewById(R.id.lblTitle)).setText("Cerrar");
            ((TextView)view.findViewById(R.id.lblMessage)).setText("¿Desea salir de la ventana de cliente?");
            ((ImageView)view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_check_white);
            ((Button)view.findViewById(R.id.btnCancel)).setText(getResources().getString(R.string.Cancel));
            ((Button)view.findViewById(R.id.btnConfirm)).setText(getResources().getString(R.string.Confirm));
            final AlertDialog alertDialog = builder.create();
            view.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { alertDialog.dismiss();}
            });

            if(alertDialog.getWindow()!=null)
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            alertDialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void crearBottonSheet() {
        if(btsDialog==null){
            View view = LayoutInflater.from(this).inflate(R.layout.bottonsheet_message,null);
            btnPositive = view.findViewById(R.id.btnPositive);
            btnNegative = view.findViewById(R.id.btnNegative);
            lblMessage = view.findViewById(R.id.lblMessage);
            lblTitle = view.findViewById(R.id.lblTitle);
            viewSeparator = view.findViewById(R.id.vSeparator);
            btnPositive.setOnClickListener(this::onClick);
            btnNegative.setOnClickListener(this::onClick);

            btsDialog = new BottomSheetDialog(this, R.style.AlertDialogTheme);
            btsDialog.setContentView(view);
        }
    }

    public void showError(String message){
        crearBottonSheet();
        lblMessage.setText(message);
        btnNegative.setVisibility(View.GONE);
        viewSeparator.setVisibility(View.GONE);
        lblTitle.setVisibility(View.GONE);
        tipoAccion = "MESSAGE";
        if(btsDialog.getWindow()!=null)
            btsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        btsDialog.show();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        try{
            switch (v.getId()){
                case R.id.txtNIP:
                    if(!hasFocus && !txtNIP.getText().toString().trim().equals(""))
                        BuscarDatos(0,txtNIP.getText().toString().trim());
                    break;
            }
        }catch (Exception e){
            Log.d("TAGCLIENTE", "onFocusChange(): " + e.getMessage());
        }
    }

    private void LlenarComboProvincias(Integer idprovincia){
        try{
            provincias = Provincia.getList();
            ArrayAdapter<Provincia> adapterProvincia = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provincias);
            adapterProvincia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbProvincia.setAdapter(adapterProvincia);
            int position = 0;
            for(int i=0; i<provincias.size(); i++){
                if(provincias.get(i).idprovincia == idprovincia){
                    position = i;
                    break;
                }
            }
            if(idprovincia>=0)
                cbProvincia.setSelection(position,true);
        }catch (Exception e){
            Log.d("TAG_CLIENTEACT", "LlenarComboProvincias(): " +e.getMessage());
        }
    }

    private void LlenarComboCantones(Integer idprovincia, Integer idcanton){
        try{
            cantones.clear();
            cantones = Canton.getList(idprovincia);
            ArrayAdapter<Canton> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cantones);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbCanton.setAdapter(adapter);
            int position = 0;
            for(int i=0; i<cantones.size(); i++){
                if(cantones.get(i).idcanton.equals(idcanton)){
                    position = i;
                    break;
                }
            }
            if(idcanton>=0)
                cbCanton.setSelection(position, true);
        }catch (Exception e){
            Log.d("TAG_CLIENTEACT", "LlenarComboCantones(): " +e.getMessage());
        }
    }

    private void LlenarComboParroquias(Integer idcanton, Integer idparroquia){
        try{
            parroquias.clear();
            parroquias = Parroquia.getList(idcanton);
            ArrayAdapter<Parroquia> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, parroquias);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbParroquia.setAdapter(adapter);
            int position = 0;
            for(int i=0; i<parroquias.size(); i++){
                if(parroquias.get(i).idparroquia.equals(idparroquia)){
                    position = i;
                    break;
                }
            }
            if(idparroquia>=0) {
                cbParroquia.setSelection(position, true);
                //band = false;
            }
        }catch (Exception e){
            Log.d("TAG_CLIENTEACT", "LlenarComboProvincias(): " +e.getMessage());
        }
    }
}
