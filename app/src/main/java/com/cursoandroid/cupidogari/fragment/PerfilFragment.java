package com.cursoandroid.cupidogari.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cursoandroid.cupidogari.config.ConfiguracaoFirebase;
import com.cursoandroid.cupidogari.cupidogari.R;
import com.cursoandroid.cupidogari.helper.Preferencias;
import com.cursoandroid.cupidogari.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class PerfilFragment extends Fragment {

    private ImageView imgBackGroud;
    private ProgressDialog progressDialog;
    private TextView txtNomeUsuario,txtEmailUsuario,txtSenhaUsuario;
    private Preferencias preferencias;
    private DatabaseReference firebabe;
    Uri uriImagemUsuario;
    public static final String FB_STORAGE_PATH = "image/";


    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Carregando...");
        progressDialog.show();
        preferencias = new Preferencias(getActivity());

        imgBackGroud = view.findViewById(R.id.imgBackPerfil);
        Button btnSalvar = view.findViewById(R.id.btnSalvar);
        txtNomeUsuario = view.findViewById(R.id.txtNomeUsuario);
        txtEmailUsuario = view.findViewById(R.id.txtEmailUsuario);
        txtSenhaUsuario = view.findViewById(R.id.txtSenhaUsuario);

        String identificadorUsuarioLogado = preferencias.getIdentificador();
        firebabe = ConfiguracaoFirebase.getFirebase()
                .child("usuarios")
                .child(identificadorUsuarioLogado);
        firebabe.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                txtNomeUsuario.setText(usuario.getNome());
                txtEmailUsuario.setText(usuario.getEmail());
                String url = usuario.getUrl().replace("*",".");
                if (!usuario.getUrl().equals("hue")){
                    Glide.with(getContext()).load(url).into(imgBackGroud);
                }else {
                    ProgressBar pb = view.findViewById(R.id.pbPerfil);
                    pb.setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        imgBackGroud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });

        /*txtMudarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trocarNome();
            }
        });*/

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Deseja salvar as alterações?");
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setTitle("Salvando dados...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        //alterarFoto();
                        alterarDadosUsuario();
                    }
                });
                alertDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.create();
                alertDialog.show();
            }
        });

        return view;
    }

    private void abrirGaleria(){
        Intent intent = new   Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        int code = 1;
        startActivityForResult(intent, code);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== RESULT_OK && requestCode== 1) {
            uriImagemUsuario = data.getData();
            imgBackGroud.setImageURI(uriImagemUsuario);
        }
    }
    private void trocarNome(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

        //Configurações do Dialog
        alertDialog.setTitle("Novo Nome");
        alertDialog.setMessage("Nome do usuário");
        alertDialog.setCancelable(false);

        final EditText editText = new EditText(getContext());
        editText.setText(txtNomeUsuario.getText());
        alertDialog.setView( editText );
        alertDialog.setPositiveButton("Mudar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editNome = editText.getText().toString();
                if( editNome.isEmpty() ){
                    Toast.makeText(getContext(), "Preencha o nome", Toast.LENGTH_LONG).show();
                }else{
                    txtNomeUsuario.setText(editNome);
                }
            }
        });
        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.create();
        alertDialog.show();
    }
    private void alterarFoto() {
            preferencias = new Preferencias(getActivity());
            String identificadorUsuarioLogado = preferencias.getIdentificador();
        StorageReference mStorageRef = ConfiguracaoFirebase.getEstorage()
                .child(FB_STORAGE_PATH + identificadorUsuarioLogado + "/" + "jpeg");
        mStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "Apagada com sucesso!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Erro ao apagar", Toast.LENGTH_LONG).show();
            }
        });

    }
    private void alterarDadosUsuario(){
        String nomeUsuario,emailUsuario,senhaUsuario;
        nomeUsuario = (txtNomeUsuario.getText().toString());
        emailUsuario = (txtEmailUsuario.getText().toString());
        senhaUsuario = (txtSenhaUsuario.getText().toString());
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("nome", nomeUsuario);
        userUpdates.put("email", emailUsuario);
        userUpdates.put("senha", senhaUsuario);
        firebabe.updateChildren(userUpdates);
        Toast.makeText(getContext(), "Auterado com sucesso", Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }
}
