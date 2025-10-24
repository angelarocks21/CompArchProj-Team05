/*
 IMPORTANT NOTES (From Angela) :
 All of our parts are combined in this file
 The outputs are formatted as needed for submission (we may have to double check for trace files)
 All variables were updated to follow camel casing for uniformity as it's commonly used in Java
 Any duplicates of variables that we each may have declared have been removed and/or renamed to avoid redundancy
 
 Something that needs to get checked is if the program works to receive input from command line and produces the correct output accordingly
 Also most of the variables are initialized/hard-coded to perform the calculations so we have to fix/change the variables to receive values through command line*/

import java.util.ArrayList;

public class MilestoneOne {
	
	public static void main(String[] args) {
		int cacheSizeKB = 512;
	    int blockSize = 16;
	    int associativity = 4;
	    String replacementPolicy = "";
	    int physicalMemoryMB = 1024;
	    int instructionsPerTimeSlice = 0;
	    double percentMemoryUsed = 0.0;
	    ArrayList<String> traceFiles = new ArrayList<>();
	    
	    for (int i = 0; i < args.length; i++) {
	    	switch (args[i]) {
	    	case "-s":
	    		cacheSizeKB = Integer.parseInt(args[++i]);
	            break;
	        case "-b":
	            blockSize = Integer.parseInt(args[++i]);
	            break;
	        case "-a":
	            associativity = Integer.parseInt(args[++i]);
	            break;
	        case "-r":
	            String policy_arg = args[++i];
	            if (policy_arg.equalsIgnoreCase("rr")) {
	            	replacementPolicy = "Round Robin";
	            } else {
	            	replacementPolicy = "Random Replacement";
	            }
	            break;
	        case "-p":
	        	physicalMemoryMB = Integer.parseInt(args[++i]);
	            break;
	        case "-n":
	            instructionsPerTimeSlice = Integer.parseInt(args[++i]);
	            break;
	        case "-u":
	            percentMemoryUsed = Double.parseDouble(args[++i]);
	            break;
	        case "-f":
	            traceFiles.add(args[++i]);
	            break;
	        default:
	            System.out.println("Unknown argument: " + args[i]);
	        }
	    }
	    
	    System.out.println("Cache Simulator - CS 3853 - Team #05\n");
	    System.out.println("Trace File(s):");
	    for (String file : traceFiles) {
	    	System.out.println(" " + file);
	    }
	    
	    System.out.println("\n***** Cache Input Parameters *****\n");
	    System.out.printf("Cache Size:                     %d KB\n", cacheSizeKB);
	    System.out.printf("Block Size:                     %d bytes\n", blockSize);
	    System.out.printf("Associativity:                  %d\n", associativity);
	    System.out.printf("Replacement Policy:             %s\n", replacementPolicy);
	    System.out.printf("Physical Memory:                %d MB\n", physicalMemoryMB);
	    System.out.printf("Percent Memory Used by System:  %.1f%%%n", percentMemoryUsed);
	    System.out.printf("Instructions / Time Slice:      %d%n\n", instructionsPerTimeSlice);
	    
	    /*----------------------------------*/
	    
		// cache calculations
		int blockOffsetSize = (int)(Math.log(blockSize) / Math.log(2));
		int totalBlocks = (cacheSizeKB * 1024) / blockSize;
		int totalRows = totalBlocks / associativity;
		int indexSize = (int)(Math.log(totalRows) / Math.log(2));
		double physicalMemoryMBLog2 = (int)(Math.log(physicalMemoryMB) / Math.log(2));
		int physicalAddressBits = (int) Math.round(physicalMemoryMBLog2 + 20.0); // needed for computing the correct tag
		int tagSize = physicalAddressBits - indexSize - blockOffsetSize;
		int overheadSizeBytes = totalBlocks * (tagSize + 1)/ 8;
		int implementationMemoryBytes = (cacheSizeKB * 1024) + overheadSizeBytes;
		double implementationMemoryKB = implementationMemoryBytes / 1024.0;
		double cost = implementationMemoryKB * 0.07;
		
		System.out.println("***** Cache Calculated Values *****\n");
		System.out.printf("Total # Blocks:                 %d\n", totalBlocks);
		System.out.printf("Tag Size:                       %d bits\n", tagSize);
		System.out.printf("Index Size:                     %d bits\n", indexSize);
		System.out.printf("Total # Rows:                   %d\n", totalRows);
		System.out.printf("Overhead Size:                  %d bytes\n", overheadSizeBytes);
		System.out.printf("Implementation Memory Size:     %.2f KB (%d bytes)\n", implementationMemoryKB, implementationMemoryBytes);
		System.out.printf("Cost:                           $%.2f @ $0.07 per KB\n\n", cost);
		
		/*-----------------------------------*/
			
		// Example inputs i got from org pdf
		double systemUsePct = 75.0; // % memory used by system
		int numTraceFiles = 3;      // number of trace files (.trc)
		int pageSizeBytes = 4096;   // 4KB page size
	    int pageTableEntries = 512 * 1024; // 512K entries per table
	    
	    // calculations
	    long physBytes = (long) physicalMemoryMB * 1024 * 1024;
		long numPhysPages = physBytes / pageSizeBytes;
		long pagesForSystem = Math.round((systemUsePct / 100.0) * numPhysPages);
		int ptePhysPageBits = (int) Math.ceil(Math.log(numPhysPages) / Math.log(2));
		int pteBits = 1 + ptePhysPageBits; // 1 valid bit + bits for physical page number
		long totalRAM = (long) pageTableEntries * numTraceFiles * pteBits / 8;
		
		// formatted print
		System.out.println("***** Physical Memory Calculated Values *****");
		System.out.println();
		System.out.printf("Number of Physical Pages:       %d%n", numPhysPages);
		System.out.printf("Number of Pages for System:     %d%n", pagesForSystem);
		System.out.printf("Size of Page Table Entry:       %d bits%n", pteBits);
		System.out.printf("Total RAM for Page Table(s):    %d bytes%n", totalRAM);
		
	}
}

