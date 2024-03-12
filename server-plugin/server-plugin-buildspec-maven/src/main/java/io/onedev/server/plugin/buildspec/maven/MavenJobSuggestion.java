package io.onedev.server.plugin.buildspec.maven;

import com.google.common.collect.Lists;
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
import io.onedev.server.plugin.report.junit.PublishJUnitReportStep;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class MavenJobSuggestion implements JobSuggestion {
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();
		
		Blob blob = project.getBlob(new BlobIdent(commitId.name(), "pom.xml", FileMode.TYPE_FILE), false);
		if (blob != null) {
			Job job = new Job();
			job.setName("maven ci");

			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout code");
			job.getSteps().add(checkout);
			
			var generateChecksum = new GenerateChecksumStep();
			generateChecksum.setName("generate pom checksum");
			generateChecksum.setFiles("**/pom.xml");
			generateChecksum.setTargetFile("checksum");
			job.getSteps().add(generateChecksum);

			var setupCache = new SetupCacheStep();
			setupCache.setName("set up maven cache");
			setupCache.setKey("maven_repository_@file:checksum@");
			setupCache.setPath("/root/.m2/repository");
			setupCache.getLoadKeys().add("maven_repository");
			job.getSteps().add(setupCache);
			
			CommandStep detectBuildVersion = new CommandStep();
			detectBuildVersion.setName("detect build version");
			detectBuildVersion.setImage("maven");
			var interpreter = new ShellInterpreter();
			interpreter.setShell("bash");
			interpreter.setCommands(
					"set -e\n" +
					"echo \"Detecting project version (may require some time while downloading maven dependencies)...\"\n" +
							"echo $(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout) > buildVersion\n");
			detectBuildVersion.setInterpreter(interpreter);						
			job.getSteps().add(detectBuildVersion);
			
			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@file:buildVersion@");
			job.getSteps().add(setBuildVersion);
			
			CommandStep runTests = new CommandStep();
			runTests.setName("run tests");
			runTests.setImage("maven");
			runTests.getInterpreter().setCommands("mvn clean test\n");
			job.getSteps().add(runTests);

			var publishUnitTestReportStep = new PublishJUnitReportStep();
			publishUnitTestReportStep.setName("publish unit test report");
			publishUnitTestReportStep.setReportName("Unit Test");
			publishUnitTestReportStep.setFilePatterns("**/TEST-*.xml");
			publishUnitTestReportStep.setCondition(ExecuteCondition.ALWAYS);
			job.getSteps().add(publishUnitTestReportStep);
			
			job.getTriggers().add(new BranchUpdateTrigger());
			job.getTriggers().add(new PullRequestUpdateTrigger());
			
			jobs.add(job);
		} 
		return jobs;
	}
	
}