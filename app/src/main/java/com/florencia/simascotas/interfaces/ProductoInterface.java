package com.florencia.simascotas.interfaces;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ProductoInterface {
    @FormUrlEncoded
    @POST("wsproducto")
    Call<JsonObject> GetProductos(@Field("usuario") String user, @Field("clave") String clave,
                                   @Field("establecimientoid") Integer establecimiento);
}
