package com.seuapp.controledeprodutividade;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface UsuarioDao {
    @Insert
    void inserir(Usuario usuario);

    @Query("SELECT * FROM usuarios ORDER BY nome ASC")
    List<Usuario> listarTodos();

    @Query("SELECT * FROM usuarios WHERE re = :re LIMIT 1")
    Usuario buscarPorRE(String re);
}
