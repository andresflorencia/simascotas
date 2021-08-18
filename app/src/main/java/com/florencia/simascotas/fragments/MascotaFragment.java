package com.florencia.simascotas.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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

import com.florencia.simascotas.R;
import com.florencia.simascotas.activities.MascotaActivity;
import com.florencia.simascotas.adapters.MascotaAdapter;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.services.SQLite;

import java.util.ArrayList;
import java.util.List;

public class MascotaFragment extends Fragment implements SearchView.OnQueryTextListener {

    View view;
    Toolbar toolbar;
    ImageView imgFondo;
    RecyclerView rvMascotas;
    List<Mascota> lstMascotas = new ArrayList<>();
    MascotaAdapter mascotaAdapter;
    SearchView svBusqueda;
    LinearLayout lyContainer;
    ProgressDialog pgCargando;
    private SwipeRefreshLayout swipeRefreshMascotas;
    String _fecha = "";
    public static String TAG = "MASCOTAFRAGMENT";

    public MascotaFragment() {
    }

    public MascotaFragment(String fecha) {
        _fecha = fecha;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_mascota, container, false);
        try {
            toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            rvMascotas = view.findViewById(R.id.rvMascotas);
            imgFondo = view.findViewById(R.id.imgFondo);
            lyContainer = view.findViewById(R.id.lyContainer);
            svBusqueda = view.findViewById(R.id.svBusqueda);
            swipeRefreshMascotas = (SwipeRefreshLayout) view.findViewById(R.id.swipeLayoutMascotas);
            svBusqueda.setOnQueryTextListener(this);

            pgCargando = new ProgressDialog(view.getContext());
            pgCargando.setTitle("Cargando personas");
            pgCargando.setMessage("Espere un momento...");
            pgCargando.setCancelable(false);

            //Inicio Refrescar el contenido del RecyclerView
            swipeRefreshMascotas.setColorSchemeResources(R.color.colorend_splash, R.color.colorAccent, R.color.colorMoradito);
            swipeRefreshMascotas.setOnRefreshListener(() -> {
                try {
                    CargarDatos(true);
                } catch (Exception e) {
                    Log.d(TAG, "SwipeRefresh(): " + e.getMessage());
                }
            });
            //Fin Refrescar el contenido del RecyclerView
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return view;
    }

    public void CargarDatos(boolean isSwipe) {
        try {
            Thread th = new Thread() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(() -> {
                        try {
                            lstMascotas = Mascota.getByPropietario(0, true);
                            if (lstMascotas == null || lstMascotas.size() == 0) {
                                imgFondo.setVisibility(View.VISIBLE);
                                lyContainer.setVisibility(View.GONE);
                            } else {
                                imgFondo.setVisibility(View.GONE);
                                lyContainer.setVisibility(View.VISIBLE);
                                mascotaAdapter = new MascotaAdapter(getActivity(), lstMascotas);
                                rvMascotas.setAdapter(mascotaAdapter);

                                String bus = svBusqueda.getQuery().toString();
                                svBusqueda.setQuery("", true);
                                if (!bus.equals(""))
                                    svBusqueda.setQuery(bus, true);
                            }
                            if (isSwipe)
                                MascotaFragment.this.swipeRefreshMascotas.setRefreshing(false);
                        } catch (Exception e) {
                            Log.d(TAG, "CargaDatos(): " + e.getMessage());
                        }
                    });
                }
            };
            th.start();
        } catch (Exception e) {
            Log.d("TAGCLIENTE", e.getMessage());
        }
    }

    @Override
    public void onResume() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        String titulo = "Mascotas";
        toolbar.setTitle(titulo);
        toolbar.setTitleTextColor(Color.WHITE);
        CargarDatos(false);
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mascota, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.option_addmascota:
                i = new Intent(getContext().getApplicationContext(), MascotaActivity.class);
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
        mascotaAdapter.filter(newText);
        return false;
    }
}
