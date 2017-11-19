package com.cursoandroid.cupidogari.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class Preferencias {

    private Context contexto;
    private SharedPreferences sharedPreferences;
    private String NOME_ARQUIVO = "cupido_gari.preferencias";
    private int MODE = 0;
    private SharedPreferences.Editor editor;

    private String CHAVE_IDENTIFICADOR = "identificadorUsuarioLogado";
    private String CHAVE_NOME = "nomeUsuarioLogado";
    private String CHAVE_URL = "urlUsuarioLogado";

    public Preferencias(Context contextoParametro) {

        contexto = contextoParametro;
        sharedPreferences = contexto.getSharedPreferences(NOME_ARQUIVO, MODE);
        editor = sharedPreferences.edit();

    }

    public void salvarDados(String identificadorUsuario,String nomeUsuario,String urlUsuario) {

        editor.putString(CHAVE_NOME, nomeUsuario);
        editor.putString(CHAVE_URL,urlUsuario);
        editor.putString(CHAVE_IDENTIFICADOR, identificadorUsuario);
        editor.commit();

    }

    public String getIdentificador(){
        return sharedPreferences.getString(CHAVE_IDENTIFICADOR,null);
    }
    public String getNome(){
        return sharedPreferences.getString(CHAVE_NOME,null);
    }
    public String getUrl(){
        return sharedPreferences.getString(CHAVE_URL,null);
    }
}
