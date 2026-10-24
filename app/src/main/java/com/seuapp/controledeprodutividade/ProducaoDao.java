package com.seuapp.controledeprodutividade;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ProducaoDao {
    @Insert
    void inserir(Producao producao);

    @Update
    void atualizar(Producao producao);

    @Query("SELECT * FROM producao WHERE usuarioRe = :re ORDER BY id DESC")
    List<Producao> listarTodasPorUsuario(String re);

    @Query("SELECT * FROM producao WHERE enviado = 0 AND usuarioRe = :re ORDER BY id DESC")
    List<Producao> listarNaoEnviadasPorUsuario(String re);

    @Query("SELECT * FROM producao WHERE data = :dataFiltro AND usuarioRe = :re")
    List<Producao> listarPorDataEUsuario(String dataFiltro, String re);
    
    @Query("SELECT * FROM producao WHERE operador = :nome AND data = :data AND secao = :secao AND enviado = 0 AND usuarioRe = :re LIMIT 1")
    Producao buscarExistente(String nome, String data, String secao, String re);

    @Query("UPDATE producao SET enviado = 1 WHERE enviado = 0 AND usuarioRe = :re")
    void marcarTodasComoEnviadasPorUsuario(String re);
}
