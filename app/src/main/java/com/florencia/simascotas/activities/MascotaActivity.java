package com.florencia.simascotas.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.florencia.simascotas.BuildConfig;
import com.florencia.simascotas.R;
import com.florencia.simascotas.adapters.ImageAdapter;
import com.florencia.simascotas.fragments.InfoDialogFragment;
import com.florencia.simascotas.models.Catalogo;
import com.florencia.simascotas.models.Cliente;
import com.florencia.simascotas.models.Foto;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;
import com.shasin.notificationbanner.Banner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MascotaActivity extends AppCompatActivity implements View.OnClickListener {

    TextView txtNombreMascota, txtPropietario, txtColor1, txtColor2, txtObservacion, txtPeso,
            lblInfoPrincipal, lblFotos;
    LinearLayout lyInfoPrincipal, lyFotos;
    Spinner spSexo, spEspecie, spRaza;
    Button btnFechaNac;
    ImageButton btnNewFoto;
    RecyclerView rvFotos;
    Mascota miMascota;
    Catalogo miespecieA = new Catalogo();
    Catalogo mirazaA = new Catalogo();

    private static String TAG = "TAGMASCOTA_ACTIVITY";
    private static final int REQUEST_NEW_FOTO = 20;
    private static final int REQUEST_SELECCIONA_FOTO = 30;
    private static final int REQUEST_CLIENTE = 2;
    Toolbar toolbar;


    String path, nameFoto;
    File fileImage;
    Bitmap bitmap;
    ImageAdapter imageAdapter;

    DatePickerDialog dtpDialog;
    Calendar calendar;
    Boolean band = false;
    List<Catalogo> lstEspecie, lstRaza;
    Cliente propietario = new Cliente();
    List<Foto> listFoto = new ArrayList<>();
    String ExternalDirectory = "";
    Integer idmascota = 0;
    ProgressDialog pgCargando;
    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mascota);

        toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        init();

        if (getIntent().getExtras() != null) {
            idmascota = getIntent().getExtras().getInt("idmascota", 0);
            Integer idpropietario = getIntent().getExtras().getInt("idpropietario", 0);
            propietario = Cliente.get(idpropietario, false);
            txtPropietario.setText(propietario.razonsocial);
            txtPropietario.setEnabled(false);
            if (idmascota > 0) {
                BuscarDatos(idmascota);
            }
            //this.isReturn = getIntent().getExtras().getBoolean("nuevo_cliente",false);
        }
    }

    private void init() {
        txtNombreMascota = findViewById(R.id.txtNombreMascota);
        txtPropietario = findViewById(R.id.txtPropietario);
        txtColor1 = findViewById(R.id.txtColor1);
        txtColor2 = findViewById(R.id.txtColor2);
        txtObservacion = findViewById(R.id.txtObservacion);
        txtPeso = findViewById(R.id.txtPeso);
        spSexo = findViewById(R.id.spSexo);
        spEspecie = findViewById(R.id.spEspecie);
        spRaza = findViewById(R.id.spRaza);
        btnFechaNac = findViewById(R.id.btnFechaNac);
        btnNewFoto = findViewById(R.id.btnNewFoto);
        rvFotos = findViewById(R.id.rvFotos);
        lblInfoPrincipal = findViewById(R.id.lblInfoPrincipal);
        lblFotos = findViewById(R.id.lblFotos);
        lyInfoPrincipal = findViewById(R.id.lyInfoPrincipal);
        lyFotos = findViewById(R.id.lyFotos);
        rootView = findViewById(android.R.id.content);

        btnNewFoto.setOnClickListener(this::onClick);
        btnFechaNac.setOnClickListener(this::onClick);
        lblInfoPrincipal.setOnClickListener(this::onClick);
        lblFotos.setOnClickListener(this::onClick);

        lstEspecie = Catalogo.getCatalogo("ESPECIE");
        lstRaza = new ArrayList<>();

        btnFechaNac.setText(Utils.getDateFormat("yyyy-MM-dd"));
        propietario = new Cliente();

        imageAdapter = new ImageAdapter(this, listFoto);
        rvFotos.setAdapter(imageAdapter);

        ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;

        LlenarComboEspecies("");
        LlenarComboRazas("", "");
        pgCargando = new ProgressDialog(this);

        spEspecie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spEspecie.getAdapter() != null) {
                    Catalogo especie = ((Catalogo) spEspecie.getItemAtPosition(position));
                    if (!band)
                        LlenarComboRazas(especie.codigocatalogo, "");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txtPeso.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    Double p = Double.parseDouble(s.toString());
                } catch (Exception e) {
                    Banner.make(rootView, MascotaActivity.this, Banner.ERROR, "Ingrese un valor válido", Banner.BOTTOM).show();
                }
            }
        });

        txtPropietario.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                Intent i = new Intent(v.getContext(), ClienteBusquedaActivity.class);
                i.putExtra("busqueda", txtPropietario.getText().toString().trim());
                startActivityForResult(i, REQUEST_CLIENTE);
                return true;
            }
            return false;
        });

        txtPropietario.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= txtPropietario.getRight() - txtPropietario.getTotalPaddingRight()) {
                    txtPropietario.setText("");
                    propietario = new Cliente();
                    return true;
                } else if (event.getRawX() <= txtPropietario.getTotalPaddingLeft() && propietario.idcliente > 0) {
                    MostrarInfoDialog(propietario.idcliente);
                }
            }
            return false;
        });

        txtPropietario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (s.length() > 0 && idmascota == 0)
                        txtPropietario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user, 0, R.drawable.ic_close, 0);
                    else
                        txtPropietario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user, 0, 0, 0);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    private void MostrarInfoDialog(Integer idcliente) {
        DialogFragment dialogFragment = new InfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("idcliente", idcliente);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void showDatePickerDialog(View v) {
        Locale l = new Locale("ES-es");
        calendar = Calendar.getInstance(l);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String[] fecha = btnFechaNac.getText().toString().split("-");
        day = Integer.valueOf(fecha[2]);
        month = Integer.valueOf(fecha[1]) - 1;
        year = Integer.valueOf(fecha[0]);
        dtpDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String dia = (day >= 0 && day < 10 ? "0" + (day) : String.valueOf(day));
                String mes = (month >= 0 && month < 9 ? "0" + (month + 1) : String.valueOf(month + 1));

                String mitextoU = year + "-" + mes + "-" + dia;
                btnFechaNac.setText(mitextoU);
            }
        }, year, month, day);
        dtpDialog.show();
    }

    private void LlenarComboEspecies(String idespecie) {
        try {
            lstEspecie = Catalogo.getCatalogo("ESPECIE");
            ArrayAdapter<Catalogo> adapterEspecie = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    lstEspecie);
            adapterEspecie.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spEspecie.setAdapter(adapterEspecie);
            int position = 0;
            for (int i = 0; i < lstEspecie.size(); i++) {
                if (lstEspecie.get(i).codigocatalogo.equals(idespecie)) {
                    position = i;
                    break;
                }
            }
            if (!idespecie.equals(""))
                spEspecie.setSelection(position, true);
        } catch (Exception e) {
            Log.d(TAG, "LlenarComboProvincias(): " + e.getMessage());
        }
    }

    private void LlenarComboRazas(String idespecie, String idraza) {
        try {
            lstRaza.clear();
            lstRaza = Catalogo.getCatalogo(idespecie);
            ArrayAdapter<Catalogo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    lstRaza);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spRaza.setAdapter(adapter);
            int position = 0;
            for (int i = 0; i < lstRaza.size(); i++) {
                Log.d(TAG, idespecie + " - " + idraza);
                if (lstRaza.get(i).codigocatalogo.equalsIgnoreCase(idraza)) {
                    position = i;
                    break;
                }
            }
            if (!idraza.equals(""))
                spRaza.setSelection(position, true);
        } catch (Exception e) {
            Log.d(TAG, "LlenarComboCantones(): " + e.getMessage());
        }
    }

    private void BuscarDatos(Integer idmascota) {
        Thread th = new Thread() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    miMascota = Mascota.getById(idmascota);
                    if (miMascota != null) {
                        band = true;
                        miespecieA = Catalogo.getByPadre(miMascota.especie.codigocatalogo, "ESPECIE");
                        Catalogo miraza = Catalogo.getByPadre(miMascota.raza.codigocatalogo, miespecieA.codigocatalogo);

                        for (Catalogo miE : lstEspecie) {
                            if (miespecieA.codigocatalogo.equals(miE.codigocatalogo)) {
                                spEspecie.setSelection(lstEspecie.indexOf(miE));
                                break;
                            }
                        }
                        LlenarComboRazas(miespecieA.codigocatalogo, miraza.codigocatalogo);

                        propietario = Cliente.get(miMascota.duenoid, false);
                        if (propietario == null)
                            propietario = Cliente.get(miMascota.nipdueno, false);
                        txtNombreMascota.setText(miMascota.nombre);
                        txtPropietario.setText(propietario.razonsocial);
                        txtPropietario.setEnabled(false);
                        btnFechaNac.setText(miMascota.fechanacimiento);
                        txtPeso.setText(Utils.RoundDecimal(miMascota.peso, 2).toString());
                        txtColor1.setText(miMascota.color1);
                        txtColor2.setText(miMascota.color2);
                        txtObservacion.setText(miMascota.observacion);
                        if (miMascota.sexo.equalsIgnoreCase("M"))
                            spSexo.setSelection(1, true);
                        else if (miMascota.sexo.equalsIgnoreCase("H"))
                            spSexo.setSelection(2, true);

                        if (miMascota.fotos != null) {
                            for (int i = 0; i < miMascota.fotos.size(); i++) {
                                try {
                                    File miFile = new File(ExternalDirectory, miMascota.fotos.get(i).name);
                                    Uri path = Uri.fromFile(miFile);
                                    miMascota.fotos.get(i).bitmap = MediaStore.Images.Media.getBitmap(
                                            MascotaActivity.this.getContentResolver(),
                                            path);
                                    imageAdapter.listFoto.add(miMascota.fotos.get(i));
                                } catch (IOException e) {
                                    Log.d(TAG, "NotFound(): " + e.getMessage());
                                }
                            }
                            imageAdapter.notifyDataSetChanged();
                        }

                        toolbar.setTitle("Modificación: ".concat(miMascota.codigomascota));
                    }
                });
            }
        };
        th.start();
    }

    private void LimpiarDatos() {
        try {
            toolbar.setTitle("Nueva Propiedad");
            idmascota = 0;
            miMascota = new Mascota();
        } catch (Exception e) {
            Log.d(TAG, "LimpiarDatos(): " + e.getMessage());
        }
    }

    private boolean ValidarDatos() {
        try {
            if (propietario == null || propietario.idcliente == 0) {
                Banner.make(rootView, this, Banner.ERROR, "Debe especificar el propietario de la mascota", Banner.BOTTOM, 2500).show();
                return false;
            }

            if (txtNombreMascota.getText().toString().trim().equals("")) {
                Banner.make(rootView, this, Banner.ERROR, "Debe especificar el nombre de la mascota", Banner.BOTTOM, 2500).show();
                return false;
            }

            if (spSexo.getSelectedItemPosition() == 0) {
                Banner.make(rootView, this, Banner.ERROR, "Debe especificar el sexo de la mascota", Banner.BOTTOM, 2500).show();
                return false;
            }

            if (txtColor1.getText().toString().trim().equals("")
                    && txtColor2.getText().toString().trim().equals("")) {
                Banner.make(rootView, this, Banner.ERROR, "Debe especificar al menos un color de la mascota", Banner.BOTTOM, 2500).show();
                return false;
            }

            if (btnFechaNac.getText().toString().trim().equals("")) {
                Banner.make(rootView, this, Banner.ERROR, "Debe especificar la fecha de nacimiento de la mascota", Banner.BOTTOM, 2500).show();
                return false;
            }
            if (txtPeso.getText().toString().trim().equals("")) {
                Banner.make(rootView, this, Banner.ERROR, "Debe especificar el peso (kg) aproximado de la mascota", Banner.BOTTOM, 2500).show();
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, "ValidarDatos(): " + e.getMessage());
            return false;
        }
        return true;
    }

    private void GuardarDatos() {
        try {
            if (!ValidarDatos()) return;

            if (miMascota == null)
                miMascota = new Mascota();

            miMascota.nombre = txtNombreMascota.getText().toString().trim();
            miMascota.duenoid = propietario.idcliente;
            miMascota.nipdueno = propietario.nip;
            miMascota.fechanacimiento = btnFechaNac.getText().toString().trim();
            miMascota.longdaten = Utils.longDate(miMascota.fechanacimiento);
            miMascota.peso = Double.parseDouble(txtPeso.getText().toString().trim());
            SQLite.gpsTracker.getLastKnownLocation();
            miMascota.lat = SQLite.gpsTracker.getLatitude();//txtLatitud.setText(miPropiedad.lat);
            miMascota.lon = SQLite.gpsTracker.getLongitude();//txtLongitud.setText(miPropiedad.lon);
            miMascota.observacion = txtObservacion.getText().toString().trim();
            miMascota.fechacelular = Utils.getDateFormat("yyyy-MM-dd HH:mm:ss");
            miMascota.actualizado = 1;
            miMascota.especie.codigocatalogo = ((Catalogo) spEspecie.getSelectedItem()).codigocatalogo;
            miMascota.raza.codigocatalogo = ((Catalogo) spRaza.getSelectedItem()).codigocatalogo;
            miMascota.color1 = txtColor1.getText().toString().trim();
            miMascota.color2 = txtColor2.getText().toString().trim();
            if (spSexo.getSelectedItemPosition() == 1)
                miMascota.sexo = "M";
            else if (spSexo.getSelectedItemPosition() == 2)
                miMascota.sexo = "H";
            miMascota.usuarioid = SQLite.usuario.IdUsuario;

            miMascota.fotos.clear();
            miMascota.fotos.addAll(imageAdapter.listFoto);
            if (miMascota.idmascota == 0)
                miMascota.codigomascota = Mascota.GeneraCodigo();

            if (miMascota.Save()) {
                ContentValues val = new ContentValues();
                val.put("actualizado", 1);
                Cliente.Update(miMascota.duenoid, val);
                Utils.showSuccessDialog(this, "Éxito", Constants.MSG_DATOS_GUARDADOS, true, true);
            } else
                Utils.showErrorDialog(this, "Error", Constants.MSG_DATOS_NO_GUARDADOS);
        } catch (Exception e) {
            Log.d(TAG, "GuardarDatos(): " + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lblInfoPrincipal:
                Utils.EfectoLayout(lyInfoPrincipal, lblInfoPrincipal);
                break;
            case R.id.lblFotos:
                Utils.EfectoLayout(lyFotos, lblFotos);
                break;
            case R.id.btnFechaNac:
                showDatePickerDialog(v);
                break;
            case R.id.btnNewFoto:
                ElegirOpcionFoto();
                break;
        }
    }

    private void ElegirOpcionFoto() {
        final CharSequence[] opciones = {"Desde cámara", "Desde galería"};
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Elija una opción");
            builder.setItems(opciones, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File miFile = new File(getExternalMediaDirs()[0], Constants.FOLDER_FILES);
                    boolean exists = miFile.exists();
                    if (!exists)
                        miFile.mkdirs();

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
                //nameFoto = consecutivo.toString() + ".jpg";
                nameFoto = propietario.nip + "_masco_" + Utils.getDateFormat("yyyyMMddHHmmss") + ".jpg";
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);
        menu.findItem(R.id.option_newdocument).setVisible(false);
        menu.findItem(R.id.option_reimprimir).setVisible(false);
        menu.findItem(R.id.option_listdocument).setVisible(false);
        menu.findItem(R.id.option_newclient).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_save:
                if (idmascota == 0 && !SQLite.usuario.VerificaPermiso(this, Constants.VISITA_GANADERO, "escritura")) {
                    Utils.showErrorDialog(this, "Error", "No tiene permisos para registrar mascotas. \nContacte a soporte.");
                    break;
                }
                if (idmascota > 0 && !SQLite.usuario.VerificaPermiso(this, Constants.VISITA_GANADERO, "modificacion")) {
                    Utils.showErrorDialog(this, "Error", "No tiene permisos para modificar datos de las mascotas. \nContacte a soporte.");
                    break;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(this).inflate(R.layout.layout_confirmation_dialog,
                        (ConstraintLayout) findViewById(R.id.lyDialogContainer));
                builder.setView(view);
                ((TextView) view.findViewById(R.id.lblTitle)).setText("Guardar mascota");
                ((TextView) view.findViewById(R.id.lblMessage)).setText("¿Está seguro que desea guardar esta mascota?");
                ((ImageView) view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_save);
                ((Button) view.findViewById(R.id.btnCancel)).setText("Cancelar");
                ((Button) view.findViewById(R.id.btnConfirm)).setText("Si");
                final AlertDialog alertDialog = builder.create();
                view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
                    GuardarDatos();
                    alertDialog.dismiss();
                });

                view.findViewById(R.id.btnCancel).setOnClickListener(v -> alertDialog.dismiss());

                if (alertDialog.getWindow() != null)
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                alertDialog.show();
                break;
            case R.id.option_newdocument:
                LimpiarDatos();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Foto mifoto;
            switch (requestCode) {
                case REQUEST_SELECCIONA_FOTO:

                    mifoto = new Foto();
                    Uri miPath = data.getData();
                    mifoto.uriFoto = miPath;
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(miPath, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path1 = cursor.getString(column_index);

                    try {
                        mifoto.bitmap = MediaStore.Images.Media.getBitmap(MascotaActivity.this.getContentResolver(), miPath);
                        //Long consecutivo = System.currentTimeMillis()/1000;
                        //String nombre = consecutivo.toString()+".jpg";
                        String nombre = propietario.nip + "_masco_" + Utils.getDateFormat("yyyyMMddHHmmss") + ".jpg";
                        path = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES
                                + File.separator + nombre;

                        Utils.insert_image(mifoto.bitmap, nombre, ExternalDirectory, getContentResolver());
                        mifoto.path = path;
                        mifoto.name = nombre;
                        mifoto.tipo = "M";
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
                    imageAdapter.listFoto.add(mifoto);
                    imageAdapter.notifyDataSetChanged();
                    //imgFoto1.setImageURI(miPath);
                    break;
                case REQUEST_NEW_FOTO:
                    MediaScannerConnection.scanFile(MascotaActivity.this, new String[]{path}, null,
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
                    mifoto.tipo = "M";
                    imageAdapter.listFoto.add(mifoto);
                    imageAdapter.notifyDataSetChanged();
                    //imgFoto1.setImageBitmap(bitmap);
                    break;
                case REQUEST_CLIENTE:
                    Integer idcliente = data.getExtras().getInt("idcliente", 0);
                    if (idcliente > 0) {
                        propietario = Cliente.get(idcliente, false);
                        txtPropietario.setText(propietario.razonsocial);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_confirmation_dialog,
                    (ConstraintLayout) findViewById(R.id.lyDialogContainer));
            builder.setView(view);
            ((TextView) view.findViewById(R.id.lblTitle)).setText("Cerrar");
            ((TextView) view.findViewById(R.id.lblMessage)).setText("¿Desea salir de la ventana de mascotas?");
            ((ImageView) view.findViewById(R.id.imgIcon)).setImageResource(R.drawable.ic_check_white);
            ((Button) view.findViewById(R.id.btnCancel)).setText("No");
            ((Button) view.findViewById(R.id.btnConfirm)).setText("Si");
            final AlertDialog alertDialog = builder.create();

            view.findViewById(R.id.btnConfirm).setOnClickListener(v -> finish());
            view.findViewById(R.id.btnCancel).setOnClickListener(v -> alertDialog.dismiss());

            if (alertDialog.getWindow() != null)
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            alertDialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String titulo = idmascota == 0 ? "Nueva mascota" : "Modificación";
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(titulo);
    }
}
