package com.example.datto.DataClass

import com.google.gson.annotations.SerializedName

data class AccountResponse(
    @SerializedName("_id")
    var id: String,
    var username: String,
    var email: String,
    var profile: ProfileResponse,
)

data class NewAccountRequest(
    var username: String,
    var email: String,
    var password: String,
)

data class NewAccountResponse(
    var token: String
)

data class AccountRequest(
    var username: String,
    var password: String,
)

data class NewPasswordRequest(
    var email: String,
    var password: String,
)

data class GoogleAccountRequest(
    var email: String,
    var googleId: String,
    var fullName: String,
    var avatar: String,
)
