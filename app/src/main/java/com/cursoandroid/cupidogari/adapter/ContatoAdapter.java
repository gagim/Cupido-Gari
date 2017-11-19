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

import java.util.List;

public class ContatoAdapter extends ArrayAdapter<Contato> {

    private List<Contato> contatos;
    private Context context;
    private TextView nomeContato,emailContato;
    private ImageView imgContato;


    public ContatoAdapter(Context c, List<Contato> objects) {
        super(c, 0, objects);
        this.contatos = objects;
        this.context = c;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View view = null;

        // Verifica se a lista está vazia
        if( contatos != null ){

            // inicializar objeto para montagem da view
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            // Monta view a partir do xml
            view = inflater.inflate(R.layout.lista_contato, parent, false);

            // recupera elemento para exibição
            nomeContato = (TextView) view.findViewById(R.id.tv_titulo);
            emailContato = (TextView) view.findViewById(R.id.tv_subTitulo);
            imgContato = (ImageView) view.findViewById(R.id.imgUsuario);

            final Contato contato = contatos.get( position );
            nomeContato.setText( contato.getNome());
            emailContato.setText( contato.getEmail() );
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


