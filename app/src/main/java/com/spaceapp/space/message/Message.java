package com.spaceapp.space.message;

import com.google.firebase.Timestamp;


/**
 * This class stands for message objects.
 */
public class Message {

    private String content;
    private Timestamp time;
    private boolean type; // true for send, false for received

    public Message(Timestamp time, String content, boolean type) {
        this.time = time;
        this.content = content;
        this.type = type;
    }

    public Timestamp getTime() { return this.time; }

    public String getContent() {
        return this.content;
    }

    public boolean getType() {
        return this.type;
    }

    public boolean isReceived() {
        return !type;
    }

    public boolean isSent() {
        return type;
    }
}
