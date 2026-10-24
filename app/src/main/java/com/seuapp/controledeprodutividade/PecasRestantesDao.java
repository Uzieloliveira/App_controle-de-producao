package com.seuapp.controledeprodutividade;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PecasRestantesDao {
    @Insert
    void inserir(PecasRestantes pecas);

    @Query("SELECT * FROM pecas_restantes WHERE usuarioRe = :re ORDER BY id DESC")
    List<PecasRestantes> listarPorUsuario(String re);
}
