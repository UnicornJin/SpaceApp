package com.spaceapp.space.account;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Contact implements Serializable {

    private final long serialVersionUID = 123456;

    private String name;
    private String lastMsg;
    private String Uid;
    private Long lastMsgTime;

    public void setName(String name) {
        this.name = name;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }

    public void setLastMsgTime(Long time) { this.lastMsgTime = time; }

    public String getName() {
        return this.name;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public String getUid() {
        return this.Uid;
    }

    public Long getLastMsgTime() { return this.lastMsgTime; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Contact) {
            return this.getUid().equals(((Contact) obj).getUid());
        } else {
            return false;
        }
    }
}
