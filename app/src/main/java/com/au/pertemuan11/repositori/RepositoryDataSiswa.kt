package com.au.pertemuan11.repositori

import com.au.pertemuan11.apiservice.ServiceApiSiswa
import com.au.pertemuan11.modeldata.DataSiswa
import com.au.pertemuan11.modeldata.DataSiswaRequest
import com.au.pertemuan11.modeldata.InsertResponse
import retrofit2.Response

interface RepositoryDataSiswa {
    suspend fun getDataSiswa(): List<DataSiswa>

    suspend fun postDataSiswa(
        dataSiswa: DataSiswaRequest
    ): Response<InsertResponse>
}

class JaringanRepositoryDataSiswa(
    private val serviceApiSiswa: ServiceApiSiswa
) : RepositoryDataSiswa {
    override suspend fun getDataSiswa(): List<DataSiswa> =
        serviceApiSiswa.getSiswa()

    override suspend fun postDataSiswa(
        dataSiswa: DataSiswaRequest
    ): Response<InsertResponse> =
        serviceApiSiswa.postSiswa(dataSiswa)
}
