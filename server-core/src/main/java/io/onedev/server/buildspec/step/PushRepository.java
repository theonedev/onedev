package io.onedev.server.buildspec.step;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=70, name="Push to Remote", group=StepGroup.REPOSITORY_MIRRORING)
public class PushRepository extends MirrorRepository {

	private static final long serialVersionUID = 1L;

	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		if (isWithLfs()) {
			Project project = build.getProject();
			Commandline git = newGit(project);
			
			String remoteUrl = getRemoteUrlWithCredential(build);
			AtomicReference<String> remoteCommitId = new AtomicReference<>(null);
			git.addArgs("ls-remote", remoteUrl, "HEAD", build.getRefName());
			git.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					String refName = line.substring(40).trim();
					if (refName.equals("HEAD")) {
						if (remoteCommitId.get() == null)
							remoteCommitId.set(line.substring(0, 40));
					} else {
						remoteCommitId.set(line.substring(0, 40));
					}
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.warning(line);
				}
				
			});
			
			if (remoteCommitId.get() != null) {
				git.clearArgs();
				git.addArgs("fetch", remoteUrl, remoteCommitId.get());
				
				String mergeBaseId = GitUtils.getMergeBase(project.getRepository(), 
						ObjectId.fromString(remoteCommitId.get()), build.getCommitId()).name();
				
				if (!mergeBaseId.equals(build.getCommitHash())) {
					String input = String.format("%s %s %s %s\n", build.getRefName(), build.getCommitHash(), 
							build.getRefName(), remoteCommitId.get());
					git.clearArgs();
					git.addArgs("lfs", "pre-push", remoteUrl, remoteUrl);
					git.execute(new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.log(line);
						}
						
					}, new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.warning(line);
						}
						
					}, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))).checkReturnCode();
				}
			} else {
				git.clearArgs();
				git.addArgs("lfs", "push", "--all", remoteUrl, build.getCommitHash());
				git.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.log(line);
					}
					
				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.warning(line);
					}
					
				}).checkReturnCode();
			}
		}
		push(build, logger);
		
		return null;
	}
	
	private void push(Build build, TaskLogger logger) {
		Commandline git = newGit(build.getProject());
		git.addArgs("push");
		if (isForce())
			git.addArgs("--force");
		git.addArgs(getRemoteUrlWithCredential(build));
		git.addArgs(build.getCommitHash() + ":" + build.getRefName());

		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.log(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.warning(line);
			}
			
		}).checkReturnCode();
	}

}
