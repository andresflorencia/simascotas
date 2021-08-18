package com.florencia.simascotas.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.florencia.simascotas.R;
import com.florencia.simascotas.adapters.FragmentAdapter;
import com.florencia.simascotas.models.Cliente;
import com.florencia.simascotas.models.Mascota;
import com.florencia.simascotas.utils.Utils;
import com.google.android.material.tabs.TabLayout;

import okhttp3.internal.Util;

public class DiagnosticoActivity extends AppCompatActivity implements View.OnClickListener {
    public static String TAG = "TAGDIAGNOSTICO_ACT";
    TabLayout tabOpciones;
    ViewPager vpTabContent;
    FragmentAdapter fragmentAdapter;
    Integer idmascota;
    Mascota miMascota;
    TextView lblInfoPrincipal, lblInfoDetalle;
    LinearLayout lyInfoPrincipal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostico);

        init();

    }

    private void init(){
        try{
            tabOpciones = findViewById(R.id.tabOpciones);
            vpTabContent = findViewById(R.id.vpTabContent);
            lblInfoPrincipal = findViewById(R.id.lblInfoPrincipal);
            lyInfoPrincipal = findViewById(R.id.lyInfoPrincipal);
            lblInfoDetalle = findViewById(R.id.lblInfoDetalle);

            lblInfoPrincipal.setOnClickListener(this::onClick);

            if(getIntent().getExtras() != null){
                this.idmascota = getIntent().getExtras().getInt("idmascota", 0);
                miMascota = Mascota.getById(idmascota);
                if(miMascota != null){
                    Integer idpropietario = getIntent().getExtras().getInt("idpropietario", 0);
                    Cliente propietario = Cliente.get(idpropietario, false);
                    String _info = "<strong>Nombre: </strong> ".concat(miMascota.nombre);
                    _info = _info.concat("<br><strong>Especie: </strong> ").concat(miMascota.especie.nombrecatalogo);
                    _info = _info.concat("<br><strong>Raza: </strong> ").concat(miMascota.raza.nombrecatalogo);
                    _info = _info.concat("<br><strong>F. Nac.: </strong> ").concat(miMascota.fechanacimiento).concat("  <strong>Edad: </strong> ").concat(Utils.getEdad(miMascota.fechanacimiento));
                    _info = _info.concat("<br><strong>Peso: </strong> ").concat(miMascota.peso + " Kg")
                            .concat("  <strong>"+(!miMascota.color1.trim().equals("") && !miMascota.color2.trim().equals("")?"Colores":"Color")+" </strong>")
                            .concat(!miMascota.color1.trim().equals("") && !miMascota.color2.trim().equals("")?miMascota.color1 + " - "+ miMascota.color2:(!miMascota.color1.trim().equals("")?miMascota.color1:miMascota.color2));
                    _info = _info.concat("<br><strong>Dueño: </strong> ").concat(propietario.razonsocial);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        lblInfoDetalle.setText(Html.fromHtml(_info, Html.FROM_HTML_MODE_COMPACT));
                    else
                        lblInfoDetalle.setText(Html.fromHtml(_info));
                }
            }

            fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), tabOpciones.getTabCount(), this.idmascota);
            vpTabContent.setAdapter(fragmentAdapter);

            tabOpciones.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    vpTabContent.setCurrentItem(tab.getPosition(), true);
                    fragmentAdapter.notifyDataSetChanged();
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });

            vpTabContent.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabOpciones));
        }catch (Exception e){
            Log.d(TAG, e.getMessage());}
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.lblInfoPrincipal:
                Utils.EfectoLayout(lyInfoPrincipal, lblInfoPrincipal);
                break;
        }
    }

    @Override
    public void onResume() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        toolbar.setTitle("Historia Clínica");
        toolbar.setTitleTextColor(Color.WHITE);
        super.onResume();
    }
}
