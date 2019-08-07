package io.onedev.server.plugin.maven;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.DefaultCISpecProvider;
import io.onedev.server.ci.job.CacheSpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.trigger.BranchUpdateTrigger;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;

public class DefaultMavenCISpecProvider implements DefaultCISpecProvider {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMavenCISpecProvider.class);
	
	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId) {
		Blob blob = project.getBlob(new BlobIdent(commitId.name(), "pom.xml", FileMode.TYPE_FILE), false);

		if (blob != null) {
			Document document;
			try {
				document = new SAXReader().read(new StringReader(blob.getText().getContent()));
			} catch (DocumentException e) {
				logger.debug("Error parsing pom.xml (project: {}, commit: {})", 
						project.getName(), commitId.getName(), e);
				return null;
			}
			
			String javaVersion = "1.8";

			// Use XPath with localname as POM project element may contain xmlns definition
			Node node = document.selectSingleNode("//*[local-name()='maven.compiler.source']");
			if (node != null) {
				javaVersion = node.getText().trim();
			} else {
				node = document.selectSingleNode("//*[local-name()='artifactId' and text()='maven-compiler-plugin']");
				if (node != null) 
					node = node.getParent().selectSingleNode(".//*[local-name()='source']");
				if (node != null) {
					javaVersion = node.getText().trim();
				} else {
					// detect java version from Spring initializer generated projects
					node = document.selectSingleNode("//*[local-name()='java.version']");
					if (node != null)
						javaVersion = node.getText().trim();
				}
			}
			
			CISpec ciSpec = new CISpec();
			
			Job job = new Job();

			job.setName("ci");

			try {
				if (Integer.parseInt(javaVersion) <= 8)
					job.setEnvironment("maven:3.6.1-jdk-8");
				else
					job.setEnvironment("maven:latest");
			} catch (NumberFormatException e) {
				job.setEnvironment("maven:3.6.1-jdk-8");
			}

			/*
			 * Before running maven test, we extract version of the project and use LogInstruction to tell 
			 * OneDev using extracted version for current build
			 */
			job.setCommands(""
					+ "echo \"Detecting project version (may require some time while downloading maven dependencies)...\"\n"
					+ "buildVersion=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout)\n"
					+ "echo \"##onedev[SetBuildVersion '$buildVersion']\"\n"
					+ "echo\n" 
					+ "mvn clean test");

			// Trigger the job automatically when there is a push to the branch			
			BranchUpdateTrigger trigger = new BranchUpdateTrigger();
			job.getTriggers().add(trigger);
			
			/*
			 * Cache Maven local repository in order not to download Maven dependencies all over again for 
			 * subsequent builds
			 */
			CacheSpec cache = new CacheSpec();
			cache.setKey("maven-local-repository");
			cache.setPath("/root/.m2");
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