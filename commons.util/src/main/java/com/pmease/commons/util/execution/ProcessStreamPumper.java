package com.pmease.commons.util.execution;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessStreamPumper {

    private static final int BUFFER_SIZE = 64000;
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessStreamPumper.class);
    
    private Future<?> stdoutPumper;

    private Future<?> stderrPumper;
    
    private Future<?> stdinPumper;

    private OutputStream stdoutStream;
    
    private OutputStream stderrStream;
    
    private InputStream stdinStream;
    
    private Process process;
    
    /**
     * @param process not null
     * @param stdoutStream might be null
     * @param stderrStream might be null
     * @param stdinStream might be null
     */
    public ProcessStreamPumper(Process process, OutputStream stdoutStream, OutputStream stderrStream, 
    		InputStream stdinStream) {
    	this.process = process;
    	
        this.stdoutStream = stdoutStream;
        this.stderrStream = stderrStream;
        this.stdinStream = stdinStream;
    }

    public void start() {
        stdoutPumper = createPump(process.getInputStream(), stdoutStream, false);
        stderrPumper = createPump(process.getErrorStream(), stderrStream, false);
        
        if (stdinStream != null)
            stdinPumper = createPump(stdinStream, process.getOutputStream(), true);
    }

	public void join() {
    	while (!stdoutPumper.isDone() || !stderrPumper.isDone() || 
    			(stdinPumper != null && !stdinPumper.isDone())) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
    	}
        try {
        	if (stderrStream != null)
        		stderrStream.flush();
        } catch (IOException e) {
        }
        try {
        	if (stdoutStream != null)
        		stdoutStream.flush();
        } catch (IOException e) {
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
		        } catch (Exception e) {
		        	logger.error("Error pumping stream.", e);
		        	process.destroy();
		        } finally {
		            if (closeWhenExhausted && output!=null) {
		                try {
		                    output.close();
		                } catch (IOException e) {
		                }
		            }
		        }
				
			}
    		
    	});
    }

}
