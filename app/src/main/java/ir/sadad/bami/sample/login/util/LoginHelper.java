package ir.sadad.bami.sample.login.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

import ir.sadad.bami.sdk.login.IHopeKidsLoginService;


public class LoginHelper {

    public static final int LOGIN_RESPONSE_RESULT_OK = 1000;
    public static final int LOGIN_RESPONSE_USER_LOGGED_IN_BEFORE = 1001;
    public static final int LOGIN_RESPONSE_RESULT_LOGIN_UNAVAILABLE = -1001;
    public static final int LOGIN_RESPONSE_BAD = -1002;
    public static final int LOGIN_RESPONSE_RESULT_EXCEPTION = -1003;
    public static final int LOGIN_RESPONSE_ERROR = -1004;
    public static final int LOGIN_RESPONSE_REMOTE_EXCEPTION = -1005;
    public static final int LOGIN_RESPONSE_SEND_INTENT_FAILED = -1006;
    public static final int LOGIN_RESPONSE_SERVICE_IS_NULL = -1007;

    public static final String LOGIN_MESSAGE_UNKNOWN_ERROR = "Unknown error. Check parameters and try again.";
    public static final String LOGIN_MESSAGE_INCOMPLETE_LOGIN = "The login is incomplete, run again and complete both steps.";
    public static final String LOGIN_MESSAGE_COMPLETED_SUCCESSFULLY = "Login completed successfully, enjoy!";
    public static final String LOGIN_MESSAGE_NULL_DATA = "Null data in login result";
    public static final String LOGIN_MESSAGE_SERVICE_IS_NULL = "Service is null, it may not setup completely yet or may disconnected. Try setup again.";
    public static final String LOGIN_MESSAGE_SETUP_SUCCESSFUL = "Setup successful.";
    public static final String LOGIN_MESSAGE_LOGIN_UNAVAILABLE = "Login service unavailable on device.";
    public static final String LOGIN_MESSAGE_USER_IS_ALREADY_VALID = "User is already valid.";

    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_LOGIN_STEP = "LOGIN_STEP";
    public static final String RESPONSE_MESSAGE = "RESPONSE_MESSAGE";
    public static final String RESPONSE_DATA = "RESPONSE_DATA";
    public static final String RESPONSE_LOGIN_INTENT = "LOGIN_INTENT";


    private Context mContext;
    // Is debug logging enabled?
    boolean mDebugLog = true;
    String mDebugTag = "LoginHelper";

    IHopeKidsLoginService mService = null;
    ServiceConnection mServiceConn = null;
    boolean mDisposed = false;

    // The request code used to launch login flow
    int mRequestCode;

    // The listener registered on launchLoginFlow, which we have to call back when
    // the purchase finishes
    OnLoginFinishedListener mLoginListener;

    public LoginHelper(Context mContext) {
        this.mContext = mContext;
    }


    public interface OnLoginSetupListener {
        void onLoginSetupFinished(LoginResult result);
    }


    public void startSetup(final OnLoginSetupListener listener) {

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                if (mDisposed) return;
                logDebug("Service Connected");

                mService = IHopeKidsLoginService.Stub.asInterface(iBinder);
                if (listener != null) {
                    listener.onLoginSetupFinished(new LoginResult(LOGIN_RESPONSE_RESULT_OK, LOGIN_MESSAGE_SETUP_SUCCESSFUL));
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                logDebug("Service Disconnected");
                mService = null;
            }
        };


        Intent serviceIntent = new Intent("ir.sadad.bami.sdk.login.HopeKidsLoginService.BIND");
        serviceIntent.setPackage("ir.sadad.bami.test");
        List<ResolveInfo> intentServices = mContext.getPackageManager().queryIntentServices(serviceIntent, 0);
        try {
            if (!intentServices.isEmpty()) {
                // service available to handle that Intent
                mContext.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
            } else {// Show the prompt dialog for install Hope
                // no service available to handle that Intent
                if (listener != null) {
                    listener.onLoginSetupFinished(
                            new LoginResult(LOGIN_RESPONSE_RESULT_LOGIN_UNAVAILABLE,
                                    LOGIN_MESSAGE_LOGIN_UNAVAILABLE));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onLoginSetupFinished(
                        new LoginResult(LOGIN_RESPONSE_RESULT_EXCEPTION,
                                e.getMessage()));
            }
        }

    }

