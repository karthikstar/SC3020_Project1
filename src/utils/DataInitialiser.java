package utils;

import database.Address;
import database.Disk;
import database.Record;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataInitialiser {

    public static final int BLOCK_SIZE = 400;
    public static void readFile(String filepath, int diskCapacity) {
        try {

            // Initialise Disk
            Disk disk = new Disk(BLOCK_SIZE, diskCapacity);
            // Initialise B+ Tree
            // TODO: B+ Tree intitialisation

            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String line;
            int count = 0;
            br.readLine(); // Skip 1st line, as it refers to the column names

            while ((line = br.readLine()) != null) {
                System.out.println(line); // Print each line to the console
                count++;
                String[] attributes = line.split("\t");

                // Create a SimpleDateFormat object with the date format used in the file
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                long GAME_DATE_EST = dateFormat.parse(attributes[0]).getTime(); // 8
                int TEAM_ID_home = Integer.parseInt(attributes[1]); // 4
                int PTS_HOME = Integer.parseInt(attributes[2]); // 4
                float FG_PCT_home = Float.parseFloat(attributes[3]); //4
                float FT_PCT_home = Float.parseFloat(attributes[4]); //4

                float FG3_PCT_home = Float.parseFloat(attributes[5]); //4
                int AST_home = Integer.parseInt(attributes[6]); //4
                int REB_home = Integer.parseInt(attributes[7]); //4
                int HOME_TEAM_WINS = Integer.parseInt(attributes[8]); //4

                // Total 40 bytes

                System.out.println(GAME_DATE_EST);

                Date date = new Date(GAME_DATE_EST);
                String formattedDate = dateFormat.format(date);
                System.out.println(date);
                System.out.println(formattedDate);

                System.out.println(TEAM_ID_home);
                System.out.println(PTS_HOME);
                System.out.println(FG_PCT_home);


                Record newRecord = generateRecord(GAME_DATE_EST, TEAM_ID_home, PTS_HOME, FG_PCT_home, FT_PCT_home,
                        FG3_PCT_home, AST_home, REB_home, HOME_TEAM_WINS);

//                Address addr = disk.writeRecToDisk(newRecord);

                break;
            }
            System.out.println("Total no. of rows read: " + count);



        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public static Record generateRecord(long GAME_DATE_EST, int TEAM_ID_home, int PTS_HOME, float FG_PCT_home, float FT_PCT_home,
                                        float FG3_PCT_home, int AST_home, int REB_home, int HOME_TEAM_WINS) {

        Record newRecord = new Record(GAME_DATE_EST, TEAM_ID_home, PTS_HOME, FG_PCT_home, FT_PCT_home,
                FG3_PCT_home, AST_home, REB_home, HOME_TEAM_WINS) {
        };

        return newRecord;
    }

}
