package ir.sadad.bami.sample.login.util;

import android.os.Bundle;

import org.jetbrains.annotations.NotNull;


public class BProfile {

    private int id;
    private String nickName;
    private String avatar;

    protected BProfile(Bundle bundle) {
        if (bundle.containsKey(LoginConsts.KEY_ID))
            id = bundle.getInt(LoginConsts.KEY_ID);
        if (bundle.containsKey(LoginConsts.KEY_NAME))
            nickName = bundle.getString(LoginConsts.KEY_NAME);
        if (bundle.containsKey(LoginConsts.KEY_AVATAR))
            avatar = bundle.getString(LoginConsts.KEY_AVATAR);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @NotNull
    @Override
    public String toString() {
        return "id = " + id +
                ", name = " + nickName +
                ", avatar = " + avatar;
    }
}
