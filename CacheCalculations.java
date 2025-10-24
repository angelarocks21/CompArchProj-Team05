/* Cache Calculations for Part 2 of Milestone 1
 * Author: Angela
 */

public class CacheCalculations {

	public static void main(String[] args) {
		
		// numbers used from doc
		int cacheSizeKB = 512;
		int blockSize = 16;
		int associativity = 4;
		int physicalMemoryMB = 1024;
		
		// calculations
		int blockOffsetSize = (int)(Math.log(blockSize) / Math.log(2));
		int totalBlocks = (cacheSizeKB * 1024) / blockSize;
		int totalRows = totalBlocks / associativity;
		int indexSize = (int)(Math.log(totalRows) / Math.log(2));
		
		double physicalMemoryMBLog2 = (int)(Math.log(physicalMemoryMB) / Math.log(2));
		int physicalAddressBits = (int) Math.round(physicalMemoryMBLog2 + 20.0);
		int tagSize = physicalAddressBits - indexSize - blockOffsetSize;
		
		int overheadSizeBytes = totalBlocks * (tagSize + 1)/ 8;
		int implementationMemoryBytes = (cacheSizeKB * 1024) + overheadSizeBytes;
		double implementationMemoryKB = implementationMemoryBytes / 1024.0;
		double cost = implementationMemoryKB * 0.07;
		
		System.out.println("***** Cache Calculated Values *****\n");
		System.out.printf("Total # Blocks:                      %d\n", totalBlocks);
		System.out.printf("Tag Size:                            %d bits\n", tagSize);
		System.out.printf("Index Size:                          %d bits\n", indexSize);
		System.out.printf("Total # Rows:                        %d\n", totalRows);
		System.out.printf("Overhead Size:                       %d bytes\n", overheadSizeBytes);
		System.out.printf("Implementation Memory Size:          %.2f KB (%d bytes)\n", implementationMemoryKB, implementationMemoryBytes);
		System.out.printf("Cost:                                $%.2f @ $0.07 per KB\n", cost);
		

	}

}
