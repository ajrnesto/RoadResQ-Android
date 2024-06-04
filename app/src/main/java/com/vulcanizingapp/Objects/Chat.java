package com.vulcanizingapp.Objects;

public class Chat {
    String id;
    String message;
    String authorUid;
    long timestamp;

    public Chat() {
    }

    public Chat(String id, String message, String authorUid, long timestamp) {
        this.id = id;
        this.message = message;
        this.authorUid = authorUid;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
