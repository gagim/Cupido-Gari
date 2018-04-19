package com.cursoandroid.orangebook.fragment;

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
import android.widget.TextView;
import com.cursoandroid.orangebook.adapter.ConversaAdapter;
import com.cursoandroid.orangebook.config.ConfiguracaoFirebase;
import com.cursoandroid.orangebook.cupidogari.ConversasActivity;
import com.cursoandroid.orangebook.cupidogari.R;
import com.cursoandroid.orangebook.helper.Base64Custom;
import com.cursoandroid.orangebook.helper.Preferencias;
import com.cursoandroid.orangebook.model.Conversa;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ConversasFragment extends Fragment {

    private ArrayAdapter<Conversa> adapter;
    private ArrayList<Conversa> conversas;

    private TextView txtSemMensagens;
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
        ListView listView = view.findViewById(R.id.contatos);
        adapter = new ConversaAdapter(getActivity(),conversas);
        txtSemMensagens = view.findViewById(R.id.txtSemMensagens);
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
                        txtSemMensagens.setVisibility(View.GONE);
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
            mDatabaseRef.addValueEventListener(valueEventListenerContatos);
    }
    @Override
    public void onStop() {
        super.onStop();
            mDatabaseRef.removeEventListener(valueEventListenerContatos);
    }

}
