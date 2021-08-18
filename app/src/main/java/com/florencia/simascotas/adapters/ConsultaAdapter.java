package com.florencia.simascotas.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.simascotas.R;
import com.florencia.simascotas.models.Consulta;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;

import java.util.List;

public class ConsultaAdapter extends RecyclerView.Adapter<ConsultaAdapter.ConsultaViewHolder>{
    public static final String TAG = "TAGCONSULTA_ADAPTER";

    public List<Consulta> consultas;
    private Activity activity;
    View rootView;

    public ConsultaAdapter(Activity activity, List<Consulta> consultas) {
        this.consultas = consultas;
        this.activity = activity;
        rootView = activity.findViewById(android.R.id.content);
    }

    @NonNull
    @Override
    public ConsultaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConsultaViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_consulta, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConsultaViewHolder holder, int position) {
        holder.bindConsulta(consultas.get(position));
    }

    @Override
    public int getItemCount() {
        return consultas.size();
    }

    class ConsultaViewHolder extends RecyclerView.ViewHolder {
        TextView tvCodigo, tvFechaConsulta, tvDiagnostico, tvAtendio, tvEstado;

        ConsultaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            tvFechaConsulta = itemView.findViewById(R.id.tvFechaConsulta);
            tvDiagnostico = itemView.findViewById(R.id.tvDiagnostico);
            tvAtendio = itemView.findViewById(R.id.tvAtendido);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }

        void bindConsulta(final Consulta consulta) {
            tvCodigo.setText(consulta.codigoconsulta);
            tvFechaConsulta.setText("Fecha: " + consulta.fechaconsulta);
            tvDiagnostico.setText("Diag: " + consulta.diagnostico);
            tvAtendio.setText("Atendió: " + consulta.nombreusuario);

            tvEstado.setVisibility(View.GONE);
            if(consulta.codigosistema == 0 || consulta.actualizado == 1)
                tvEstado.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(v -> ModalConsulta(getAdapterPosition()));
        }

        private void ModalConsulta(int pos){
            try{
                Consulta miConsulta = consultas.get(pos);

                EditText txtDiagnostico, txtReceta, txtPrescripcion;
                AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(activity).inflate(R.layout.layout_add_consulta,
                        (ConstraintLayout) activity.findViewById(R.id.lyDialogContainer));
                builder.setView(view);
                txtDiagnostico = view.findViewById(R.id.txtDiagnostico);
                txtReceta = view.findViewById(R.id.txtReceta);
                txtPrescripcion = view.findViewById(R.id.txtPrescripcion);

                txtDiagnostico.setText(miConsulta.diagnostico);
                txtReceta.setText(miConsulta.receta);
                txtPrescripcion.setText(miConsulta.prescripcion);

                ((TextView)view.findViewById(R.id.lblTitle)).setText("Consulta: " + miConsulta.codigoconsulta);

                final AlertDialog alertDialog = builder.create();
                view.findViewById(R.id.btnConfirm).setOnClickListener( v -> {

                    miConsulta.diagnostico = txtDiagnostico.getText().toString().trim();
                    miConsulta.receta = txtReceta.getText().toString().trim();
                    miConsulta.prescripcion = txtPrescripcion.getText().toString().trim();

                    if(miConsulta.diagnostico.trim().equals("")
                            && miConsulta.receta.trim().equals("")
                            && miConsulta.prescripcion.trim().equals("")){
                        Utils.showMessage(activity, "Debe ingresar datos para actualizar.");
                    }else {
                        miConsulta.actualizado = 1;

                        if(miConsulta.Save()) {
                            ContentValues values = new ContentValues();
                            values.put("actualizado", 1);
                            Mascota.Update(miConsulta.mascotaid, values);
                            alertDialog.dismiss();
                            Utils.showMessage(activity, Constants.MSG_DATOS_GUARDADOS);
                            notifyDataSetChanged();
                        }else
                            Utils.showMessage(activity, Constants.MSG_DATOS_NO_GUARDADOS);
                    }
                });

                view.findViewById(R.id.btnCancel).setOnClickListener(v -> alertDialog.dismiss());

                if(alertDialog.getWindow()!=null)
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                alertDialog.setCancelable(false);
                alertDialog.show();
            }catch (Exception e){
                Log.d(TAG,"ModalConsulta(): " + e.getMessage());
            }
        }

    }
}
