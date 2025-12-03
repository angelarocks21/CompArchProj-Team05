import java.util.Random;

public class CacheSimulator {

    int cacheSizeKB;
    int blockSize;
    int associativity;
    boolean useRR;

    int totalBlocks;
    int totalRows;
    int indexBits;
    int offsetBits;
    int tagBits;

    CacheLine[][] cache;
    int[] rrPointer;

    long totalAccesses = 0;
    long instrBytes = 0;
    long dataBytes = 0;
    long hits = 0;
    long misses = 0;
    long compulsoryMisses = 0;
    long conflictMisses = 0;

    long totalCycles = 0;
    long totalInstructions = 0;

    Random rnd = new Random();

    public CacheSimulator(int cacheSizeKB, int blockSize, int associativity,
                          boolean useRR, int physAddrBits) {

        this.cacheSizeKB = cacheSizeKB;
        this.blockSize = blockSize;
        this.associativity = associativity;
        this.useRR = useRR;

        offsetBits = (int)(Math.log(blockSize) / Math.log(2));
        totalBlocks = (cacheSizeKB * 1024) / blockSize;
        totalRows = totalBlocks / associativity;
        indexBits = (int)(Math.log(totalRows) / Math.log(2));
        tagBits = physAddrBits - indexBits - offsetBits;

        cache = new CacheLine[totalRows][associativity];
        rrPointer = new int[totalRows];

        for (int i = 0; i < totalRows; i++)
            for (int j = 0; j < associativity; j++)
                cache[i][j] = new CacheLine();
    }

    public void access(long vaddr, int length, boolean instr) {

    if (instr) {                 //FIZA - POTENTIAL ISSUE
        instrBytes += length;
        totalInstructions++;
    } else {
        dataBytes += length;
    }
    
    //Angela - this is what I added to calculate cache hits and accesses
        long startBlock = vaddr >>> offsetBits;
        long endAddr = vaddr + (length - 1);
        long endBlock = endAddr >>> offsetBits;
        
        for (long b = startBlock; b <= endBlock; b++) { //hit rate is correct but  unused cache space 
            simulateAccess(vaddr);
        }
        //simulateAccess(vaddr); //i attempted to loop did not work
    
}

    private void simulateAccess(long addr) {

        totalAccesses++;

        long blockAddr = addr >>> offsetBits;
        int row = (int)(blockAddr & (totalRows - 1));
        long tag = blockAddr >>> indexBits;

        // HIT?
        for (int w = 0; w < associativity; w++) {
            if (cache[row][w].valid && cache[row][w].tag == tag) {
                hits++;
                totalCycles += 1;
                return;
            }
        }

        // MISS
        misses++;

        // COMPULSORY?
        for (int w = 0; w < associativity; w++) {
            if (!cache[row][w].valid) {
                compulsoryMisses++;
                fill(row, w, tag);
                return;
            }
        }

        // CONFLICT
        conflictMisses++;

        int victim = useRR ? rrPointer[row] : rnd.nextInt(associativity);
        if (useRR) rrPointer[row] = (rrPointer[row] + 1) % associativity;

        fill(row, victim, tag);
    }

    private void fill(int row, int way, long tag) {

        cache[row][way].valid = true;
        cache[row][way].tag = tag;

        int memReads = blockSize / 4;
        if (blockSize % 4 != 0) memReads++;

        totalCycles += memReads * 4;
    }

    public void printResults(long sysPages, long freePages,
                             int overheadBytes, int implBytes) {

        System.out.println("\n***** CACHE SIMULATION RESULTS *****\n");

        System.out.printf("Total Cache Accesses:   %d  (%d addresses)%n",
                totalAccesses, totalAccesses);
        System.out.printf("--- Instruction Bytes:  %d%n", instrBytes);
        System.out.printf("--- SrcDst Bytes:       %d%n", dataBytes);

        System.out.printf("Cache Hits:             %d%n", hits);
        System.out.printf("Cache Misses:           %d%n", misses);
        System.out.printf("--- Compulsory Misses:  %d%n", compulsoryMisses);
        System.out.printf("--- Conflict Misses:    %d%n", conflictMisses);

        double hitRate = hits * 100.0 / totalAccesses;
        double missRate = 100.0 - hitRate;

        double CPI = (totalInstructions == 0) ? 0.0 : (double)(totalCycles / totalInstructions);

        System.out.println("\n***** *****  CACHE HIT & MISS RATE:  ***** *****\n");
        System.out.printf("Hit  Rate:              %.4f%%%n", hitRate);
        System.out.printf("Miss Rate:              %.4f%%%n", missRate);
        System.out.printf("CPI:                    %.2f Cycles/Instruction  (%d)%n",
                CPI, totalCycles);

        int unusedBlocks = totalBlocks - (int)compulsoryMisses;

        double unusedKB = unusedBlocks *
                (blockSize + (overheadBytes / (double)totalBlocks)) / 1024.0;

        double wasteCost = unusedKB * 0.07;

        System.out.printf("Unused Cache Space:     %.2f KB / %.2f KB = %.2f%%  Waste: $%.2f%n",
                unusedKB,
                implBytes / 1024.0,
                (unusedKB / (implBytes / 1024.0)) * 100.0,
                wasteCost);

        System.out.printf("Unused Cache Blocks:    %d / %d%n",
                unusedBlocks, totalBlocks);
    }

    private static class CacheLine {
        boolean valid = false;
        long tag = 0;
    }
}

