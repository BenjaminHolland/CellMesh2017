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

class nameManager {
    private SortedMap<Long, String> knownNames;
    private String knownNamesHash;

    public nameManager(Long nodeId, String myName) {
        this.knownNames = new TreeMap<Long, String>();
        this.addName(nodeId, myName);
    }

    public void addName(Long nodeId, String name) {
        this.knownNames.put(nodeId, name);
        this.updateHash();
    }

    private void updateHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();

            for ( Map.Entry<Long,String> item : this.knownNames.entrySet() ) {
                ByteBuffer LongBuffer = ByteBuffer.allocate(Long.BYTES);
                LongBuffer.putLong(0, item.getKey());
                md.update(LongBuffer);

                byte[] stringBytes = item.getValue().getBytes();
                md.update(stringBytes);
            }

            byte[] digest = md.digest();

            String hexStr = "";
            for (int i = 0; i < digest.length; i++) {
                hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
            }

            this.knownNamesHash = hexStr;
        } catch (Exception NoSuchAlgorithmException) {

        }
    }

    public String getHash() {
        return this.knownNamesHash;
    }
}