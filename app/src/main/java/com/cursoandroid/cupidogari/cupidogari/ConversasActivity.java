package com.cursoandroid.cupidogari.cupidogari;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class ConversasActivity extends AppCompatActivity {

    private String nomeUsuario;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_conversa);
        ImageButton btn_mensagem = (ImageButton) findViewById(R.id.img_conversas);
        edit_mensagem = (EditText) findViewById(R.id.edit_conversas);
        txtMensagem = (TextView) findViewById(R.id.txtMensagem);
        ListView listView = (ListView) findViewById(R.id.lv_conversas);

        Preferencias preferencias = new Preferencias(ConversasActivity.this);
        idUsuarioRemetente = preferencias.getIdentificador();
        nomeUsuarioRemetente = preferencias.getNome();
        urlUsuarioRemetente = preferencias.getUrl();

        Log.d("Remetente",urlUsuarioRemetente);

        Bundle extra = getIntent().getExtras();

        if (extra != null){

            nomeUsuario = extra.getString("nome");
            String emailUsuario = extra.getString("email");
            urlUsuarioDestinatario = extra.getString("url");
            assert emailUsuario != null;
            idUsuarioDestinatario = Base64Custom.codificarBase64(emailUsuario);

            String[] destinarario = urlUsuarioRemetente.split("/");
            String destrincho = destinarario[7];
            if (destrincho.contains(".") || destrincho.contains("#") || destrincho.contains("$")
                    || destrincho.contains("[") || destrincho.contains("]")){
                Toast.makeText(ConversasActivity.this,"Tem!",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(ConversasActivity.this,"Não tem!",Toast.LENGTH_LONG).show();
            }
        }

        toolbar.setTitle(nomeUsuario);
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

                for(DataSnapshot dado: dataSnapshot.getChildren()){
                    Mensagem mensagem = dado.getValue(Mensagem.class);
                    if (mensagem == null){
                        txtMensagem.setVisibility(View.GONE);
                    }
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
                    Toast.makeText(ConversasActivity.this,"Digite uma mensagem!",Toast.LENGTH_LONG).show();

                }else {

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioRemetente);
                    mensagem.setUrl(urlUsuarioRemetente);
                    mensagem.setMensagem(textoMensagem);

                    Boolean retornoMensagemRemetente = salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, urlUsuarioRemetente, mensagem);

                    if (!retornoMensagemRemetente) {

                        Toast.makeText(ConversasActivity.this, "Não foi possivel enviar a mensagem!",
                                Toast.LENGTH_SHORT).show();

                    } else {

                        Boolean retornoMensagemDestinatario = salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente,urlUsuarioDestinatario, mensagem);
                        if (!retornoMensagemDestinatario) {
                            Toast.makeText(ConversasActivity.this, "Não foi possivel enviar a mensagem 2!",
                                    Toast.LENGTH_SHORT).show();

                        }

                        Conversa conversa = new Conversa();
                        conversa.setIdUsuario( idUsuarioDestinatario);
                        conversa.setNome(nomeUsuario);
                        conversa.setUrl(urlUsuarioDestinatario);
                        conversa.setMensagem(textoMensagem);

                        Boolean retornoConversaRemetente = salvarConversa(idUsuarioRemetente,idUsuarioDestinatario, urlUsuarioRemetente,conversa);

                        if (!retornoConversaRemetente) {
                            Toast.makeText(ConversasActivity.this, "Não foi possivel salvar a mensagem!",
                                    Toast.LENGTH_SHORT).show();
                        }else {

                            conversa.setIdUsuario(idUsuarioRemetente);
                            conversa.setNome(nomeUsuarioRemetente);
                            conversa.setUrl(urlUsuarioRemetente);
                            conversa.setMensagem(textoMensagem);
                            salvarConversa(idUsuarioDestinatario,idUsuarioRemetente,urlUsuarioDestinatario,conversa);

                            Boolean retornoConversaDestinatario = salvarConversa(idUsuarioDestinatario, idUsuarioRemetente,urlUsuarioDestinatario, conversa);

                            if (!retornoConversaDestinatario) {
                                Toast.makeText(ConversasActivity.this, "Não foi possivel enviar a mensagem pro destinatario!",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }

                        edit_mensagem.setText("");
                    }
                }
            }
        });
    }

    private boolean salvarMensagem(String idRemetente,String idDestinatario,String urlDestinatario,Mensagem mensagem){
        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("mensagens");
            firebase.child(idRemetente)
                    .child(urlDestinatario)
                    .child(idDestinatario)
                    .push()
                    .setValue(mensagem);

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean salvarConversa(String idRemetente,String idDestinatario,String urlDestinatario,Conversa conversa){
        try {

            firebase = ConfiguracaoFirebase.getFirebase().child("conversas");
            firebase.child(idRemetente)
                    .child(urlDestinatario)
                    .child(idDestinatario)
                    .setValue(conversa);

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
