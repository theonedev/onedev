package io.onedev.server.buildspec.step;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Editable(order=70, name="Push to Remote", group=StepGroup.REPOSITORY_SYNC, 
		description="This step pushes current commit to same ref on remote")
public class PushRepository extends SyncRepository {

	private static final long serialVersionUID = 1L;

	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		var certificateFile = writeCertificate(getCertificate());
		try {
			if (OneDev.getInstance(ProjectManager.class).hasLfsObjects(build.getProject().getId())) {
				Project project = build.getProject();
				Commandline git = CommandUtils.newGit();
				git.workingDir(OneDev.getInstance(ProjectManager.class).getGitDir(project.getId()));
				configureProxy(git, getProxy());
				configureCertificate(git, certificateFile);

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
					configureProxy(git, getProxy());
					configureCertificate(git, certificateFile);
					git.addArgs("fetch", remoteUrl, remoteCommitId.get());
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

					Repository repository = OneDev.getInstance(ProjectManager.class)
							.getRepository(project.getId());
					String mergeBaseId = GitUtils.getMergeBase(repository,
							ObjectId.fromString(remoteCommitId.get()), build.getCommitId()).name();

					if (!mergeBaseId.equals(build.getCommitHash())) {
						String input = String.format("%s %s %s %s\n", build.getRefName(), build.getCommitHash(),
								build.getRefName(), remoteCommitId.get());
						git.clearArgs();
						configureProxy(git, getProxy());
						configureCertificate(git, certificateFile);
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
					configureProxy(git, getProxy());
					configureCertificate(git, certificateFile);
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
			
			Commandline git = CommandUtils.newGit();
			configureProxy(git, getProxy());
			configureCertificate(git, certificateFile);
			git.workingDir(OneDev.getInstance(ProjectManager.class).getGitDir(build.getProject().getId()));
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

			return null;
		} finally {
			if (certificateFile != null)
				FileUtils.deleteFile(certificateFile);
		}
	}
	
}
