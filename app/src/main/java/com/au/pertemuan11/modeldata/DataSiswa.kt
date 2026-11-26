package com.au.pertemuan11.modeldata

import kotlinx.serialization.Serializable

@Serializable
data class DataSiswa(
    val id: Int = 0,
    val nama: String,
    val alamat: String,
    val telpon: String
)

@Serializable
data class DataSiswaRequest(
    val nama: String,
    val alamat: String,
    val telpon: String
)

@Serializable
data class InsertResponse(
    val message: String? = null,
    val error: String? = null
)
