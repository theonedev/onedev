package com.pmease.commons.util.execution;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStreamPumper {

    private static final int BUFFER_SIZE = 64000;
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessStreamPumper.class);
    
    private final Future<?> stdoutPumper;

    private final Future<?> stderrPumper;
    
    private final Future<?> stdinPumper;

    private final OutputStream stdoutStream;
    
    private final OutputStream stderrStream;
    
    private ProcessStreamPumper(Process process, @Nullable OutputStream stdoutStream, 
    		@Nullable OutputStream stderrStream, @Nullable InputStream stdinStream) {
        this.stdoutStream = stdoutStream;
        this.stderrStream = stderrStream;
        
        stdoutPumper = createPump(process.getInputStream(), stdoutStream, false);
        stderrPumper = createPump(process.getErrorStream(), stderrStream, false);
        
        if (stdinStream != null)
            stdinPumper = createPump(stdinStream, process.getOutputStream(), true);
        else
        	stdinPumper = null;
    }
    
    public static ProcessStreamPumper pump(Process process, @Nullable OutputStream stdoutStream, 
    		@Nullable OutputStream stderrStream, @Nullable InputStream stdinStream) {
    	return new ProcessStreamPumper(process, stdoutStream, stderrStream, stdinStream); 
    }
    
	public void waitFor() {
    	while (!stdoutPumper.isDone() || !stderrPumper.isDone() || 
    			(stdinPumper != null && !stdinPumper.isDone())) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
    	}
    	if (stdoutStream != null) {
    		try {
				stdoutStream.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    	}
    	if (stderrStream != null) {
    		try {
				stderrStream.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    	}
    }

    private Future<?> createPump(final InputStream input, final OutputStream output, 
    		final boolean closeWhenExhausted) {
    	
    	return Commandline.EXECUTOR_SERVICE.submit(new Runnable() {

			public void run() {
		        byte[] buf = new byte[BUFFER_SIZE];

		        try {
			        int length;
		            while ((length = input.read(buf)) > 0) {
		            	if (output != null)
		            		output.write(buf, 0, length);
		            }

		            if (closeWhenExhausted && output!=null) {
		            	output.close();
		            }
		            
		        } catch (Exception e) {
		        	logger.error("Error pumping stream.", e);
		        }
			}
    		
    	});
    }

}
