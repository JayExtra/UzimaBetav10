package com.example.uzimabetav10.mpesa

interface MpesaListener {
    fun sendSuccesfull(amount: String, phone: String, date: String, receipt: String)
    fun sendFailed(reason: String)
}