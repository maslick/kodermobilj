# =kodermobilj=
Android native client for [barkoder](https://github.com/maslick/barkoder)

[![Build Status](https://travis-ci.org/maslick/kodermobilj.svg?branch=master)](https://travis-ci.org/maslick/kodermobilj)
[![Maintainability](https://api.codeclimate.com/v1/badges/1d57fca80b02f009b961/maintainability)](https://codeclimate.com/github/maslick/kodermobilj/maintainability)
[![codecov](https://codecov.io/gh/maslick/kodermobilj/branch/master/graph/badge.svg)](https://codecov.io/gh/maslick/kodermobilj)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Download](https://img.shields.io/badge/Download-apk-green.svg)](https://dl.bintray.com/maslick/generic/kodermobile/)

![barkoder mobile](presentation.jpg)

## Features
* Scans the product barcode and pushes it to a ``barkoder`` hosted backend
* Simple CRUD operations
* MVP architecture (Kotlin + Koin)
* Protected by Keycloak
* Using OkHttp v3, Retrofit v2, RxJava v2, Koin v1


## Usage
Edit the file ``Config.kt`` according to your setup:
```kt
object Config {
    const val barkoderBaseUrl = "[BARKODER_URL]"
    const val keycloakBaseUrl = "https://[KEYCLOAK_URL]/auth/realms/[REALM]/protocol/openid-connect"
    const val clientId = "[CLIENT_ID]"
    const val authenticationCodeUrl = "$keycloakBaseUrl/auth"
    const val redirectUri = "barkoder://oauthresponse"
}
```
