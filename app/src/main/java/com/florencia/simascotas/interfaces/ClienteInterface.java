package com.florencia.simascotas.interfaces;

import com.florencia.simascotas.models.Cliente;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ClienteInterface {
    @FormUrlEncoded
    @POST("loadclientes")
    Call<JsonObject> LoadCliente(@Field("usuario") String user, @Field("clave") String clave,
                                  @Body List<Cliente> clientes);

    @POST("loadganaderos")
    Call<JsonObject> LoadCliente2(@Body Map<String,Object> clientes);
}
