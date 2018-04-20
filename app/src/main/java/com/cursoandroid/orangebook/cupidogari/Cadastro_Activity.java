package com.cursoandroid.orangebook.cupidogari;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cursoandroid.orangebook.config.ConfiguracaoFirebase;
import com.cursoandroid.orangebook.helper.Base64Custom;
import com.cursoandroid.orangebook.helper.Preferencias;
import com.cursoandroid.orangebook.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Cadastro_Activity extends AppCompatActivity {

    private EditText editSenha,editNome,editEmail;

    private ImageView imgBackGround;

    private ProgressDialog dialog;

    private Usuario usuario;

    Uri uriImagemUsuario;

    private String dataConcatenada;

    private DatabaseReference firebabe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_);

        editEmail = findViewById(R.id.editEmail);
        editNome = findViewById(R.id.editNome);
        editSenha = findViewById(R.id.editSenha);

        imgBackGround = findViewById(R.id.imgBackCadastro);

        Button btnSalvar = findViewById(R.id.btnSalvar);

        imgBackGround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });


        btnSalvar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email,nome,senha;
                email = editEmail.getText().toString();
                nome = editNome.getText().toString();
                senha = editSenha.getText().toString();
                if (email.isEmpty() || nome.isEmpty() || senha.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Nenhum campo pode está vazio!", Toast.LENGTH_SHORT).show();
                }else {
                    if (uriImagemUsuario == null){
                        cadastrarUsuarioSemFoto();
                    }else {
                        cadastrarUsuarioComFoto();
                    }
                }
            }
        });

        getDate();

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

    private void cadastrarUsuarioSemFoto() {

        dialog = new ProgressDialog(this);
        dialog.setTitle("Salvando Dados...");
        dialog.setCancelable(false);
        dialog.show();

        usuario = new Usuario();
        usuario.setNome(editNome.getText().toString() );
        usuario.setEmail(editEmail.getText().toString());
        usuario.setSenha(editSenha.getText().toString());
        usuario.setAtt(dataConcatenada);
        usuario.setUrl("hue");

        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(Cadastro_Activity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setId(identificadorUsuario);
                    usuario.Salvar();

                    Preferencias preferencias = new Preferencias(Cadastro_Activity.this);
                    preferencias.salvarDados(identificadorUsuario,usuario.getNome(),usuario.getUrl());

                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Sucesso ao se cadastrar!", Toast.LENGTH_SHORT).show();

                    Intent salvar = new Intent(Cadastro_Activity.this, Login_Activity.class);
                    startActivity(salvar);
                    finish();

                } else {

                    String erroExecucao;

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        erroExecucao = "Digite uma senha mais forte,contendo mais caracteres,letras ou números";
                        limparDados(2);
                        dimiss();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erroExecucao = "O e-mail digitado é inválido,digite um novo e-mail";
                        limparDados(1);
                        dimiss();
                    } catch (FirebaseAuthUserCollisionException e) {
                        erroExecucao = "O e-mail digitado é já existe,por favor digite outro";
                        limparDados(1);
                        dimiss();
                    } catch (Exception e) {
                        erroExecucao = "Erro ao cadastrar usuário";
                        limparDados(3);
                        dimiss();
                    }

                    Toast.makeText(Cadastro_Activity.this, "Erro: " + erroExecucao, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void limparDados(int num){
        switch (num){
            case 1:
                editEmail.setText("");
                break;
            case 2:
                editSenha.setText("");
                break;
                default:
                    editEmail.setText("");
                    editNome.setText("");
                    editSenha.setText("");
        }

    }

    public String getImageExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void abrirGaleria(){
        Intent intent = new   Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        int code = 1;
        startActivityForResult(intent, code);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== RESULT_OK && requestCode== 1) {
            uriImagemUsuario = data.getData();
            String[] filePath= { MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(uriImagemUsuario,filePath, null, null, null);
            c.moveToFirst();
            int columnIndex= c.getColumnIndex(filePath[0]);
            String picturePath= c.getString(columnIndex);c.close();
            Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
            imgBackGround.setImageBitmap(thumbnail);
            imgBackGround.setImageURI(uriImagemUsuario);
            TextView retirar = findViewById(R.id.retirar);
            retirar.setText("Mudar Foto");
        }
    }

    private void dimiss(){
        dialog.dismiss();
    }

    @SuppressWarnings("VisibleForTests")
    private void cadastrarUsuarioComFoto() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Salvando Dados...");
        dialog.setMessage("Salvando Dados 0%");
        dialog.setCancelable(false);
        dialog.show();

        usuario = new Usuario();
        usuario.setNome(editNome.getText().toString());
        usuario.setEmail(editEmail.getText().toString());
        usuario.setSenha(editSenha.getText().toString());
        usuario.setAtt(dataConcatenada);
        usuario.setUrl("hue");

        String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
        usuario.setId(identificadorUsuario);
        usuario.Salvar();

        Preferencias preferencias = new Preferencias(Cadastro_Activity.this);
        preferencias.salvarDados(identificadorUsuario, usuario.getNome(), usuario.getUrl());

        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(Cadastro_Activity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    Preferencias preferencias = new Preferencias(Cadastro_Activity.this);
                    final String identificadorUsuarioLogado = preferencias.getIdentificador();

                    final StorageReference mStorageRef = ConfiguracaoFirebase.getEstorage()
                            .child(identificadorUsuarioLogado + "." + getImageExt(uriImagemUsuario));

                    mStorageRef.putFile(uriImagemUsuario).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            firebabe = ConfiguracaoFirebase.getFirebase()
                                    .child("usuarios")
                                    .child(identificadorUsuarioLogado);

                            String nome,email;
                            nome = editNome.getText().toString();
                            email = editEmail.getText().toString();
                            String url = taskSnapshot.getDownloadUrl().toString().replace(".","*");
                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("nome", nome);
                            userUpdates.put("email", email);
                            userUpdates.put("att",dataConcatenada);
                            userUpdates.put("url",url);
                            firebabe.updateChildren(userUpdates);

                            usuario.setUrl(url);

                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Sucesso ao se cadastrar!", Toast.LENGTH_SHORT).show();

                            Intent salvar = new Intent(Cadastro_Activity.this, Login_Activity.class);
                            startActivity(salvar);
                            finish();

                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Error cadastro: ", e.getMessage());
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    dialog.setMessage("Salvando Dados " + (int) progress + "%");
                                }
                            });


                } else {

                    String erroExecucao;

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        erroExecucao = "Digite uma senha mais forte,contendo mais caracteres,letras ou números";
                        limparDados(2);
                        dialog.dismiss();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erroExecucao = "O e-mail digitado é inválido,digite um novo e-mail";
                        limparDados(1);
                        dialog.dismiss();
                    } catch (FirebaseAuthUserCollisionException e) {
                        erroExecucao = "O e-mail digitado é já existe,por favor digite outro";
                        limparDados(1);
                        dialog.dismiss();
                    } catch (Exception e) {
                        erroExecucao = "Erro ao cadastrar usuário";
                        limparDados(3);
                        dialog.dismiss();
                    }

                    Toast.makeText(Cadastro_Activity.this, "Erro: " + erroExecucao, Toast.LENGTH_LONG).show();
                }
            }
        });

    }


}
