package com.florencia.simascotas.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.simascotas.R;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.models.Medicamento;
import com.florencia.simascotas.models.MedicamentoMascota;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {
    public static final String TAG = "TAGCONSULTA_ADAPTER";

    public List<MedicamentoMascota> medicamentos;
    private Activity activity;
    View rootView;
    DatePickerDialog dtpDialog;
    Calendar calendar;

    public MedicamentoAdapter(Activity activity, List<MedicamentoMascota> medicamentos) {
        this.medicamentos = medicamentos;
        this.activity = activity;
        rootView = activity.findViewById(android.R.id.content);
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MedicamentoViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_medicamento, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        holder.bindMedicamento(medicamentos.get(position));
    }

    @Override
    public int getItemCount() {
        return medicamentos.size();
    }

    class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        TextView tvCodigo, tvMedicamento, tvAplicacion, tvAtendio, tvEstado;

        MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            tvMedicamento = itemView.findViewById(R.id.tvMedicamento);
            tvAplicacion = itemView.findViewById(R.id.tvAplicacion);
            tvAtendio = itemView.findViewById(R.id.tvAtendido);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }

        void bindMedicamento(final MedicamentoMascota medicamento) {
            tvCodigo.setText(medicamento.codigo);
            tvMedicamento.setText(medicamento.medicamento.nombre + " >> " + medicamento.medicamento.frecuencia);
            tvAplicacion.setText("Aplicación: "+medicamento.fechaaplicacion+", Próxima: " + medicamento.proximaaplicacion);
            tvAtendio.setText("Atendió: " + medicamento.nombreusuario);
            tvEstado.setVisibility(View.GONE);
            if(medicamento.codigosistema == 0 || medicamento.actualizado == 1)
                tvEstado.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(v -> ModalMedicamento(getAdapterPosition()));
        }


        private void ModalMedicamento(int pos){
            try{
                MedicamentoMascota miVacuna =  medicamentos.get(pos);
                EditText txtObservacion;
                Button btnFechaAplica;
                TextView lblProxima;
                Spinner spMedicamento;

                AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
                View view = LayoutInflater.from(activity).inflate(R.layout.layout_add_medicamento,
                        (ConstraintLayout) activity.findViewById(R.id.lyDialogContainer));
                ((TextView)view.findViewById(R.id.lblTitle)).setText((miVacuna.tipo.equals("V")?"Vacuna":"Medicamento") + miVacuna.codigo);
                builder.setView(view);
                txtObservacion = view.findViewById(R.id.txtObservacion);
                btnFechaAplica = view.findViewById(R.id.btnFechaAplica);
                lblProxima = view.findViewById(R.id.lblProxima);
                spMedicamento = view.findViewById(R.id.spMedicamento);

                txtObservacion.setText(miVacuna.observacion);
                btnFechaAplica.setText(miVacuna.fechaaplicacion);

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
                LlenarCombo(spMedicamento, miVacuna.medicamento.idmedicamento, btnFechaAplica.getText().toString(), lblProxima, miVacuna.tipo);

                final AlertDialog alertDialog = builder.create();
                view.findViewById(R.id.btnConfirm).setOnClickListener( v -> {
                    Medicamento medic = (Medicamento) spMedicamento.getSelectedItem();
                    if(medic.idmedicamento == 0){
                        Utils.showMessage(activity, "Debe especifificar "+ (miVacuna.tipo.equals("V")? "la vacuna":"el medicamento") + " a aplicar.");
                    }else {
                        miVacuna.actualizado = 1;
                        miVacuna.medicamento = medic;

                        if(miVacuna.Save()) {
                            ContentValues values = new ContentValues();
                            values.put("actualizado", 1);
                            Mascota.Update(miVacuna.mascotaid, values);
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
                Log.d(TAG,"ModalMedicamento(): " + e.getMessage());
            }
        }

        private void LlenarCombo(Spinner spinner, int value, String fecha, TextView lblProxima, String tipo){
            try{
                List<Medicamento> medicamentos = Medicamento.getList(tipo);
                medicamentos.add(0, new Medicamento("", "Escoja una opción"));
                ArrayAdapter<Medicamento> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, medicamentos);
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
    }

}
