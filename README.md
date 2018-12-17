# =kodermobilj=
Android native client for [barkoder](https://github.com/maslick/barkoder)

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
