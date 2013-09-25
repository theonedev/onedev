package com.pmease.commons.util.execution;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStreamPumper {

    private static final int BUFFER_SIZE = 64*1024;
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessStreamPumper.class);
    
    private final Future<?> stdoutPumper;

    private final Future<?> stderrPumper;
    
    private final Future<?> stdinPumper;

    private final OutputStream stdout;
    
    private final OutputStream stderr;
    
    private ProcessStreamPumper(Process process, @Nullable OutputStream stdout, 
    		@Nullable OutputStream stderr, @Nullable InputStream stdin) {
        this.stdout = stdout;
        this.stderr = stderr;
        
        stdoutPumper = createPump(process.getInputStream(), stdout, false);
        stderrPumper = createPump(process.getErrorStream(), stderr, false);
        
        if (stdin != null)
            stdinPumper = createPump(stdin, process.getOutputStream(), true);
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
    	if (stdout != null) {
    		try {
				stdout.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    	}
    	if (stderr != null) {
    		try {
				stderr.flush();
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
		            	if (output != null) {
		            		output.write(buf, 0, length);
		            		//output.flush();
		            	}
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
