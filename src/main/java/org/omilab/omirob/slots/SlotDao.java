package org.omilab.omirob.slots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by martink82cs on 16.09.2016.
 */
public class SlotDao {

    private final static Logger logger = LoggerFactory.getLogger(SlotDao.class);
    private static HashMap<Integer, Slot> _slots;

    public static void save(HashMap<Integer, Slot> s){
        synchronized (SlotDao.class) {
            _slots = s;
            writeSlots(_slots, "slots.txt");
        }
    }

    private static void writeSlots(HashMap<Integer, Slot> slots, String fileName){
        try (BufferedWriter writer=new BufferedWriter(new FileWriter(fileName))) {
            for(Slot s: slots.values()){
                writer.write(String.valueOf(s.which));
                writer.write(" ");
                writer.write(s.userName);
                writer.write(" ");
                writer.write(s.secret);
                writer.write("\r\n");
            }
          } catch (IOException ee) {
            ee.printStackTrace();
        }
    }

    private static HashMap<Integer, Slot> readSlots(String fileName) {
        HashMap<Integer, Slot> slots=_slots;
        if(slots==null)
            slots=new HashMap<>();

            File f = new File(fileName);
            if (!f.exists())
                return new HashMap<>();
            try (Scanner s = new Scanner(new FileReader(fileName))) {
                while (s.hasNext()) {
                    Slot slot = new Slot();
                    slot.which = s.nextInt();
                    slot.userName = s.next();
                    slot.secret = s.next();
                    slots.put(slot.which, slot);
                    s.nextLine();
                }
            } catch (IOException e) {
                logger.warn("readSlots failed", e);
                return new HashMap<>();
            }
            return slots;
    }

    public static HashMap<Integer, Slot> getSlots() {
        synchronized (SlotDao.class) {
            if (_slots == null)
                _slots = readSlots("slots.txt");
        }
        return new HashMap<Integer, Slot>(_slots);
    }

    public static void clear() {
        logger.info("clear slot database");
        synchronized (SlotDao.class) {
            save(new HashMap<>());
        }
    }
}
