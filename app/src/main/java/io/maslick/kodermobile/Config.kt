package io.maslick.kodermobile

object Config {
    const val clientId = "[CLIENT_ID]"
    const val baseUrl = "[KEYCLOAK_URL]/auth/realms/[REALM]/protocol/openid-connect"
    const val authenticationCodeUrl = "$baseUrl/auth"
    const val redirectUri = "https://maslick.io/barkoder"
    const val barkoderBaseDevUrl = ""
    const val barkoderBaseProdUrl = ""
}