package com.cursoandroid.cupidogari.cupidogari;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.cursoandroid.cupidogari.config.ConfiguracaoFirebase;
import com.cursoandroid.cupidogari.helper.Base64Custom;
import com.cursoandroid.cupidogari.helper.Preferencias;
import com.cursoandroid.cupidogari.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Login_Activity extends AppCompatActivity {

    private EditText editNome, editSenha;

    private Button btnLogar;

    private ProgressDialog progressDialog;

    private Usuario usuario;
    private FirebaseAuth autenticacao;
    private ValueEventListener valueEventListener;
    private DatabaseReference databaseReference;
    private String identificadorUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);

        progressDialog = new ProgressDialog(this);

        verificarUsuarioLogado();

        editNome = (EditText) findViewById(R.id.editEmail);
        editSenha = (EditText) findViewById(R.id.editSenha);

        btnLogar = (Button) findViewById(R.id.btnLogar);

        btnLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editNome == null || editSenha == null) {
                    Toast.makeText(Login_Activity.this,"Nenhum campo pode está vazio",Toast.LENGTH_LONG).show();
                }else{
                    usuario = new Usuario();
                    usuario.setEmail(editNome.getText().toString());
                    usuario.setSenha(editSenha.getText().toString());
                    validarLogin();
                }
            }
        });
    }

    private void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if( autenticacao.getCurrentUser() != null ){
            progressDialog.setMessage("Logando...");
            progressDialog.show();
            abrirTelaPrincipal();
            progressDialog.dismiss();
        }
    }

    private void validarLogin(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                 if( task.isSuccessful() ){
                     identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());

                     databaseReference = ConfiguracaoFirebase.getFirebase()
                             .child("usuarios")
                             .child(identificadorUsuario);

                     valueEventListener = new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot dataSnapshot) {

                             Usuario usuarioRecuperado = dataSnapshot.getValue(Usuario.class);

                             Preferencias preferencias = new Preferencias(Login_Activity.this);
                             preferencias.salvarDados( identificadorUsuario,usuarioRecuperado.getNome(),usuarioRecuperado.getUrl() );
                         }

                         @Override
                         public void onCancelled(DatabaseError databaseError) {

                         }
                     };
                     databaseReference.addListenerForSingleValueEvent(valueEventListener);


                    abrirTelaPrincipal();
                }else{
                    Toast.makeText(Login_Activity.this, "Erro ao fazer login!", Toast.LENGTH_LONG ).show();
            }
        }

    });
    }

    private void abrirTelaPrincipal(){
        Intent telaPrincipal = new Intent(Login_Activity.this,MainActivity.class);
        startActivity(telaPrincipal);
        finish();
    }

    public void cadastro(View view){
        Intent cadastro = new Intent(Login_Activity.this,Cadastro_Activity.class);
        startActivity(cadastro);
        onStop();
    }
}