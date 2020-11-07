package com.example.uzimabetav10.mpesa

data class StkCallback(
        val CallbackMetadata: CallbackMetadata,
        val CheckoutRequestID: String,
        val MerchantRequestID: String,
        val ResultCode: Int,
        val ResultDesc: String
)