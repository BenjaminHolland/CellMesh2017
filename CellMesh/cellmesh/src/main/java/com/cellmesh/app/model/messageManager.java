package com.cellmesh.app.model;

import android.util.Log;
import android.util.Xml;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;
import java.io.UnsupportedEncodingException;

public class messageManager implements IManager {
    private SortedMap<Long, Message> messageHistory;
    private String messagesHash;
    private Boolean hashDirty;

    public messageManager() {
        this.messageHistory = new TreeMap<Long, Message>();
        Log.d("messageManager", "Message manager created.");
        hashDirty = true;
    }

    public Map<Long, Message> getMap() {
        return messageHistory;
    }

    public void addMessage(Long time, Long fromId, String message) {
        this.messageHistory.put(time, new Message(time, fromId, message));
        Log.d("messageManager", "Message added: " + message);
        hashDirty = true;

        if ( this.messageHistory.size() > 10 ) {
            this.messageHistory.remove(this.messageHistory.firstKey());
        }
    }

    private void updateHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();

            for ( Map.Entry<Long, Message> item : this.messageHistory.entrySet() ) {
                ByteBuffer LongBuffer = ByteBuffer.allocate(Long.SIZE / 8);
                LongBuffer.putLong(0, item.getKey());
                md.update(LongBuffer);

                byte[] stringBytes = item.getValue().toString().getBytes();
                md.update(stringBytes);
            }

            byte[] digest = md.digest();

            String hexStr = "";
            for (int i = 0; i < digest.length; i++) {
                hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
            }

            this.messagesHash = hexStr;

            hashDirty = false;
        } catch (Exception NoSuchAlgorithmException) {

        }
    }

    public String getHash() {
        if ( hashDirty ) {
            updateHash();
        }
        return this.messagesHash;
    }
}