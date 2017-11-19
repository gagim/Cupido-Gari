package com.cursoandroid.cupidogari.model;

/**
 * Created by Henrique on 28/04/2017.
 */

public class Notas {

    public Notas() {

    }

    private String id;
    private String nome;
    private String email;
    private String url;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
