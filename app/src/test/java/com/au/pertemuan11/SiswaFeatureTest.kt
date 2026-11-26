package com.au.pertemuan11

import com.au.pertemuan11.apiservice.ServiceApiSiswa
import com.au.pertemuan11.modeldata.DataSiswa
import com.au.pertemuan11.modeldata.DataSiswaRequest
import com.au.pertemuan11.modeldata.InsertResponse
import com.au.pertemuan11.repositori.JaringanRepositoryDataSiswa
import com.au.pertemuan11.uicontroller.viewmodel.DetailSiswa
import com.au.pertemuan11.uicontroller.viewmodel.EntryViewModel
import com.au.pertemuan11.uicontroller.viewmodel.toRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class SiswaFeatureTest {

    private class FakeServiceApi(
        private val siswaList: List<DataSiswa> = emptyList(),
        private val postResult: Response<InsertResponse> = Response.success(
            InsertResponse(message = "Data berhasil ditambahkan")
        )
    ) : ServiceApiSiswa {
        val sentRequests = mutableListOf<DataSiswaRequest>()

        override suspend fun getSiswa(): List<DataSiswa> = siswaList

        override suspend fun postSiswa(dataSiswa: DataSiswaRequest): Response<InsertResponse> {
            sentRequests.add(dataSiswa)
            return postResult
        }
    }

    @Test
    fun entryValidation_requiresAllFieldsNonBlank() {
        val repo = JaringanRepositoryDataSiswa(FakeServiceApi())
        val viewModel = EntryViewModel(repo)

        assertFalse(viewModel.uiStateSiswa.isEntryValid)

        viewModel.updateUiState(DetailSiswa(nama = "Budi", alamat = "", telpon = "081234"))
        assertFalse(viewModel.uiStateSiswa.isEntryValid)

        viewModel.updateUiState(DetailSiswa(nama = "Budi", alamat = "Yogyakarta", telpon = "   "))
        assertFalse(viewModel.uiStateSiswa.isEntryValid)

        viewModel.updateUiState(DetailSiswa(nama = "Budi", alamat = "Yogyakarta", telpon = "08123456789"))
        assertTrue(viewModel.uiStateSiswa.isEntryValid)
    }

    @Test
    fun detailSiswaToRequest_trimsFieldsAndOmitsId() {
        val request = DetailSiswa(
            id = 99,
            nama = "  Ani  ",
            alamat = " Sleman ",
            telpon = " 08999 "
        ).toRequest()

        assertEquals(DataSiswaRequest(nama = "Ani", alamat = "Sleman", telpon = "08999"), request)
    }

    @Test
    fun repository_fetchesListAndHandlesPostResponses() = runBlocking {
        val fakeService = FakeServiceApi(
            siswaList = listOf(DataSiswa(id = 1, nama = "Siti", alamat = "Bantul", telpon = "08111"))
        )
        val repo = JaringanRepositoryDataSiswa(fakeService)

        val fetched = repo.getDataSiswa()
        assertEquals(1, fetched.size)
        assertEquals("Siti", fetched.first().nama)

        val response = repo.postDataSiswa(DataSiswaRequest(nama = "Ani", alamat = "Sleman", telpon = "08999"))
        assertTrue(response.isSuccessful)
        assertEquals("Data berhasil ditambahkan", response.body()?.message)
        assertEquals(1, fakeService.sentRequests.size)

        val errorService = FakeServiceApi(
            postResult = Response.error(
                400,
                """{"error":"Data tidak boleh kosong"}""".toResponseBody("application/json".toMediaType())
            )
        )
        val errorRepo = JaringanRepositoryDataSiswa(errorService)
        val errorResponse = errorRepo.postDataSiswa(DataSiswaRequest("", "", ""))
        assertFalse(errorResponse.isSuccessful)
        assertEquals(400, errorResponse.code())
    }

    @Test
    fun dataSiswaJson_parsesPhpArrayAndEncodesRequestWithoutId() {
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        val raw = """[{"id":"1","nama":"Siti","alamat":"Bantul","telpon":"08111","extra":"ignored"}]"""
        val decoded = json.decodeFromString<List<DataSiswa>>(raw)

        assertEquals(1, decoded.size)
        assertEquals(1, decoded.first().id)
        assertEquals("Siti", decoded.first().nama)

        val insertSuccess = json.decodeFromString<InsertResponse>("""{"message":"Data berhasil ditambahkan"}""")
        assertEquals("Data berhasil ditambahkan", insertSuccess.message)

        val compactJson = Json { ignoreUnknownKeys = true }
        val encoded = compactJson.encodeToString(
            DataSiswaRequest.serializer(),
            DataSiswaRequest(nama = "Siti", alamat = "Bantul", telpon = "08111")
        )
        assertEquals("""{"nama":"Siti","alamat":"Bantul","telpon":"08111"}""", encoded)
    }
}
