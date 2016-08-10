package org.omilab.omirob;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Martin on 10.08.2016.
 */
public class WSSessions {
    private static List<Session> sessions=new ArrayList<Session>();

    public static void addSession(Session session){
        sessions.add(session);
    }

    public static void send(ByteBuffer data){
        Iterator<Session> i = sessions.iterator();
        while (i.hasNext()) {
            Session s = i.next(); // must be called before you can call i.remove()
            try {
                data.rewind();
                s.getBasicRemote().sendBinary(data);
            } catch (IOException e) {
                e.printStackTrace();
                i.remove();
            }

        }

    }


}
