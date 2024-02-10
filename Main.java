package main;

import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scnr = new Scanner(System.in);

        // options for what to do when run
        System.out.println("Enter command or enter nothing to start to program");

        String command = scnr.nextLine();
        // clear .txt files
        if (command.equals("clear")) {
            boolean recentFileCouldDelete = true;
            int fileNum = 0;
            while (recentFileCouldDelete) {
                File file = new File(fileNum + ".txt");
                try {
                    recentFileCouldDelete = file.delete();
                    System.out.println("File " + fileNum + ".txt has been deleted");
                    fileNum++;
                } catch (Exception e){
                    System.out.println("No more files to delete found");
                }
            }
            System.exit(0);
        }

        System.out.println("Enter number of individuals. Must be a perfect square number");
        int individuals = scnr.nextInt();
        while (Math.pow(Math.sqrt(individuals), 2) != individuals) {
            System.out.println("Number must be a perfect square. ex: 4, 9, 16, 25. Please try again");
            individuals = scnr.nextInt();
        }
        System.out.println("Enter number of time steps");
        int timeSteps = scnr.nextInt();
        System.out.println("Please enter an infection rate from 0.00 - 1.00 inclusive");
        double infectionRate = scnr.nextDouble();
        while (infectionRate < 0.0 || infectionRate > 1.0) {
            System.out.println("Infection rate must be from 0.00 - 1.00 inclusive. Please try again");
            infectionRate = scnr.nextDouble();
        }

        System.out.println("Please enter an recover rate from 0.00 - 1.00 inclusive");
        double recoverRate = scnr.nextDouble();
        while (recoverRate < 0.0 || recoverRate > 1.0) {
            System.out.println("Recover rate must be from 0.00 - 1.00 inclusive. Please try again");
            recoverRate = scnr.nextDouble();
           
        }

        // grid width is = to height since individuals should be perfect square
        int gridWidth = (int) Math.sqrt(individuals);

        char[][] grid = new char[gridWidth][gridWidth];
        Random rand = new Random();

        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridWidth; j++) {
                grid[i][j] = 'S';
            }
        }

        // select a patient 0
        int patientZeroX = rand.nextInt(0, gridWidth-1);
        int patientZeroY = rand.nextInt(0, gridWidth-1);
        grid[patientZeroX][patientZeroY] = 'I';
        
        // Counts of the status of individuals to output
        int susceptibleCount = individuals;
        int infectedCount = 0;
        int recoveredCount = 0;

        writeGridAsCSV(grid, 0);
        System.out.println("Patient Zero is located at row " + patientZeroX + ", column " + patientZeroY);
        System.out.println("");
        
        
        //Outputs each time step along with information on each
        for (int i = 0 ; i < timeSteps; i++) {
        	infectedCount = 0;
         	recoveredCount = 0;
         	susceptibleCount = individuals;
        	grid = calculateTimestep(grid, infectionRate, recoverRate);
            writeGridAsCSV(grid, i+1);
            for (int a = 0; a < grid.length; a++) {
                for (int b = 0; b < grid.length; b++) {
                	if (grid[a][b] == 'I') {
                		infectedCount++;
                		susceptibleCount--;
                	}
                	if (grid[a][b] == 'R') {
                		//infectedCount--;
                		recoveredCount++;
                		susceptibleCount--;
                	}
                	if (infectedCount < 0) {
                		infectedCount = 0;
                	}
                }
            }
            System.out.println("Timestep " + (i + 1));
            System.out.println(gridToString(grid));
            System.out.println("Number of susceptible: " + susceptibleCount + ", Number of infected: " + infectedCount + ", Number of recovered: "  + recoveredCount);
            System.out.println("Percent infected: " + (100.0 *((double)infectedCount/(double)individuals)));
            System.out.println("");
        }
    }

    private static char[][] calculateTimestep(char[][] grid, double infectionRate, double recoverRate) {

        Random rand = new Random();

        // There are multiple ways of doing this, but I ended up choosing this way for the fun of it
        // also should be faster since instead of counting # of infected neighbors for every tile
        // during each timestep, it just goes through grid once adding up 1 to the count of neighboring infected
        // individuals of each cell, and its only doing the adding for if it finds an infected individual anyways
        // -> filters out cells that don't have any neighboring infected individuals

        // keeps track of number of neighbors of each tile on grid
        int[][] neighborCounts = new int[grid.length][grid.length];
        
        // calculate number of neighbors for all tiles
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                if (grid[i][j] == 'I') {
                    if (i + 1 < grid.length) {
                        neighborCounts[i+1][j]++;
                    }
                    if (i - 1 >= 0) {
                        neighborCounts[i-1][j]++;
                    }
                    if (j + 1 < grid.length) {
                        neighborCounts[i][j+1]++;
                    }
                    if (j - 1 >= 0) {
                        neighborCounts[i][j-1]++;	
                    }
                    // person recovers here
                    if (rand.nextDouble() < recoverRate) {
                        grid[i][j] = 'R';
                        
                    }
                }
            }
        }

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                if (grid[i][j] == 'S') {
                    if (rand.nextDouble() < infectionRate * neighborCounts[i][j]) {
                        grid[i][j] = 'I';
                        
                    }
                }
            }
        }
        return grid;
    }


    private static String gridToString(char[][] grid) {
        // string builder is faster than string concatenation. worth using when doing so much of it
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length - 1; j++) {
                stringBuilder.append(grid[i][j] + ", ");
            }
            stringBuilder.append(grid[i][grid.length - 1]);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }


    /**
     *
     * @param grid the current grid
     * @param timeStepNumber the current timestep that the input grid occurs on
     */	
    private static void writeGridAsCSV(char[][] grid, int timeStepNumber) {

        File file = new File(timeStepNumber + ".txt");
        try {
            // don't need result
            file.delete();
        } catch (Exception e) {
            // shouldn't have to do anything with this
            // if the file doesn't exist than that's fine because we're creating it anyways
        }
        try {
            // don't need result
            file.createNewFile();
        } catch (Exception e) {
            // this should never be running
            e.printStackTrace();
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gridToString(grid));
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}