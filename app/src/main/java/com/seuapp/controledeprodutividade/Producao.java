package com.seuapp.controledeprodutividade;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@Entity(tableName = "producao")
@IgnoreExtraProperties
public class Producao {
    @PrimaryKey(autoGenerate = true)
    @Exclude
    public int id;
    
    public String operador;
    public int quantidade;
    public String data;
    public String secao;
    public String anotacao;
    public String usuarioRe;
    public String departamento; // Novo campo
    public boolean enviado;

    public Producao() {
    }

    public Producao(String operador, int quantidade, String data, String secao, String anotacao, String usuarioRe, String departamento) {
        this.operador = operador;
        this.quantidade = quantidade;
        this.data = data;
        this.secao = secao;
        this.anotacao = anotacao;
        this.usuarioRe = usuarioRe;
        this.departamento = departamento;
        this.enviado = false;
    }
}
