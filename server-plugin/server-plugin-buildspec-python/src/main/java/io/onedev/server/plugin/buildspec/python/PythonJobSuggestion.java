package io.onedev.server.plugin.buildspec.python;

import com.google.common.collect.Lists;
import com.moandjiezana.toml.Toml;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.onedev.server.buildspec.step.*;
import io.onedev.server.buildspec.step.commandinterpreter.ShellInterpreter;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.plugin.report.cobertura.PublishCoberturaReportStep;
import io.onedev.server.plugin.report.coverage.PublishCoverageReportStep;
import io.onedev.server.plugin.report.junit.PublishJUnitReportStep;
import io.onedev.server.plugin.report.ruff.PublishRuffReportStep;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PythonJobSuggestion implements JobSuggestion {
	
	private PublishJUnitReportStep newUnitTestReportPublishStep() {
		var publishUnitTestReport = new PublishJUnitReportStep();
		publishUnitTestReport.setName("publish unit test report");
		publishUnitTestReport.setReportName("Unit Test");
		publishUnitTestReport.setFilePatterns("test-result.xml");
		publishUnitTestReport.setCondition(ExecuteCondition.ALWAYS);
		return publishUnitTestReport;
	}

	private PublishCoverageReportStep newCoverageReportPublishStep() {
		var publishCoverageReport = new PublishCoberturaReportStep();
		publishCoverageReport.setName("publish coverage report");
		publishCoverageReport.setReportName("Coverage");
		publishCoverageReport.setFilePatterns("coverage.xml");
		publishCoverageReport.setCondition(ExecuteCondition.ALWAYS);
		return publishCoverageReport;
	}

	private PublishRuffReportStep newRuffReportPublishStep() {
		var publishRuffReport = new PublishRuffReportStep();
		publishRuffReport.setName("publish ruff report");
		publishRuffReport.setReportName("Ruff");
		publishRuffReport.setFilePatterns("ruff-result.json");
		publishRuffReport.setCondition(ExecuteCondition.ALWAYS);
		return publishRuffReport;
	}
	
	private SetupCacheStep newPipCacheSetupStep() {
		var setupCache = new SetupCacheStep();
		setupCache.setName("set up dependency cache");
		setupCache.setKey("pip_cache_@file:checksum@");
		setupCache.setPaths(Lists.newArrayList("/root/.cache/pip"));
		setupCache.getLoadKeys().add("pip_cache");
		return setupCache;
	}
	
	private String getCoverageAndRuffCommand(boolean withPytest, String prefix) {
		String command;
		if (withPytest)
			command = prefix + "coverage run -m pytest --junitxml=./test-result.xml\n";
		else
			command = prefix + "coverage run -m unittest\n";
		return command + 
				prefix + "coverage xml\n" +
				prefix + "ruff check --exit-zero --output-format=json --output-file=ruff-result.json --exclude=.git";
	}
	
	private CommandStep newPipTestAndLintStep(String dependencyInstallCommand, boolean withPytest) {
		CommandStep testAndLint = new CommandStep();
		var interpreter = new ShellInterpreter();
		testAndLint.setInterpreter(interpreter);
		testAndLint.setName("test and lint");
		testAndLint.setImage("python:3.11");
		interpreter.setCommands(dependencyInstallCommand + "\n" + "pip install coverage ruff\n" + getCoverageAndRuffCommand(withPytest, ""));
		return testAndLint;
	}
	
	private GenerateChecksumStep newChecksumGenerateStep(String files) {
		var generateChecksum = new GenerateChecksumStep();
		generateChecksum.setName("generate dependency checksum");
		generateChecksum.setFiles(files);
		generateChecksum.setTargetFile("checksum");
		return generateChecksum;
	}
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Job job = new Job();
		job.setName("python ci");
		var checkout = new CheckoutStep();
		checkout.setName("checkout code");
		job.getSteps().add(checkout);
		
		Blob blob;
		if ((blob = project.getBlob(new BlobIdent(commitId.name(), "poetry.lock", FileMode.TYPE_FILE), false)) != null) {
			job.getSteps().add(newChecksumGenerateStep("poetry.lock"));

			var setupCache = new SetupCacheStep();
			setupCache.setName("set up dependency cache");
			setupCache.setKey("poetry_cache_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList("/root/.cache/pypoetry"));
			setupCache.getLoadKeys().add("poetry_cache");
			job.getSteps().add(setupCache);
			
			CommandStep detectBuildVersion = new CommandStep();
			detectBuildVersion.setName("detect build version");
			detectBuildVersion.setImage("1dev/yq:1.0.0");
			detectBuildVersion.getInterpreter().setCommands("yq '.tool.poetry.version' pyproject.toml > buildVersion");
			job.getSteps().add(detectBuildVersion);
			
			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@file:buildVersion@");
			job.getSteps().add(setBuildVersion);

			CommandStep testAndLint = new CommandStep();
			testAndLint.setName("test and lint");
			testAndLint.setImage("1dev/poetry:1.0.1");
			String commands = "" +
					"poetry config virtualenvs.create false\n" +
					"poetry install\n" +
					"poetry add coverage ruff\n";

			var withPytest = blob.getText().getContent().contains("pytest");
			testAndLint.getInterpreter().setCommands(commands + getCoverageAndRuffCommand(withPytest, "poetry run "));
			job.getSteps().add(testAndLint);
			if (withPytest) 
				job.getSteps().add(newUnitTestReportPublishStep());
		} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "requirements.txt", FileMode.TYPE_FILE), false)) != null) {
			job.getSteps().add(newChecksumGenerateStep("requirements.txt"));
			job.getSteps().add(newPipCacheSetupStep());
			
			var withPytest = blob.getText().getContent().contains("pytest");
			job.getSteps().add(newPipTestAndLintStep("pip install -r requirements.txt", withPytest));
			if (withPytest)
				job.getSteps().add(newUnitTestReportPublishStep());
		} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "pyproject.toml", FileMode.TYPE_FILE), false)) != null) {
			var dependencyFiles = "pyproject.toml";
			var setupPy = project.getBlob(new BlobIdent(commitId.name(), "setup.py", FileMode.TYPE_FILE), false);
			if (setupPy != null)
				dependencyFiles += " setup.py";
			var setupCfg = project.getBlob(new BlobIdent(commitId.name(), "setup.cfg", FileMode.TYPE_FILE), false);
			if (setupCfg != null)
				dependencyFiles += " setup.cfg";

			job.getSteps().add(newChecksumGenerateStep(dependencyFiles));
			job.getSteps().add(newPipCacheSetupStep());

			var blobContent = blob.getText().getContent();
			if (new Toml().read(blobContent).getString("project.version") != null) {
				CommandStep detectBuildVersion = new CommandStep();
				detectBuildVersion.setName("detect build version");
				detectBuildVersion.setImage("1dev/yq:1.0.0");
				detectBuildVersion.getInterpreter().setCommands("yq '.project.version' pyproject.toml > buildVersion");
				job.getSteps().add(detectBuildVersion);

				SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
				setBuildVersion.setName("set build version");
				setBuildVersion.setBuildVersion("@file:buildVersion@");
				job.getSteps().add(setBuildVersion);
			}

			var withPytest = blobContent.contains("pytest")
					|| setupPy != null && setupPy.getText().getContent().contains("pytest")
					|| setupCfg != null && setupCfg.getText().getContent().contains("pytest");
			job.getSteps().add(newPipTestAndLintStep("pip install .", withPytest));			
			if (withPytest)
				job.getSteps().add(newUnitTestReportPublishStep());
		} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "setup.py", FileMode.TYPE_FILE), false)) != null) {
			var dependencyFiles = "setup.py";
			var setupCfg = project.getBlob(new BlobIdent(commitId.name(), "setup.cfg", FileMode.TYPE_FILE), false);
			if (setupCfg != null)
				dependencyFiles += " setup.cfg";
			
			job.getSteps().add(newChecksumGenerateStep(dependencyFiles));
			job.getSteps().add(newPipCacheSetupStep());

			var blobContent = blob.getText().getContent();
			var withPytest = blobContent.contains("pytest") || setupCfg != null && setupCfg.getText().getContent().contains("pytest");
			job.getSteps().add(newPipTestAndLintStep("pip install .", withPytest));
			if (withPytest)
				job.getSteps().add(newUnitTestReportPublishStep());
		} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "environment.yml", FileMode.TYPE_FILE), false)) != null) {
			job.getSteps().add(newChecksumGenerateStep("environment.yml"));

			var setupCache = new SetupCacheStep();
			setupCache.setName("set up dependency cache");
			setupCache.setKey("conda_cache_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList("/root/miniconda3/envs"));
			setupCache.getLoadKeys().add("conda_cache");
			job.getSteps().add(setupCache);
			
			var blobContent = blob.getText().getContent();
			Map<String, Object> environments = new Yaml().load(blobContent);
			CommandStep testAndLint = new CommandStep();
			testAndLint.setName("test and lint");
			testAndLint.setImage("1dev/conda:1.0.4");
			testAndLint.setInterpreter(new ShellInterpreter());
			String commands = "" +
					"source /root/.bashrc\n" +
					"conda env update\n" +
					"conda activate " + environments.get("name") + "\n" +
					"conda install -y coverage ruff\n";
			
			var withPytest = blobContent.contains("pytest");
			testAndLint.getInterpreter().setCommands(commands + getCoverageAndRuffCommand(withPytest, ""));
			job.getSteps().add(testAndLint);
			if (withPytest)
				job.getSteps().add(newUnitTestReportPublishStep());				
		}
		if (job.getSteps().size() > 1) {
			job.getSteps().add(newCoverageReportPublishStep());
			job.getSteps().add(newRuffReportPublishStep());
			job.getTriggers().add(new BranchUpdateTrigger());
			job.getTriggers().add(new PullRequestUpdateTrigger());
			
			return Lists.newArrayList(job);
		} else {
			return new ArrayList<>();
		}
	}

}