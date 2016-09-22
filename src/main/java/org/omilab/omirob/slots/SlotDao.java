package org.omilab.omirob.slots;

import org.omilab.omirob.microservice.PSMConnectorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Created by martink82cs on 16.09.2016.
 */
public class SlotDao {

    private final static Logger logger = LoggerFactory.getLogger(SlotDao.class);
    private final static Object slotsFileLock=new Object();

    public static void writeSlots(Slots slots, String fileName){
        synchronized (slotsFileLock){
        try (BufferedWriter writer=new BufferedWriter(new FileWriter(fileName))) {
            for(Slot s: slots.slots.values()){
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
    }

    public static Slots readSlots(String fileName) {
        synchronized (slotsFileLock) {


            Slots slots = new Slots();
            File f = new File(fileName);
            if (!f.exists())
                return new Slots();
            try (Scanner s = new Scanner(new FileReader(fileName))) {
                while (s.hasNext()) {
                    Slot slot = new Slot();
                    slot.which = s.nextInt();
                    slot.userName = s.next();
                    slot.secret = s.next();
                    slots.slots.put(slot.which, slot);
                    s.nextLine();
                }
            } catch (IOException e) {
                logger.warn("readSlots failed", e);
                return new Slots();
            }
            return slots;
        }
    }
}
