package io.maslick.kodermobile.oauth

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.migcomponents.migbase64.Base64

data class Principal(
    val userId: String? = null,
    val email: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val roles: List<String> = emptyList()
)


fun parseJwtToken(jwtToken: String?): Principal {
    jwtToken ?: return Principal()
    jwtToken.apply {
        val splitString = split(".")
        val base64EncodedBody = splitString[1]

        val body = String(Base64.decodeFast(base64EncodedBody))
        val jsonBody = Gson().fromJson(body, JsonObject::class.java)

        val userId = jsonBody.get("sub")?.asString
        val email = jsonBody.get("email")?.asString ?: "n/a"
        val name = jsonBody.get("given_name")?.asString ?: "n/a"
        val surname = jsonBody.get("family_name")?.asString ?: "n/a"
        val roles = jsonBody.get("realm_access")?.asJsonObject?.getAsJsonArray("roles")?.map {it.asString} ?: emptyList()

        return Principal(userId, email, name, surname, roles)
    }
}