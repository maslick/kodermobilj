package io.maslick.kodermobile

object Config {
    const val keycloakUrl = "https://auth.maslick.ru"
    const val realm = "barkoder"
    const val clientId = "barkoder-frontend"
    const val keycloakBaseUrl = "$keycloakUrl/auth/realms/$realm/protocol/openid-connect"
    const val authenticationCodeUrl = "$keycloakBaseUrl/auth"

    const val barkoderBaseUrl = "https://koder-api.maslick.ru"
    const val redirectUri = "barkoder://oauthresponse"
}