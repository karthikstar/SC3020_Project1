package utils;

import database.Disk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class DataInitialiser {

    public static final int BLOCK_SIZE = 400;
    public static void readFile(String filepath, int DataInitializer) {
        try {

            // Initialise Disk

            // Initialise B+ Tree
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String line;
            int count = 0;
            br.readLine(); // Skip 1st line, as it refers to the column names

            while ((line = br.readLine()) != null) {
                System.out.println(line); // Print each line to the console
                count++;
                String[] attributes = line.split("\t");

                String GAME_DATE_EST = attributes[0];
                int TEAM_ID_home = Integer.parseInt(attributes[1]);
                int PTS_HOME = Integer.parseInt(attributes[2]);
                float FG_PCT_home = Float.parseFloat(attributes[3]);
                float FT_PCT_home = Float.parseFloat(attributes[4]);

                float FG3_PCT_home = Float.parseFloat(attributes[5]);
                int AST_home = Integer.parseInt(attributes[6]);
                int REB_home = Integer.parseInt(attributes[7]);
                int HOME_TEAM_WINS = Integer.parseInt(attributes[8]);

                System.out.println(TEAM_ID_home);
                System.out.println(PTS_HOME);
                System.out.println(FG_PCT_home);

                break;
            }
            System.out.println("Total no. of rows read: " + count);



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
