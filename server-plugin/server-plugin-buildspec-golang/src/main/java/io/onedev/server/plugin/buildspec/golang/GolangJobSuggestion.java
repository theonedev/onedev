package io.onedev.server.plugin.buildspec.golang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.onedev.server.buildspec.step.CheckoutStep;
import io.onedev.server.buildspec.step.CommandStep;
import io.onedev.server.buildspec.step.GenerateChecksumStep;
import io.onedev.server.buildspec.step.SetupCacheStep;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.plugin.report.checkstyle.PublishCheckstyleReportStep;
import io.onedev.server.plugin.report.cobertura.PublishCoberturaReportStep;
import io.onedev.server.plugin.report.coverage.PublishCoverageReportStep;
import io.onedev.server.plugin.report.junit.PublishJUnitReportStep;
import io.onedev.server.util.interpolative.VariableInterpolator;

public class GolangJobSuggestion implements JobSuggestion {

	public static final String DETERMINE_GO_VERSION = "golang:determine-go-version";
	
	private GenerateChecksumStep newChecksumGenerateStep(String name, String files) {
		var generateChecksum = new GenerateChecksumStep();
		generateChecksum.setName(name);
		generateChecksum.setFiles(files);
		generateChecksum.setTargetFile("checksum");
		return generateChecksum;
	}
	
	private Job newJob() {
		Job job = new Job();
		job.setName("go ci");
		return job;
	}
	
	private void addCommonJobsAndTriggers(Job job) {
		var checkout = new CheckoutStep();
		checkout.setName("checkout code");
		job.getSteps().add(0, checkout);
		job.getSteps().add(newCoverageReportPublishStep());
		job.getSteps().add(newUnitTestReportPublishStep());
		job.getSteps().add(newLintReportPublishStep());
		job.getTriggers().add(new BranchUpdateTrigger());
		job.getTriggers().add(new PullRequestUpdateTrigger());
	}

	private PublishCoverageReportStep newCoverageReportPublishStep() {
		var publishCoverageReport = new PublishCoberturaReportStep();
		publishCoverageReport.setName("publish coverage report");
		publishCoverageReport.setReportName("Coverage");
		publishCoverageReport.setFilePatterns("coverage.xml");
		publishCoverageReport.setCondition(ExecuteCondition.ALWAYS);
		return publishCoverageReport;
	}

	private PublishJUnitReportStep newUnitTestReportPublishStep() {
		var publishUnitTestReport = new PublishJUnitReportStep();
		publishUnitTestReport.setName("publish unit test report");
		publishUnitTestReport.setReportName("Unit Test");
		publishUnitTestReport.setFilePatterns("test-result.xml");
		publishUnitTestReport.setCondition(ExecuteCondition.ALWAYS);
		return publishUnitTestReport;
	}

	private PublishCheckstyleReportStep newLintReportPublishStep() {
		var publishCheckstyleReportStep = new PublishCheckstyleReportStep();
		publishCheckstyleReportStep.setName("publish lint report");
		publishCheckstyleReportStep.setReportName("Lint");
		publishCheckstyleReportStep.setFilePatterns("lint-result.xml");
		publishCheckstyleReportStep.setCondition(ExecuteCondition.ALWAYS);
		publishCheckstyleReportStep.setTabWidth(1);
		return publishCheckstyleReportStep;
	}
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		List<Job> jobs = new ArrayList<>();
		if (project.getBlob(new BlobIdent(commitId.name(), "go.mod", FileMode.TYPE_FILE), false) != null) {
			Job job = newJob();
			job.getSteps().add(newChecksumGenerateStep("generate dependency checksum", "**/go.mod"));
			var setupCache = new SetupCacheStep();
			setupCache.setName("set up dependency cache");
			setupCache.setKey("go_cache_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList("/root/.cache/go_build", "/root/.cache/golangci-lint", "/go/pkg/mod"));
			setupCache.getLoadKeys().add("go_cache");
			job.getSteps().add(setupCache);

			CommandStep buildAndTest = new CommandStep();
			buildAndTest.setName("build and test");
			
			buildAndTest.setImage("golang:@" + VariableInterpolator.PREFIX_SCRIPT + GroovyScript.BUILTIN_PREFIX + DETERMINE_GO_VERSION + "@");
			buildAndTest.getInterpreter().setCommands("" +
					"set -e\n" +
					"# Use double at to avoid being interpreted as OneDev variable substitution\n" +
					"go install github.com/axw/gocov/gocov@@latest\n" + 
					"go install github.com/AlekSi/gocov-xml@@latest\n" +
					"go install github.com/jstemmer/go-junit-report/v2@@latest\n" +
					"set +e\n" +
					"# Turn off vet as the \"check and lint\" step can do this \n" +
					"go test -vet=off -v -coverprofile=coverage.out ./... > test-result.out\n" +
					"TEST_STATUS=$?\n" +
					"go-junit-report -in test-result.out -out test-result.xml -set-exit-code\n" +
					"if [ $? -ne 0 ]; then echo \"\\033[1;31mThere are test failures. Check test report for details\\033[0m\"; exit 1; fi\n" +
					"if [ $TEST_STATUS -ne 0 ]; then exit 1; fi\n" +
					"gocov convert coverage.out | gocov-xml > coverage.xml");
			job.getSteps().add(buildAndTest);
			
			var checkAndLint = new CommandStep();
			checkAndLint.setName("check and lint");
			checkAndLint.setImage("golangci/golangci-lint");
			checkAndLint.setCondition(ExecuteCondition.NEVER);
			checkAndLint.getInterpreter().setCommands("golangci-lint run --timeout=10m --issues-exit-code=0 --out-format=checkstyle > lint-result.xml");
			job.getSteps().add(checkAndLint);
			
			addCommonJobsAndTriggers(job);
			jobs.add(job);
		}
		return jobs;
	}

	public static String determineGoVersion() {
		String goVersion = "latest";
		Build build = Build.get();
		if (build != null) {
			var blob = build.getProject().getBlob(new BlobIdent(build.getCommitHash(), "go.mod", FileMode.TYPE_FILE), false);			
			if (blob != null && blob.getText() != null) {
				for (var line : blob.getText().getLines()) {
					if (line.startsWith("go "))
						goVersion = line.substring("go ".length()).trim();
				}
			}
		}
		return goVersion;
	}
	
}