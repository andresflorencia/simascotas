package com.florencia.simascotas.fragments;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.florencia.simascotas.R;
import com.florencia.simascotas.adapters.ConsultaAdapter;
import com.florencia.simascotas.models.Consulta;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shasin.notificationbanner.Banner;

import java.util.List;

public class ConsultaFragment extends Fragment implements View.OnClickListener{
    public static String TAG = "TAGCONSULTA_FRAGMENT";
    View view, rootView;
    int _idmascota;
    ConsultaAdapter consultaAdapter;
    RecyclerView rvConsultas;
    FloatingActionButton btnNuevo;
    ImageView imgFondo;

    public ConsultaFragment() {}
    public ConsultaFragment(int idmascota) {
        this._idmascota = idmascota;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_consulta, container, false);
        init();
        return view;
    }

    private void init(){
        rootView = view.findViewById(android.R.id.content);
        rvConsultas = view.findViewById(R.id.rvConsultas);
        btnNuevo = view.findViewById(R.id.btnNuevo);
        imgFondo = view.findViewById(R.id.imgFondo);
        btnNuevo.setOnClickListener(this::onClick);
        CargarDatos();
    }

    private void CargarDatos(){
        List<Consulta> datos = Consulta.getByMascota(_idmascota);
        if(datos != null && datos.size()>0) {
            consultaAdapter = new ConsultaAdapter(getActivity(), datos);
            rvConsultas.setAdapter(consultaAdapter);
            imgFondo.setVisibility(View.GONE);
            rvConsultas.setVisibility(View.VISIBLE);
        }else{
            rvConsultas.setVisibility(View.GONE);
            imgFondo.setVisibility(View.VISIBLE);
        }
    }

    private void ModalNewConsulta(){
        try{
            EditText txtDiagnostico, txtReceta, txtPrescripcion;
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_add_consulta,
                    (ConstraintLayout) getActivity().findViewById(R.id.lyDialogContainer));
            builder.setView(view);
            txtDiagnostico = view.findViewById(R.id.txtDiagnostico);
            txtReceta = view.findViewById(R.id.txtReceta);
            txtPrescripcion = view.findViewById(R.id.txtPrescripcion);

            final AlertDialog alertDialog = builder.create();
            view.findViewById(R.id.btnConfirm).setOnClickListener( v -> {
                Consulta newConsulta = new Consulta();
                newConsulta.diagnostico = txtDiagnostico.getText().toString().trim();
                newConsulta.receta = txtReceta.getText().toString().trim();
                newConsulta.prescripcion = txtPrescripcion.getText().toString().trim();

                if(newConsulta.diagnostico.trim().equals("")
                        && newConsulta.receta.trim().equals("")
                        && newConsulta.prescripcion.trim().equals("")){
                    Utils.showMessage(getContext(), "Debe ingresar datos para registrar.");
                }else {
                    newConsulta.mascotaid = _idmascota;
                    newConsulta.codigoconsulta = Consulta.GeneraCodigo();
                    newConsulta.fechacelular = Utils.getDateFormat("yyyy-MM-dd HH:mm:ss");
                    newConsulta.fechaconsulta = Utils.getDateFormat("yyyy-MM-dd");
                    newConsulta.longdatec = Utils.longDateF(newConsulta.fechacelular, "yyyy-MM-dd HH:mm:ss");
                    newConsulta.usuarioid = SQLite.usuario.IdUsuario;
                    newConsulta.nombreusuario = SQLite.usuario.RazonSocial;
                    SQLite.gpsTracker.getLastKnownLocation();
                    newConsulta.lat = SQLite.gpsTracker.getLatitude();
                    newConsulta.lon = SQLite.gpsTracker.getLongitude();
                    newConsulta.actualizado = 1;
                    if(newConsulta.lat == null)
                        newConsulta.lat = 0d;
                    if(newConsulta.lon == null)
                        newConsulta.lon = 0d;

                    if(newConsulta.Save()) {
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
            Log.d(TAG,"ModalNewConsulta(): " + e.getMessage());
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnNuevo:
                ModalNewConsulta();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
