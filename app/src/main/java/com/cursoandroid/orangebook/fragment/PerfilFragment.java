package com.cursoandroid.orangebook.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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
import com.cursoandroid.orangebook.config.ConfiguracaoFirebase;
import com.cursoandroid.orangebook.cupidogari.MainActivity;
import com.cursoandroid.orangebook.cupidogari.R;
import com.cursoandroid.orangebook.helper.Preferencias;
import com.cursoandroid.orangebook.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static android.app.Activity.RESULT_OK;

public class PerfilFragment extends Fragment {

    private ImageView imgBackGroud;
    private TextView txtNomeUsuario,txtAtt;
    private Preferencias preferencias;
    private DatabaseReference firebabe;
    private ProgressDialog progressDialog;
    Uri uriImagemUsuario;
    private String Url,dataConcatenada;


    public PerfilFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Carregando...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        preferencias = new Preferencias(getActivity());

        imgBackGroud = view.findViewById(R.id.imgBackPerfil);
        Button btnSalvar = view.findViewById(R.id.btnSalvar);

        txtNomeUsuario = view.findViewById(R.id.txtNomeUsuario);
        txtAtt = view.findViewById(R.id.txtAtt);

        String identificadorUsuarioLogado = preferencias.getIdentificador();
        firebabe = ConfiguracaoFirebase.getFirebase()
                .child("usuarios")
                .child(identificadorUsuarioLogado);
        firebabe.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NewApi", "SetTextI18n"})
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                txtNomeUsuario.setText(usuario.getNome());
                txtAtt.setText("Ultima Atualização: "+usuario.getAtt());
                Url = usuario.getUrl().replace("*",".");
                if (!usuario.getUrl().equals("hue") && !usuario.getUrl().isEmpty()){
                    Glide.with(getContext()).load(Url).into(imgBackGroud);
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
                        progressDialog.setMessage("Salvando dados 0%");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        verificacao();
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

        getDate();

        return view;
    }

    private void verificacao(){
        if (uriImagemUsuario == null){
            alterarDadosUsuario();
        }else {
            alterarFoto();
        }
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

    public String getImageExt(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void alterarFoto() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String urlMake = preferencias.getUrl().replace("*",".");
        if(urlMake.equals("hue")){
            String identificadorUsuarioLogado = preferencias.getIdentificador();
            StorageReference mStorageRef = ConfiguracaoFirebase.getEstorage()
                    .child(identificadorUsuarioLogado + "." + getImageExt(uriImagemUsuario));

            mStorageRef.putFile(uriImagemUsuario).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Url = taskSnapshot.getDownloadUrl().toString().replace(".", "*");
                    alterarDadosUsuario();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Error Aualizar: ", e.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Salvando Dados " + (int) progress + "%");
                        }
                    });
        }else {
            StorageReference mStorageRef = storage.getReferenceFromUrl(urlMake);
            mStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    String identificadorUsuarioLogado = preferencias.getIdentificador();
                    StorageReference mStorageRef = ConfiguracaoFirebase.getEstorage()
                            .child(identificadorUsuarioLogado + "." + getImageExt(uriImagemUsuario));

                    mStorageRef.putFile(uriImagemUsuario).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Url = taskSnapshot.getDownloadUrl().toString().replace(".", "*");
                            alterarDadosUsuario();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Error Aualizar: ", e.getMessage());
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    progressDialog.setMessage("Salvando Dados " + (int) progress + "%");
                                }
                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Erro ao apagar", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }

    }

    private void getDate(){
        Date date = new Date();
        int hora = date.getHours();
        int min  = date.getMinutes();
        int dia = date.getDate();
        int mes = date.getMonth()+1;
        if(date.getMinutes() < 10) {
            String minString = "0" + date.getMinutes();
            dataConcatenada = dia +"/"+ mes + " as " + hora+":"+minString;
            //Toast.makeText(getActivity(), dataConcatenada, Toast.LENGTH_SHORT).show();
        }else {
            dataConcatenada = dia +"/"+ mes + " as " + hora+":"+min;
            //Toast.makeText(getActivity(), dataConcatenada, Toast.LENGTH_SHORT).show();
        }
    }

    private void alterarDadosUsuario(){
        String nomeUsuario;
        nomeUsuario = (txtNomeUsuario.getText().toString());
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("nome", nomeUsuario);
        userUpdates.put("att",dataConcatenada);
        userUpdates.put("url",Url);
        firebabe.updateChildren(userUpdates);
        Toast.makeText(getContext(), "Alterado com sucesso", Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }
}
