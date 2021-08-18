package com.florencia.simascotas.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.florencia.simascotas.fragments.ConsultaFragment;
import com.florencia.simascotas.fragments.VacunaFragment;

public class FragmentAdapter extends FragmentPagerAdapter {
    int numtabs, idmascota;

    public FragmentAdapter(@NonNull FragmentManager fm, int behavior, int idmascota) {
        super(fm, behavior);
        this.numtabs = behavior;
        this.idmascota = idmascota;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ConsultaFragment(idmascota);
            case 1:
                return new VacunaFragment(idmascota, "V");
            case 2:
                return new VacunaFragment(idmascota, "M");
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numtabs;
    }
}
