package ir.sadad.bami.sample.login.util;

public class LoginResult {
    int mResponse;
    String mMessage;

    public LoginResult(int response, String message) {
        mResponse = response;
        mMessage = message;

    }


    public int getResponse() {
        return mResponse;
    }

    public String getMessage() {
        return mMessage;
    }

    public boolean isSuccess() {
        return mResponse == LoginHelper.LOGIN_RESPONSE_RESULT_OK || mResponse == LoginHelper.LOGIN_RESPONSE_USER_LOGGED_IN_BEFORE;
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public String toString() {
        return "LoginResult: code = " + getResponse() + ", message = " + getMessage();
    }
}
