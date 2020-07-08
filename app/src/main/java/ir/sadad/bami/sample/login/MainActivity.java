package ir.sadad.bami.sample.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ir.sadad.bami.sample.login.util.BProfile;
import ir.sadad.bami.sample.login.util.LoginHelper;
import ir.sadad.bami.sample.login.util.LoginResult;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivityTAG";
    private LoginHelper mLoginHelper;
    private final int RC_REQUEST = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnAuthenticate = findViewById(R.id.btnAuthenticate);
        final TextView txtUserAvailabilityStatus = findViewById(R.id.txtUserAvailabilityStatus);
        final TextView txtDetails = findViewById(R.id.txtDetails);
        final TextView txtConnectionStatus = findViewById(R.id.txtConnectionStatus);


        btnAuthenticate.setVisibility(View.GONE);
        txtUserAvailabilityStatus.setVisibility(View.GONE);
        txtDetails.setVisibility(View.GONE);


        mLoginHelper = new LoginHelper(this);

        mLoginHelper.startSetup(new LoginHelper.OnLoginSetupListener() {
            @Override
            public void onLoginSetupFinished(LoginResult result) {
                Log.d(TAG, "startSetup() called with: result = " + result.getMessage());

                if (result.isSuccess()) {
                    btnAuthenticate.setVisibility(View.VISIBLE);

                    txtConnectionStatus.setText("Connected");
                    txtConnectionStatus.setTextColor(Color.GREEN);
                } else {
                    txtConnectionStatus.setText("Failed: " + result.getMessage());
                    txtConnectionStatus.setTextColor(Color.RED);
                }
            }
        });


        btnAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDetails.setVisibility(View.GONE);
                txtUserAvailabilityStatus.setVisibility(View.VISIBLE);
                txtUserAvailabilityStatus.setText("Checking user availability...");

                if (mLoginHelper != null) {
                    mLoginHelper.authenticate(MainActivity.this, RC_REQUEST, new LoginHelper.OnLoginFinishedListener() {
                        @Override
                        public void onLoginFinished(LoginResult loginResult, BProfile user) {
                            Log.d(TAG, "onLoginFinished() called with: loginResult = [" + loginResult + "], user = [" + user + "]");

                            txtDetails.setVisibility(View.VISIBLE);

                            switch (loginResult.getResponse()) {
                                case LoginHelper.LOGIN_RESPONSE_USER_LOGGED_IN_BEFORE:
                                    txtUserAvailabilityStatus.setText("User is already available.");
                                    txtDetails.setText(user.toString());
                                    break;
                                case LoginHelper.LOGIN_RESPONSE_RESULT_OK:
                                    txtUserAvailabilityStatus.setText("Authentication completed successfully.");
                                    txtDetails.setText(user.toString());
                                    break;
                                default:
                                    txtDetails.setText("Failed to authenticate: " + loginResult.getMessage());
                                    txtUserAvailabilityStatus.setVisibility(View.GONE);
                                    break;
                            }
                        }
                    });
                } else {
                    txtDetails.setText("Failed to authenticate: Login helper class is null. Initialize first");
                    txtUserAvailabilityStatus.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult(): requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (mLoginHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mLoginHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to login sdk
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by LoginHelper.");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // very important:

        if (mLoginHelper != null) {
            mLoginHelper.dispose();
            mLoginHelper = null;
        }
    }
}