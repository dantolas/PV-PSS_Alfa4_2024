package com.kuta.tcp;

import com.google.gson.annotations.SerializedName;

/**
 * Class used for creating and JSON serialization of new messages to be sent through TCP
 */
public class TCPNewMessage {
    @SerializedName("command")
    public String command;
    @SerializedName("message_id")
    public String msgId;
    @SerializedName("message")
    public String msg;

    public TCPNewMessage(String command, String msgId, String msg) {
        this.command = command;
        this.msgId = msgId;
        this.msg = msg;
    }

    public boolean isValid(){
        return command!=null&&msgId!=null&&msg!=null;
    }
}
