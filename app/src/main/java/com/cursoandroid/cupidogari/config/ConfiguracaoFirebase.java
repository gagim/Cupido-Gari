package com.cursoandroid.cupidogari.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {

    private static DatabaseReference referenceDatabase;
    private static FirebaseAuth autenticacao;
    private static StorageReference estorage;


    public  static DatabaseReference getFirebase(){

        if( referenceDatabase == null){
            referenceDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return referenceDatabase;
    }

    public static FirebaseAuth getFirebaseAutenticacao(){
        if ( autenticacao == null){
            autenticacao = FirebaseAuth.getInstance();
        }
        return autenticacao;
    }

    public static StorageReference getEstorage(){
        if ( estorage == null){
            estorage = FirebaseStorage.getInstance().getReference();
        }
        return estorage;
    }
}
