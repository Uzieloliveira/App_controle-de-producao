package com.seuapp.controledeprodutividade;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@Keep
@IgnoreExtraProperties
@Entity(tableName = "usuarios")
public class Usuario {
    
    @PrimaryKey(autoGenerate = true)
    @Exclude
    public int id;
    
    public String nome;
    public String re;
    public String departamento;
    public String lastDeviceId; // Controle de sessão única

    // CONSTRUTOR VAZIO PÚBLICO E EXPLÍCITO - OBRIGATÓRIO PARA O FIREBASE
    public Usuario() {
    }

    public Usuario(String nome, String re, String departamento) {
        this.nome = nome;
        this.re = re;
        this.departamento = departamento;
    }

    @Override
    public String toString() {
        return nome + " (RE: " + re + ") - " + departamento;
    }
}
