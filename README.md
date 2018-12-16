# =kodermobilj=
Android native client for barkoder

## Features
* Scans a barcode and push it to a ``barkoder`` hosted backend
* Written in Kotlin
* Using OkHttp3, Retrofit2, RxJava2, Koin v1
* MVP architecture


## Usage
Edit the file ``Config.kt`` according to your setup:
```kt
object Config {
    const val clientId = "[CLIENT_ID]"
    const val baseUrl = "https://[KEYCLOAK_URL]/auth/realms/[REALM]/protocol/openid-connect"
    const val authenticationCodeUrl = "$baseUrl/auth"
    const val redirectUri = "https://maslick.io/barkoder"
    const val barkoderBaseDevUrl = "[BARKODER_URL]"
    const val barkoderBaseProdUrl = "[BARKODER_URL]"
}
```
