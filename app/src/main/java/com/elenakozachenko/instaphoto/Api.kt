package com.elenakozachenko.instaphoto

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class Photo (
        val id: Long,
        val author: String,
        val image: String,
        val likes: List<String>
)

interface Api{
    @GET("photos")
    fun getPhotos() : Call<MutableList<Photo>>

    @POST("photos")
    fun addPhoto(@Body body: RequestBody) : Call<Photo>

    @POST("photos/{idPhoto}/toggle_like")
    @FormUrlEncoded
    fun likePhoto(@Path("idPhoto") id: Long, @Field("nick") nickname: String) : Call<Photo>
}

val api = Retrofit.Builder()
        .baseUrl("https://skillbox.trinitydigital.ru/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(Api::class.java)
