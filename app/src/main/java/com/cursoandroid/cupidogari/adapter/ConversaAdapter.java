package com.cursoandroid.cupidogari.adapter;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cursoandroid.cupidogari.cupidogari.R;
import com.cursoandroid.cupidogari.model.Contato;
import com.cursoandroid.cupidogari.model.Conversa;

import java.util.ArrayList;

public class ConversaAdapter extends ArrayAdapter<Conversa> {


    private ArrayList<Conversa> conversas;
    private Context context;
    private TextView nomeContato,emailContato;
    private ImageView imgContato;

    public ConversaAdapter( Context c, ArrayList<Conversa> objects) {
        super(c, 0,objects);
        this.conversas = objects;
        this.context = c;
    }
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        View view = null;

        if (conversas != null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.lista_contato, parent, false);
            nomeContato = (TextView) view.findViewById(R.id.tv_titulo);
            emailContato = (TextView) view.findViewById(R.id.tv_subTitulo);
            imgContato = (ImageView) view.findViewById(R.id.imgUsuario);
            final Conversa contato = conversas.get(position);
            nomeContato.setText(contato.getNome());
            emailContato.setText(contato.getMensagem());
            Glide.with(context).load(contato.getUrl()).into(imgContato);
            imgContato.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    View imageLayoutView = inflater.inflate(R.layout.perfil_contato, null);
                    ImageView image = (ImageView) imageLayoutView.findViewById(R.id.imgDoContato);
                    //TextView AvaliarContato = (TextView) imageLayoutView.findViewById(R.id.nomeContato);
                    //AvaliarContato.setText(contato.getNome());
                    Glide.with(context).load(contato.getUrl()).into(image);
                    builder.setView(imageLayoutView);
                    builder.create();
                    builder.show();
                }

            });
        }
        return view;
    }
}
