package com.cursoandroid.cupidogari.fragment;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.cursoandroid.cupidogari.adapter.ContatoAdapter;
import com.cursoandroid.cupidogari.config.ConfiguracaoFirebase;
import com.cursoandroid.cupidogari.cupidogari.ConversasActivity;
import com.cursoandroid.cupidogari.cupidogari.R;
import com.cursoandroid.cupidogari.helper.Preferencias;
import com.cursoandroid.cupidogari.model.Contato;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContatosFragment extends Fragment {

    private ArrayAdapter adapter;
    private TextView txtSemContato;
    private ArrayList<Contato> contatos;
    private ValueEventListener valueEventListenerContatos;

    private DatabaseReference mDatabaseRef;

    public ContatosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDatabaseRef != null) {
            mDatabaseRef.addValueEventListener(valueEventListenerContatos);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDatabaseRef != null) {
            mDatabaseRef.removeEventListener(valueEventListenerContatos);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        contatos = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        ListView listView = view.findViewById(R.id.contatos);
        txtSemContato = view.findViewById(R.id.txtSemContatos);

        adapter = new ContatoAdapter(getActivity(), contatos);
        listView.setAdapter(adapter);

        //Recuperar contatos do firebase
        Preferencias preferencias = new Preferencias(getActivity());
        String identificadorUsuarioLogado = preferencias.getIdentificador();

        if(identificadorUsuarioLogado != null) {
            mDatabaseRef = ConfiguracaoFirebase.getFirebase()
                    .child("contatos")
                    .child(identificadorUsuarioLogado);
        }

        valueEventListenerContatos = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()){
                    txtSemContato.setVisibility(View.VISIBLE);
                }

                    //Limpar lista
                    contatos.clear();

                    //Listar contatos
                    for (DataSnapshot dados : dataSnapshot.getChildren()) {
                        Contato contato = dados.getValue(Contato.class);
                        contatos.add(contato);
                        txtSemContato.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ConversasActivity.class);
                Contato contato = contatos.get(position);
                intent.putExtra("nome",contato.getNome());
                intent.putExtra("email",contato.getEmail());
                intent.putExtra("url",contato.getUrl());
                startActivity(intent);
            }
        });
        return view;
    }

}
