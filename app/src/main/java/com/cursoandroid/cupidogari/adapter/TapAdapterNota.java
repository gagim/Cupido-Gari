package com.cursoandroid.cupidogari.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cursoandroid.cupidogari.fragment.NotasFragment;
import com.cursoandroid.cupidogari.fragment.PerfilFragment;

/**
 * Created by Henrique on 01/05/2017.
 */

public class TapAdapterNota extends FragmentStatePagerAdapter {

    private String[] tituloAbs = {"PERFIL","NOTAS"};

    public TapAdapterNota(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;
        switch ( position ){
            case 0:
                fragment = new PerfilFragment();
                break;
            case 1:
                fragment = new NotasFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return tituloAbs.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tituloAbs[ position ];
    }
}
