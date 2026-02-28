package io.onedev.server.terminal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ProcessTreeKiller;
import io.onedev.commons.utils.command.PtyMode;
import io.onedev.commons.utils.command.PtyMode.ResizeSupport;
import io.onedev.commons.utils.command.StreamPumper;
import io.onedev.server.OneDev;

public class CommandlineShell implements Shell {

	private static final Logger logger = LoggerFactory.getLogger(CommandlineShell.class);
	
	private final PtyMode ptyMode;
	
	private volatile OutputStream stdin;
	
	private final Future<?> future;
	
	public CommandlineShell(Terminal terminal, Commandline cmdline) {
        ptyMode = new PtyMode();
        cmdline.ptyMode(ptyMode);

        future = OneDev.getInstance(ExecutorService.class).submit(new Runnable() {

			private Future<?> pump(InputStream is, Terminal terminal) {
				return StreamPumper.pump(is, new OutputStream() {

					@Override
					public void write(byte[] b, int off, int len) {
						terminal.onShellOutput(Base64.getEncoder().encodeToString(Arrays.copyOfRange(b, off, off + len)));
					}

					@Override
					public void write(int b) {
						throw new UnsupportedOperationException();
					}

				});
			}

			@Override
			public void run() {
                try {
					var stdoutHolder = new AtomicReference<InputStream>(null);
					Function<InputStream, Future<?>> stdoutHandler = is -> {
						stdoutHolder.set(is);
						return pump(is, terminal);
					};

					var stderrHolder = new AtomicReference<InputStream>(null);
					Function<InputStream, Future<?>> stderrHandler = is -> {
						stderrHolder.set(is);
						return pump(is, terminal);
					};
					
                    cmdline.processKiller(new ProcessTreeKiller() {

                        @Override
                        public void kill(Process process, String executionId) {
	                        closeQuietly(stdoutHolder.get());
							closeQuietly(stderrHolder.get());
	                        super.kill(process, executionId);
                        }

                    });
                    var result = cmdline.execute(stdoutHandler, stderrHandler, os -> {
						stdin = os;
						return CompletableFuture.completedFuture(null);
					});
                    if (result.getReturnCode() != 0)
                    	terminal.onShellError("Shell exited with return code: " + result.getReturnCode());
                    else
                    	terminal.onShellExit();
	            } catch (Throwable e) {
	            	ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
	            	if (explicitException != null) {
		            	terminal.onShellError("Shell exited with error: " + explicitException.getMessage());
	            	} else if (ExceptionUtils.find(e, InterruptedException.class) == null) {
	                	logger.error("Error running shell", e);
	                    terminal.onShellError("Error running shell, check server log for details");
					}
	            } finally {
					closeQuietly(stdin);
					stdin = null;
				}
			}
        	
        });
        
	}

	@Override
	public void writeToStdin(String data) {
		var stdinCopy = stdin;
		if (stdinCopy != null) {
			try {
				stdinCopy.write(data.getBytes(UTF_8));
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
	public void terminate() {
		future.cancel(true);
	}

}
