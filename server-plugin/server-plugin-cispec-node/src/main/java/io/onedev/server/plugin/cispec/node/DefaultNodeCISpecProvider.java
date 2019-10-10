package io.onedev.server.plugin.cispec.node;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.DefaultCISpecProvider;
import io.onedev.server.ci.job.CacheSpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.trigger.BranchUpdateTrigger;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;

public class DefaultNodeCISpecProvider implements DefaultCISpecProvider {

	private static final Logger logger = LoggerFactory.getLogger(DefaultNodeCISpecProvider.class);
	
	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId) {
		Blob blob = project.getBlob(new BlobIdent(commitId.name(), "package.json", FileMode.TYPE_FILE), false);

		if (blob != null) {
			String content = null;
			String version = null;

			content = blob.getText().getContent();
			ObjectMapper objectMapper = OneDev.getInstance(ObjectMapper.class);
			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(content);
			} catch (IOException e) {
				logger.error("Error parsing package.json", e);
				return null;
			}

			CISpec ciSpec = new CISpec();

			Job job = new Job();

			job.setName("ci");

			job.setImage("node:10.16-alpine");

			if (content.indexOf("angular/core") != -1) { // Recognize angular projects

				job.setImage("1dev/cispec-node:10.16-alpine-chrome");

				version = jsonNode.findValue("version").asText();

				List<String> commands = Lists.newArrayList( 
						"buildVersion=" + version,
						"echo \"##onedev[SetBuildVersion '$buildVersion']\"", 
						"echo", 
						"npm install",
						"npm install @angular/cli");

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
						for (int i = 0; i < valueIndex; i++) {
							commands.add("npx " + valueArray[i]);
						}
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

			} else if (content.indexOf("react") != -1) { // Recognize react projects

				version = jsonNode.findValue("version").asText();

				List<String> commands = Lists.newArrayList( 
						"buildVersion=" + version,
						"echo \"##onedev[SetBuildVersion '$buildVersion']\"", 
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
			} else if (content.indexOf("vue") != -1) { // Recognize vue projects
				version = jsonNode.findValue("version").asText();

				List<String> commands = Lists.newArrayList( 
						"buildVersion=" + version,
						"echo \"##onedev[SetBuildVersion '$buildVersion']\"", 
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

			} else if (content.indexOf("express") != -1) { // Recognize express projects

				version = jsonNode.findValue("version").asText();

				String Commands = "" 
						+ "buildVersion=" + version + " \n"
						+ "echo \"##onedev[SetBuildVersion '$buildVersion']\"\n" 
						+ "echo\n" 
						+ "npm install \n";

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
							Commands = Commands + "npx " + valueArray[i] + " \n";
						}
					} else {
						Commands = Commands 
								+ "npx mocha \n";
					}
				} else {
					Commands = Commands 
							+ "npx mocha \n";
				}
			} else {
				return null;
			}
			// Trigger the job automatically when there is a push to the branch
			BranchUpdateTrigger trigger = new BranchUpdateTrigger();
			job.getTriggers().add(trigger);

			CacheSpec cache = new CacheSpec();
			cache.setKey("node_modules");
			cache.setPath("node_modules");
			job.getCaches().add(cache);

			ciSpec.getJobs().add(job);

			return ciSpec;
		} else {
			return null;
		}
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
