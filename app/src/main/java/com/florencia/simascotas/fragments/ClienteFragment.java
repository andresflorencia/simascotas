package com.florencia.simascotas.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.florencia.simascotas.MainActivity;
import com.florencia.simascotas.R;
import com.florencia.simascotas.activities.ClienteActivity;
import com.florencia.simascotas.activities.ComprobanteActivity;
import com.florencia.simascotas.activities.PedidoActivity;
import com.florencia.simascotas.adapters.ClienteAdapter;
import com.florencia.simascotas.models.Cliente;
import com.florencia.simascotas.services.SQLite;
import com.florencia.simascotas.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClienteFragment extends Fragment implements SearchView.OnQueryTextListener{

    View view;
    Toolbar toolbar;
    ImageView imgFondo;
    RecyclerView rvClientes;
    List<Cliente> lstClientes= new ArrayList<>();
    ClienteAdapter clienteAdapter;
    SearchView svBusqueda;
    LinearLayout lyContainer;
    ProgressDialog pgCargando;
    private SwipeRefreshLayout swipeRefreshClientes;
    public static final int CLIENTE_FRAGMENT = 1;
    String _fecha = "";
    public ClienteFragment() {
        // Required empty public constructor
    }
    public ClienteFragment(String fecha){
        _fecha = fecha;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_cliente, container, false);
        try {
            toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            rvClientes = view.findViewById(R.id.rvClientes);
            imgFondo = view.findViewById(R.id.imgFondo);
            lyContainer = view.findViewById(R.id.lyContainer);
            svBusqueda = view.findViewById(R.id.svBusqueda);
            swipeRefreshClientes = (SwipeRefreshLayout) view.findViewById(R.id.swipeLayoutClientes);
            svBusqueda.setOnQueryTextListener(this);

            pgCargando = new ProgressDialog(view.getContext());
            pgCargando.setTitle("Cargando personas");
            pgCargando.setMessage("Espere un momento...");
            pgCargando.setCancelable(false);

            //Inicio Refrescar el contenido del RecyclerView
            swipeRefreshClientes.setColorSchemeResources(R.color.colorend_splash, R.color.colorAccent, R.color.colorMoradito);
            swipeRefreshClientes.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            try {
                                CargarDatos(true);
                            } catch (Exception e) {
                                Log.d("TAGCLIENTEFRAGMENT", "SwipeRefresh(): " + e.getMessage());
                            }
                        }
                    });
            //Fin Refrescar el contenido del RecyclerView

        }catch (Exception e){
            Log.d("TAGCLIENTEFRAGMENT", "onCreate(): " + e.getMessage());
        }
        return view;
    }

    public void CargarDatos( boolean isSwipe){
        try {
            Thread th = new Thread() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                lstClientes = Cliente.getClientes(SQLite.usuario.IdUsuario,_fecha, false);
                                if (lstClientes == null || lstClientes.size() == 0) {
                                    imgFondo.setVisibility(View.VISIBLE);
                                    lyContainer.setVisibility(View.GONE);
                                } else {
                                    imgFondo.setVisibility(View.GONE);
                                    lyContainer.setVisibility(View.VISIBLE);
                                    clienteAdapter = new ClienteAdapter(getContext(), lstClientes);
                                    rvClientes.setAdapter(clienteAdapter);

                                    String bus = svBusqueda.getQuery().toString();
                                    svBusqueda.setQuery("", true);
                                    if (!bus.equals(""))
                                        svBusqueda.setQuery(bus, true);
                                }
                                if (isSwipe)
                                    ClienteFragment.this.swipeRefreshClientes.setRefreshing(false);
                            }catch (Exception e){
                                Log.d("TAGCLIENTEFRAGMENT", "CargaDatos(): " + e.getMessage());
                            }
                        }
                    });
                }
            };
            th.start();
        }catch (Exception e){
            Log.d("TAGCLIENTE",e.getMessage());
        }
    }

    @Override
    public void onResume() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        String titulo="Personas" ;
        toolbar.setTitle(titulo);
        toolbar.setTitleTextColor(Color.WHITE);
        CargarDatos(false);
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cliente,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.option_addclient:
                i = new Intent(getContext().getApplicationContext(), ClienteActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        clienteAdapter.filter(newText);
        return false;
    }
}
