package io.maslick.kodermobile

object Config {
    const val keycloakUrl = "https://keycloak-maslick-io.1d35.starter-us-east-1.openshiftapps.com"
    const val realm = "barkoder"
    const val clientId = "barkoder-frontend"
    const val keycloakBaseUrl = "$keycloakUrl/auth/realms/$realm/protocol/openid-connect"
    const val authenticationCodeUrl = "$keycloakBaseUrl/auth"

    const val barkoderBaseUrl = "https://barkoder.herokuapp.com"
    const val redirectUri = "barkoder://oauthresponse"
}