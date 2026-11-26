package com.au.pertemuan11.uicontroller.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.au.pertemuan11.modeldata.DataSiswa
import com.au.pertemuan11.modeldata.DataSiswaRequest
import com.au.pertemuan11.modeldata.InsertResponse
import com.au.pertemuan11.repositori.RepositoryDataSiswa
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class EntryViewModel(
    private val repositoryDataSiswa: RepositoryDataSiswa
) : ViewModel() {
    var uiStateSiswa by mutableStateOf(UIStateSiswa())
        private set

    var isSaving by mutableStateOf(false)
        private set

    var saveError by mutableStateOf<String?>(null)
        private set

    var isSaveSuccess by mutableStateOf(false)
        private set

    private val errorJson = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun validasiInput(uiState: DetailSiswa = uiStateSiswa.detailSiswa): Boolean {
        return with(uiState) {
            nama.isNotBlank() && alamat.isNotBlank() && telpon.isNotBlank()
        }
    }

    fun updateUiState(detailSiswa: DetailSiswa) {
        saveError = null
        uiStateSiswa = UIStateSiswa(
            detailSiswa = detailSiswa,
            isEntryValid = validasiInput(detailSiswa)
        )
    }

    fun addSiswa() {
        if (!validasiInput() || isSaving) return
        isSaving = true
        saveError = null
        isSaveSuccess = false

        viewModelScope.launch {
            try {
                val response = repositoryDataSiswa.postDataSiswa(
                    uiStateSiswa.detailSiswa.toRequest()
                )
                if (response.isSuccessful) {
                    isSaveSuccess = true
                } else {
                    saveError = parseErrorMessage(response)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                saveError = e.localizedMessage ?: "Network error"
            } catch (e: HttpException) {
                saveError = "HTTP ${e.code()}"
            } catch (e: Exception) {
                saveError = e.localizedMessage ?: "Unknown error"
            } finally {
                isSaving = false
            }
        }
    }

    fun onSaveNavigated() {
        isSaveSuccess = false
    }

    private fun parseErrorMessage(response: Response<InsertResponse>): String {
        val rawBody = response.errorBody()?.string()
        if (!rawBody.isNullOrBlank()) {
            val parsed = runCatching {
                errorJson.decodeFromString<InsertResponse>(rawBody).error
            }.getOrNull()
            if (!parsed.isNullOrBlank()) {
                return parsed
            }
        }
        return "HTTP ${response.code()}"
    }
}

data class UIStateSiswa(
    val detailSiswa: DetailSiswa = DetailSiswa(),
    val isEntryValid: Boolean = false
)

/** Merepresentasikan field input formulir data siswa. */
data class DetailSiswa(
    val id: Int = 0,
    val nama: String = "",
    val alamat: String = "",
    val telpon: String = ""
)

fun DetailSiswa.toDataSiswa(): DataSiswa = DataSiswa(
    id = id,
    nama = nama,
    alamat = alamat,
    telpon = telpon
)

fun DetailSiswa.toRequest(): DataSiswaRequest = DataSiswaRequest(
    nama = nama.trim(),
    alamat = alamat.trim(),
    telpon = telpon.trim()
)

fun DataSiswa.toUiStateSiswa(isEntryValid: Boolean = false): UIStateSiswa = UIStateSiswa(
    detailSiswa = this.toDetailSiswa(),
    isEntryValid = isEntryValid
)

fun DataSiswa.toDetailSiswa(): DetailSiswa = DetailSiswa(
    id = id,
    nama = nama,
    alamat = alamat,
    telpon = telpon
)
