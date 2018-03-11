package com.cursoandroid.cupidogari.fragment;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class PerfilFragment extends Fragment {

    private ImageView imgUsuario;
    private Button btnSalvar;
    private StorageReference mStorageRef;
    private TextView txtNomeUsuario,txtMudarNome,txtMudarFoto;
    private int Code = 1;
    private Preferencias preferencias;
    private DatabaseReference firebabe;
    Uri uriImagemUsuario;
    public static final String FB_STORAGE_PATH = "image/";


    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);



        preferencias = new Preferencias(getActivity());
        String identificadorUsuarioLogado = preferencias.getIdentificador();
        firebabe = ConfiguracaoFirebase.getFirebase()
                .child("usuarios")
                .child(identificadorUsuarioLogado);
        firebabe.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              Usuario usuario = dataSnapshot.getValue(Usuario.class);
                txtNomeUsuario.setText(usuario.getNome());
                Glide.with(getContext()).load(usuario.getUrl()).into(imgUsuario);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        imgUsuario = (ImageView) view.findViewById(R.id.img);
        btnSalvar = (Button) view.findViewById(R.id.btnSalvar);
        txtNomeUsuario = (TextView) view.findViewById(R.id.txtNomeUsuario);
        txtMudarFoto = (TextView) view.findViewById(R.id.txtMudarFoto);
        txtMudarNome = (TextView) view.findViewById(R.id.txtMudarNome);

        txtMudarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });

        txtMudarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trocarNome();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Deseja salvar as alterações?");
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alterarFoto();
                        alterarNomeUsuario();
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
    public String getImageExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private ContentResolver getContentResolver() {
        this.getContext();
        return getContentResolver();
    }
    private void abrirGaleria(){
        Intent intent = new   Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Code);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== RESULT_OK && requestCode== 1) {
            uriImagemUsuario = data.getData();
            imgUsuario.setImageURI(uriImagemUsuario);
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
            mStorageRef = ConfiguracaoFirebase.getEstorage()
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
    private void alterarNomeUsuario(){
        String nomeUsuario = (txtNomeUsuario.getText().toString());
        Map<String, Object> userUpdates = new HashMap<String, Object>();
        userUpdates.put("nome", nomeUsuario);
        firebabe.updateChildren(userUpdates);
    }
}
