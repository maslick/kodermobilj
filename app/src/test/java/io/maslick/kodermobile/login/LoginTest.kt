package io.maslick.kodermobile.login

import io.maslick.kodermobile.helpers.RxImmediateSchedulerRule
import io.maslick.kodermobile.helpers.any
import io.maslick.kodermobile.helpers.kogda
import io.maslick.kodermobile.mvp.login.LoginContract
import io.maslick.kodermobile.mvp.login.LoginPresenter
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.rest.IKeycloakRest
import io.maslick.kodermobile.rest.KeycloakToken
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import java.net.SocketTimeoutException

class LoginTest {
    @Rule @JvmField val rule = MockitoJUnit.rule()!!
    @Rule @JvmField val testSchedulerRule = RxImmediateSchedulerRule()

    @Mock private lateinit var api: IKeycloakRest
    @Mock private lateinit var storage: IOAuth2AccessTokenStorage
    @Mock private lateinit var loginView: LoginContract.View

    private lateinit var loginPresenter: LoginPresenter

    @Before
    fun before() {}

    @Test
    fun testAuthenticateOk() {
        loginPresenter = LoginPresenter(api, storage)
        loginPresenter.view = loginView

        kogda(api.grantNewAccessToken(any(), any(), any(), any()))
            .thenReturn(Observable.just(
                KeycloakToken(
                    accessToken = "",
                    refreshToken = "",
                    expiresIn = 1000,
                    refreshExpiresIn = 1000
                )
            ))

        loginPresenter.authenticate("barkoder://oauthresponse?code=123456")
        verify(loginView).success()
    }

    @Test
    fun testAuthenticateError() {
        loginPresenter = LoginPresenter(api, storage)
        loginPresenter.view = loginView

        kogda(api.grantNewAccessToken(any(), any(), any(), any()))
            .thenReturn(Observable.error(SocketTimeoutException()))

        loginPresenter.authenticate("barkoder://oauthresponse?code=123456")
        verify(loginView).failure(any())
    }
}