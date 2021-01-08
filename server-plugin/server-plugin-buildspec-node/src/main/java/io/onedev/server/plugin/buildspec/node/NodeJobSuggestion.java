package io.onedev.server.plugin.buildspec.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.CacheSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.VariableInterpolator;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GroovyScript;

public class NodeJobSuggestion implements JobSuggestion {

	private static final Logger logger = LoggerFactory.getLogger(NodeJobSuggestion.class);
	
	public static final String DETERMINE_PROJECT_VERSION = "node:determine-project-version";
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();

		Blob blob = project.getBlob(new BlobIdent(commitId.name(), "package.json", FileMode.TYPE_FILE), false);
		if (blob == null)
			return jobs;
		
		String content = blob.getText().getContent();
		ObjectMapper objectMapper = OneDev.getInstance(ObjectMapper.class);
		JsonNode jsonNode;
		try {
			jsonNode = objectMapper.readTree(content);
		} catch (IOException e) {
			logger.error("Error parsing package.json", e);
			return jobs;
		}

		if (content.indexOf("angular/core") != -1) { // Recognize angular projects
			Job job = new Job();
			job.setName("angular ci");
			job.setImage("1dev/buildspec-node:10.16-alpine-chrome");
			List<String> commands = Lists.newArrayList( 
					"echo \"##onedev[SetBuildVersion '@" + VariableInterpolator.PREFIX_SCRIPTS + GroovyScript.BUILTIN_PREFIX + DETERMINE_PROJECT_VERSION + "@']\"", 
					"echo", 
					"npm install",
					"npm install @@angular/cli");

			if (jsonNode.has("scripts")) {
				JsonNode jsonScripts = jsonNode.get("scripts");
				Iterator<String> iterator = jsonScripts.fieldNames();
				int length = jsonScripts.size();
				String[] valueArray = new String[length];
				int valueIndex = 0;

				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					if (key.indexOf("lint") != -1 || key.indexOf("build") != -1) {
						String value = jsonScripts.findValue(key).asText();
						valueArray[valueIndex] = value;
						valueIndex++;
					} else if (key.indexOf("test") != -1) {
						String value = jsonScripts.findValue(key).asText();
						valueArray[valueIndex] = value + " --watch=false --browsers=ChromeHeadless";
						valueIndex++;
					}
				}

				if (valueArray[0] != null) {
					for (int i = 0; i < valueIndex; i++) 
						commands.add("npx " + valueArray[i]);
				} else {
					commands.addAll(Lists.newArrayList(
							"npx ng lint",
							"npx ng test --watch=false --browsers=ChromeHeadless", 
							"npx ng build"));
				}
			} else {
				commands.addAll(Lists.newArrayList(
						"npx ng lint",
						"npx ng test --watch=false --browsers=ChromeHeadless", 
						"npx ng build"));
			}

