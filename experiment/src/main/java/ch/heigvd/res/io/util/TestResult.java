package ch.heigvd.res.io.util;

import ch.heigvd.res.io.BufferedIOBenchmark;

/**
 * This represents a result for a test of IO Benchmarking
 *
 * @author Benjamin Schubert
 */

public class TestResult {
    private final OperationType operation;
    private final BufferedIOBenchmark.IOStrategy strategy;
    private final int blockSize;
    private final long fileSizeInBytes;
    private final long durationInMs;

    public TestResult(OperationType op, BufferedIOBenchmark.IOStrategy stg, int bsize, long fsize, long duration) {
        operation = op;
        strategy = stg;
        blockSize = bsize;
        fileSizeInBytes = fsize;
        durationInMs = duration;
    }

    public OperationType getOperation() {
        return operation;
    }

    public BufferedIOBenchmark.IOStrategy getStrategy() {
        return strategy;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public long getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    public long getDurationInMs() {
        return durationInMs;
    }
}
