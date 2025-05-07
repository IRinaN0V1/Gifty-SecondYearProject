package com.example.gifty

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {
    @FormUrlEncoded
    @POST("Api.php?apicall=getuser")
    suspend fun getUser(@Field("email") email: String, @Field("password") password: String): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=getuserbyemail")
    suspend fun getUserByEmail(@Field("email") email: String): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=createuser")
    suspend fun createUser(@Field("email") email: String, @Field("password") password: String): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=getformbyuseridandname")
    suspend fun getFormByUserIdAndName(@Field("name") name: String, @Field("user") user_id: Int): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=createform")
    suspend fun createForm(@Field("name") name: String, @Field("image") image: String
                           , @Field("birthday_date") birthday_date: String, @Field("user") user_id: Int): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=createevent")
    suspend fun createEvent( @Field("user") user: Int, @Field("name") name: String, @Field("reminder_time") reminder_time: String
                           , @Field("description") description: String, @Field("event_date") event_date: String,): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=getformsbyuserid")
    suspend fun getFormsByUserId(@Field("user") user_id: Int): Response<JsonObject>

    @GET("Api.php?apicall=deleteform")
    suspend fun deleteForm(
        @Query("id") name: Int
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=updateform")
    suspend fun updateForm(
        @Field("id") id: Int,
        @Field("name") name: String,
        @Field("birthday_date") birthday_date: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=geteventsbyuserid")
    suspend fun getEventsByUserId(@Field("user") user_id: Int): Response<JsonObject>

    @GET("Api.php?apicall=deleteevent")
    suspend fun deleteEvent(
        @Query("id") id: Int
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=updateevent")
    suspend fun updateEvent(
        @Field("id") id: Int,
        @Field("name") name: String,
        @Field("reminder_time") reminder_time: String,
        @Field("description") description: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=geteventbyid")
    suspend fun getEventById(@Field("id") id: Int): Response<JsonObject>

    @GET("Api.php?apicall=getgifts")
    suspend fun getGifts(): Response<JsonObject>

    @GET("Api.php?apicall=gethobbies")
    suspend fun getHobbies(): Response<JsonObject>

    @GET("Api.php?apicall=getholidays")
    suspend fun getHolidays(): Response<JsonObject>

    @GET("Api.php?apicall=getprofessions")
    suspend fun getProfessions(): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=addgifttoform")
    suspend fun addGiftToForm( @Field("gift") gift: Int, @Field("form") form: Int): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=getselectedgiftbygiftidandformid")
    suspend fun getSelectedGiftByGiftIdAndFormId(@Field("gift") gift: Int, @Field("form") form: Int): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=getselectedgifts")
    suspend fun getSelectedGifts(@Field("form") form: Int): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=deleteselectedgifts")
    suspend fun deleteSelectedGifts(
        @Field("form") formId: Int,
        @Field("gift") giftIds: String
    ):  Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=getformbyid")
    suspend fun getFormById(@Field("id") id: Int): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=getagecategorybyage")
    suspend fun getAgeCategoryByAge(@Field("age") age: Int): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=getfoundgifts")
    suspend fun getFoundGifts(
        @Field("genderId") genderId: Int,
        @Field("ageCategoryId") ageCategoryId: Int,
        @Field("hobbies") hobbies: String,
        @Field("professions") professions: String,
        @Field("holidays") holidays: String
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=getgiftbyid")
    suspend fun getGiftById(@Field("id") id: Int): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=deleteuser")
    suspend fun deleteUser(
        @Field("id") id: Int
    ): Response<JsonObject>

    @FormUrlEncoded
    @POST("Api.php?apicall=updateuserpassword")
    suspend fun updateUserPassword(
        @Field("id") id: Int,
        @Field("password") password: String
    ): Response<JsonObject>
}

