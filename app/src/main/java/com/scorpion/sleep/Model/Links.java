package com.scorpion.sleep.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.scorpion.sleep.Model.meta.Friends_;
import com.scorpion.sleep.Model.meta.Self;

public class Links {

    @SerializedName("self")
    @Expose
    private Self self;
    @SerializedName("friends")
    @Expose
    private Friends_ friends;

    public Self getSelf() {
        return self;
    }

    public void setSelf(Self self) {
        this.self = self;
    }

    public Friends_ getFriends() {
        return friends;
    }

    public void setFriends(Friends_ friends) {
        this.friends = friends;
    }

}