package io.onedev.server.plugin.buildspec.bazel;

import com.google.common.collect.Lists;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.onedev.server.buildspec.step.CheckoutStep;
import io.onedev.server.buildspec.step.CommandStep;
import io.onedev.server.buildspec.step.GenerateChecksumStep;
import io.onedev.server.buildspec.step.SetupCacheStep;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BazelJobSuggestion implements JobSuggestion {
	
	private GenerateChecksumStep newChecksumGenerateStep(String name, String files) {
		var generateChecksum = new GenerateChecksumStep();
		generateChecksum.setName(name);
		generateChecksum.setFiles(files);
		generateChecksum.setTargetFile("checksum");
		return generateChecksum;
	}
	
	private Job newJob() {
		Job job = new Job();
		job.setName("bazel ci");
		return job;
	}
	
	private void addCommonJobsAndTriggers(Job job) {
		var checkout = new CheckoutStep();
		checkout.setName("checkout code");
		job.getSteps().add(0, checkout);
		job.getTriggers().add(new BranchUpdateTrigger());
		job.getTriggers().add(new PullRequestUpdateTrigger());
	}
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		List<Job> jobs = new ArrayList<>();
		if (project.getBlob(new BlobIdent(commitId.name(), "MODULE.bazel", FileMode.TYPE_FILE), false) != null ||
				project.getBlob(new BlobIdent(commitId.name(), "BUILD.bazel", FileMode.TYPE_FILE), false) != null ||
				project.getBlob(new BlobIdent(commitId.name(), "WORKSPACE.bazel", FileMode.TYPE_FILE), false) != null) {
			Job job = newJob();
			job.getSteps().add(newChecksumGenerateStep("generate bazel checksum", ".bazelversion MODULE.bazel MODULE.bazel.lock **/BUILD **/BUILD.bazel"));
			var setupCache = new SetupCacheStep();
			setupCache.setName("set up bazel cache");
			setupCache.setKey("bazel_cache_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList("/root/.cache/bazelisk", "/root/.cache/bazel"));
			setupCache.getLoadKeys().add("bazel_cache");
			job.getSteps().add(setupCache);

			CommandStep buildAndTest = new CommandStep();
			buildAndTest.setName("build and test");
			buildAndTest.setImage("1dev/bazelisk:1.0.2");
			buildAndTest.getInterpreter().setCommands("" +
					"set -e\n" +
					"bazelisk build //...\n" +
					"bazelisk test //...");
			job.getSteps().add(buildAndTest);
			addCommonJobsAndTriggers(job);
			jobs.add(job);
		}
		return jobs;
	}

}