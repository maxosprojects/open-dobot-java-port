package org.omilab.omirob;

import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import java.nio.ByteBuffer;

/**
 * Created by Martin on 12.08.2016.
 */
public class ClientSession implements SendHandler {
    private final Session session;
    private ByteBuffer byteBuffer;
    private volatile int writepos=0;
    private volatile boolean alive=true;

    public ClientSession(Session session, byte[] buffer) {
        this.session=session;
        byteBuffer=ByteBuffer.wrap(buffer);
    }

    public void sendAsync(int writepos) {
        this.writepos=writepos;
        send();
    }

    private synchronized void send(){
        if(byteBuffer.position()==byteBuffer.capacity())
            byteBuffer.position(0);
        byteBuffer.limit(writepos);
        if(byteBuffer.limit()>byteBuffer.position())
            session.getAsyncRemote().sendBinary(byteBuffer, this);
    }

    @Override
    public void onResult(SendResult sendResult) {
        if(sendResult.isOK())
            send();
        else alive=false;
    }

    public boolean isAlive() {
        return alive;
    }

    public Session getSession() {
        return session;
    }
}
