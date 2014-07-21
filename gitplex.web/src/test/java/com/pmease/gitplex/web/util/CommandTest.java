package com.pmease.gitplex.web.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;

import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSource;
import com.pmease.gitplex.web.common.quantity.Data;

public class CommandTest {

	static final File gitDir = new File("/Users/zhenyu/temp/aaa/.git");
	
	public void testCancel() throws ExecuteException, IOException {
		CommandLine cmd = new CommandLine("/usr/local/bin/git");
		cmd.addArguments(new String[] { "cat-file", "-p"});
		cmd.addArgument("82c4424d00d3ad7bf89e622141811b39b1052458:guava.jar");
		DefaultExecutor executor = new DefaultExecutor();
		final ExecuteWatchdog watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
		executor.setWatchdog(watchDog);
		executor.setWorkingDirectory(gitDir);
		
		
		try (PipedOutputStream output = new PipedOutputStream();
				BufferedInputStream is = new BufferedInputStream(new PipedInputStream(output), BUFFER_SIZE)) {

//			OutputHandler handler = new OutputHandler(is);
//			handler.setDaemon(true);
//			handler.start();
			
//			ExecuteStreamHandler streamHandler = new PumpStreamHandler(System.out, System.err);
			executor.setStreamHandler(new ProcessStreamHandler() {

				@Override
				protected void processBinary(final BufferedInputStream in) {
					System.out.println("Binary");
					try {
						ByteStreams.copy(in, System.out);
					} catch (IOException e) {
						throw Throwables.propagate(e);
					}
				}
				
			});
			int exitValue = executor.execute(cmd);
			System.out.println(exitValue);
		}
	}

	static abstract class MyOutputHandler {
		public void process(InputStream output) throws IOException {
			try (BufferedInputStream bufferedIn = new BufferedInputStream(output, BUFFER_SIZE)) {
				if (UniversalEncodingDetector.isBinary(bufferedIn)) {
					handleBinary(bufferedIn);
				} else {
					Charset charset = UniversalEncodingDetector.detect(bufferedIn);
					CharSource cs = ByteSource.wrap(ByteStreams.toByteArray(bufferedIn))
						.asCharSource(charset);
					System.out.println(cs.read());
				}
			}
		}

		abstract protected void handleBinary(BufferedInputStream in);
	}
	
	static abstract class ProcessStreamHandler implements ExecuteStreamHandler {
	    private Thread outputThread;

	    private Thread errorThread;

	    private Thread inputThread;

	    static final ExecutorService executor = Executors.newCachedThreadPool();
	    
	    private MyOutputHandler outputHandler;
	    
		@Override
		public void setProcessInputStream(OutputStream os) throws IOException {
			inputThread = new Thread();
			inputThread.setDaemon(true);
		}

		@Override
		public void setProcessErrorStream(InputStream is) throws IOException {
			errorThread = new Thread();
			errorThread.setDaemon(true);
		}

		@Override
		public void setProcessOutputStream(final InputStream is) throws IOException {
			outputHandler = new MyOutputHandler() {
				@Override
				public void handleBinary(BufferedInputStream in) {
					processBinary(in);
				}
			};
			
			outputThread = new Thread() {
				@Override
				public void run() {
					try {
						outputHandler.process(is);
					} catch (IOException e) {
						throw Throwables.propagate(e);
					}
				}
			};
			outputThread.setDaemon(true);
		}
		
		@Override
		public void start() throws IOException {
			if (outputThread != null) {
				outputThread.run();
			}
			
			if (errorThread != null) {
				executor.submit(errorThread);
			}
			
			if (inputThread != null) {
				executor.submit(inputThread);
			}
		}

		private void stopThread(Thread thread) {
			if (thread != null) {
				try {
					thread.join();
					thread = null;
				} catch (InterruptedException e) {
				}
			}
		}
		
		@Override
		public void stop() {
			stopThread(inputThread);
			stopThread(outputThread);
			stopThread(errorThread);
		}
		
		abstract protected void processBinary(BufferedInputStream in);
	}
	
	/** Initial buffer size. */
    private static final int BUFFER_SIZE = (int) Data.ONE_KB * 8;

}
