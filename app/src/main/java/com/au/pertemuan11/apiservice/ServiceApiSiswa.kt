package com.au.pertemuan11.apiservice

import com.au.pertemuan11.modeldata.DataSiswa
import com.au.pertemuan11.modeldata.DataSiswaRequest
import com.au.pertemuan11.modeldata.InsertResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ServiceApiSiswa {
    @GET("bacateman.php")
    suspend fun getSiswa(): List<DataSiswa>

    @POST("insertTM.php")
    suspend fun postSiswa(
        @Body dataSiswa: DataSiswaRequest
    ): Response<InsertResponse>
}
