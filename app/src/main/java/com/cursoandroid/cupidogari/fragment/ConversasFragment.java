package com.cursoandroid.cupidogari.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cursoandroid.cupidogari.adapter.ConversaAdapter;
import com.cursoandroid.cupidogari.config.ConfiguracaoFirebase;
import com.cursoandroid.cupidogari.cupidogari.ConversasActivity;
import com.cursoandroid.cupidogari.cupidogari.R;
import com.cursoandroid.cupidogari.helper.Base64Custom;
import com.cursoandroid.cupidogari.helper.Preferencias;
import com.cursoandroid.cupidogari.model.Contato;
import com.cursoandroid.cupidogari.model.Conversa;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ConversasFragment extends Fragment {

    private ArrayAdapter<Conversa> adapter;
    private ArrayList<Conversa> conversas;

    private ValueEventListener valueEventListenerContatos;
    private DatabaseReference mDatabaseRef;

    public ConversasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        conversas = new ArrayList<>();
        ListView listView = (ListView) view.findViewById(R.id.contatos);
        adapter = new ConversaAdapter(getActivity(),conversas);
        listView.setAdapter(adapter);

        Preferencias preferencias = new Preferencias(getActivity());
        String identificadorUsuarioLogado = preferencias.getIdentificador();

        if (identificadorUsuarioLogado != null) {
            mDatabaseRef = ConfiguracaoFirebase.getFirebase()
                    .child("conversas")
                    .child(identificadorUsuarioLogado);
        }

        valueEventListenerContatos = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                conversas.clear();

                for (DataSnapshot dados : dataSnapshot.getChildren()) {
                    Conversa contato = dados.getValue(Conversa.class);
                    conversas.add(contato);
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
                Conversa conversa = conversas.get(position);
                Intent intent = new Intent(getActivity(),ConversasActivity.class);
                intent.putExtra("nome",conversa.getNome());
                String email = Base64Custom.decodificarBase64(conversa.getIdUsuario());
                intent.putExtra("email",email);
                intent.putExtra("url",conversa.getUrl());
                startActivity(intent);
            }
        });

        return view;
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

}
