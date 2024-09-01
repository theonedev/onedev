package io.onedev.server.plugin.buildspec.python;

import com.google.common.collect.Lists;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.onedev.server.buildspec.step.*;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import java.util.ArrayList;
import java.util.Collection;

public class PythonJobSuggestion implements JobSuggestion {
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();
		if (project.getBlob(new BlobIdent(commitId.name(), "poetry.lock", FileMode.TYPE_FILE), false) != null) {
			Job job = new Job();
			job.setName("python ci");
			
			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout code");
			job.getSteps().add(checkout);

			var generateChecksum = new GenerateChecksumStep();
			generateChecksum.setName("generate poetry lock checksum");
			generateChecksum.setFiles("poetry.lock");
			generateChecksum.setTargetFile("checksum");
			job.getSteps().add(generateChecksum);

			var setupCache = new SetupCacheStep();
			setupCache.setName("set up poetry cache");
			setupCache.setKey("poetry_cache_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList("/root/.cache/pypoetry"));
			setupCache.getLoadKeys().add("poetry_cache");
			job.getSteps().add(setupCache);
			
			CommandStep detectBuildVersion = new CommandStep();
			detectBuildVersion.setName("detect build version");
			detectBuildVersion.setImage("alpine");
			detectBuildVersion.getInterpreter().setCommands("" +
					"apk add yq\n" +
					"yq '.tool.poetry.version' pyproject.toml > buildVersion\n");
			job.getSteps().add(detectBuildVersion);
			
			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@file:buildVersion@");
			job.getSteps().add(setBuildVersion);

			CommandStep build = new CommandStep();
			build.setName("test");
			build.setImage("python");
			build.getInterpreter().setCommands("" +
					"curl -sSL https://install.python-poetry.org | python\n" +
					"/root/.local/bin/poetry install\n" +
					"/root/.local/bin/poetry run pytest\n");
			job.getSteps().add(build);

			job.getTriggers().add(new BranchUpdateTrigger());
			job.getTriggers().add(new PullRequestUpdateTrigger());
			
			jobs.add(job);
		}		
		return jobs;
	}

}