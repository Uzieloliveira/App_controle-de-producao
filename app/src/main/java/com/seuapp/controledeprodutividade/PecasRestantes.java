package com.seuapp.controledeprodutividade;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@Keep
@IgnoreExtraProperties
@Entity(tableName = "pecas_restantes")
public class PecasRestantes {
    @PrimaryKey(autoGenerate = true)
    @Exclude
    public int id;

    public String data;
    public String usuarioRe;
    public int ligacao;
    public int solda;
    public int bandagem;
    public int amarracao;
    public int prenca;
    public int acabamento;

    public PecasRestantes() {
    }

    public PecasRestantes(String data, String usuarioRe, int ligacao, int solda, int bandagem, int amarracao, int prenca, int acabamento) {
        this.data = data;
        this.usuarioRe = usuarioRe;
        this.ligacao = ligacao;
        this.solda = solda;
        this.bandagem = bandagem;
        this.amarracao = amarracao;
        this.prenca = prenca;
        this.acabamento = acabamento;
    }
}
