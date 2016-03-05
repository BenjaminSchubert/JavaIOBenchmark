package ch.heigvd.res.io.util;

import java.io.*;

/**
 * This is a logger for TestResult objects
 *
 * @author Benjamin Schubert
 */
public abstract class AbstractTestResultLogger implements AutoCloseable {
    abstract String format(TestResult result);
    private OutputStreamWriter output = null;

    public AbstractTestResultLogger(OutputStream out) {
        output = new OutputStreamWriter(new BufferedOutputStream(out));
    }

    protected void write(String str) throws IOException{
        output.write(str);
        output.flush();
    }

    public void log(TestResult result) throws IOException {
        write(format(result));
    }

    @Override
    public void close() throws IOException {
        if(output != null) {
            output.close();
        }
    }
}
