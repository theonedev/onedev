package io.onedev.server.plugin.buildspec.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.step.*;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.util.interpolative.VariableInterpolator;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jspecify.annotations.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
		
		if (content.indexOf("react") != -1) { // Recognize react projects
			Job job = new Job();
			job.setName("react ci");
			
			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout code");
			job.getSteps().add(checkout);
			
			job.getSteps().addAll(newCacheSteps());
			
			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@" + VariableInterpolator.PREFIX_SCRIPT + GroovyScript.BUILTIN_PREFIX + DETERMINE_PROJECT_VERSION + "@");
			job.getSteps().add(setBuildVersion);
			
			CommandStep runCommands = new CommandStep();
			runCommands.setName("build & test");
			runCommands.setImage("node");

			var commandsBuilder = new StringBuilder();
			commandsBuilder.append("npm install\n");
			commandsBuilder.append("export CI=TRUE\n");

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
						commandsBuilder.append("npx ").append(valueArray[i]).append("\n");
					}
				} else {
					commandsBuilder.append("npx react-scripts test\n");
					commandsBuilder.append("npx react-scripts build\n");
				}
			} else {
				commandsBuilder.append("npx react-scripts test\n");
				commandsBuilder.append("npx react-scripts build\n");
			}

			runCommands.getInterpreter().setCommands(commandsBuilder.toString());
			job.getSteps().add(runCommands);
			
			setupTriggers(job);
			jobs.add(job);
		} 
		
		if (content.indexOf("vue") != -1) { // Recognize vue projects
			Job job = new Job();
			job.setName("vue ci");
			
			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout code");
			job.getSteps().add(checkout);
			
			job.getSteps().addAll(newCacheSteps());
			
			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@" + VariableInterpolator.PREFIX_SCRIPT + GroovyScript.BUILTIN_PREFIX + DETERMINE_PROJECT_VERSION + "@");
			job.getSteps().add(setBuildVersion);
			
			CommandStep runCommands = new CommandStep();
			runCommands.setName("build & test");
			runCommands.setImage("node");

			var commandsBuilder = new StringBuilder("npm install\n");

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
						commandsBuilder.append("npx ").append(valueArray[i]).append("\n");
					}
				} else {
					commandsBuilder.append("npx jest\n");
				}
			} else {
				commandsBuilder.append("npx jest\n");
			}

			runCommands.getInterpreter().setCommands(commandsBuilder.toString());
			job.getSteps().add(runCommands);
			
			setupTriggers(job);
			jobs.add(job);
		} 
		
		if (content.indexOf("express") != -1) { // Recognize express projects
			Job job = new Job();
			job.setName("express ci");
			
			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout code");
			job.getSteps().add(checkout);
			
			job.getSteps().addAll(newCacheSteps());
			
			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@" + VariableInterpolator.PREFIX_SCRIPT + GroovyScript.BUILTIN_PREFIX + DETERMINE_PROJECT_VERSION + "@");
			job.getSteps().add(setBuildVersion);
			
			CommandStep runCommands = new CommandStep();
			runCommands.setName("build & test");
			runCommands.setImage("node");

			var commandsBuilder = new StringBuilder("npm install\n");

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
						commandsBuilder.append("npx ").append(valueArray[i]).append("\n");
				} else {
					commandsBuilder.append("npx mocha\n");
				}
			} else {
				commandsBuilder.append("npx mocha\n");
			}
			runCommands.getInterpreter().setCommands(commandsBuilder.toString());
			
			job.getSteps().add(runCommands);
			
			setupTriggers(job);
			jobs.add(job);
		} 
		return jobs;
	}
	
	private List<Step> newCacheSteps() {
		var generateChecksum = new GenerateChecksumStep();
		generateChecksum.setName("generate package checksum");
		generateChecksum.setFiles("package-lock.json yarn.lock");
		generateChecksum.setTargetFile("checksum");
		
		var setupCache = new SetupCacheStep();
		setupCache.setName("set up npm cache");
		setupCache.setKey("node_modules_@file:checksum@");
		setupCache.setPaths(Lists.newArrayList("node_modules"));
		setupCache.getLoadKeys().add("node_modules");
		
		return Lists.newArrayList(generateChecksum, setupCache);
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
