package io.onedev.server.plugin.buildspec.gradle;

import com.google.common.collect.Lists;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.onedev.server.buildspec.step.*;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.plugin.report.junit.PublishJUnitReportStep;
import org.eclipse.jgit.lib.ObjectId;

import java.util.ArrayList;
import java.util.Collection;

public class GradleJobSuggestion implements JobSuggestion {
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();
		
		Blob gradleBlob = getGradleBlob(project, commitId);
		Blob kotlinGradleBlob = getKotlinGradleBlob(project, commitId);

		if (gradleBlob != null || kotlinGradleBlob != null) {
			Job job = new Job();
			job.setName("gradle ci");
			
			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout code");
			job.getSteps().add(checkout);

			var generateChecksum = new GenerateChecksumStep();
			generateChecksum.setName("generate gradle checksum");
			generateChecksum.setFiles("**/build.gradle **/build.gradle.kts");
			generateChecksum.setTargetFile("checksum");
			job.getSteps().add(generateChecksum);

			var setupCache = new SetupCacheStep();
			setupCache.setName("set up gradle cache");
			setupCache.setKey("gradle_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList(
					"/home/gradle/.gradle/caches",
					"/home/gradle/.gradle/jdks",
					"/home/gradle/.gradle/native",
					"/home/gradle/.gradle/wrapper"));
			setupCache.getLoadKeys().add("gradle");
			job.getSteps().add(setupCache);
			
			String imageName = "gradle";
			
			CommandStep detectBuildVersion = new CommandStep();
			detectBuildVersion.setName("detect build version");
			detectBuildVersion.setImage(imageName);
			detectBuildVersion.getInterpreter().setCommands(
					"echo \"Detecting project version (may require some time while downloading gradle dependencies)...\"\n" + 
					"echo $(gradle properties | grep ^version: | grep -v unspecified | cut -c10-) > buildVersion\n");
			job.getSteps().add(detectBuildVersion);
			
			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@file:buildVersion@");
			job.getSteps().add(setBuildVersion);
			
			CommandStep runTest = new CommandStep();
			runTest.setName("run test");
			runTest.setImage(imageName);
			runTest.getInterpreter().setCommands("gradle test\n");
			job.getSteps().add(runTest);

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
	
	private static Blob getGradleBlob(Project project, ObjectId commitId) {
		var blob = project.getBlob(new BlobIdent(commitId.name(), "buildSrc/build.gradle"), false);		
		if (blob == null)
			blob = project.getBlob(new BlobIdent(commitId.name(), "build.gradle"), false);
		return blob;
	}
	
	private static Blob getKotlinGradleBlob(Project project, ObjectId commitId) {
		var blob = project.getBlob(new BlobIdent(commitId.name(), "buildSrc/build.gradle.kts"), false);		
		if (blob == null)
			blob = project.getBlob(new BlobIdent(commitId.name(), "build.gradle.kts"), false);
		return blob;
	}
	
}
