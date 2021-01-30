package io.onedev.server.plugin.buildspec.gradle;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.buildspec.job.CacheSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.VariableInterpolator;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestTrigger;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GroovyScript;

public class GradleJobSuggestion implements JobSuggestion {
		
	public static final String DETERMINE_DOCKER_IMAGE = "gradle:determine-docker-image";
	
	public static final String DETERMINE_PROJECT_VERSION = "gradle:determine-project-version";
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();
		
		Blob gradleBlob = getGradleBlob(project, commitId);
		Blob kotlinGradleBlob = getKotlinGradleBlob(project, commitId);

		if (gradleBlob != null || kotlinGradleBlob != null) {
			Job job = new Job();
			job.setName("gradle ci");
			job.setImage("@" + VariableInterpolator.PREFIX_SCRIPTS + GroovyScript.BUILTIN_PREFIX + DETERMINE_DOCKER_IMAGE + "@");
			job.setCommands(Lists.newArrayList(
					"set -e",
					"echo \"Detecting project version (may require some time while downloading gradle dependencies)...\"",
					"buildVersion=$(gradle properties | grep ^version: | grep -v unspecified | cut -c10-)",
					"echo \"##onedev[SetBuildVersion '$buildVersion']\"",
					"gradle build"));
			setupTriggers(job);
			setupCaches(job);
			jobs.add(job);
		} 
		
		return jobs;
	}
	
	private static Blob getGradleBlob(Project project, ObjectId commitId) {
		return project.getBlob(new BlobIdent(commitId.name(), "build.gradle", FileMode.TYPE_FILE), false);		
	}
	
	private static Blob getKotlinGradleBlob(Project project, ObjectId commitId) {
		return project.getBlob(new BlobIdent(commitId.name(), "build.gradle.kts", FileMode.TYPE_FILE), false);		
	}
	
	private static Blob getGradlePropertiesBlob(Project project, ObjectId commitId) {
		return project.getBlob(new BlobIdent(commitId.name(), "gradle.properties", FileMode.TYPE_FILE), false);		
	}
	
	private void setupTriggers(Job job) {
		job.getTriggers().add(new BranchUpdateTrigger());
		job.getTriggers().add(new PullRequestTrigger());
	}
	
	private void setupCaches(Job job) {
		CacheSpec cache = new CacheSpec();
		cache.setKey("gradle-cache");
		cache.setPath("/home/gradle/.gradle");
		job.getCaches().add(cache);
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
