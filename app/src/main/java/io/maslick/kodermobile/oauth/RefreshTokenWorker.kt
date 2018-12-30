package io.maslick.kodermobile.oauth

import android.content.Context
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import io.maslick.kodermobile.Config
import io.maslick.kodermobile.rest.IKeycloakRest
import io.maslick.kodermobile.rest.KeycloakToken
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES

class RefreshTokenWorker(context: Context, params: WorkerParameters): Worker(context, params), KoinComponent {

    companion object {
        fun startPeriodicRefreshTokenTask() {
            val workConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWork = PeriodicWorkRequest.Builder(RefreshTokenWorker::class.java, 15, MINUTES)
                .setConstraints(workConstraints)
                .build()

            WorkManager.getInstance().enqueueUniquePeriodicWork("barkoder-refresh-token-work", REPLACE, periodicWork)
        }
    }

    private val api: IKeycloakRest by inject()
    private val storage: IOAuth2AccessTokenStorage by inject()

    override fun doWork(): Result {
        if (storage.getStoredAccessToken() == null || storage.getStoredAccessToken()!!.refreshToken == null)
            return Result.failure()

        return try {
            saveTokenToStorage(api.refreshAccessToken(storage.getStoredAccessToken()!!.refreshToken!!, Config.clientId).blockingFirst())
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun saveTokenToStorage(token: KeycloakToken): KeycloakToken {
        val expirationDate = Calendar.getInstance().clone() as Calendar
        val refreshExpirationDate = Calendar.getInstance().clone() as Calendar
        expirationDate.add(Calendar.SECOND, token.expiresIn!!)
        refreshExpirationDate.add(Calendar.SECOND, token.refreshExpiresIn!!)
        token.tokenExpirationDate = expirationDate
        token.refreshTokenExpirationDate = refreshExpirationDate
        storage.storeAccessToken(token)
        return token
    }
}