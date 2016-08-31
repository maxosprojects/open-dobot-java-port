package org.omilab.omirob.streaming;

import org.omilab.omirob.Settings;
import org.omilab.omirob.streaming.WSSessions;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

@ClientEndpoint
@ServerEndpoint(value="/stream/output")
public class ServiceWS
{
    private static final String STREAM_MAGIC_BYTES ="jsmp" ;

    public ServiceWS(){
        System.out.println("ServiceWS()");
    }
    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
        System.out.println("Socket Connected: " + sess);
        try {
            ByteBuffer b= ByteBuffer.allocate(8);
            b.order(ByteOrder.BIG_ENDIAN);
            b.put(STREAM_MAGIC_BYTES.getBytes(StandardCharsets.US_ASCII));
            b.putShort((short) Settings.width);
            b.putShort((short) Settings.height);
            b.rewind();
            sess.getBasicRemote().sendBinary(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        WSSessions.addSession(sess);
    }

    @OnMessage
    public void onWebSocketText(String message)
    {
        System.out.println("Received TEXT message: " + message);
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        System.out.println("Socket Closed: " + reason);
    }

    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }
}