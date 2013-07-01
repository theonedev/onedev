package com.pmease.commons.util.execution;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.tools.ant.types.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.util.StringUtils;

public class Commandline {
	
    static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

	private static final int MAX_COMMAND_LENGTH = 1024;
	
	private static final Logger logger = LoggerFactory.getLogger(Commandline.class);

    private List<Argument> arguments = new ArrayList<Argument>();

    private String executable = null;
    
    public Commandline(String command) {
    	if (command != null) {
	        String[] parts = StringUtils.parseQuoteTokens(command);
	        if (parts != null && parts.length > 0) {
	            setExecutable(parts[0]);
	            for (int i = 1; i < parts.length; i++) 
	                addArgValue(parts[i]);
	        }
    	}
    }
    
    public Argument createArgument() {
        return createArgument(false);
    }
    
    public Commandline addArgValue(String argValue) {
    	createArgument().setValue(argValue);
    	return this;
    }
    
    public Commandline addArgLine(String argLine) {
    	createArgument().setLine(argLine);
    	return this;
    }

    public Argument createArgument(boolean insertAtStart) {
        Argument argument = new Argument();
        if (insertAtStart) {
            arguments.add(0, argument);
        } else {
            arguments.add(argument);
        }
        return argument;
    }

    public Commandline setExecutable(String executable) {
        if (executable == null || executable.length() == 0)
            return this;
        this.executable = executable.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        
        return this;
    }

    public String getExecutable() {
        return executable;
    }

    public Commandline addArguments(String[] argumentValues) {
        for (int i = 0; i < argumentValues.length; i++) {
            createArgument().setValue(argumentValues[i]);
        }
        return this;
    }

    public String[] getCommandParts() {
        List<String> parts = new LinkedList<String>();
        if (executable != null) 
            parts.add(executable);
        parts.addAll(Arrays.asList(getArgumentParts()));
        return parts.toArray(new String[parts.size()]);
    }
    
    public boolean willOverflow(String argValue) {
    	int length = 0;
    	if (executable != null) {
    		// executable length plus trailing space
    		length = executable.length() + 1;
    		// if contains whitespaces, surrounding quote will be used 
    		if (executable.indexOf(' ') != -1 || executable.indexOf('\t') != -1)
    			length += 2;
    	}
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            String[] s = arg.getParts();
            if (s != null) {
                for (int j = 0; j < s.length; j++) {
                    length += s[j].length() + 1;
                    if (s[j].indexOf(' ') != -1 || s[j].indexOf('\t') != -1)
                    	length += 2;
                }
            }
        }
    	length += argValue.length();
    	if (argValue.indexOf(' ') != -1 || argValue.indexOf('\t') != -1)
    		length += 2;
    	
