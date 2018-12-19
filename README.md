# =kodermobilj=
Android native client for [barkoder](https://github.com/maslick/barkoder)

[![Build Status](https://travis-ci.org/maslick/kodermobilj.svg?branch=master)](https://travis-ci.org/maslick/kodermobilj)
[![codecov](https://codecov.io/gh/maslick/kodermobilj/branch/master/graph/badge.svg)](https://codecov.io/gh/maslick/kodermobilj)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

![barkoder mobile](presentation.png)

## Features
* Scans the product barcode and pushes it to a ``barkoder`` hosted backend
* Simple CRUD operations
* MVP architecture (Kotlin + Koin)
* Using OkHttp v3, Retrofit v2, RxJava v2, Koin v1


## Usage
Edit the file ``Config.kt`` according to your setup:
```kt
object Config {
    const val barkoderBaseDevUrl = "[BARKODER_URL]"
    const val barkoderBaseProdUrl = "[BARKODER_URL]"
}
```
