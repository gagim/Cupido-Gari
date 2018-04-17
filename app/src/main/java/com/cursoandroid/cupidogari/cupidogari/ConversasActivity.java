package com.cursoandroid.cupidogari.cupidogari;

import android.annotation.SuppressLint;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.cursoandroid.cupidogari.adapter.MensagemAdapter;
import com.cursoandroid.cupidogari.config.ConfiguracaoFirebase;
import com.cursoandroid.cupidogari.helper.Base64Custom;
import com.cursoandroid.cupidogari.helper.Preferencias;
import com.cursoandroid.cupidogari.model.Conversa;
import com.cursoandroid.cupidogari.model.Mensagem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Date;

public class ConversasActivity extends AppCompatActivity {

    private String nomeUsuarioDestinatario,dataConcatenada;

    private String idUsuarioDestinatario,urlUsuarioDestinatario;
    private String idUsuarioRemetente,urlUsuarioRemetente,nomeUsuarioRemetente;

    private EditText edit_mensagem;
    private TextView txtMensagem;

    private ArrayList<Mensagem> mensagens;
    private ArrayAdapter<Mensagem> adapter;
    private ValueEventListener valueEventListenerMensagem;

    private DatabaseReference firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversas);

        Toolbar toolbar = findViewById(R.id.tb_conversa);
        ImageButton btn_mensagem = findViewById(R.id.img_conversas);
        edit_mensagem = findViewById(R.id.edit_conversas);
        txtMensagem =  findViewById(R.id.txtSemConversa);
        ListView listView = findViewById(R.id.lv_conversas);

        Preferencias preferencias = new Preferencias(ConversasActivity.this);
        idUsuarioRemetente = preferencias.getIdentificador();
        nomeUsuarioRemetente = preferencias.getNome();
        urlUsuarioRemetente = preferencias.getUrl();

        getDate();

        Bundle extra = getIntent().getExtras();

        if (extra != null){

            nomeUsuarioDestinatario = extra.getString("nome");
            String emailUsuario = extra.getString("email");
            urlUsuarioDestinatario = extra.getString("url");
            idUsuarioDestinatario = Base64Custom.codificarBase64(emailUsuario);
            
        }

        toolbar.setTitle(nomeUsuarioDestinatario);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left);
        setSupportActionBar(toolbar);

        mensagens = new ArrayList<>();
        adapter = new MensagemAdapter(ConversasActivity.this,mensagens);
        listView.setAdapter(adapter);

        firebase = ConfiguracaoFirebase.getFirebase()
                .child("mensagens")
                .child( idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        valueEventListenerMensagem = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mensagens.clear();
                if (!dataSnapshot.hasChildren()){
                    txtMensagem.setVisibility(View.VISIBLE);
                }
                for(DataSnapshot dado: dataSnapshot.getChildren()){
                    Mensagem mensagem = dado.getValue(Mensagem.class);
                    mensagens.add(mensagem);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        firebase.addValueEventListener( valueEventListenerMensagem );

        btn_mensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoMensagem = edit_mensagem.getText().toString();

                if(textoMensagem.isEmpty()){
                    Toast.makeText(ConversasActivity.this,"Digite uma mensagem!",Toast.LENGTH_SHORT).show();

                }else {
                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioRemetente);
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setDate(dataConcatenada);

                    Boolean retornoMensagemRemetente = salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                    if (!retornoMensagemRemetente) {

                        Toast.makeText(ConversasActivity.this, "Não foi possivel enviar a mensagem!",
                                Toast.LENGTH_SHORT).show();

                    } else {

                        Boolean retornoMensagemDestinatario = salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);
                        if (!retornoMensagemDestinatario) {
                            Toast.makeText(ConversasActivity.this, "Não foi possivel enviar a mensagem 2!",
                                    Toast.LENGTH_SHORT).show();

                        }

                        Conversa conversa = new Conversa();
                        conversa.setIdUsuario( idUsuarioDestinatario );
                        conversa.setNome( nomeUsuarioDestinatario );
                        conversa.setUrl(urlUsuarioDestinatario);
                        conversa.setMensagem(textoMensagem);

                        Boolean retornoConversaRemetente = salvarConversa(idUsuarioRemetente, idUsuarioDestinatario,conversa);

                        if (!retornoConversaRemetente) {
                            Toast.makeText(ConversasActivity.this, "Não foi possivel salvar a conversa!",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            conversa = new Conversa();
                            conversa.setIdUsuario( idUsuarioRemetente );
                            conversa.setNome( nomeUsuarioRemetente );
                            conversa.setUrl(urlUsuarioRemetente);
                            conversa.setMensagem( textoMensagem );

                            Boolean retornoConversaDestinatario = salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, conversa);

                            if (!retornoConversaDestinatario) {
                                Toast.makeText(ConversasActivity.this, "Não foi possivel enviar a conversa pro destinatario!",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }

                        edit_mensagem.setText("");
                    }
                }
            }
        });
    }

    private void getDate(){
        Date date = new Date();
        int hora = date.getHours();
        int min  = date.getMinutes();
        if(date.getMinutes() < 10) {
            String minString = "0" + date.getMinutes();
            dataConcatenada = hora+":"+minString;
            Toast.makeText(ConversasActivity.this,dataConcatenada,Toast.LENGTH_SHORT).show();
        }else {
            dataConcatenada = hora+":"+min;
        }
    }

    private boolean salvarMensagem(String idRemetente,String idDestinatario,Mensagem mensagem){
        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("mensagens");
            firebase.child( idRemetente )
                    .child( idDestinatario )
                    .push()
                    .setValue( mensagem );

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean salvarConversa(String idRemetente,String idDestinatario,Conversa conversa){
        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("conversas");
            firebase.child( idRemetente )
                    .child( idDestinatario )
                    .setValue( conversa );

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerMensagem);
    }
}
