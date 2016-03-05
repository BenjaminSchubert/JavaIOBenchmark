package ch.heigvd.res.io;

import ch.heigvd.res.io.util.*;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a very simple program, which main objective is to show that you can
 * observe very significant performance differences, depending on how you implement
 * IO processing. 
 * 
 * Running the program allows you to compare both the WRITING and the READING of
 * bytes to the local file system. Different methods are compared: processing bytes
 * one by one, processing bytes in blocks, using buffered streams or not.
 * 
 * @author Olivier Liechti
 * @author Benjamin Schubert
 */
public class BufferedIOBenchmark {

	static final Logger LOG = Logger.getLogger(BufferedIOBenchmark.class.getName());

	/**
	 * This enum is used to describe the 4 different strategies for doing the IOs
	 */
	public enum IOStrategy {
		ByteByByteWithoutBufferedStream,
		ByteByByteWithBufferedStream,
		BlockByBlockWithoutBufferedStream,
		BlockByBlockWithBufferedStream
	};

	final static String FILENAME_PREFIX = "test-data"; // we will write and read test files at this location
	final static long NUMBER_OF_BYTES_TO_WRITE = 1024 * 1024 * 100; // we will write and read 100 MB files
	
	/**
	 * This method drives the generation of test data file, based on the parameters passed. The method opens a
	 * FileOutputStream. Depending on the strategy, it wraps a BufferedOutputStream around it, or not. The method
	 * then delegates the actual production of bytes to another method, passing it the stream.
	 */
	private TestResult produceTestData(IOStrategy ioStrategy, long numberOfBytesToWrite, int blockSize) {
		LOG.log(Level.INFO, "Generating test data ({0}, {1} bytes, block size: {2}...", new Object[]{ioStrategy, numberOfBytesToWrite, blockSize});
		Timer.start();

		OutputStream os = null;
		try {
			// Let's connect our stream to a file data sink
			os = new FileOutputStream(FILENAME_PREFIX + "-" + ioStrategy + "-" + blockSize + ".bin");

			// If the strategy dictates to use a buffered stream, then let's wrap one around our file output stream
			if ((ioStrategy == IOStrategy.BlockByBlockWithBufferedStream) || (ioStrategy == IOStrategy.ByteByByteWithBufferedStream)) {
				os = new BufferedOutputStream(os);
			}

			// Now, let's call the method that does the actual work and produces bytes on the stream
			produceDataToStream(os, ioStrategy, numberOfBytesToWrite, blockSize);

			// We are done, so we only have to close the output stream
			os.close();
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}

		long timeTaken = Timer.takeTime();
		LOG.log(Level.INFO, "  > Done in {0} ms.", timeTaken);
		return new TestResult(OperationType.WRITE, ioStrategy, blockSize, numberOfBytesToWrite, timeTaken);
	}
	
	/**
	 * This method produces bytes on the passed stream (the method does not know this stream is buffered or not)
	 * Depending on the strategy, the method either writes bytes one by one OR in chunks (the size of the chunk
	 * is passed in parameter)
	 */ 
	private void produceDataToStream(OutputStream os, IOStrategy ioStrategy, long numberOfBytesToWrite, int blockSize) throws IOException {
		// If the strategy dictates to write byte by byte, then it's easy to write the loop; but let's just hope that our client has 
		// given us a buffered output stream, otherwise the performance will be really bad
		if ((ioStrategy == IOStrategy.ByteByByteWithBufferedStream) || (ioStrategy == IOStrategy.ByteByByteWithoutBufferedStream)) {
			for (int i = 0; i < numberOfBytesToWrite; i++) {
				os.write('h');
			}

			// If the strategy dictates to write block by block, then the loop is a bit longer to write
		} else {
			long remainder = numberOfBytesToWrite % blockSize;
			long numberOfBlocks = (numberOfBytesToWrite / blockSize);
			byte[] block = new byte[blockSize];

			// we start by writing a number of entire blocks
			for (int i = 0; i < numberOfBlocks; i++) {
				for (int j = 0; j < blockSize; j++) {
					block[j] = 'b';
				}
				os.write(block);
			}

			// and we write a partial block at the end
			if (remainder != 0) {
				for (int j = 0; j < remainder; j++) {
					block[j] = 'B';
				}
				os.write(block, 0, (int) remainder);
			}
		}
	}

	/**
	 * This method drives the consumption of test data file, based on the parameters passed. The method opens a
	 * FileInputStream. Depending on the strategy, it wraps a BufferedInputStream around it, or not. The method
	 * then delegates the actual consumption of bytes to another method, passing it the stream.
	 */
	private TestResult consumeTestData(IOStrategy ioStrategy, int blockSize) {
		LOG.log(Level.INFO, "Consuming test data ({0}, block size: {1}...", new Object[]{ioStrategy, blockSize});
        int dataRead = 0;
		Timer.start();

		InputStream is = null;
		try {
			// Let's connect our stream to a file data sink
			is = new FileInputStream(FILENAME_PREFIX + "-" + ioStrategy + "-" + blockSize + ".bin");

			// If the strategy dictates to use a buffered stream, then let's wrap one around our file input stream
			if ((ioStrategy == IOStrategy.BlockByBlockWithBufferedStream) || (ioStrategy == IOStrategy.ByteByByteWithBufferedStream)) {
				is = new BufferedInputStream(is);
			}

			// Now, let's call the method that does the actual work and produces bytes on the stream
			dataRead = consumeDataFromStream(is, ioStrategy, blockSize);

			// We are done, so we only have to close the input stream
			is.close();
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}

		long timeTaken = Timer.takeTime();
		LOG.log(Level.INFO, "  > Done in {0} ms.", timeTaken);
        return new TestResult(OperationType.READ, ioStrategy, blockSize, dataRead, timeTaken);
	}

