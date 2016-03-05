package ch.heigvd.res.io.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is a logger for TestResults that logs them in a cvs format
 *
 * @author Benjamin Schubert
 */
public class CSVResultLogger extends AbstractTestResultLogger {
    private final String header = "operation,strategy,blockSize,fileSizeInBytes,durationInMs\n";

    public CSVResultLogger(OutputStream out) throws IOException {
        super(out);
        write(header);
    }

    @Override
    String format(TestResult result) {
        return result.getOperation() + "," +
                result.getStrategy() + "," +
                result.getBlockSize() + "," +
                result.getFileSizeInBytes() + "," +
                result.getDurationInMs() + "\n";
    }
}