    public void dispose() {
        logDebug("Disposing.");
        if (mServiceConn != null) {
            logDebug("Unbinding from service.");
            if (mContext != null) mContext.unbindService(mServiceConn);
        }
        mDisposed = true;
        mContext = null;
        mServiceConn = null;
        mService = null;
    }

    void logDebug(String msg) {
        if (mDebugLog) Log.d(mDebugTag, msg);
    }

    public BProfile getUser() {
        try {
            Bundle bundle = mService.getUser();
            if (bundle == null || bundle.isEmpty()) return null;
            return new BProfile(bundle);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface OnLoginFinishedListener {
        void onLoginFinished(LoginResult loginResult, BProfile user);
    }

    public void authenticate(Activity activity, int requestCode, OnLoginFinishedListener listener) {

        if (mDisposed || mService == null) {
            if (listener != null)
                listener.onLoginFinished(new LoginResult(LOGIN_RESPONSE_SERVICE_IS_NULL, LOGIN_MESSAGE_SERVICE_IS_NULL), null);
            return;
        }

        BProfile user = getUser();
        if (user != null) {
            if (listener != null)
                listener.onLoginFinished(new LoginResult(LOGIN_RESPONSE_USER_LOGGED_IN_BEFORE, LOGIN_MESSAGE_USER_IS_ALREADY_VALID), user);
            return;
        }

        mRequestCode = requestCode;
        mLoginListener = listener;
        try {
            Bundle loginIntentBundle = mService.getLoginIntent();

            PendingIntent pendingIntent = loginIntentBundle.getParcelable(RESPONSE_LOGIN_INTENT);

            activity.startIntentSenderForResult(
                    pendingIntent.getIntentSender(),
                    requestCode,
                    new Intent(),
                    Integer.valueOf(0),
                    Integer.valueOf(0),
                    Integer.valueOf(0));
        } catch (RemoteException e) {
            e.printStackTrace();
            if (listener != null)
                listener.onLoginFinished(new LoginResult(LOGIN_RESPONSE_REMOTE_EXCEPTION, e.getMessage()), null);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            if (listener != null)
                listener.onLoginFinished(new LoginResult(LOGIN_RESPONSE_SEND_INTENT_FAILED, e.getMessage()), null);
        }

    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != mRequestCode) return false;

        if (data == null) {
            if (mLoginListener != null)
                mLoginListener.onLoginFinished(new LoginResult(LOGIN_RESPONSE_BAD, LOGIN_MESSAGE_NULL_DATA), null);
            return true;
        }

        if (resultCode == Activity.RESULT_OK) {
            int loginStep = data.getIntExtra(LoginHelper.RESPONSE_LOGIN_STEP, 1);
            int responseCode = data.getIntExtra(LoginHelper.RESPONSE_CODE, -1);
            String responseMessage = data.getStringExtra(LoginHelper.RESPONSE_MESSAGE);
            Bundle loginBundle = data.getBundleExtra(LoginHelper.RESPONSE_DATA);
            BProfile profile = null;
            if (loginBundle != null)
                profile = new BProfile(loginBundle);


            if (mLoginListener != null) {
                if (loginStep == 1)
                    mLoginListener.onLoginFinished(new LoginResult(-1, LOGIN_MESSAGE_INCOMPLETE_LOGIN), null);
                else if (loginStep == 2)
                    mLoginListener.onLoginFinished(new LoginResult(responseCode, responseMessage), profile);
            }
            return true;
        }
        return false;
    }

}
