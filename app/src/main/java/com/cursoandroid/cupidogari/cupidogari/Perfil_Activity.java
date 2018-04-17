package com.cursoandroid.cupidogari.cupidogari;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cursoandroid.cupidogari.adapter.TabAdapter;
import com.cursoandroid.cupidogari.adapter.TapAdapterNota;
import com.cursoandroid.cupidogari.config.ConfiguracaoFirebase;
import com.cursoandroid.cupidogari.helper.SlidingTabLayout;
import com.google.firebase.auth.FirebaseAuth;

public class Perfil_Activity extends AppCompatActivity {

    private FirebaseAuth usuarioFirebase;
    private Toolbar toolbar;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_);

        usuarioFirebase = ConfiguracaoFirebase.getFirebaseAutenticacao();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left);
        setSupportActionBar(toolbar);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.stl_tabs);
        viewPager = (ViewPager) findViewById(R.id.vp_pagina);

        //Configurar sliding tabs
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this,R.color.colorAccent));

        //Configurar adapter
        TapAdapterNota tabAdapter = new TapAdapterNota( getSupportFragmentManager() );
        viewPager.setAdapter(tabAdapter);
        slidingTabLayout.setViewPager(viewPager);

    }


    private void deslogarUsuario(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Perfil_Activity.this);
        alertDialog.setTitle("Deseja sair?");
        alertDialog.setPositiveButton("sim", new DialogInterface.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                usuarioFirebase.signOut();

                Intent intent = new Intent(Perfil_Activity.this, Login_Activity.class);
                startActivity(intent);
                finishAfterTransition();
            }
        });
        alertDialog.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertDialog.create();
        alertDialog.show();
    }
}
