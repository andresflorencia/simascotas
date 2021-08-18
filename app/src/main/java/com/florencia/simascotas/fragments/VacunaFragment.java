package com.florencia.simascotas.fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.florencia.simascotas.R;
import com.florencia.simascotas.adapters.MedicamentoAdapter;
import com.florencia.simascotas.models.Consulta;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.models.Medicamento;
import com.florencia.simascotas.models.MedicamentoMascota;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class VacunaFragment extends Fragment implements View.OnClickListener{
    public static String TAG = "TAGVACUNA_FRAGMENT";
    View view;
    int _idmascota;
    String _tipo = "V";
    RecyclerView rvMedicamentos;
    MedicamentoAdapter medicamentoAdapter;
    FloatingActionButton btnNuevo;
    DatePickerDialog dtpDialog;
    Calendar calendar;
    ImageView imgFondo;

    public VacunaFragment() {}
    public VacunaFragment(int idmascota, String tipo) {
        this._idmascota = idmascota; this._tipo = tipo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_vacuna, container, false);
        init();
        return view;
    }
    private void init(){
        rvMedicamentos = view.findViewById(R.id.rvVacunas);
        btnNuevo = view.findViewById(R.id.btnNuevo);
        imgFondo = view.findViewById(R.id.imgFondo);
        btnNuevo.setOnClickListener(this::onClick);
        CargarDatos();
    }

    private void CargarDatos(){
        List<MedicamentoMascota> datos = MedicamentoMascota.getByMascota(_idmascota, _tipo);
        if(datos != null && datos.size()>0) {
            medicamentoAdapter = new MedicamentoAdapter(getActivity(), datos);
            rvMedicamentos.setAdapter(medicamentoAdapter);
            imgFondo.setVisibility(View.GONE);
            rvMedicamentos.setVisibility(View.VISIBLE);
        }else{
            rvMedicamentos.setVisibility(View.GONE);
            imgFondo.setVisibility(View.VISIBLE);
        }
    }

    private void ModalNewMedicamento(){
        try{
            EditText txtObservacion;
            Button btnFechaAplica;
            TextView lblProxima;
            Spinner spMedicamento;

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_add_medicamento,
                    (ConstraintLayout) getActivity().findViewById(R.id.lyDialogContainer));
            ((TextView)view.findViewById(R.id.lblTitle)).setText(_tipo.equals("V")?"Nueva vacuna":"Nuevo medicamento");
            builder.setView(view);
            txtObservacion = view.findViewById(R.id.txtObservacion);
            btnFechaAplica = view.findViewById(R.id.btnFechaAplica);
            lblProxima = view.findViewById(R.id.lblProxima);
            spMedicamento = view.findViewById(R.id.spMedicamento);

            btnFechaAplica.setText(Utils.getDateFormat("yyyy-MM-dd"));
            btnFechaAplica.setOnClickListener(v -> {
                Medicamento med = (Medicamento) spMedicamento.getSelectedItem();
                int intervalo = 0;
                if(med.frecuencia.equals("ANUAL"))
                    intervalo = Calendar.YEAR;
                else if(med.frecuencia.equals("MENSUAL"))
                    intervalo = Calendar.MONTH;
                else if (med.frecuencia.equals("DIARIA"))
                    intervalo = Calendar.DAY_OF_YEAR;
                showDatePickerDialog((Button) v, lblProxima, intervalo, med.numfrecuencia);
            });
            LlenarCombo(spMedicamento, 0, btnFechaAplica.getText().toString(), lblProxima);

            final AlertDialog alertDialog = builder.create();
            view.findViewById(R.id.btnConfirm).setOnClickListener( v -> {
                Medicamento medic = (Medicamento) spMedicamento.getSelectedItem();
                if(medic.idmedicamento == 0){
                    Utils.showMessage(getContext(), "Debe especifificar "+ (_tipo.equals("V")? "la vacuna":"el medicamento") + " a aplicar.");
                }else {
                    MedicamentoMascota newMedic = new MedicamentoMascota();
                    newMedic.mascotaid = _idmascota;
                    newMedic.medicamento.idmedicamento = medic.idmedicamento;
                    newMedic.tipo = _tipo;
                    newMedic.codigo = MedicamentoMascota.GeneraCodigo(_tipo.equals("V")?"VAC":"MED");
                    newMedic.fechacelular = Utils.getDateFormat("yyyy-MM-dd HH:mm:ss");
                    newMedic.fechaaplicacion = btnFechaAplica.getText().toString();
                    newMedic.proximaaplicacion = lblProxima.getTag().toString();
                    newMedic.longdate = Utils.longDateF(newMedic.fechacelular, "yyyy-MM-dd HH:mm:ss");
                    newMedic.usuarioid = SQLite.usuario.IdUsuario;
                    newMedic.nombreusuario = SQLite.usuario.RazonSocial;
                    newMedic.observacion = txtObservacion.getText().toString();
                    SQLite.gpsTracker.getLastKnownLocation();
                    newMedic.lat = SQLite.gpsTracker.getLatitude();
                    newMedic.lon = SQLite.gpsTracker.getLongitude();
                    newMedic.actualizado = 1;
                    if(newMedic.lat == null)
                        newMedic.lat = 0d;
                    if(newMedic.lon == null)
                        newMedic.lon = 0d;

                    if(newMedic.Save()) {
                        ContentValues values = new ContentValues();
                        values.put("actualizado", 1);
                        Mascota.Update(_idmascota, values);
                        alertDialog.dismiss();
                        Utils.showMessage(getContext(), Constants.MSG_DATOS_GUARDADOS);
                        CargarDatos();
                    }else
                        Utils.showMessage(getContext(), Constants.MSG_DATOS_NO_GUARDADOS);
                }
            });

            view.findViewById(R.id.btnCancel).setOnClickListener(v -> alertDialog.dismiss());

            if(alertDialog.getWindow()!=null)
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            alertDialog.setCancelable(false);
            alertDialog.show();
        }catch (Exception e){
            Log.d(TAG,"ModalNewMedicamento(): " + e.getMessage());
        }
    }

    private void LlenarCombo(Spinner spinner, int value, String fecha, TextView lblProxima){
        try{
            List<Medicamento> medicamentos = Medicamento.getList(_tipo);
            medicamentos.add(0, new Medicamento("", "Escoja una opción"));
            ArrayAdapter<Medicamento> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, medicamentos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            if(value>0) {
                int position = 0;
                for (int i = 0; i < medicamentos.size(); i++) {
                    if (medicamentos.get(i).idmedicamento == value) {
                        position = i;
                        break;
                    }
                }
                spinner.setSelection(position, true);
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Medicamento med = (Medicamento) parent.getSelectedItem();
                    int intervalo = 0;
                    if(med.frecuencia.equals("ANUAL"))
                        intervalo = Calendar.YEAR;
                    else if(med.frecuencia.equals("MENSUAL"))
                        intervalo = Calendar.MONTH;
                    else if (med.frecuencia.equals("DIARIA"))
                        intervalo = Calendar.DAY_OF_YEAR;
                    String newFecha = Utils.CambiarFecha(fecha, intervalo, med.numfrecuencia);
                    lblProxima.setText("Próxima aplicación:\n" + newFecha);
                    lblProxima.setTag(newFecha);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }catch (Exception e){
            Log.d(TAG, "LlenarCombo(): " +e.getMessage());
        }
    }

    public void showDatePickerDialog(Button v, TextView lblFecha, int intervalo, int frecuencia) {
        Locale l = new Locale("ES-es");
        calendar = Calendar.getInstance(l);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String[] fecha = v.getText().toString().split("-");
        day = Integer.valueOf(fecha[2]);
        month = Integer.valueOf(fecha[1]) - 1;
        year = Integer.valueOf(fecha[0]);
        dtpDialog = new DatePickerDialog(v.getContext(), (view, y, m, d) -> {
                String dia = (d >= 0 && d < 10 ? "0" + (d) : String.valueOf(d));
                String mes = (m >= 0 && m < 9 ? "0" + (m + 1) : String.valueOf(m + 1));

                String newFecha = y + "-" + mes + "-" + dia;
                v.setText(newFecha);
                String proxima = Utils.CambiarFecha(newFecha, intervalo, frecuencia);
                lblFecha.setText("Próxima aplicación\n" + proxima);
                lblFecha.setTag(proxima);

        }, year, month, day);
        dtpDialog.show();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnNuevo:
                ModalNewMedicamento();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
