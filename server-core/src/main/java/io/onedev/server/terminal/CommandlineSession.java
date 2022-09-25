package io.onedev.server.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.command.ExposeOutputStream;
import io.onedev.commons.utils.command.ProcessKiller;
import io.onedev.commons.utils.command.ProcessTreeKiller;
import io.onedev.commons.utils.command.PtyMode;
import io.onedev.commons.utils.command.PtyMode.ResizeSupport;
import io.onedev.commons.utils.command.PumpInputToOutput;
import io.onedev.server.OneDev;

public class CommandlineSession implements ShellSession {

	private static final Logger logger = LoggerFactory.getLogger(CommandlineSession.class);
	
	private final PtyMode ptyMode;
	
	private final ExposeOutputStream shellInput;
	
	private final Future<?> execution;
	
	public CommandlineSession(IWebSocketConnection connection, Commandline cmdline) {
        ptyMode = new PtyMode();
        cmdline.ptyMode(ptyMode);

        shellInput = new ExposeOutputStream();
        execution = OneDev.getInstance(ExecutorService.class).submit(new Runnable() {

			@Override
			public void run() {
                try {
                    PumpInputToOutput outputHandler = new PumpInputToOutput(new OutputStream() {

            	        @Override
            	        public void write(byte[] b, int off, int len) throws IOException {
                            TerminalUtils.sendOutput(connection, new String(b, off, len, StandardCharsets.UTF_8));
            	        }
            	
            	        @Override
            	        public void write(int b) throws IOException {
            	        	throw new UnsupportedOperationException();
            	        }

                    });
                    PumpInputToOutput errorHandler = new PumpInputToOutput(new OutputStream() {

                        @Override
                        public void write(byte[] b, int off, int len) throws IOException {
                        	TerminalUtils.sendError(connection, new String(b, off, len, StandardCharsets.UTF_8));
                        }

                        @Override
                        public void write(int b) throws IOException {
                        	throw new UnsupportedOperationException();
                        }

                    });
                    ProcessKiller processKiller = new ProcessTreeKiller() {

                        @Override
                        public void kill(Process process, String executionId) {
	                        outputHandler.close();
	                        errorHandler.close();
	                        super.kill(process, executionId);
                        }

                    };
                    ExecutionResult result = cmdline.execute(outputHandler, errorHandler, shellInput, processKiller);
                    if (result.getReturnCode() != 0)
                    	TerminalUtils.sendError(connection, "Shell exited");
                    else
                    	TerminalUtils.close(connection);
	            } catch (ExplicitException e) {
	            	TerminalUtils.sendError(connection, e.getMessage());
	            } catch (Throwable e) {
	            	ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
	            	if (explicitException != null) {
		            	TerminalUtils.sendError(connection, explicitException.getMessage());
	            	} else if (ExceptionUtils.find(e, InterruptedException.class) != null) {
	                    TerminalUtils.sendError(connection, "Shell exited");
	                } else {
	                	logger.error("Error running shell", e);
	                    TerminalUtils.sendError(connection, "Error running shell, check server log for details");
	                }
	            }
			}
        	
        });
        
	}

	@Override
	public void sendInput(String input) {
		try {
			shellInput.write(input);
		} catch (IOException e) {
		}
	}

	@Override
	public void resize(int rows, int cols) {
		ResizeSupport resizeSupport = ptyMode.getResizeSupport();
		if (resizeSupport != null)
			resizeSupport.resize(rows, cols);
	}

	@Override
	public void exit() {
		sendInput("exit\n");
		execution.cancel(true);
	}

}
