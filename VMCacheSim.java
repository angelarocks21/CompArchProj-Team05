import java.io.*;
import java.util.*;

class MemoryAccess {
    	long address;
    	int length;

		MemoryAccess(long address, int length) {
			this.address = address;
			this.length = length;
		}

		@Override
		public String toString() {
			return String.format("Address: 0x%X, Length: %d bytes", address, length);
    	}
}

public class VMCacheSim {
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
	    	System.out.println("         " + file);
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
		
		/*  MILESTONE TWO  */
		
		System.out.println("\n***** VIRTUAL MEMORY SIMULATION RESULTS *****\n");
		System.out.printf("Physical Pages Used By SYSTEM:  %d%n", pagesForSystem);
		long pagesAvailableToUser = numPhysPages - pagesForSystem;
		System.out.printf("Pages Available to User:         %d%n%n", pagesAvailableToUser);
		
		List<List<MemoryAccess>> allProcesses = new ArrayList<>();
		
        List<Long> mappedPages = new ArrayList<>();
        long pageTableHits = 0;
        long pagesFromFree = 0;
        long totalPageFaults = 0;
        long virtualPagesMapped = 0;
		
		for (String traceFile : traceFiles) {
			List<MemoryAccess> accesses = new ArrayList<>();
			System.out.println("Reading " + traceFile + "...");

		try (BufferedReader br = new BufferedReader(new FileReader(traceFile))) {
			String line;
			int lineCount = 0;

			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) continue;

				if (line.startsWith("EIP")) {
					String lengthStr = line.substring(5, 7); // characters 5-6
					int length = Integer.parseInt(lengthStr, 16); // hex -> int

					String addrStr = line.substring(10, 18); // characters 10-17
					long address = Long.parseLong(addrStr, 16);

					accesses.add(new MemoryAccess(address, length));
				}

				else if (line.startsWith("dstM")) {
					String dstStr = line.substring(6, 14); // 6-13
					if (!dstStr.equals("00000000") && !dstStr.equals("--------")) {
						long dstAddr = Long.parseLong(dstStr, 16);
						accesses.add(new MemoryAccess(dstAddr, 4)); // dstM always 4 bytes
					}

					String srcStr = line.substring(33, 41); // 24-31
					if (!srcStr.equals("00000000") && !srcStr.equals("--------")) {
						long srcAddr = Long.parseLong(srcStr, 16);
						accesses.add(new MemoryAccess(srcAddr, 4)); // srcM always 4 bytes
					}
				}

				lineCount++;
				// Optional: stop early for testing small files
				if (lineCount >= 20) break;
			}

			System.out.println("Parsed " + accesses.size() + " entries from " + traceFile);
			System.out.println("Memory accesses:");
                for (MemoryAccess ma : accesses) {
                    System.out.println(ma);
                }

		} catch (IOException e) {
			System.err.println("Error reading " + traceFile + ": " + e.getMessage());
		}

		allProcesses.add(accesses);
		
		/*------------MAPPING----------------*/
        
        for (List<MemoryAccess> process : allProcesses) {
            for (MemoryAccess access : process) {
                long virtPage = access.address / pageSizeBytes;

                boolean alreadyMapped = mappedPages.contains(virtPage);

                if (!alreadyMapped) {
                    virtualPagesMapped++;

                    if (mappedPages.size() < pagesAvailableToUser) {
                        // allocate a free page
                        mappedPages.add(virtPage);
                        pagesFromFree++;
                    } else {
                        // no free physical page → page fault
                        totalPageFaults++;
                    }
                } else {
                    // hit
                    pageTableHits++;
                }
            }
        }
		
	}
		System.out.println("\nSummary of parsed traces:");
		for (int i = 0; i < allProcesses.size(); i++) {
			System.out.println("Process " + i + ": " + allProcesses.get(i).size() + " memory accesses");
		}
		
		// ---- Formatted Output Of Mapping ----
        System.out.printf("Virtual Pages Mapped:           %d%n", virtualPagesMapped);
        System.out.println("        ------------------------------");
        System.out.printf("        Page Table Hits:        %d%n", pageTableHits);
        System.out.printf("        Pages from Free:         %d%n", pagesFromFree);
        System.out.printf("        Total Page Faults:       %d%n", totalPageFaults);
	}
}

