package org.omilab.omirob.slots;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Created by martink82cs on 16.09.2016.
 */
public class SlotDao {
    public static void writeSlots(Slots slots, String fileName){
        try (BufferedWriter writer=new BufferedWriter(new FileWriter(fileName))) {
            for(Slot s: slots.slots){
                writer.write(s.which);
                writer.write(" ");
                writer.write(s.userName);
                writer.write(" ");
                writer.write(Long.toString(s.secret));
                writer.write("\n");
            }
          } catch (IOException ee) {
            ee.printStackTrace();
        }
    }

    public static Slots readSlots(String fileName){
        Slots slots=new Slots();
        try (Scanner s = new Scanner(new FileReader(fileName)))
        {
            Slot slot=new Slot();
            slot.which=s.nextInt();
            slot.userName=s.next();
            slot.secret=s.nextLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return slots;
    }
}
