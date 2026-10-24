package com.seuapp.controledeprodutividade;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    // Exemplo de POST para salvar no servidor
    @POST("producao")
    Call<Producao> salvarProducao(@Body Producao producao);

    // Exemplo de GET para buscar do servidor
    @GET("producao")
    Call<List<Producao>> listarProducao();
}
