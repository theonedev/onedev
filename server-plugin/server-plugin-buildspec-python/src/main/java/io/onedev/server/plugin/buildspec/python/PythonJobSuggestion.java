package io.onedev.server.plugin.buildspec.python;

import com.google.common.collect.Lists;
import com.moandjiezana.toml.Toml;
import io.onedev.commons.utils.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonJobSuggestion implements JobSuggestion {
	
	private static final Logger logger = LoggerFactory.getLogger(PythonJobSuggestion.class);
	
	private static final Pattern SETUP_PY_EXTRAS_REQUIRE_PATTERN = Pattern.compile("extras_require\\s*=\\s*\\{(.*?)}", Pattern.CASE_INSENSITIVE);

	private static final Pattern SETUP_PY_EXTRAS_REQUIRE_GROUP_PATTERN = Pattern.compile("[\"'](\\w+)[\"']\\s*:");
	
	private PublishJUnitReportStep newUnitTestReportPublishStep() {
		var publishUnitTestReport = new PublishJUnitReportStep();
		publishUnitTestReport.setName("publish unit test report");
		publishUnitTestReport.setReportName("Unit Test");
		publishUnitTestReport.setFilePatterns("pytest-result.xml");
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
			command = prefix + "pytest --cov --junitxml=./pytest-result.xml\n";
		else
			command = prefix + "coverage run -m unittest\n";
		return command + 
				prefix + "coverage xml\n" +
				"#" + prefix + "ruff check --exit-zero --output-format=json --output-file=ruff-result.json --exclude=.git";
	}
	
	private String getCoveragePackage(boolean withPytest) {
		return withPytest?"pytest-cov":"coverage";
	}
	
	private CommandStep newPipBuildAndTestStep(String dependencyInstallCommand, boolean withPytest) {
		CommandStep buildAndTest = new CommandStep();
		var interpreter = new ShellInterpreter();
		buildAndTest.setInterpreter(interpreter);
		buildAndTest.setName("build and test");
		buildAndTest.setImage("python");
		interpreter.setCommands("set -e\n" + dependencyInstallCommand + "\n" + "pip install " + getCoveragePackage(withPytest) + " ruff\n" + getCoverageAndRuffCommand(withPytest, ""));
		return buildAndTest;
	}
	
	private GenerateChecksumStep newChecksumGenerateStep(String files) {
		var generateChecksum = new GenerateChecksumStep();
		generateChecksum.setName("generate dependency checksum");
		generateChecksum.setFiles(files);
		generateChecksum.setTargetFile("checksum");
		return generateChecksum;
	}
	
	@Nullable
	private String guessPoetryOptionalTestGroup(Toml pyprojectToml) {
		if (pyprojectToml.getBoolean("tool.poetry.group.test.optional", false))
			return "test";
		else if (pyprojectToml.getBoolean("tool.poetry.group.dev.optional", false))
			return "dev";
		else 
			return null;
	}

	@Nullable
	private String guessPyprojectOptionalTestGroup(Toml pyprojectToml) {
		if (pyprojectToml.getList("project.optional-dependencies.test") != null)
			return "test";
		else if (pyprojectToml.getList("project.optional-dependencies.dev") != null)
			return "dev";
		else
			return null;
	}
	
	@Nullable
	private String guessSetupPyOptionalTestGroup(String setupPy) {
		Matcher extrasRequireMatcher = SETUP_PY_EXTRAS_REQUIRE_PATTERN.matcher(StringUtils.deleteWhitespace(setupPy));
		while (extrasRequireMatcher.find()) {
			Matcher groupMatcher = SETUP_PY_EXTRAS_REQUIRE_GROUP_PATTERN.matcher(extrasRequireMatcher.group(1));
			while (groupMatcher.find()) {
				var group = groupMatcher.group(1);
				if (group.equals("test") || group.equals("dev")) 
					return group;
			}
		}
		return null;
	}
	
	@Nullable
	private String guessSetupCfgOptionalTestGroup(String setupCfg) {
		var inExtrasRequire = false;
		for (var line: StringUtils.split(setupCfg, '\n')) {
			if (line.trim().equals("[options.extras_require]")) {
				inExtrasRequire = true;
			} else if (line.trim().startsWith("[")) {
				inExtrasRequire = false;
			} else if (inExtrasRequire) {
				var group = StringUtils.substringBefore(line, "=").trim();
				if (group.equals("test") || group.equals("dev")) 
					return group;
			}
		}
		return null;
	}
	
	private Job newJob() {
		var job = new Job();
		job.setName("python ci");
		return job;
	}
	
	private void addCommonStepsAndTriggers(Job job) {
		var checkout = new CheckoutStep();
		checkout.setName("checkout code");
		job.getSteps().add(0, checkout);
		job.getSteps().add(newCoverageReportPublishStep());
		job.getSteps().add(newRuffReportPublishStep());
		job.getTriggers().add(new BranchUpdateTrigger());
		job.getTriggers().add(new PullRequestUpdateTrigger());
	}
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		var jobs = new ArrayList<Job>();
		try {
			Blob blob;
			if ((blob = project.getBlob(new BlobIdent(commitId.name(), "tox.ini", FileMode.TYPE_FILE), false)) != null) {
				var job = newJob();
				job.getSteps().add(newChecksumGenerateStep("tox.ini"));
				var setupCache = new SetupCacheStep();
				setupCache.setName("set up dependency cache");
				setupCache.setKey("tox_cache_@file:checksum@");
				setupCache.setPaths(Lists.newArrayList(".tox"));
				setupCache.getLoadKeys().add("tox_cache");
				job.getSteps().add(setupCache);

				CommandStep buildAndTest = new CommandStep();
				buildAndTest.setName("build and test");
				buildAndTest.setImage("1dev/tox:1.0.0");
				buildAndTest.getInterpreter().setCommands("tox");
				job.getSteps().add(buildAndTest);
				if (blob.getText().getContent().contains("pytest"))
					job.getSteps().add(newUnitTestReportPublishStep());
				addCommonStepsAndTriggers(job);
				jobs.add(job);
			} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "poetry.lock", FileMode.TYPE_FILE), false)) != null) {
				var job = newJob();
				job.getSteps().add(newChecksumGenerateStep("poetry.lock"));

				var setupCache = new SetupCacheStep();
				setupCache.setName("set up dependency cache");
				setupCache.setKey("poetry_cache_@file:checksum@");
				setupCache.setPaths(Lists.newArrayList("/root/.cache/pypoetry"));
				setupCache.getLoadKeys().add("poetry_cache");
				job.getSteps().add(setupCache);

				var pyproject = project.getBlob(new BlobIdent(commitId.name(), "pyproject.toml", FileMode.TYPE_FILE), false);
				var pyprojectToml = pyproject != null ? new Toml().read(pyproject.getText().getContent()) : null;
				if (pyprojectToml != null && pyprojectToml.getString("tool.poetry.version") != null) {
					CommandStep detectBuildVersion = new CommandStep();
					detectBuildVersion.setName("detect build version");
					detectBuildVersion.setImage("1dev/yq:1.0.0");
					detectBuildVersion.getInterpreter().setCommands("yq '.tool.poetry.version' pyproject.toml > buildVersion");
					job.getSteps().add(detectBuildVersion);

					SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
					setBuildVersion.setName("set build version");
					setBuildVersion.setBuildVersion("@file:buildVersion@");
					job.getSteps().add(setBuildVersion);
				}

				String installCommand = "poetry install";
				if (pyprojectToml != null) {
					var optionalTestGroup = guessPoetryOptionalTestGroup(pyprojectToml);
					if (optionalTestGroup != null)
						installCommand += " --with " + optionalTestGroup;
				}
				
				var withPytest = blob.getText().getContent().contains("pytest");
				
				CommandStep buildAndTest = new CommandStep();
				buildAndTest.setName("build and test");
				buildAndTest.setImage("1dev/poetry:1.0.2");
				String commands = "" +
						"set -e\n" +
						"poetry config virtualenvs.create false\n" +
						installCommand + "\n" +
						"poetry add " + getCoveragePackage(withPytest) + " ruff\n";

				buildAndTest.getInterpreter().setCommands(commands + getCoverageAndRuffCommand(withPytest, "poetry run "));
				job.getSteps().add(buildAndTest);
				if (withPytest)
					job.getSteps().add(newUnitTestReportPublishStep());
				addCommonStepsAndTriggers(job);
				jobs.add(job);
			} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "pyproject.toml", FileMode.TYPE_FILE), false)) != null) {
				var job = newJob();
				var dependencyFiles = "pyproject.toml";
				var setupPy = project.getBlob(new BlobIdent(commitId.name(), "setup.py", FileMode.TYPE_FILE), false);
				if (setupPy != null)
					dependencyFiles += " setup.py";
				var setupCfg = project.getBlob(new BlobIdent(commitId.name(), "setup.cfg", FileMode.TYPE_FILE), false);
				if (setupCfg != null)
					dependencyFiles += " setup.cfg";
				var requirements = project.getBlob(new BlobIdent(commitId.name(), "requirements.txt", FileMode.TYPE_FILE), false);
				if (requirements != null)
					dependencyFiles += " requirements.txt";

				job.getSteps().add(newChecksumGenerateStep(dependencyFiles));
				job.getSteps().add(newPipCacheSetupStep());

				var blobContent = blob.getText().getContent();
				var pyprojectToml = new Toml().read(blobContent);
				if (pyprojectToml.getString("project.version") != null) {
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

				var installCommand = "pip install -e .";
				var optionalTestGroup = guessPyprojectOptionalTestGroup(pyprojectToml);
				if (optionalTestGroup == null && setupPy != null)
					optionalTestGroup = guessSetupPyOptionalTestGroup(setupPy.getText().getContent());
				if (optionalTestGroup == null && setupCfg != null)
					optionalTestGroup = guessSetupCfgOptionalTestGroup(setupCfg.getText().getContent());
				if (optionalTestGroup != null)
					installCommand += "[" + optionalTestGroup + "]";
				if (requirements != null)
					installCommand += "\npip install -r requirements.txt";

				var withPytest = blobContent.contains("pytest")
						|| setupPy != null && setupPy.getText().getContent().contains("pytest")
						|| setupCfg != null && setupCfg.getText().getContent().contains("pytest")
						|| requirements != null && requirements.getText().getContent().contains("pytest");
				job.getSteps().add(newPipBuildAndTestStep(installCommand, withPytest));
				if (withPytest)
					job.getSteps().add(newUnitTestReportPublishStep());
				addCommonStepsAndTriggers(job);
				jobs.add(job);
			} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "setup.py", FileMode.TYPE_FILE), false)) != null) {
				var job = newJob();
				var dependencyFiles = "setup.py";
				var setupCfg = project.getBlob(new BlobIdent(commitId.name(), "setup.cfg", FileMode.TYPE_FILE), false);
				if (setupCfg != null)
					dependencyFiles += " setup.cfg";
				var requirements = project.getBlob(new BlobIdent(commitId.name(), "requirements.txt", FileMode.TYPE_FILE), false);
				if (requirements != null)
					dependencyFiles += " requirements.txt";
				
				job.getSteps().add(newChecksumGenerateStep(dependencyFiles));
				job.getSteps().add(newPipCacheSetupStep());

				var blobContent = blob.getText().getContent();
				var installCommand = "pip install -e .";
				var optionalTestGroup = guessSetupPyOptionalTestGroup(blobContent);
				if (optionalTestGroup == null && setupCfg != null)
					optionalTestGroup = guessSetupCfgOptionalTestGroup(setupCfg.getText().getContent());
				if (optionalTestGroup != null)
					installCommand += "[" + optionalTestGroup + "]";
				if (requirements != null)
					installCommand += "\npip install -r requirements.txt";

				var withPytest = blobContent.contains("pytest") 
						|| setupCfg != null && setupCfg.getText().getContent().contains("pytest")
						|| requirements != null && requirements.getText().getContent().contains("pytest");
				job.getSteps().add(newPipBuildAndTestStep(installCommand, withPytest));
				if (withPytest)
					job.getSteps().add(newUnitTestReportPublishStep());
				addCommonStepsAndTriggers(job);
				jobs.add(job);
			} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "requirements.txt", FileMode.TYPE_FILE), false)) != null) {
				var job = newJob();
				job.getSteps().add(newChecksumGenerateStep("requirements.txt"));
				job.getSteps().add(newPipCacheSetupStep());

				var withPytest = blob.getText().getContent().contains("pytest");
				job.getSteps().add(newPipBuildAndTestStep("pip install -r requirements.txt", withPytest));
				if (withPytest)
					job.getSteps().add(newUnitTestReportPublishStep());
				addCommonStepsAndTriggers(job);
				jobs.add(job);
			} else if ((blob = project.getBlob(new BlobIdent(commitId.name(), "environment.yml", FileMode.TYPE_FILE), false)) != null) {
				var job = newJob();
				job.getSteps().add(newChecksumGenerateStep("environment.yml"));

				var setupCache = new SetupCacheStep();
				setupCache.setName("set up dependency cache");
				setupCache.setKey("conda_cache_@file:checksum@");
				setupCache.setPaths(Lists.newArrayList("/root/miniconda3/envs"));
				setupCache.getLoadKeys().add("conda_cache");
				job.getSteps().add(setupCache);

				var blobContent = blob.getText().getContent();
				Map<String, Object> environments = new Yaml().load(blobContent);
				CommandStep buildAndTest = new CommandStep();
				buildAndTest.setName("build and test");
				buildAndTest.setImage("1dev/conda:1.0.4");
				buildAndTest.setInterpreter(new ShellInterpreter());
				String commands = "" +
						"set -e\n" +
						"source /root/.bashrc\n" +
						"conda env update\n" +
						"conda activate " + environments.get("name") + "\n" +
						"conda install -y coverage ruff\n";

				var withPytest = blobContent.contains("pytest");
				buildAndTest.getInterpreter().setCommands(commands + getCoverageAndRuffCommand(withPytest, ""));
				job.getSteps().add(buildAndTest);
				if (withPytest)
					job.getSteps().add(newUnitTestReportPublishStep());
				addCommonStepsAndTriggers(job);
				jobs.add(job);
			}
		} catch (Exception e) {
			logger.error("Error suggesting python jobs", e);
		}
		return jobs;
	}

}