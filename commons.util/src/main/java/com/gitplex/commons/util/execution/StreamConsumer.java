package com.gitplex.commons.util.execution;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.io.IOUtils;

public abstract class StreamConsumer extends PipedOutputStream {

	private static final int BUFFER_SIZE = 64*1024;
	
	public StreamConsumer() {
		final PipedInputStream input = new PipedInputStream(BUFFER_SIZE);
		try {
			connect(input);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		Commandline.EXECUTOR_SERVICE.submit(new Runnable() {

			@Override
			public void run() {
				try {
					consume(input);

					// In case consumer does not care about the stream, we pump to the end 
					// of the input stream so that the other side of the pipe will not 
					// block.
					byte[] buffer = new byte[BUFFER_SIZE];
		            while (input.read(buffer) != -1);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					IOUtils.closeQuietly(input);
				}
			}
			
		});
	}
	
	/**
	 * Consume specified input stream.
	 * 
	 * @param input
	 * 			input stream supplied by caller. Will be closed by caller
	 */
	protected abstract void consume(InputStream input);
	
}
