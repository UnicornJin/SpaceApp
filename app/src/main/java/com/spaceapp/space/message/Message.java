package com.spaceapp.space.message;

import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;


/**
 * This class stands for message objects.
 */
public class Message {

    private String sender;
    private String senderId;
    private String receiver;
    private String receiverId;
    private String content;
    private Timestamp time;
    private boolean isread;

    public boolean type;//true for send, false for received

    public Message(Timestamp time, String content) {
        this.time = time;
        this.content = content;
        this.isread = true;
    }

    public Message(Timestamp time, String content, boolean isread) {
        this.time = time;
        this.content = content;
        this.isread = isread;
    }

    public Message(String sender, String senderId, String receiver, String receiverId, String content, Timestamp time) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.time = time;
        this.content = content;
        this.isread = false;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public Timestamp getTime() { return this.time; }

    public String getContent() {
        return this.content;
    }

    public String getSender() { return this.sender; }

    public String getReceiver() {
        return receiver;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderId() {
        return senderId;
    }
}
