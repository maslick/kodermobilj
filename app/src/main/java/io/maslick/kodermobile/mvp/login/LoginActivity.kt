package io.maslick.kodermobile.mvp.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.maslick.kodermobile.R
import io.maslick.kodermobile.helper.setupActionBar
import kotlinx.android.synthetic.main.login_act.*
import org.koin.android.ext.android.inject


class LoginActivity : RxAppCompatActivity(), LoginContract.View {

    override val presenter by inject<LoginContract.Presenter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_act)

        setupActionBar(R.id.toolbar) {
            title = "Authentication"
        }

        presenter.view = this
        presenter.start()

        presenter.authenticate(intent.data?.toString())
        initViews()

        login_button.setOnClickListener {
                hideLoginButton()
                startActivity(Intent(Intent.ACTION_VIEW, presenter.authUrl()))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        presenter.authenticate(intent?.data?.toString())
    }

    override fun initViews() {
        login_text.visibility = View.GONE
        login_button.visibility = View.VISIBLE
    }

    override fun hideAll() {
        login_text.visibility = View.GONE
        login_button.visibility = View.GONE
    }

    override fun hideLoginButton() {
        Handler().postDelayed({
            login_text.visibility = View.VISIBLE
            login_button.visibility = View.GONE
        }, 1000L)
    }

    override fun onBackPressed() {}

    override fun success() {
        setResult(RESULT_OK)
        finish()
    }

    override fun failure(message: String?) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
