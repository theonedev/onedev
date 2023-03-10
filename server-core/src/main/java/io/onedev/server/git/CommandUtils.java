package io.onedev.server.git;

import io.onedev.agent.Agent;
import io.onedev.commons.bootstrap.SensitiveMasker;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ErrorCollector;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.git.command.FileChange;
import io.onedev.server.git.command.ReceivePackCommand;
import io.onedev.server.git.command.UploadPackCommand;
import io.onedev.server.git.location.GitLocation;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandUtils {

	private static final Logger logger = LoggerFactory.getLogger(CommandUtils.class);
	
	private static final String MIN_VERSION = "2.11.1";
	
	public static Commandline newGit() {
		Commandline cmdline = new Commandline(OneDev.getInstance(GitLocation.class).getExecutable());
		if (SystemUtils.IS_OS_MAC_OSX) {
			String path = System.getenv("PATH") + ":/usr/local/bin";
			cmdline.environments().put("PATH", path);
		}
		return cmdline;
	}
	
	public static <T> T callWithClusterCredential(GitTask<T> task) {
		File homeDir = FileUtils.createTempDir("githome"); 
		
		ClusterManager clusterManager = OneDev.getInstance(ClusterManager.class);
		SensitiveMasker.push(text -> StringUtils.replace(text, clusterManager.getCredential(), "******"));
		try {
			Commandline git = newGit();
			git.environments().put("HOME", homeDir.getAbsolutePath());
			String extraHeader = KubernetesHelper.AUTHORIZATION + ": " 
					+ KubernetesHelper.BEARER + " " + clusterManager.getCredential();
			git.addArgs("config", "--global", "http.extraHeader", extraHeader);
			git.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.info(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.warn(line);
				}
				
			}).checkReturnCode();
			
			git.clearArgs();
			
			return task.call(git);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			SensitiveMasker.pop();
			FileUtils.deleteDir(homeDir);
		}
	}
	
	/**
	 * Check if there are any errors with git command line. 
	 *
	 * @return
	 * 			error message if failed to check git command line, 
	 * 			or <tt>null</tt> otherwise
	 * 			
	 */
	public static String checkError(String gitExe) {
		return Agent.checkGitError(gitExe, MIN_VERSION);
	}
	
	public static FileChange parseNumStats(String line) {
		FileChange change;
		StringTokenizer tokenizer = new StringTokenizer(line, "\t");
		String additionsToken = tokenizer.nextToken();
		int additions = additionsToken.equals("-")?-1:Integer.parseInt(additionsToken);
		String deletionsToken = tokenizer.nextToken();
		int deletions = deletionsToken.equals("-")?-1:Integer.parseInt(deletionsToken);
		
		String path = tokenizer.nextToken();
		int renameSignIndex = path.indexOf(" => ");
		if (renameSignIndex != -1) {
			int leftBraceIndex = path.indexOf("{");
			int rightBraceIndex = path.indexOf("}");
			if (leftBraceIndex != -1 && rightBraceIndex != -1 && leftBraceIndex<renameSignIndex
					&& rightBraceIndex>renameSignIndex) {
				String leftCommon = path.substring(0, leftBraceIndex);
				String rightCommon = path.substring(rightBraceIndex+1);
				String oldPath = leftCommon + path.substring(leftBraceIndex+1, renameSignIndex) 
						+ rightCommon;
				String newPath = leftCommon + path.substring(renameSignIndex+4, rightBraceIndex) 
						+ rightCommon;
    			change = new FileChange(oldPath, newPath, additions, deletions);
			} else {
				String oldPath = QuotedString.GIT_PATH.dequote(path.substring(0, renameSignIndex));
				String newPath = QuotedString.GIT_PATH.dequote(path.substring(renameSignIndex+4));
    			change = new FileChange(oldPath, newPath, additions, deletions);
			}
		} else {
			path = QuotedString.GIT_PATH.dequote(path);
			change = new FileChange(null, path, additions, deletions);
		}            			
		return change;
	}
	
	public static void uploadPack(File gitDir, Map<String, String> environments, String protocol, 
			InputStream stdin, OutputStream stdout) {
		AtomicBoolean toleratedErrors = new AtomicBoolean(false);
		ErrorCollector stderr = new ErrorCollector(StandardCharsets.UTF_8.name()) {

			@Override
			public void consume(String line) {
				super.consume(line);
				// This error may happen during a normal shallow fetch/clone 
				if (line.contains("remote end hung up unexpectedly")) {
					toleratedErrors.set(true);
					logger.debug(line);
				} else {
					logger.error(line);
				}
			}
			
		};

		ExecutionResult result;
		UploadPackCommand upload = new UploadPackCommand(gitDir, stdin, stdout, stderr, environments);
		upload.statelessRpc(true).protocol(protocol);
		result = upload.run();
		result.setStderr(stderr.getMessage());
		
		if (result.getReturnCode() != 0 && !toleratedErrors.get())
			throw result.buildException();
	}
	
	public static void receivePack(File gitDir, Map<String, String> environments, String protocol, 
			InputStream stdin, OutputStream stdout) {
		ErrorCollector stderr = new ErrorCollector(StandardCharsets.UTF_8.name()) {

			@Override
			public void consume(String line) {
				super.consume(line);
				logger.error(line);
			}
			
		};
		
		ReceivePackCommand receive = new ReceivePackCommand(gitDir, stdin, stdout, stderr, environments);
		receive.statelessRpc(true).protocol(protocol);
		ExecutionResult result = receive.run();
		result.setStderr(stderr.getMessage());
		result.checkReturnCode();
	}
	
}
