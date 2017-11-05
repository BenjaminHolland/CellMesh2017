package com.cellmesh.app.model;

public class Message {
    private Long time;
    private Long fromId;
    private String content;

    Message(Long time, Long fromId, String content) {
        this.time = time;
        this.fromId = fromId;
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }

    public String getSendString() {
        return Long.toString(this.time) + ":" + Long.toString(fromId) + ":" + this.content;
    }

    public Long getFromId() {
        return fromId;
    }
}
