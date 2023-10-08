package utils;

import BplusTree.Tree;
import database.Address;
import database.Disk;
import database.Record;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * The DataInitialiser Class handles reading of file, and generation of records from it
 */
public class DataInitialiser {

    public static final int BLOCK_SIZE = 400;
    public static final int OVERHEAD = 8;
    public static final int POINTER_SIZE = 8;
    public static final int KEY_SIZE = 4;

    /**
     * Handles the reading of the games.txt file line by line, generation of records, writing to the disk and insertion into the B+ Tree
     * @param filepath the filepath of the data file
     * @param diskCapacity the disk capacity entered by the user
     */
    public static void readFile(String filepath, int diskCapacity) {
        try {

            // Initialise Disk
            Disk disk = new Disk(BLOCK_SIZE, diskCapacity);
            // Initialise B+ Tree
            Tree tree = new Tree();

            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String line;
            int count = 0;
            int rowsWithMissingCount = 0;

            br.readLine(); // Skip 1st line, as it refers to the column names

            while ((line = br.readLine()) != null) {
                int missingValueFlag = 0;
//                System.out.println(line); // Print each line to the console
                count++;
                String[] attributes = line.split("\t");

                // Check if any of the attributes have missing values
                for (String attr: attributes) {
                    if(attr == ""){
                        rowsWithMissingCount++;
                        missingValueFlag = 1;
                        break;
                    }
                }

                if(missingValueFlag == 1) continue; // skip reading of this line, and go on to the next line

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
//                System.out.println(GAME_DATE_EST);

                Date date = new Date(GAME_DATE_EST);
                String formattedDate = dateFormat.format(date);


                Record newRecord = generateRecord(GAME_DATE_EST, TEAM_ID_home, PTS_HOME, FG_PCT_home, FT_PCT_home,
                        FG3_PCT_home, AST_home, REB_home, HOME_TEAM_WINS);

                Address address = disk.writeRecToDisk(newRecord);

                float key = newRecord.getFG_PCT_home();
                tree.addRecord(key, address);
            }

            br.close();
            System.out.println("---------------");
            System.out.println("Total no. of rows read (excl. row with col. names): " + count);
            System.out.println("Rows with missing values that will be ignored: " + rowsWithMissingCount);
            System.out.println("---------------");

            while(true) {
                System.out.println("Please select one of the options below: ");
                System.out.println("1: Run Experiment 1");
                System.out.println("2: Run Experiment 2");
                System.out.println("3: Run Experiment 3");
                System.out.println("4: Run Experiment 4");
                System.out.println("5: Run Experiment 5");
                System.out.println("0: Exit Application");

                Scanner sc = new Scanner(System.in);
                int userChoice = sc.nextInt();



                if(userChoice == 0) break;

                switch(userChoice) {
                    case 1:
                        disk.runExptOne();
                        break;
                    case 2:
                        Tree.runExptTwo(tree);
                        break;
                    case 3:
                        Tree.runExptThree(disk,tree);
                        break;
                    case 4:
                        Tree.runExptFour(disk,tree);
                        break;
                    case 5:
                        Tree.runExptFive(disk, tree);
                        break;
                    default:
                        System.out.println("Please select an option between 0 - 5!");
                        break;
                }

            }




        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Handles the generation of Record Objects for each of the line read by readFile()
     * @param GAME_DATE_EST a long value representing a date
     * @param TEAM_ID_home an integer value
     * @param PTS_HOME an integer value
     * @param FG_PCT_home a float value
     * @param FT_PCT_home a float value
     * @param FG3_PCT_home a float value
     * @param AST_home an integer value
     * @param REB_home an integer value
     * @param HOME_TEAM_WINS an integer value
     * @return a Record Object
     */
    public static Record generateRecord(long GAME_DATE_EST, int TEAM_ID_home, int PTS_HOME, float FG_PCT_home, float FT_PCT_home,
                                        float FG3_PCT_home, int AST_home, int REB_home, int HOME_TEAM_WINS) {

        Record newRecord = new Record(GAME_DATE_EST, TEAM_ID_home, PTS_HOME, FG_PCT_home, FT_PCT_home,
                FG3_PCT_home, AST_home, REB_home, HOME_TEAM_WINS) {
        };

        return newRecord;
    }

}