	/**
	 * This method consumes bytes on the passed stream (the method does not know this stream is buffered or not)
	 * Depending on the strategy, the method either reads bytes one by one OR in chunks (the size of the chunk
	 * is passed in parameter). The method does not do anything with the read bytes, except counting them.
	 */ 
	private int consumeDataFromStream(InputStream is, IOStrategy ioStrategy, int blockSize) throws IOException {
		int totalBytes = 0;
		// If the strategy dictates to write byte by byte, then it's easy to write the loop; but let's just hope that our client has 
		// given us a buffered output stream, otherwise the performance will be really bad
		if ((ioStrategy == IOStrategy.ByteByByteWithBufferedStream) || (ioStrategy == IOStrategy.ByteByByteWithoutBufferedStream)) {
			int c;
			while ((c = is.read()) != -1) {
				// here, we could cast c to a byte and process it
				totalBytes++;
			}

			// If the strategy dictates to write block by block, then the loop is a bit longer to write
		} else {
			byte[] block = new byte[blockSize];
			int bytesRead = 0;
			while ((bytesRead = is.read(block)) != -1) {
				// here, we can process bytes block[0..bytesRead]
				totalBytes += bytesRead;
			}
		}
		
		LOG.log(Level.INFO, "Number of bytes read: {0}", new Object[]{totalBytes});
        return totalBytes;
	}

	/**
	 * Runs benchmarking operation on READ and WRITE operations with difference configurations
	 *
	 * @param resultLogger used to log the results
	 * @throws IOException
     */
	private static void runBenchmark(AbstractTestResultLogger resultLogger, int fsBlockSize) throws IOException {
		LinkedList<Integer> bytesNumberToTest = new LinkedList<>(
				Arrays.asList(1, 2, 3, 4, 5, 10, 20, 50, 100, 200, 256, 500, 512));
		int x = 1024;

		while(x <= 2 * fsBlockSize) {
			bytesNumberToTest.add(x - 100);
			bytesNumberToTest.add(x);
			bytesNumberToTest.add(x + 100);
			x += 512;
		}

		BufferedIOBenchmark bm = new BufferedIOBenchmark();

		LOG.log(Level.INFO, "");
		LOG.log(Level.INFO, "*** BENCHMARKING WRITE OPERATIONS (with BufferedStream)", Timer.takeTime());

		resultLogger.log(bm.produceTestData(IOStrategy.ByteByByteWithBufferedStream, NUMBER_OF_BYTES_TO_WRITE, 0));
		for(int i: bytesNumberToTest) {
			resultLogger.log(bm.produceTestData(IOStrategy.BlockByBlockWithBufferedStream, NUMBER_OF_BYTES_TO_WRITE, i));
		}


		LOG.log(Level.INFO, "");
		LOG.log(Level.INFO, "*** BENCHMARKING WRITE OPERATIONS (without BufferedStream)", Timer.takeTime());

		resultLogger.log(bm.produceTestData(IOStrategy.ByteByByteWithoutBufferedStream, NUMBER_OF_BYTES_TO_WRITE, 0));
		for(int i: bytesNumberToTest) {
			resultLogger.log(bm.produceTestData(IOStrategy.BlockByBlockWithoutBufferedStream, NUMBER_OF_BYTES_TO_WRITE, i));
		}


		LOG.log(Level.INFO, "");
		LOG.log(Level.INFO, "*** BENCHMARKING READ OPERATIONS (with BufferedStream)", Timer.takeTime());

		resultLogger.log(bm.consumeTestData(IOStrategy.ByteByByteWithBufferedStream, 0));
		for(int i: bytesNumberToTest) {
			resultLogger.log(bm.consumeTestData(IOStrategy.BlockByBlockWithBufferedStream, i));
		}


		LOG.log(Level.INFO, "");
		LOG.log(Level.INFO, "*** BENCHMARKING READ OPERATIONS (without BufferedStream)", Timer.takeTime());

		resultLogger.log(bm.consumeTestData(IOStrategy.ByteByByteWithoutBufferedStream, 0));
		for(int i: bytesNumberToTest) {
			resultLogger.log(bm.consumeTestData(IOStrategy.BlockByBlockWithoutBufferedStream, i));
		}

	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");

		if(args.length != 1) {
			System.err.println("Exactly one argument, the block size, is required.");
			System.exit(2);
		}

		try(OutputStreamWriter size_log = new OutputStreamWriter(new FileOutputStream("./report/size.log"))) {
			size_log.write(Long.toString(BufferedIOBenchmark.NUMBER_OF_BYTES_TO_WRITE));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}

		try (AbstractTestResultLogger resultLogger = new CSVResultLogger(new FileOutputStream("./report/metrics.csv"))){
			runBenchmark(resultLogger, Integer.parseInt(args[0]));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(4);
		}
    }

}
