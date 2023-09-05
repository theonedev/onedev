package io.onedev.server.plugin.buildspec.dotnet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.CacheSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.onedev.server.buildspec.step.CheckoutStep;
import io.onedev.server.buildspec.step.CommandStep;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.plugin.report.cobertura.PublishCoberturaReportStep;
import io.onedev.server.plugin.report.roslynator.PublishRoslynatorReportStep;
import io.onedev.server.plugin.report.trx.PublishTRXReportStep;
import io.onedev.server.util.interpolative.VariableInterpolator;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class DotnetJobSuggestion implements JobSuggestion {

	private static final Logger logger = LoggerFactory.getLogger(DotnetJobSuggestion.class);
	
	public static final String DETERMINE_DOCKER_IMAGE = "dotnet:determine-docker-image";
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();

		var rootBlobIdent = new BlobIdent(commitId.name(), null);
		var blobIdentFilter = (BlobIdentFilter) blobIdent -> blobIdent.path.endsWith(".sln") || blobIdent.path.endsWith(".csproj");
		if (!project.getBlobChildren(rootBlobIdent, blobIdentFilter).isEmpty()) {
			Job job = new Job();
			job.setName("dotnet ci");

			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout");
			job.getSteps().add(checkout);
			
			String imageName = "@" + VariableInterpolator.PREFIX_SCRIPT + GroovyScript.BUILTIN_PREFIX + DETERMINE_DOCKER_IMAGE + "@";
			
			var runTest = new CommandStep();
			runTest.setName("run tests");
			runTest.setImage(imageName);
			runTest.getInterpreter().setCommands(Lists.newArrayList(
					"dotnet tool install -g roslynator.dotnet.cli",
					"dotnet test -l trx --collect:\"XPlat Code Coverage\"", 
					"/root/.dotnet/tools/roslynator analyze -o roslynator-analysis.xml"
					));
			job.getSteps().add(runTest);
			
			var publishUnitTestReportStep = new PublishTRXReportStep();
			publishUnitTestReportStep.setName("publish unit test report");
			publishUnitTestReportStep.setReportName("Unit Test");
			publishUnitTestReportStep.setFilePatterns("**/*.trx");
			publishUnitTestReportStep.setCondition(ExecuteCondition.ALWAYS);
			job.getSteps().add(publishUnitTestReportStep);

			var publishCoverageReportStep = new PublishCoberturaReportStep();
			publishCoverageReportStep.setName("publish code coverage report");
			publishCoverageReportStep.setReportName("Code Coverage");
			publishCoverageReportStep.setFilePatterns("**/coverage.cobertura.xml");
			publishCoverageReportStep.setCondition(ExecuteCondition.ALWAYS);
			job.getSteps().add(publishCoverageReportStep);

			var publishCodeProblemReportStep = new PublishRoslynatorReportStep();
			publishCodeProblemReportStep.setName("publish code problem report");
			publishCodeProblemReportStep.setReportName("Code Problems");
			publishCodeProblemReportStep.setFilePatterns("roslynator-analysis.xml");
			publishCodeProblemReportStep.setCondition(ExecuteCondition.ALWAYS);
			job.getSteps().add(publishCodeProblemReportStep);
			
			job.getTriggers().add(new BranchUpdateTrigger());
			job.getTriggers().add(new PullRequestUpdateTrigger());

			CacheSpec cache = new CacheSpec();
			cache.setKey("nuget-cache");
			cache.setPath("/root/.nuget/packages");
			job.getCaches().add(cache);
			
			jobs.add(job);
		} 
		return jobs;
	}

	public static String determineDockerImage() {
		var version = "latest";
		Build build = Build.get();
		if (build != null) {
			Project project = build.getProject();
			ObjectId commitId = build.getCommitId();

			Blob blob = project.getBlob(new BlobIdent(commitId.name(), "global.json"), false);
			if (blob != null) {
				ObjectMapper objectMapper = OneDev.getInstance(ObjectMapper.class);
				try {
					var node = objectMapper.readTree(blob.getText().getContent());
					if (node.has("sdk") && node.get("sdk").has("version"))
						version = node.get("sdk").get("version").asText();
				} catch (IOException e) {
					logger.error("Error parsing global.json", e);
				}
			}
		}
		return "mcr.microsoft.com/dotnet/sdk:" + version;
	}
}