    	return length > MAX_COMMAND_LENGTH;
    }

    public String[] getArgumentParts() {
        List<String> parts = new ArrayList<String>(arguments.size() * 2);
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            String[] s = arg.getParts();
            if (s != null) {
                for (int j = 0; j < s.length; j++) {
                    parts.add(s[j]);
                }
            }
        }
        return parts.toArray(new String[parts.size()]);
    }

    public String toString() {
    	return describe();
    }

    public Commandline clear() {
        executable = null;
        arguments.clear();
        return this;
    }

    public Commandline clearArgs() {
        arguments.clear();
        return this;
    }

    public String describe() {
        String[] args = getCommandParts();
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
        	if (args[i].contains(" ") || args[i].contains("\t")) {
        		buf.append("\"").append(StringUtils.replace(
        				args[i], "\n", "\\n")).append("\"").append(" ");
        	} else {
        		buf.append(StringUtils.replace(
        				args[i], "\n", "\\n")).append(" ");
        	}
        }
        return buf.toString();
    }

    public ExecuteResult execute(File workingDir, Environment execEnv, OutputStream stdoutConsumer, 
    		LineConsumer stderrConsumer) {
    	return execute(workingDir, execEnv, stdoutConsumer, stderrConsumer, null);
    }
    
    public ExecuteResult execute(File workingDir, OutputStream stdoutConsumer, LineConsumer stderrConsumer) {
    	return execute(workingDir, null, stdoutConsumer, stderrConsumer, null);
    }
    
    public ExecuteResult execute(OutputStream stdoutConsumer, LineConsumer stderrConsumer) {
    	return execute(null, null, stdoutConsumer, stderrConsumer, null);
    }
    
    public ExecuteResult execute(Environment execEnv, OutputStream stdoutConsumer, LineConsumer stderrConsumer) {
    	return execute(null, execEnv, stdoutConsumer, stderrConsumer, null);
    }

    public ExecuteResult execute(OutputStream stdoutConsumer, LineConsumer stderrConsumer, String stdinString) {
    	return execute(null, null, stdoutConsumer, stderrConsumer, stdinString);
    }

    public ExecuteResult execute(Environment execEnv, OutputStream stdoutConsumer, LineConsumer stderrConsumer, 
    		String stdinString) {
    	return execute(null, execEnv, stdoutConsumer, stderrConsumer, stdinString);
    }

	private ProcessBuilder getProcessBuilder(File workingDir, Environment execEnv) {
    	if (workingDir == null)
    		workingDir = com.pmease.commons.bootstrap.Bootstrap.getBinDir();
    	String[] cmdParts = getCommandParts();
        if (!new File(cmdParts[0]).isAbsolute()) {
            if (new File(workingDir, cmdParts[0]).isFile())
            	cmdParts[0] = new File(workingDir, cmdParts[0]).getAbsolutePath();
            else if (new File(workingDir, cmdParts[0] + ".exe").isFile())
            	cmdParts[0] = new File(workingDir, cmdParts[0] + ".exe").getAbsolutePath();
            else if (new File(workingDir, cmdParts[0] + ".bat").isFile())
            	cmdParts[0] = new File(workingDir, cmdParts[0] + ".bat").getAbsolutePath();
        }

        ProcessBuilder processBuilder = new ProcessBuilder(cmdParts);
    	processBuilder.directory(workingDir);

        if (execEnv != null) {
    		Map<String, String> procEnv = processBuilder.environment();
    		for (Environment.Variable var: (Vector<Environment.Variable>)execEnv.getVariablesVector()) {
    			if (var.getKey() != null && var.getValue() != null)
    				procEnv.put(var.getKey(), var.getValue());
    		}
    	}

        if (logger.isDebugEnabled()) {
    		logger.debug("Executing command: " + describe());
    		logger.debug("Command working directory: " + 
    				processBuilder.directory().getAbsolutePath());
    		StringBuffer buffer = new StringBuffer();
    		for (Map.Entry<String, String> entry: processBuilder.environment().entrySet())
    			buffer.append("	" + entry.getKey() + "=" + entry.getValue() + "\n");
    		logger.trace("Command execution environments:\n" + 
    				StringUtils.stripEnd(buffer.toString(), "\n"));
    	}

    	return processBuilder;
    }
    
	public ExecuteResult execute(File workingDir, Environment execEnv, OutputStream stdoutConsumer, 
			final LineConsumer stderrConsumer, String stdinString) {
    	Process process;
        try {
        	ProcessBuilder processBuilder = getProcessBuilder(workingDir, execEnv);
        	process = processBuilder.redirectErrorStream(stderrConsumer == null).start();
        } catch (IOException e) {
        	throw new RuntimeException(e.getMessage());
        }

    	ByteArrayInputStream inputStream = null;
    	if (stdinString != null && stdinString.length() != 0) 
    		inputStream = new ByteArrayInputStream(stdinString.getBytes());
    	
    	final StringBuffer errorMessage = new StringBuffer();
		OutputStream errorMessageCollector = null;
		if (stderrConsumer != null) {
			errorMessageCollector = new LineConsumer(stderrConsumer.getEncoding()) {

				@Override
				public void consume(String line) {
					if (errorMessage.length() != 0)
						errorMessage.append("\n");
					errorMessage.append(line);
					stderrConsumer.consume(line);
				}
				
			};
		}
    	
        ProcessStreamPumper streamPumper = new ProcessStreamPumper(process, stdoutConsumer, 
        		errorMessageCollector, inputStream);
        streamPumper.start();
        
        ExecuteResult result = new ExecuteResult();
        try {
	        while (true) {
		        try {
		            result.setReturnCode(process.waitFor());
		            break;
				} catch (InterruptedException e) {
					process.destroy();
					throw new RuntimeException(e);
				}
	        } 
        } finally {
			streamPumper.join();
        	Thread.interrupted();
        }
        if (errorMessage.length() != 0)
        	result.setErrorMessage(errorMessage.toString());
        
        return result;
    }
    
    public void executeWithoutWait() {
    	executeWithoutWait(null, null);
    }

    public void executeWithoutWait(File workingDir) {
    	executeWithoutWait(workingDir, null);
    }

    public void executeWithoutWait(File workingDir, Environment execEnv) {
    	executeWithoutWait(workingDir, execEnv, null);
    }
    
    public void executeWithoutWait(Environment execEnv) {
    	executeWithoutWait(null, execEnv);
    }
    
	public void executeWithoutWait(File workingDir, Environment execEnv, String stdinString) {
    	ByteArrayInputStream stdinStream = null;
    	if (stdinString != null && stdinString.length() != 0) 
    		stdinStream = new ByteArrayInputStream(stdinString.getBytes());
        
    	final Process process;
        try {
        	process = getProcessBuilder(workingDir, execEnv).start();
            new ProcessStreamPumper(process, null, null, stdinStream).start();
        } catch (IOException e) {
        	throw new RuntimeException(e);
        }

        EXECUTOR_SERVICE.submit(new Runnable() {

			@Override
			public void run() {
		        try {
		        	process.waitFor();
		        } catch (InterruptedException e) {
				} 
			}
			
    	});
    	
    }
    
    public class ExecuteResult {

    	private int returnCode;
    	
    	private String errorMessage;

		public int getReturnCode() {
			return returnCode;
		}

		public void setReturnCode(int returnCode) {
			this.returnCode = returnCode;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
    	
		/**
		 * Build an exception object with command description, stderr output and return code.
		 * @return
		 */
		public RuntimeException buildException() {
	    	if (errorMessage != null) {
	            throw new RuntimeException("Failed to run command: " + describe() + 
	            		"\nCommand return code: " + getReturnCode() + 
	                    "\nCommand error output: " + errorMessage);
	    	} else {
	            throw new RuntimeException("Failed to run command: " + describe() + 
	            		"\nCommand return code: " + getReturnCode());
	    	}
			
		}
		
		/**
		 * Check return code and throw exception if it does not equal to 0.
		 */
		public void checkReturnCode() {
			if (getReturnCode() != 0)
				throw buildException();
		}
    }

    public static class Argument {

        private String[] parts;

        public void setValue(String value) {
            parts = new String[] {value};
        }
        
        public void setLine(String line) {
            parts = StringUtils.parseQuoteTokens(line);
        }
        
        public String[] getParts() {
            return parts;
        }

    }
    
}
