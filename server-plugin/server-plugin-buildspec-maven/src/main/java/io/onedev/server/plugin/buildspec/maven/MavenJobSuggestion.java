package io.onedev.server.plugin.buildspec.maven;

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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

public class MavenJobSuggestion implements JobSuggestion {

	private static final Logger logger = LoggerFactory.getLogger(MavenJobSuggestion.class);
	
	public static final String DETERMINE_DOCKER_IMAGE = "maven:determine-docker-image";
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();
		
		Blob blob = project.getBlob(new BlobIdent(commitId.name(), "pom.xml", FileMode.TYPE_FILE), false);
		if (blob != null) {
			Job job = new Job();
			job.setName("maven ci");

			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout");
			job.getSteps().add(checkout);
			
			String imageName = "@" + VariableInterpolator.PREFIX_SCRIPT + GroovyScript.BUILTIN_PREFIX + DETERMINE_DOCKER_IMAGE + "@";
			
			CommandStep detectBuildVersion = new CommandStep();
			detectBuildVersion.setName("detect build version");
			detectBuildVersion.setImage(imageName);
			detectBuildVersion.getInterpreter().setCommands(Lists.newArrayList(
					"echo \"Detecting project version (may require some time while downloading maven dependencies)...\"",
					"echo $(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout) > buildVersion"));
			job.getSteps().add(detectBuildVersion);
			
			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@file:buildVersion@");
			job.getSteps().add(setBuildVersion);
			
			CommandStep runTests = new CommandStep();
			runTests.setName("run tests");
			runTests.setImage(imageName);
			runTests.getInterpreter().setCommands(Lists.newArrayList("mvn clean test"));
			job.getSteps().add(runTests);

			var publishUnitTestReportStep = new PublishJUnitReportStep();
			publishUnitTestReportStep.setName("publish unit test report");
			publishUnitTestReportStep.setReportName("Unit Test");
			publishUnitTestReportStep.setFilePatterns("**/TEST-*.xml");
			publishUnitTestReportStep.setCondition(ExecuteCondition.ALWAYS);
			job.getSteps().add(publishUnitTestReportStep);
			
			job.getTriggers().add(new BranchUpdateTrigger());
			job.getTriggers().add(new PullRequestUpdateTrigger());
			
			CacheSpec cache = new CacheSpec();
			cache.setKey("maven-cache");
			cache.setPath("/root/.m2/repository");
			job.getCaches().add(cache);
			
			jobs.add(job);
		} 
		return jobs;
	}

	@Nullable
	public static String determineDockerImage() {
		Build build = Build.get();
		if (build != null) {
			Project project = build.getProject();
			ObjectId commitId = build.getCommitId();
	
			Blob blob = project.getBlob(new BlobIdent(commitId.name(), "pom.xml", FileMode.TYPE_FILE), false);
	
			Document document;
			try {
				document = new SAXReader().read(new StringReader(blob.getText().getContent()));
			} catch (DocumentException e) {
				logger.debug("Error parsing pom.xml (project: {}, commit: {})",
						project.getPath(), commitId.getName(), e);
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
	
			try {
				if (Integer.parseInt(javaVersion) <= 8)
					return "maven:3.8.4-jdk-8";
				else
					return "maven:latest";
			} catch (NumberFormatException e) {
				return "maven:3.8.4-jdk-8";
			}		
		} else {
			return null;
		}
	}
}