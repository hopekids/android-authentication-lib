package ir.sadad.bami.sample.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ir.sadad.bami.sample.login.util.LoginHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivityTAG"

    private var mLoginHelper: LoginHelper? = null

    val RC_REQUEST = 10001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAuthenticate.visibility = View.GONE
        txtUserAvailabilityStatus.visibility = View.GONE
        txtDetails.visibility = View.GONE

        mLoginHelper = LoginHelper(this)

        mLoginHelper?.startSetup { result ->
            Log.d(TAG, "startSetup() called with: result = ${result.message}")

            if (result.isSuccess) {
                btnAuthenticate.visibility = View.VISIBLE

                txtConnectionStatus.text = "Connected"
                txtConnectionStatus.setTextColor(Color.GREEN)
            } else {
                txtConnectionStatus.text = "Failed: ${result.message}}"
                txtConnectionStatus.setTextColor(Color.RED)
            }

        }

        btnAuthenticate.setOnClickListener {
            txtDetails.visibility = View.GONE
            txtUserAvailabilityStatus.visibility = View.VISIBLE
            txtUserAvailabilityStatus.text = "Checking user availability..."
            mLoginHelper?.authenticate(
                this,
                RC_REQUEST
            ) { loginResult, user ->
                Log.d(
                    TAG,
                    "launchLoginFlow() called with: loginResult = $loginResult, user = $user"
                )
                txtDetails.visibility = View.VISIBLE

                when (loginResult.response) {
                    LoginHelper.LOGIN_RESPONSE_USER_LOGGED_IN_BEFORE -> {
                        txtUserAvailabilityStatus.text = "User is already available."
                        txtDetails.text = user.toString()
                    }
                    LoginHelper.LOGIN_RESPONSE_RESULT_OK -> {
                        txtUserAvailabilityStatus.text = "Authentication completed successfully."
                        txtDetails.text = user.toString()
                    }
                    else -> {
                        txtDetails.text = "Failed to authenticate: ${loginResult.message}"
                        txtUserAvailabilityStatus.visibility = View.GONE
                    }
                }
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult($requestCode,$resultCode,$data")
        if (mLoginHelper == null) return

        // Pass on the activity result to the helper for handling
        if (!mLoginHelper!!.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data)
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.")
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        // very important:
        mLoginHelper?.dispose()
        mLoginHelper = null
    }
}