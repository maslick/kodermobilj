package io.maslick.kodermobile

object Config {
    const val barkoderBaseUrl = "https://barkoder.herokuapp.com"
    const val keycloakBaseUrl = "https://activeclouder.ijs.si/auth/realms/barkoder/protocol/openid-connect"
    const val clientId = "barkoder-frontend"
    const val authenticationCodeUrl = "$keycloakBaseUrl/auth"
    const val redirectUri = "barkoder://oauthresponse"
}