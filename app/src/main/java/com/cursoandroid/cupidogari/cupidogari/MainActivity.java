package com.cursoandroid.cupidogari.cupidogari;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cursoandroid.cupidogari.adapter.TabAdapter;
import com.cursoandroid.cupidogari.config.ConfiguracaoFirebase;
import com.cursoandroid.cupidogari.helper.Base64Custom;
import com.cursoandroid.cupidogari.helper.Preferencias;
import com.cursoandroid.cupidogari.helper.SlidingTabLayout;
import com.cursoandroid.cupidogari.model.Contato;
import com.cursoandroid.cupidogari.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth usuarioFirebase;
    private String identificadorContato;
    private DatabaseReference firebase;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usuarioFirebase = ConfiguracaoFirebase.getFirebaseAutenticacao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Orange Book");
        toolbar.setSubtitleTextColor(R.color.colorAccent);
        setSupportActionBar(toolbar);

        SlidingTabLayout slidingTabLayout = findViewById(R.id.stl_tabs);
        ViewPager viewPager = findViewById(R.id.vp_pagina);

        //Configurar sliding tabs
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this, R.color.colorAccent));

        //Configurar adapter
        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);

        slidingTabLayout.setViewPager(viewPager);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sair:
                deslogarUsuario();
                return true;
            case R.id.Perfil:
                abrirPaginaPerfil();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("NewApi")
    public void cadastrar(View view2) {

        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        View view = li.inflate(R.layout.layout_contato, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog bui = alertDialog.create();
        Button adicionar = view.findViewById(R.id.btn_adicionar_contato);
        Button cancelar = view.findViewById(R.id.btn_cancelar_contato);
        final EditText email = view.findViewById(R.id.editEmailContato);
        bui.setCancelable(false);
        bui.setView(view);
        bui.create();
        bui.show();
        adicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailContato = email.getText().toString();
                //Valida se o e-mail foi digitado
                if( emailContato.isEmpty() ){
                    Toast.makeText(MainActivity.this, "Preencha o campo e-mail", Toast.LENGTH_SHORT).show();
                }else{

                    //Verificar se o usuário já está cadastrado no nosso App
                    identificadorContato = Base64Custom.codificarBase64(emailContato);

                    //Recuperar instância Firebase
                    firebase = ConfiguracaoFirebase.getFirebase().child("usuarios").child(identificadorContato);

                    firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            if( dataSnapshot.getValue() != null ){

                                //Recuperar dados do contato a ser adicionado
                                Usuario usuarioContato = dataSnapshot.getValue( Usuario.class );

                                //Recuperar identificador usuario logado (base64)
                                Preferencias preferencias = new Preferencias(MainActivity.this);
                                String identificadorUsuarioLogado = preferencias.getIdentificador();

                                    firebase = ConfiguracaoFirebase.getFirebase();
                                    firebase = firebase.child("contatos")
                                            .child( identificadorUsuarioLogado )
                                            .child( identificadorContato );

                                if(!identificadorUsuarioLogado.equals(identificadorContato)){

                                    Contato contato = new Contato();
                                    contato.setIdentificadorUsuario( identificadorContato );
                                    contato.setEmail( usuarioContato.getEmail() );
                                    contato.setNome( usuarioContato.getNome() );
                                    contato.setUrl( usuarioContato.getUrl() );

                                    firebase.setValue( contato );

                                    bui.dismiss();
                                }else {
                                    Toast.makeText(MainActivity.this, "E-mail inválido!", Toast.LENGTH_LONG).show();
                                }

                            }else {
                                Toast.makeText(MainActivity.this, "Usuário não possui cadastro.", Toast.LENGTH_LONG)
                                        .show();
                                bui.dismiss();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            bui.dismiss();
                        }
                    });

                }

            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bui.dismiss();
            }
        });
    }

    private void deslogarUsuario(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Deseja sair?");
        alertDialog.setPositiveButton("sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                usuarioFirebase.signOut();

                Intent intent = new Intent(MainActivity.this, Login_Activity.class);
                startActivity(intent);
                finish();
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


    @SuppressLint("ResourceType")
    private void abrirPaginaPerfil(){
        Intent intent = new Intent(MainActivity.this, Perfil_Activity.class);
        startActivity(intent);
        onStop();
    }

}