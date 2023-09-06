package io.onedev.server.plugin.buildspec.gradle;

import com.google.common.collect.Lists;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.buildspec.job.CacheSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.onedev.server.buildspec.step.CheckoutStep;
import io.onedev.server.buildspec.step.CommandStep;
import io.onedev.server.buildspec.step.SetBuildVersionStep;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.plugin.report.junit.PublishJUnitReportStep;
import io.onedev.server.util.interpolative.VariableInterpolator;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class GradleJobSuggestion implements JobSuggestion {
		
	public static final String DETERMINE_DOCKER_IMAGE = "gradle:determine-docker-image";
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();
		
		Blob gradleBlob = getGradleBlob(project, commitId);
		Blob kotlinGradleBlob = getKotlinGradleBlob(project, commitId);

		if (gradleBlob != null || kotlinGradleBlob != null) {
			Job job = new Job();
			job.setName("gradle ci");
			
			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout");
			job.getSteps().add(checkout);
			
			String imageName = "@" + VariableInterpolator.PREFIX_SCRIPT + GroovyScript.BUILTIN_PREFIX + DETERMINE_DOCKER_IMAGE + "@";
			
			CommandStep detectBuildVersion = new CommandStep();
			detectBuildVersion.setName("detect build version");
			detectBuildVersion.setImage(imageName);
			detectBuildVersion.getInterpreter().setCommands(Lists.newArrayList(
					"echo \"Detecting project version (may require some time while downloading gradle dependencies)...\"",
					"echo $(gradle properties | grep ^version: | grep -v unspecified | cut -c10-) > buildVersion"));
			job.getSteps().add(detectBuildVersion);
			
			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@file:buildVersion@");
			job.getSteps().add(setBuildVersion);
			
			CommandStep runTest = new CommandStep();
			runTest.setName("run test");
			runTest.setImage(imageName);
			runTest.getInterpreter().setCommands(Lists.newArrayList("gradle test"));
			job.getSteps().add(runTest);

			var publishUnitTestReportStep = new PublishJUnitReportStep();
			publishUnitTestReportStep.setName("publish unit test report");
			publishUnitTestReportStep.setReportName("Unit Test");
			publishUnitTestReportStep.setFilePatterns("**/TEST-*.xml");
			publishUnitTestReportStep.setCondition(ExecuteCondition.ALWAYS);
			job.getSteps().add(publishUnitTestReportStep);

			job.getTriggers().add(new BranchUpdateTrigger());
			job.getTriggers().add(new PullRequestUpdateTrigger());

			CacheSpec cache = new CacheSpec();
			cache.setKey("gradle-cache");
			cache.setPath("/home/gradle/.gradle");
			job.getCaches().add(cache);
			
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
	
	private static Blob getGradlePropertiesBlob(Project project, ObjectId commitId) {
		return project.getBlob(new BlobIdent(commitId.name(), "gradle.properties"), false);		
	}
	
	@Nullable
	private static String getJdkVersion(Blob blob) {
		for(String line: blob.getText().getContent().split("\n")) {
			line = line.toLowerCase().trim();
			if (line.contains("sourcecompatibility") && line.contains("=")) {
				String jdkVersion = StringUtils.substringAfter(line, "=").trim();
				jdkVersion = StringUtils.strip(jdkVersion, "'\"").trim();
				if (jdkVersion.startsWith("JavaVersion.VERSION_"))
					jdkVersion = jdkVersion.substring("JavaVersion.VERSION_".length());;
				return jdkVersion;
			}
		}
		return null;
	}

	@Nullable
	public static String determineDockerImage() {
		Build build = Build.get();
		if (build != null) {
			String jdkVersion = null;
			Blob blob = getGradlePropertiesBlob(build.getProject(), build.getCommitId());
			if (blob != null)
				jdkVersion = getJdkVersion(blob);
			if (jdkVersion == null) {
				blob = getGradleBlob(build.getProject(), build.getCommitId());
				if (blob != null) 
					jdkVersion = getJdkVersion(blob);
			}
			if (jdkVersion == null) {
				blob = getKotlinGradleBlob(build.getProject(), build.getCommitId());
				if (blob != null)
					jdkVersion = getJdkVersion(blob);
			}
			if (jdkVersion != null) {
				try {
					if (Integer.parseInt(jdkVersion) > 8)
						return "gradle";
				} catch (NumberFormatException e) {
				}
				return "gradle:5.6.3-jdk8";
			} else {
				return "gradle";
			}
		} else {
			return null;
		}
	}
	
}
