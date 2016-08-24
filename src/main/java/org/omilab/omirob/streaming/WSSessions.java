package org.omilab.omirob.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Martin on 10.08.2016.
 */
public class WSSessions {
    private static List<ClientSession> sessions=new ArrayList<ClientSession>();
    private final static Logger logger = LoggerFactory.getLogger(WSSessions.class);

    private static final int BUFFER_SIZE_MB=1;
    private static final int BUFFER_SIZE_BYTE=BUFFER_SIZE_MB*1024*1024;
    private static final byte[] buffer=new byte[BUFFER_SIZE_BYTE];
    private static volatile int writepos=0;

    public static void addSession(Session session){
       synchronized (sessions) {
           ClientSession s = new ClientSession(session, buffer, writepos);
           sessions.add(s);
           logger.info(String.format("Client added: %s; count: %d",s.getSession().getId(),sessions.size()));
       }
    }


    public static int feed(InputStream is) throws IOException {
        int count=is.read(buffer, writepos, buffer.length-writepos);
        if(count>0)
            writepos=(writepos+count)%BUFFER_SIZE_BYTE;
        return count;
    }


    public synchronized static void send(){
        synchronized (sessions){
            Iterator<ClientSession> i = sessions.iterator();
            while (i.hasNext()) {
                ClientSession s = i.next(); // must be called before you can call i.remove()
                s.send(writepos);
                if(!s.isAlive()){
                    i.remove();
                    logger.info(String.format("Client removed: %s; count: %d",s.getSession().getId(),sessions.size()));
                }
            }}
    }
}
