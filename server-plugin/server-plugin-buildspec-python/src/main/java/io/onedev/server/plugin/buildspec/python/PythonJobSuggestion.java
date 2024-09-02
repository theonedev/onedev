package io.onedev.server.plugin.buildspec.python;

import com.google.common.collect.Lists;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.onedev.server.buildspec.step.*;
import io.onedev.server.buildspec.step.commandinterpreter.ShellInterpreter;
import io.onedev.server.git.Blob;
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
		Blob blob;
		if ((blob = project.getBlob(new BlobIdent(commitId.name(), "poetry.lock", FileMode.TYPE_FILE), false)) != null) {
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

			CommandStep test = new CommandStep();
			test.setName("test");
			test.setImage("python");
			String commands = "" +
					"curl -sSL https://install.python-poetry.org | python\n" +
					"/root/.local/bin/poetry install\n";

			if (blob.getText().getContent().contains("pytest"))
				commands += "/root/.local/bin/poetry run pytest";
			else
				commands += "/root/.local/bin/poetry run python -m unittest";
			test.getInterpreter().setCommands(commands);
			
			job.getSteps().add(test);

			job.getTriggers().add(new BranchUpdateTrigger());
			job.getTriggers().add(new PullRequestUpdateTrigger());
			
			jobs.add(job);
		} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "requirements.txt", FileMode.TYPE_FILE), false)) != null) {
			Job job = new Job();
			job.setName("python ci");

			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout code");
			job.getSteps().add(checkout);

			var generateChecksum = new GenerateChecksumStep();
			generateChecksum.setName("generate requirements checksum");
			generateChecksum.setFiles("requirements.txt");
			generateChecksum.setTargetFile("checksum");
			job.getSteps().add(generateChecksum);

			var setupCache = new SetupCacheStep();
			setupCache.setName("set up venv cache");
			setupCache.setKey("venv_cache_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList(".venv"));
			setupCache.getLoadKeys().add("venv_cache");
			job.getSteps().add(setupCache);
			
			CommandStep test = new CommandStep();
			var interpreter = new ShellInterpreter();
			test.setInterpreter(interpreter);
			test.setName("test");
			test.setImage("python:3.11");
			var commands = "" +
					"python -m venv .venv\n" +
					"source .venv/bin/activate\n" +
					"pip install -r requirements.txt\n";
			
			if (blob.getText().getContent().contains("pytest")) 
				commands += "pytest";
			else 
				commands += "python -m unittest";
			interpreter.setCommands(commands);
			job.getSteps().add(test);

			job.getTriggers().add(new BranchUpdateTrigger());
			job.getTriggers().add(new PullRequestUpdateTrigger());

			jobs.add(job);			
		}
		return jobs;
	}

}