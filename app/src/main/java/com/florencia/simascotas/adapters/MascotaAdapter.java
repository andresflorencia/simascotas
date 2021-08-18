package com.florencia.simascotas.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.simascotas.R;
import com.florencia.simascotas.activities.ClienteActivity;
import com.florencia.simascotas.activities.DiagnosticoActivity;
import com.florencia.simascotas.activities.MascotaActivity;
import com.florencia.simascotas.activities.PropiedadActivity;
import com.florencia.simascotas.models.Catalogo;
import com.florencia.simascotas.models.Cliente;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.models.Propiedad;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>{

    public List<Mascota> listMascotas;
    private List<Mascota> orginalItems = new ArrayList<>();
    Activity context;

    public MascotaAdapter(Activity context, List<Mascota> listMascotas) {
        this.context = context;
        this.listMascotas = listMascotas;
        this.orginalItems.addAll(this.listMascotas);
    }

    @NonNull
    @Override
    public MascotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MascotaViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_mascota, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaViewHolder holder, int position) {
        holder.bindMascota(listMascotas.get(position));
    }

    @Override
    public int getItemCount() {
        return listMascotas.size();
    }

    public void filter(final String busqueda){
        listMascotas.clear();
        if(busqueda.length()==0){
            listMascotas.addAll(orginalItems);
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<Mascota> collect = orginalItems.stream()
                        .filter(i -> i.nombre.concat(i.nipdueno)
                                .concat(i.color1.toLowerCase())
                                .concat(i.color2.toLowerCase())
                                .concat(i.fechanacimiento.toLowerCase())
                                .concat(i.especie.nombrecatalogo.toLowerCase())
                                .concat(i.raza.nombrecatalogo.toLowerCase())
                                .concat(i.codigomascota.toLowerCase())
                                .contains(busqueda.toLowerCase()))
                        .collect(Collectors.<Mascota>toList());
                listMascotas.addAll(collect);
            }else{
                for(Mascota i: orginalItems){
                    if(i.nombre.concat(i.nipdueno)
                            .concat(i.color1.toLowerCase())
                            .concat(i.color2.toLowerCase())
                            .concat(i.especie.nombrecatalogo.toLowerCase())
                            .concat(i.raza.nombrecatalogo.toLowerCase())
                            .concat(i.fechanacimiento.toLowerCase())
                            .concat(i.codigomascota.toLowerCase())
                            .contains(busqueda.toLowerCase()))
                        listMascotas.add(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    class MascotaViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        TextView tvNombreMascota, tvPropietario, tvColor, tvCodigo, tvFechaNac, tvEspecie, tvEstado;
        ImageButton btnOptions;
        CardView cvPropiedad;

        MascotaViewHolder(@NonNull View itemView){
            super(itemView);
            tvNombreMascota = itemView.findViewById(R.id.tvNombreMascota);
            tvPropietario = itemView.findViewById(R.id.tvPropietario);
            tvColor = itemView.findViewById(R.id.tvColor);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            tvEspecie = itemView.findViewById(R.id.tvEspecie);
            tvFechaNac = itemView.findViewById(R.id.tvFechaNac);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            btnOptions = itemView.findViewById(R.id.btnOpciones);
            cvPropiedad = itemView.findViewById(R.id.cvPropiedad);
        }

        void bindMascota(final Mascota mascota){
            Cliente propietario = Cliente.get(mascota.duenoid,false);
            if(propietario == null)
                propietario = Cliente.get(mascota.nipdueno,false);
            tvNombreMascota.setText(mascota.nombre);
            tvPropietario.setText(propietario.razonsocial);
            tvColor.setText(mascota.color1.concat(" - ").concat(mascota.color2).concat("\nPeso: ").concat(mascota.peso.toString()+"kg"));
            tvCodigo.setText(mascota.codigomascota);
            tvFechaNac.setText(mascota.fechanacimiento + "\n" + Utils.getEdad(mascota.fechanacimiento));
            tvEspecie.setText(mascota.especie.nombrecatalogo.concat(" - ").concat(mascota.raza.nombrecatalogo));

            if(mascota.codigosistema==0 || mascota.actualizado==1){
                tvEstado.setText("No sincronizado");
                tvEstado.setTextColor(itemView.getContext().getResources().getColor(R.color.colorend_splash));
                tvEstado.setVisibility(View.VISIBLE);
            }else
                tvEstado.setVisibility(View.GONE);

            btnOptions.setOnClickListener(v -> showPopupMenu(v));

            itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, MascotaActivity.class);
                i.putExtra("idmascota", listMascotas.get(getAdapterPosition()).idmascota);
                i.putExtra("idpropietario",listMascotas.get(getAdapterPosition()).duenoid);
                context.startActivityForResult(i, ClienteActivity.REQUEST_NEW_MASCOTA);
            });

        }

        private void showPopupMenu(View v){
            PopupMenu menu = new PopupMenu(v.getContext(),v);
            menu.inflate(R.menu.popup_menu_mascota);
            if((listMascotas.get(this.getAdapterPosition()).codigosistema.equals(0)
                    || SQLite.usuario.VerificaPermiso(v.getContext(), Constants.VISITA_GANADERO,"lectura")
                    || SQLite.usuario.VerificaPermiso(v.getContext(), Constants.VISITA_GANADERO,"modificacion"))) {
                menu.getMenu().findItem(R.id.action_editar).setVisible(true);
            }else
                menu.getMenu().findItem(R.id.action_editar).setVisible(false);
            menu.setOnMenuItemClickListener(this);
            menu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Intent i;
            switch (item.getItemId()) {
                case R.id.action_editar:
                    i = new Intent(context, MascotaActivity.class);
                    i.putExtra("idmascota", listMascotas.get(getAdapterPosition()).idmascota);
                    i.putExtra("idpropietario",listMascotas.get(getAdapterPosition()).duenoid);
                    context.startActivityForResult(i, ClienteActivity.REQUEST_NEW_MASCOTA);
                    break;
                case R.id.action_historia:
                    i = new Intent(context, DiagnosticoActivity.class);
                    i.putExtra("idmascota", listMascotas.get(getAdapterPosition()).idmascota);
                    i.putExtra("idpropietario",listMascotas.get(getAdapterPosition()).duenoid);
                    context.startActivity(i);
                    break;
            }
            return false;
        }
    }
}
