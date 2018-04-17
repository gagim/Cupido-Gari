package com.cursoandroid.cupidogari.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cursoandroid.cupidogari.cupidogari.ConversasActivity;
import com.cursoandroid.cupidogari.cupidogari.R;
import com.cursoandroid.cupidogari.helper.Preferencias;
import com.cursoandroid.cupidogari.model.Mensagem;

import java.util.ArrayList;

public class MensagemAdapter extends ArrayAdapter<Mensagem> {

    private Context context;
    private ArrayList<Mensagem> mensagens;

    public MensagemAdapter(Context c, ArrayList<Mensagem> objects) {
        super(c, 0, objects);
        this.context = c;
        this.mensagens = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View converterView, @NonNull ViewGroup parent){

        View view = null;
        if (mensagens != null) {
            Preferencias preferencias = new Preferencias(context);
            String idUsuarioRemetente = preferencias.getIdentificador();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Mensagem mensagem = mensagens.get(position);

            if (idUsuarioRemetente.equals(mensagem.getIdUsuario())) {
                view = inflater.inflate(R.layout.item_mensagem_direita, parent, false);
            } else {
                view = inflater.inflate(R.layout.item_mensagem_esquerda, parent, false);
            }
            TextView textoMensagem = view.findViewById(R.id.edit_quemManda);
            textoMensagem.setText(mensagem.getMensagem());
            TextView txtHora = view.findViewById(R.id.txtHoras);
            txtHora.setText(mensagem.getDate());
        }

        return view;
    }
}
