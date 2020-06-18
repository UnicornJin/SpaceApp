package com.spaceapp.space.account;

import java.io.Serializable;

public class Contact implements Serializable {
    private String name;
    private String lastMsg;
    private String Uid;

    public void setName(String name) {
        this.name = name;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }

    public String getName() {
        return this.name;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public String getUid() {
        return this.Uid;
    }
}
