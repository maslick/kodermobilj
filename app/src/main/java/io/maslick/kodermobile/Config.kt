package io.maslick.kodermobile

object Config {
    const val barkoderBaseDevUrl = "https://barkoder-dev.herokuapp.com"
    const val barkoderBaseProdUrl = "https://barkoder.herokuapp.com"

    const val clientId = "barkoder-frontend"
    const val baseUrl = "https://activeclouder.ijs.si/auth/realms/barkoder/protocol/openid-connect"
    const val authenticationCodeUrl = "$baseUrl/auth"
    const val redirectUri = "barkoder://oauthresponse"
}