package com.cursoandroid.cupidogari.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.cursoandroid.cupidogari.cupidogari.R;
import com.cursoandroid.cupidogari.model.Contato;

import java.util.List;

public class ContatoAdapter extends ArrayAdapter<Contato> {

    private List<Contato> contatos;
    private Context context;


    public ContatoAdapter(Context c, List<Contato> objects) {
        super(c, 0, objects);
        this.contatos = objects;
        this.context = c;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {

        View view = null;

        // Verifica se a lista está vazia
        if( contatos != null ){

            // inicializar objeto para montagem da view
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Monta view a partir do xml
            view = inflater.inflate(R.layout.lista_contato, parent, false);

            // recupera elemento para exibição
            TextView nomeContato = view.findViewById(R.id.tv_titulo);
            TextView emailContato = view.findViewById(R.id.tv_subTitulo);
            final ImageView imgContato = view.findViewById(R.id.imgUsuario);
            final ImageView imgReload = view.findViewById(R.id.imgContato);
            final Contato contato = contatos.get( position );
            nomeContato.setText( contato.getNome());
            emailContato.setText( contato.getEmail() );
            final String url = contato.getUrl().replace("*",".");
                if (!contato.getUrl().equals("hue")) {
                    imgReload.setVisibility(View.GONE);
                    Glide.with(context).load(url).into(imgContato);
                    imgContato.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onClick(final View v) {
                            LayoutInflater li = LayoutInflater.from(v.getContext());
                            @SuppressLint("InflateParams")
                            View view = li.inflate(R.layout.perfil_contato, null);
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
                            final AlertDialog bui = alertDialog.create();
                            ImageView image = view.findViewById(R.id.imgDoContato);
                            Glide.with(context).load(url).into(image);
                            bui.setCancelable(true);
                            bui.setView(view);
                            bui.create();
                            bui.show();
                        }
                    });
            }else {
                    ProgressBar pb = view.findViewById(R.id.pbContato);
                    pb.setVisibility(View.GONE);
                }
        }
        return view;
    }
}


