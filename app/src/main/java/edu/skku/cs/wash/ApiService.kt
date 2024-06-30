package edu.skku.cs.wash

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/register")
    fun registerUser(@Body userData: User): Call<ApiResponse>

    @POST("/login")
    fun loginUser(@Body userData: User): Call<ApiResponse>

    @GET("/washers")
    fun getWashers(): Call<List<Washer>>

    @GET("/washers1")
    fun getWashersDorm1(): Call<List<Washer>>

    @GET("/washers2")
    fun getWashersDorm2(): Call<List<Washer>>

    @GET("/washers3")
    fun getWashersDorm3(): Call<List<Washer>>

    @GET("/washers4")
    fun getWashersDorm4(): Call<List<Washer>>

    @POST("/washerstatus/{id}")
    fun updateWasherStatus(
        @Path("id") id: Int,
        @Body timeSet: TimeSet
    ): Call<WasherStatusResponse>

    @POST("/checkUserId")
    fun checkUserId(@Body request: UserIdCheckRequest): Call<UserIdCheckResponse>

    @GET("/userdetails/{userid}")
    fun getUserDetails(@Path("userid") userid: String): Call<User>

    @POST("/washerend/{id}")
    fun endWasherSession(
        @Path("id") id: Int
    ): Call<ApiResponse>

    @GET("/washerReservations/{washerId}")
    fun getWasherReservations(@Path("washerId") washerId: Int): Call<List<String>>

    @POST("/reserveWasher")
    fun reserveWasher(@Body reservationRequest: ReservationRequest): Call<ApiResponse>

    @GET("/washersByUser/{userid}")
    fun getWashersByUser(@Path("userid") userid: String): Call<List<Washer>>

    @POST("/cancelWasherReservation")
    fun cancelWasherReservation(@Body userId: UserId): Call<ApiResponse>

    @POST("/updateUser")
    fun updateUser(@Body updateRequest: UserUpdateRequest): Call<ApiResponse>

    @GET("/washerReservationsByUser/{userid}")
    fun getWasherReservationsByUser(@Path("userid") userid: String): Call<List<Washer>>

    @GET("/usernamesByWasher/{washerId}")
    fun getUsernamesByWasher(@Path("washerId") washerId: Int): Call<List<String>>

    @POST("/resetWashers")
    fun resetWashers(): Call<ApiResponse>
}

data class User(
    val userid: String,
    val pw: String,
    val username: String,
    val dormitory: String,
    val gender: String,
    val image: String?
)
data class ApiResponse(val message: Boolean, val UID: Int = -1)

data class Washer(
    val id: Int,
    val washername: String,
    var washerstatus: String,
    val dorm: String,
    val starttime: Long,
    val settime: Long
)

data class TimeSet(
    val starttime: Long,
    val settime: Long,
    val userid: String
)

data class WasherStatusResponse(val washerstatus: String)

data class UserIdCheckRequest(val userid: String)
data class UserIdCheckResponse(val isAvailable: Boolean)

data class ReservationRequest(val userid: String, val washerId: Int)

data class UserId(val userid: String)

data class UserUpdateRequest(
    val userid: String,
    val currentPassword: String,
    val newPassword: String,
    val username: String,
    val dormitory: String,
    val gender: String,
    val image: String?
)
