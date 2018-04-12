package com.cursoandroid.cupidogari.cupidogari;

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
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cursoandroid.cupidogari.config.ConfiguracaoFirebase;
import com.cursoandroid.cupidogari.helper.Base64Custom;
import com.cursoandroid.cupidogari.helper.Preferencias;
import com.cursoandroid.cupidogari.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Cadastro_Activity extends AppCompatActivity {

    private EditText editSenha,editNome,editEmail;

    private ImageView imageUsuario;

    private Button btnSalvar;

    private Usuario usuario;

    private FirebaseAuth autenticacao;

    private StorageReference mStorageRef;

    public static final String FB_STORAGE_PATH = "image/";

    private int Code = 1;

    Uri uriImagemUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_);

        editEmail = (EditText) findViewById(R.id.editEmail);
        editNome = (EditText) findViewById(R.id.editNome);
        editSenha = (EditText) findViewById(R.id.editSenha);

        imageUsuario = (ImageView) findViewById(R.id.img);

        btnSalvar = (Button) findViewById(R.id.btnSalvar);

        imageUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                salvarDados();
            }
        });

    }

    private void cadastrarUsuario() {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
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

                    Intent salvar = new Intent(Cadastro_Activity.this, Login_Activity.class);
                    startActivity(salvar);
                    finish();

                } else {

                    String erroExecucao = "";

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        erroExecucao = "Digite uma senha mais forte,contendo mais caracteres,letras ou números";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erroExecucao = "O e-mail digitado é inválido,digite um novo e-mail";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erroExecucao = "O e-mail digitado é já existe,por favor digite outro";
                    } catch (Exception e) {
                        erroExecucao = "Erro ao cadastrar usuário";
                        e.printStackTrace();
                    }

                    Toast.makeText(Cadastro_Activity.this, "Erro: " + erroExecucao, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public String getImageExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void abrirGaleria(){
        Intent intent = new   Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Code);
    }

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
            imageUsuario.setImageBitmap(thumbnail);
        }
    }

    @SuppressWarnings("VisibleForTests")
    private void salvarDados(){
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Salvando Dados...");
            dialog.setCancelable(false);
            dialog.show();

            Preferencias preferencias = new Preferencias(Cadastro_Activity.this);
            String identificadorUsuarioLogado = preferencias.getIdentificador();

            mStorageRef = ConfiguracaoFirebase.getEstorage()
                    .child(FB_STORAGE_PATH + identificadorUsuarioLogado + "/" + getImageExt(uriImagemUsuario));


            //Add file to reference

            mStorageRef.putFile(uriImagemUsuario).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    //Dimiss dialog when success
                    dialog.dismiss();
                    //Display success toast msg
                    Toast.makeText(getApplicationContext(), "Sucesso ao se cadastrar!", Toast.LENGTH_SHORT).show();
                    usuario = new Usuario();
                    usuario.setNome(editNome.getText().toString() );
                    usuario.setEmail(editEmail.getText().toString());
                    usuario.setSenha(editSenha.getText().toString());
                    usuario.setUrl(taskSnapshot.getDownloadUrl().toString());
                    cadastrarUsuario();


                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //Dimiss dialog when error
                            dialog.dismiss();
                            //Display err toast msg
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            //Show upload progress

                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            dialog.setMessage("Salvando Dados " + (int) progress + "%");
                        }
                    });

        }


    public void btnSalvar(View view) {
    }
}
