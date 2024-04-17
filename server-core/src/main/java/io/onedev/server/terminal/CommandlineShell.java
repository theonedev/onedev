package io.onedev.server.terminal;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.ImmediateFuture;
import io.onedev.commons.utils.command.*;
import io.onedev.commons.utils.command.PtyMode.ResizeSupport;
import io.onedev.server.OneDev;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class CommandlineShell implements Shell {

	private static final Logger logger = LoggerFactory.getLogger(CommandlineShell.class);
	
	private final PtyMode ptyMode;
	
	private volatile OutputStream shellStdin;
	
	private final Future<?> execution;
	
	public CommandlineShell(Terminal terminal, Commandline cmdline) {
        ptyMode = new PtyMode();
        cmdline.ptyMode(ptyMode);

        execution = OneDev.getInstance(ExecutorService.class).submit(new Runnable() {

			@Override
			public void run() {
                try {
					var stdoutHolder = new AtomicReference<InputStream>(null);
					Function<InputStream, Future<?>> stdoutHandler = is -> {
						stdoutHolder.set(is);
						return StreamPumper.pump(is, new OutputStream() {

							@Override
							public void write(byte[] b, int off, int len) {
								terminal.sendOutput(new String(b, off, len, StandardCharsets.UTF_8));
							}

							@Override
							public void write(int b) {
								throw new UnsupportedOperationException();
							}

						});
					};

					var stderrHolder = new AtomicReference<InputStream>(null);
					Function<InputStream, Future<?>> stderrHandler = is -> {
						stderrHolder.set(is);
						return StreamPumper.pump(is, new OutputStream() {

							@Override
							public void write(byte[] b, int off, int len) {
								terminal.sendError(new String(b, off, len, StandardCharsets.UTF_8));
							}

							@Override
							public void write(int b) {
								throw new UnsupportedOperationException();
							}

						});
					};
					
                    cmdline.processKiller(new ProcessTreeKiller() {

                        @Override
                        public void kill(Process process, String executionId) {
	                        closeQuietly(stdoutHolder.get());
							closeQuietly(stderrHolder.get());
	                        super.kill(process, executionId);
                        }

                    });
                    ExecutionResult result = cmdline.execute(stdoutHandler, stderrHandler, os -> {
						shellStdin = os;
						return new ImmediateFuture<Void>(null);
					});
                    if (result.getReturnCode() != 0)
                    	terminal.sendError("Shell exited");
                    else
                    	terminal.close();
	            } catch (ExplicitException e) {
	            	terminal.sendError(e.getMessage());
	            } catch (Throwable e) {
	            	ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
	            	if (explicitException != null) {
		            	terminal.sendError(explicitException.getMessage());
	            	} else if (ExceptionUtils.find(e, InterruptedException.class) != null) {
	                    terminal.sendError("Shell exited");
	                } else {
	                	logger.error("Error running shell", e);
	                    terminal.sendError("Error running shell, check server log for details");
	                }
	            } finally {
					closeQuietly(shellStdin);
					shellStdin = null;
				}
			}
        	
        });
        
	}

	@Override
	public void sendInput(String input) {
		var shellStdinCopy = shellStdin;
		if (shellStdinCopy != null) {
			try {
				shellStdinCopy.write(input.getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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