			job.setCommands(commands);
			setupTriggers(job);
			setupCaches(job);
			jobs.add(job);
		} 
		
		if (content.indexOf("react") != -1) { // Recognize react projects
			Job job = new Job();
			job.setName("react ci");
			job.setImage("node:10.16-alpine");

			List<String> commands = Lists.newArrayList( 
					"echo \"##onedev[SetBuildVersion '@" + VariableInterpolator.PREFIX_SCRIPTS + GroovyScript.BUILTIN_PREFIX + DETERMINE_PROJECT_VERSION + "@']\"", 
					"echo",
					"npm install typescript", 
					"npm install", 
					"export CI=TRUE");

			if (jsonNode.has("scripts")) {
				JsonNode jsonScripts = jsonNode.get("scripts");
				Iterator<String> iterator = jsonScripts.fieldNames();
				int length = jsonScripts.size();
				String[] valueArray = new String[length];
				int valueIndex = 0;

				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					if (key.indexOf("lint") != -1 || key.indexOf("test") != -1 || key.indexOf("build") != -1) {
						String value = jsonScripts.findValue(key).asText();
						valueArray[valueIndex] = value;
						valueIndex++;
					}
				}

				if (valueArray[0] != null) {
					for (int i = 0; i < valueIndex; i++) {
						commands.add("npx " + valueArray[i]);
					}
				} else {
					commands.addAll(Lists.newArrayList(
							"npx react-scripts test", 
							"npx react-scripts build"));
				}
			} else {
				commands.addAll(Lists.newArrayList(
						"npx react-scripts test", 
						"npx react-scripts build"));
			}

			job.setCommands(commands);
			setupTriggers(job);
			setupCaches(job);
			jobs.add(job);
		} 
		
		if (content.indexOf("vue") != -1) { // Recognize vue projects
			Job job = new Job();
			job.setName("vue ci");
			job.setImage("node:10.16-alpine");

			List<String> commands = Lists.newArrayList( 
					"echo \"##onedev[SetBuildVersion '@" + VariableInterpolator.PREFIX_SCRIPTS + GroovyScript.BUILTIN_PREFIX + DETERMINE_PROJECT_VERSION + "@']\"", 
					"echo", 
					"npm install");

			if (jsonNode.has("scripts")) {
				JsonNode jsonScripts = jsonNode.get("scripts");
				Iterator<String> iterator = jsonScripts.fieldNames();
				int length = jsonScripts.size();
				String[] valueArray = new String[length];
				int valueIndex = 0;

				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					if (key.indexOf("lint") != -1 || key.indexOf("test") != -1 || key.indexOf("build") != -1) {
						String value = jsonScripts.findValue(key).asText();
						valueArray[valueIndex] = value;
						valueIndex++;
					}
				}

				if (valueArray[0] != null) {
					for (int i = 0; i < valueIndex; i++) {
						commands.add("npx " + valueArray[i]);
					}
				} else {
					commands.add("npx jest");
				}
			} else {
				commands.add("npx jest");
			}

			job.setCommands(commands);
			setupTriggers(job);
			setupCaches(job);
			jobs.add(job);
		} 
		
		if (content.indexOf("express") != -1) { // Recognize express projects
			Job job = new Job();
			job.setName("express ci");
			job.setImage("node:10.16-alpine");

			List<String> commands = Lists.newArrayList( 
					"echo \"##onedev[SetBuildVersion '@" + VariableInterpolator.PREFIX_SCRIPTS + GroovyScript.BUILTIN_PREFIX + DETERMINE_PROJECT_VERSION + "@']\"", 
					"echo", 
					"npm install");

			if (jsonNode.has("scripts")) {
				JsonNode jsonScripts = jsonNode.get("scripts");
				Iterator<String> iterator = jsonScripts.fieldNames();
				int length = jsonScripts.size();
				String[] valueArray = new String[length];
				int valueIndex = 0;

				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					if (key.indexOf("lint") != -1 || key.indexOf("test") != -1 || key.indexOf("build") != -1) {
						String value = jsonScripts.findValue(key).asText();
						valueArray[valueIndex] = value;
						valueIndex++;
					}
				}

				if (valueArray[0] != null) {
					for (int i = 0; i < valueIndex; i++) 
						commands.add("npx " + valueArray[i]);
				} else {
					commands.add("npx mocha");
				}
			} else {
				commands.add("npx mocha");
			}
			job.setCommands(commands);
			setupTriggers(job);
			setupCaches(job);
			jobs.add(job);
		} 
		return jobs;
	}
	
	private void setupCaches(Job job) {
		CacheSpec cache = new CacheSpec();
		cache.setKey("npm-cache");
		cache.setPath("/root/.npm");
		job.getCaches().add(cache);
	}

	private void setupTriggers(Job job) {
		BranchUpdateTrigger trigger = new BranchUpdateTrigger();
		job.getTriggers().add(trigger);
	}
	
	@Nullable
	private static JsonNode getPackageJson(Project project, ObjectId commitId) {
		Blob blob = project.getBlob(new BlobIdent(commitId.name(), "package.json", FileMode.TYPE_FILE), false);
		if (blob != null) {
			String content = blob.getText().getContent();
			ObjectMapper objectMapper = OneDev.getInstance(ObjectMapper.class);
			try {
				return objectMapper.readTree(content);
			} catch (IOException e) {
				logger.error("Error parsing package.json", e);
			}
		} 
		return null;
	}

	@Nullable
	public static String determineProjectVersion() {
		Build build = Build.get();
		if (build != null) {
			JsonNode jsonNode = getPackageJson(build.getProject(), build.getCommitId());
			if (jsonNode != null) {
				JsonNode versionNode = jsonNode.findValue("version");
				if (versionNode != null)
					return versionNode.asText();
				else
					return null;
			}
			else
				return null;
		} else {
			return null;
		}
	}
}
