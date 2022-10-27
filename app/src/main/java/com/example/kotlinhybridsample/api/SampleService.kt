package com.example.kotlinhybridsample.api

import com.example.kotlinhybridsample.api.data.sample.SampleUser
import com.example.kotlinhybridsample.api.data.sample.SampleUserList
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface SampleService {


    /**
     * Sample
     * @throws Exception
     */
    @GET("/api/unknown")
    fun doGetListResouces(): Observable<ResponseBody>

    /**
     * Sample
     * @throws Exception
     */
    @POST("/api/users")
    fun createUser(@Body user: SampleUser): Observable<ResponseBody>

    /**
     * Sample
     * @throws Exception
     */
    @GET("/api/users?")
    fun doGetUserList(@Query("page") page:String): Observable<SampleUserList>

    /**
     * Sample
     * @throws Exception
     */
    @GET("/api/users?")
    fun doGetUserListForJsonObject(@Query("page") page:String): Observable<ResponseBody>

    /**
     * Sample
     * @throws Exception
     */
    @GET("/api/users?")
    fun doCreateUserWithField(@Query("name") name:String, @Query("job") job:String): Observable<ResponseBody>
}