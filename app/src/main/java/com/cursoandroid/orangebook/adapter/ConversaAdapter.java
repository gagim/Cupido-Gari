package com.cursoandroid.orangebook.adapter;

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
import com.cursoandroid.orangebook.cupidogari.R;
import com.cursoandroid.orangebook.model.Conversa;

import java.util.ArrayList;

public class ConversaAdapter extends ArrayAdapter<Conversa> {


    private ArrayList<Conversa> conversas;
    private Context context;

    public ConversaAdapter( Context c, ArrayList<Conversa> objects) {
        super(c, 0,objects);
        this.conversas = objects;
        this.context = c;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull final ViewGroup parent) {

        View view = null;

        if (conversas != null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.lista_contato, parent, false);
            TextView nomeContato = view.findViewById(R.id.tv_titulo);
            TextView emailContato = view.findViewById(R.id.tv_subTitulo);
            ImageView imgContato = view.findViewById(R.id.imgUsuario);
            final ImageView imgReload = view.findViewById(R.id.imgContato);
            final Conversa contato = conversas.get(position);
            nomeContato.setText(contato.getNome());
            emailContato.setText(contato.getMensagem());
            final String url = contato.getUrl().replace("*",".");
                if (!contato.getUrl().equals("hue")) {
                    imgReload.setVisibility(View.GONE);
                    Glide.with(context).load(url).into(imgContato);
                    imgContato.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            @SuppressLint("InflateParams")
                            View view = inflater.inflate(R.layout.perfil_contato, null);
                            view.forceLayout();
                            ImageView image = view.findViewById(R.id.imgDoContato);
                            Glide.with(context).load(url).into(image);
                            builder.setView(view);
                            builder.create();
                            builder.show();
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
