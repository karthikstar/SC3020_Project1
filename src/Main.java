import utils.DataInitialiser;

import java.io.File;
import java.util.Scanner;

/**
 * The Main Class serves as the entry point into the application
 */
public class Main {
    private static final int MAX_DISK_CAPACITY_DEFAULT = (int) Math.pow(10, 6) * 500;

    public static void main(String[] args) throws Exception {
        System.out.println("Welcome to..");
        System.out.println("\n" +
                "  ___   _ _____ _   ___   _   ___ ___ \n" +
                " |   \\ /_\\_   _/_\\ | _ ) /_\\ / __| __|\n" +
                " | |) / _ \\| |/ _ \\| _ \\/ _ \\\\__ \\ _| \n" +
                " |___/_/ \\_\\_/_/ \\_\\___/_/ \\_\\___/___|\n" +
                "                                      \n");
        File f = new File("games.txt");

        if (f.exists()) {
            System.out.println("File Found! Loading Data..");
            int diskCapacity = getCapacityChoice();
            System.out.println("Chosen Disk Capacity (MB):" + diskCapacity / (int) (Math.pow(10, 6)));
            DataInitialiser.readFile("games.txt", diskCapacity);

        } else {
            System.out.println("File not found! Please download the games.txt file from Dropbox, save it in the project folder and restart this application.");
        }

    }

    /**
     * Get a choice for disk capacity from the user, between the range 100 - 500 MB
     * @return disk capacity choice entered by the user
     */
    private static int getCapacityChoice() {
        int flag = 0;
        Scanner sc = new Scanner(System.in);

        while (flag == 0) {
            try {
                System.out.println("Please enter your desired disk capacity (between 100-500MB):");

                int diskCapacity = sc.nextInt();
                if (diskCapacity >= 100 && diskCapacity <= 500) {
                    return (int) Math.pow(10, 6) * diskCapacity;
                } else {
                    System.out.printf("Invalid disk capacity entered. Reverting back to default disk capacity of %d MB\n", (MAX_DISK_CAPACITY_DEFAULT / (int) Math.pow(10,6)));
                    return MAX_DISK_CAPACITY_DEFAULT;
                }
            } catch (Exception e) {
                System.out.printf("Invalid disk capacity entered. Reverting back to default disk capacity of %d MB\n", (MAX_DISK_CAPACITY_DEFAULT / (int) Math.pow(10,6)));
                return MAX_DISK_CAPACITY_DEFAULT;
            }
        }
        return MAX_DISK_CAPACITY_DEFAULT;
    }
}