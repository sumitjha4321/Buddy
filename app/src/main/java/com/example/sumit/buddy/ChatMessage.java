package com.example.sumit.buddy;

import java.util.Date;


public class ChatMessage {

    private String id; // this is the push-id that firebase has generated
    private String sender;
    private String message;
    private long messageTimestamp;
    private String messageType;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChatMessage() {

    }

    public ChatMessage(String sender, String message, String id, String messageType) {
        this.sender = sender;
        this.message = message;
        this.messageTimestamp = new Date().getTime();
        this.id = id;
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getMessageTimestamp() {
        return messageTimestamp;
    }


}
