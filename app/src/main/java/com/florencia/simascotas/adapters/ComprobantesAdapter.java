package com.florencia.simascotas.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.florencia.simascotas.R;
import com.florencia.simascotas.activities.ComprobanteActivity;
import com.florencia.simascotas.activities.ListaComprobantesActivity;
import com.florencia.simascotas.activities.PedidoActivity;
import com.florencia.simascotas.activities.PedidoInventarioActivity;
import com.florencia.simascotas.activities.RecepcionActivity;
import com.florencia.simascotas.activities.TransferenciaActivity;
import com.florencia.simascotas.fragments.InfoFacturaDialogFragment;
import com.florencia.simascotas.models.Comprobante;
import com.florencia.simascotas.models.Pedido;
import com.florencia.simascotas.models.PedidoInventario;
import com.florencia.simascotas.utils.Constants;
import com.florencia.simascotas.utils.Utils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.shasin.notificationbanner.Banner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ComprobantesAdapter extends RecyclerView.Adapter<ComprobantesAdapter.ComprobanteViewHolder> {

    public List<Comprobante> listComprobantes;
    public List<Pedido> listPedidos;
    public List<PedidoInventario> listPedidosInv;
    private List<Comprobante> orginalItems = new ArrayList<>();
    private List<Pedido> orginalItemsP = new ArrayList<>();
    private List<PedidoInventario> orginalItemsPI = new ArrayList<>();
    ListaComprobantesActivity activity;
    String tipobusqueda;
    View rootView;
    Boolean retornar;

    public ComprobantesAdapter(ListaComprobantesActivity activity, List<Comprobante> listComprobantes, List<Pedido> listPedidos, List<PedidoInventario> listPedidosInv, String tipobusqueda, Boolean retornar) {
        this.activity = activity;
        this.listComprobantes= listComprobantes;
        this.listPedidos = listPedidos;
        this.listPedidosInv = listPedidosInv;
        this.tipobusqueda = tipobusqueda;
        this.rootView = activity.findViewById(android.R.id.content);
        this.retornar = retornar;

        if((this.tipobusqueda.equals("01")
                || this.tipobusqueda.equals("8,23") || this.tipobusqueda.equals("23,8")
                || this.tipobusqueda.equals("4,20") || this.tipobusqueda.equals("20,4"))
                && this.listComprobantes != null) {
            this.orginalItems.addAll(this.listComprobantes);
        }
        if(this.tipobusqueda.equals("PC") && this.listPedidos != null)
            this.orginalItemsP.addAll(this.listPedidos);
        if(this.tipobusqueda.equals("PI") && this.listPedidosInv != null)
            this.orginalItemsPI.addAll(this.listPedidosInv);
    }

    @NonNull
    @Override
    public ComprobanteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ComprobanteViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_comprobante, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ComprobanteViewHolder holder, int position) {
        if(tipobusqueda.equals("01")
                || tipobusqueda.equals("8,23") || tipobusqueda.equals("23,8")
                || tipobusqueda.equals("4,20") || tipobusqueda.equals("20,4")) {
            holder.bindComprobante(listComprobantes.get(position), null, null);
        }else if(tipobusqueda.equals("PC"))
            holder.bindComprobante(null, listPedidos.get(position), null);
        else if(tipobusqueda.equals("PI"))
            holder.bindComprobante(null, null, listPedidosInv.get(position));
    }

    @Override
    public int getItemCount() {
        int size = 0;
        if(this.tipobusqueda.equals("01")
                || this.tipobusqueda.equals("8,23") || this.tipobusqueda.equals("23,8")
                || tipobusqueda.equals("4,20") || tipobusqueda.equals("20,4")) {
            size = listComprobantes.size();
        }else if(this.tipobusqueda.equals("PC"))
            size = listPedidos.size();
        else if(this.tipobusqueda.equals("PI"))
            size = listPedidosInv.size();
        return size;
    }

    public void filter(final String busqueda){
        if(this.tipobusqueda.equals("01")
                || this.tipobusqueda.equals("8,23") || this.tipobusqueda.equals("23,8")
                || this.tipobusqueda.equals("4,20")  || this.tipobusqueda.equals("20,4")) {
            listComprobantes.clear();
            if (busqueda.length() == 0) {
                listComprobantes.addAll(orginalItems);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    List<Comprobante> collect = orginalItems.stream()
                            .filter(i -> i.cliente.nip.concat(i.cliente.razonsocial.toLowerCase())
                                    .concat(i.cliente.nombrecomercial.toLowerCase())
                                    .concat(i.total.toString())
                                    .concat(i.codigotransaccion)
                                    .concat(i.claveacceso)
                                    .contains(busqueda.toLowerCase()))
                            .collect(Collectors.<Comprobante>toList());
                    listComprobantes.addAll(collect);
                } else {
                    for (Comprobante i : orginalItems) {
                        if (i.cliente.nip.concat(i.cliente.razonsocial.toLowerCase())
                                .concat(i.cliente.nombrecomercial.toLowerCase())
                                .concat(i.total.toString())
                                .concat(i.codigotransaccion)
                                .concat(i.claveacceso)
                                .contains(busqueda.toLowerCase()))
                            listComprobantes.add(i);
                    }
                }
            }
        }else if(this.tipobusqueda.equals("PC")){
            listPedidos.clear();
            if(busqueda.length()==0){
                listPedidos.addAll(orginalItemsP);
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    List<Pedido> collect = orginalItemsP.stream()
                            .filter(i -> i.cliente.nip.concat(i.cliente.razonsocial.toLowerCase())
                                    .concat(i.cliente.nombrecomercial.toLowerCase())
                                    .concat(i.total.toString())
                                    .concat(i.secuencialpedido)
                                    .contains(busqueda.toLowerCase()))
                            .collect(Collectors.<Pedido>toList());
                    listPedidos.addAll(collect);
                }else{
                    for(Pedido i: orginalItemsP){
                        if(i.cliente.nip.concat(i.cliente.razonsocial.toLowerCase())
                                .concat(i.cliente.nombrecomercial.toLowerCase())
                                .concat(i.total.toString())
                                .concat(i.secuencialpedido)
                                .contains(busqueda.toLowerCase()))
                            listPedidos.add(i);
                    }
                }
            }
        }else if(this.tipobusqueda.equals("PI")){
            listPedidosInv.clear();
            if(busqueda.length()==0){
                listPedidosInv.addAll(orginalItemsPI);
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    List<PedidoInventario> collect = orginalItemsPI.stream()
                            .filter(i -> i.codigopedido.toLowerCase()
                                    .concat(i.fecharegistro)
                                    .concat(i.observacion.toLowerCase())
                                    .contains(busqueda.toLowerCase()))
                            .collect(Collectors.<PedidoInventario>toList());
                    listPedidosInv.addAll(collect);
                }else{
                    for(PedidoInventario i: orginalItemsPI){
                        if(i.codigopedido.toLowerCase()
                                .concat(i.fecharegistro)
                                .concat(i.observacion.toLowerCase())
                                .contains(busqueda.toLowerCase()))
                            listPedidosInv.add(i);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class ComprobanteViewHolder extends RecyclerView.ViewHolder{

        TextView tvNombreCliente, tvNumeroComprobante, tvTotal, tvFecha;
        ImageButton btnAnular, btnPreview;

        ComprobanteViewHolder(@NonNull View itemView){
            super(itemView);
            tvNombreCliente = itemView.findViewById(R.id.tv_NombreCliente);
            tvNumeroComprobante = itemView.findViewById(R.id.tv_NumeroComprobante);
            tvTotal = itemView.findViewById(R.id.tv_Total);
            tvFecha = itemView.findViewById(R.id.tv_Fecha);
            btnAnular = itemView.findViewById(R.id.btnAnular);
            btnPreview = itemView.findViewById(R.id.btnPreview);
        }

        void bindComprobante(final Comprobante comprobante, final Pedido pedido, final PedidoInventario pedidoinv){
            try {
                if (tipobusqueda.equals("01")) {
                    tvNombreCliente.setText(comprobante.cliente.nip + " - " + comprobante.cliente.razonsocial);
                    tvNumeroComprobante.setText("N°: " + comprobante.codigotransaccion);
                    tvTotal.setText("Total: " + Utils.FormatoMoneda(comprobante.total, 2));
                    tvFecha.setText("Fecha: " + comprobante.fechadocumento);
                    btnPreview.setVisibility(View.VISIBLE);

                    if (comprobante.estado == 0 && comprobante.codigosistema == 0) {
                        btnAnular.setVisibility(View.VISIBLE);
                        btnAnular.setImageResource(R.drawable.ic_delete2);
                        btnAnular.setEnabled(true);
                        itemView.setBackgroundResource(R.drawable.bg_btn_red);
                    } else {
                        btnAnular.setVisibility(View.VISIBLE);
                        btnAnular.setImageResource(R.drawable.ic_cloud_green);
                        btnAnular.setEnabled(false);
                        itemView.setBackgroundResource(R.drawable.bg_btn_green);
                    }
                } else if (tipobusqueda.equals("PC")) {
                    tvNombreCliente.setText(pedido.cliente.nip + " - " + pedido.cliente.razonsocial);
                    tvNumeroComprobante.setText("N°: " + pedido.secuencialpedido);
                    tvTotal.setText("Total: " + Utils.FormatoMoneda(pedido.total, 2));
                    tvFecha.setText("F. Pedido: " + pedido.fechapedido + "\nF. Regist: " + pedido.fechacelular);
                    btnPreview.setVisibility(View.VISIBLE);
                    if (pedido.estado == 1 && pedido.codigosistema == 0) {
                        btnAnular.setVisibility(View.VISIBLE);
                        btnAnular.setImageResource(R.drawable.ic_delete2);
                        btnAnular.setEnabled(true);
                        itemView.setBackgroundResource(R.drawable.bg_btn_red);
                    } else {
                        btnAnular.setVisibility(View.VISIBLE);
                        btnAnular.setImageResource(R.drawable.ic_cloud_green);
                        btnAnular.setEnabled(false);
                        itemView.setBackgroundResource(R.drawable.bg_btn_green);
                    }
                }else if (tipobusqueda.equals("8,23") || tipobusqueda.equals("23,8")) { //RECEPCIONES - RECEPDEVOLUCION
                    tvNombreCliente.setText("RECEP: " + comprobante.codigotransaccion);
                    tvNumeroComprobante.setText("TRANSF: " + comprobante.claveacceso);
                    tvTotal.setText("");
                    tvFecha.setText("F. Reg.: " + comprobante.fechacelular);
                    btnAnular.setVisibility(View.GONE);
                    itemView.setBackgroundResource(R.drawable.bg_btn_gps);
                }else if (tipobusqueda.equals("4,20") || tipobusqueda.equals("20,4")) { //TRANSFERENCIAS
                    tvNombreCliente.setText((comprobante.tipotransaccion.equals("4")?"TRANSF: ":"DEVOL: ") + comprobante.claveacceso);
                    tvNumeroComprobante.setText("Origen: " + comprobante.sucursalenvia.split("-")[1]
                                        + "\nDestino: " + comprobante.sucursalrecibe.split("-")[1]);
                    tvTotal.setText("");
                    tvFecha.setText("F. Reg.: " + comprobante.fechacelular);
                    btnAnular.setVisibility(View.GONE);
                    itemView.setBackgroundResource(R.drawable.bg_btn_gps);
                }else if (tipobusqueda.equals("PI")) { //PEDIDO INVENTARIO
                    tvNombreCliente.setText("DOC: " + pedidoinv.codigopedido);
                    tvNumeroComprobante.setText("Fecha: " + pedidoinv.fecharegistro);
                    tvTotal.setText("Num Items: " + pedidoinv.detalle.size());
                    btnAnular.setVisibility(View.GONE);
                    if (pedidoinv.estadomovil == 1 && pedidoinv.codigosistema == 0) {
                        btnAnular.setVisibility(View.VISIBLE);
                        btnAnular.setImageResource(R.drawable.ic_delete2);
                        btnAnular.setEnabled(true);
                        tvFecha.setText("No sincronizado");
                        tvFecha.setTextColor(activity.getResources().getColor(R.color.black_overlay));
                        itemView.setBackgroundResource(R.drawable.bg_btn_red);
                    } else {
                        btnAnular.setVisibility(View.VISIBLE);
                        btnAnular.setImageResource(R.drawable.ic_cloud_green);
                        btnAnular.setEnabled(false);
                        tvFecha.setText("Sincronizado");
                        tvFecha.setTextColor(activity.getResources().getColor(R.color.colorSuccess));
                        itemView.setBackgroundResource(R.drawable.bg_btn_green);
                    }
                }
            }catch (Exception e){
                Log.d("TAGCOMPROBANTEADAPTER", "bindComprobante(): " + e.getMessage());
            }

            itemView.setOnClickListener(v -> {
                    int idcomprobante=0;
                    if(tipobusqueda.equals("01")
                            || tipobusqueda.equals("8,23") || tipobusqueda.equals("23,8")
                            || tipobusqueda.equals("4,20") || tipobusqueda.equals("20,4")) {
                        idcomprobante = listComprobantes.get(getAdapterPosition()).idcomprobante;
                    }else if(tipobusqueda.equals("PC"))
                        idcomprobante = listPedidos.get(getAdapterPosition()).idpedido;
                    else if(tipobusqueda.equals("PI"))
                        idcomprobante = listPedidosInv.get(getAdapterPosition()).idpedido;

                    if(retornar) {
                        activity.setResult(Activity.RESULT_OK, new Intent().putExtra("idcomprobante", idcomprobante));
                        activity.finish();
                    } else{
                        Intent i = null;
                        switch (tipobusqueda) {
                            case "01":
                                i = new Intent(activity, ComprobanteActivity.class);
                                break;
                            case "4,20":
                            case "20,4":
                                i = new Intent(activity, TransferenciaActivity.class);
                                break;
                            case "8,23":
                            case "23,8":
                                i = new Intent(activity, RecepcionActivity.class);
                                break;
                            case "PC":
                                i = new Intent(activity, PedidoActivity.class);
                                break;
                            case "PI":
                                i = new Intent(activity, PedidoInventarioActivity.class);
                                break;
                        }
                        i.putExtra("idcomprobante", idcomprobante);
                        activity.startActivity(i);
                    }
                }
            );


            itemView.setOnLongClickListener(v -> {
                        String secuencial = "";
                        switch (tipobusqueda) {
                            case "01":
                            case "4,20":
                            case "20,4":
                            case "8,23":
                            case "23,8":
                                secuencial = listComprobantes.get(getAdapterPosition()).codigotransaccion;
                                break;
                            case "PC":
                                secuencial = listPedidos.get(getAdapterPosition()).secuencialpedido;
                                break;
                            case "PI":
                                secuencial = listPedidosInv.get(getAdapterPosition()).codigopedido;
                                break;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Eliminar documento «" + secuencial + "»");
                        builder.setMessage("¿Está seguro que desea eliminar este documento?\n" +
                                "Nota:\n" +
                                "1.- Después de eliminado no podrá sincronizarse y ya no será visible.\n" +
                                "2.- Este registro solo se eliminará de su dispositivo.");
                        builder.setIcon(R.drawable.ic_delete2);
                        builder.setPositiveButton(activity.getResources().getString(R.string.Confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int eliminado = 0;
                                String numdoc = "";
                                switch (tipobusqueda) {
                                    case "01":
                                    case "4,20":
                                    case "20,4":
                                    case "8,23":
                                    case "23,8":
                                        eliminado = Comprobante.Delete(listComprobantes.get(getAdapterPosition()).idcomprobante, "", "", 0, false);
                                        if (eliminado > 0) {
                                            numdoc = listComprobantes.get(getAdapterPosition()).codigotransaccion;
                                            listComprobantes.remove(getAdapterPosition());
                                            notifyDataSetChanged();
                                        }
                                        break;
                                    case "PC":
                                        eliminado = Pedido.Delete(listPedidos.get(getAdapterPosition()).idpedido, "", "", 0, false);
                                        if (eliminado > 0) {
                                            numdoc = listPedidos.get(getAdapterPosition()).secuencialpedido;
                                            listPedidos.remove(getAdapterPosition());
                                            notifyDataSetChanged();
                                        }
                                        break;
                                    case "PI":
                                        eliminado = PedidoInventario.Delete(listPedidosInv.get(getAdapterPosition()).idpedido, "", "", 0, false);
                                        if (eliminado > 0) {
                                            numdoc = listPedidosInv.get(getAdapterPosition()).codigopedido;
                                            listPedidosInv.remove(getAdapterPosition());
                                            notifyDataSetChanged();
                                        }
                                        break;
                                }

                                if (eliminado > 0) {
                                    Banner.make(rootView, activity, Banner.SUCCESS, "Documento «" + numdoc + "» eliminado correctamente.", Banner.BOTTOM, 3000).show();
                                } else {
                                    Banner.make(rootView, activity, Banner.ERROR, Constants.MSG_DATOS_NO_GUARDADOS, Banner.BOTTOM, 3000).show();
                                }
                            }
                        });
                        builder.setNegativeButton(activity.getResources().getString(R.string.Cancel), null);
                        builder.show();
                        return false;
                    }
            );

            btnAnular.setOnClickListener(v -> {
                    String secuencial = "";
                    if(tipobusqueda.equals("01"))
                        secuencial = listComprobantes.get(getAdapterPosition()).codigotransaccion;
                    else if(tipobusqueda.equals("PC"))
                            secuencial = listPedidos.get(getAdapterPosition()).secuencialpedido;
                    else if(tipobusqueda.equals("PI"))
                        secuencial = listPedidosInv.get(getAdapterPosition()).codigopedido;
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Anular documento " + secuencial);
                    builder.setMessage("¿Está seguro que desea anular este documento?\n" +
                            "Nota: Después de anulado no podrá sincronizarse y ya no será visible.");
                    builder.setIcon(R.drawable.ic_delete2);
                    builder.setPositiveButton(activity.getResources().getString(R.string.Confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean actualizado = false;
                            int id = 0;
                            ContentValues values = new ContentValues();
                            values.put("estado", -1);
                            if(tipobusqueda.equals("01")) {
                                id = listComprobantes.get(getAdapterPosition()).idcomprobante;
                                actualizado = Comprobante.Update(id,values);
                                listComprobantes.remove(getAdapterPosition());
                                notifyDataSetChanged();
                            }else if(tipobusqueda.equals("PC")) {
                                id = listPedidos.get(getAdapterPosition()).idpedido;
                                actualizado = Pedido.Update(id, values);
                                listPedidos.remove(getAdapterPosition());
                                notifyDataSetChanged();
                            }else if(tipobusqueda.equals("PI")) {
                                id = listPedidosInv.get(getAdapterPosition()).idpedido;
                                values = new ContentValues();
                                values.put("estadomovil", -1);
                                values.put("estado", "A");
                                actualizado = PedidoInventario.Update(id, values);
                                listPedidosInv.remove(getAdapterPosition());
                                notifyDataSetChanged();
                            }

                            if(actualizado){
                                Banner.make(rootView,activity,Banner.SUCCESS, Constants.MSG_DATOS_GUARDADOS, Banner.BOTTOM,3000).show();
                            }else{
                                Banner.make(rootView,activity,Banner.ERROR, Constants.MSG_DATOS_NO_GUARDADOS, Banner.BOTTOM,3000).show();
                            }
                        }
                    });
                    builder.setNegativeButton(activity.getResources().getString(R.string.Cancel), null);
                    builder.show();
                }
            );

            btnPreview.setOnClickListener(v -> {
                        DialogFragment dialogFragment = new InfoFacturaDialogFragment();
                        Bundle bundle = new Bundle();
                        switch (tipobusqueda) {
                            case "01":
                                bundle.putInt("id", listComprobantes.get(getAdapterPosition()).idcomprobante);
                                break;
                            case "PC":
                                bundle.putInt("id", listPedidos.get(getAdapterPosition()).idpedido);
                                break;
                        }
                        bundle.putString("tipobusqueda", tipobusqueda);
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getSupportFragmentManager(), "dialog");
                    }
            );
        }
    }
}
