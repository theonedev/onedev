package io.onedev.server.plugin.buildspec.dotnet;

import com.google.common.collect.Lists;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobSuggestion;
import io.onedev.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.onedev.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.onedev.server.buildspec.step.CheckoutStep;
import io.onedev.server.buildspec.step.CommandStep;
import io.onedev.server.buildspec.step.GenerateChecksumStep;
import io.onedev.server.buildspec.step.SetupCacheStep;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.model.Project;
import io.onedev.server.plugin.report.cobertura.PublishCoberturaReportStep;
import io.onedev.server.plugin.report.roslynator.PublishRoslynatorReportStep;
import io.onedev.server.plugin.report.trx.PublishTRXReportStep;
import org.eclipse.jgit.lib.ObjectId;

import java.util.ArrayList;
import java.util.Collection;

public class DotnetJobSuggestion implements JobSuggestion {
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();

		var rootBlobIdent = new BlobIdent(commitId.name(), null);
		var blobIdentFilter = (BlobIdentFilter) blobIdent -> blobIdent.path.endsWith(".sln") || blobIdent.path.endsWith(".csproj");
		if (!project.getBlobChildren(rootBlobIdent, blobIdentFilter).isEmpty()) {
			Job job = new Job();
			job.setName("dotnet ci");

			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout code");
			job.getSteps().add(checkout);

			var generateChecksum = new GenerateChecksumStep();
			generateChecksum.setName("generate project checksum");
			generateChecksum.setFiles("**/*.csproj");
			generateChecksum.setTargetFile("checksum");
			job.getSteps().add(generateChecksum);

			var setupCache = new SetupCacheStep();
			setupCache.setName("set up nuget cache");
			setupCache.setKey("nuget_packages_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList("/root/.nuget/packages"));
			setupCache.getLoadKeys().add("nuget_packages");
			job.getSteps().add(setupCache);
			
			var testAndAnalyze = new CommandStep();
			testAndAnalyze.setName("test and analyze");
			testAndAnalyze.setImage("mcr.microsoft.com/dotnet/sdk");
			testAndAnalyze.getInterpreter().setCommands("" +
					"set -e\n" +
					"dotnet tool install -g roslynator.dotnet.cli\n" + 
					"dotnet test -l trx --collect:\"XPlat Code Coverage\"\n" +  
					"#/root/.dotnet/tools/roslynator analyze -o roslynator-analysis.xml\n");
			job.getSteps().add(testAndAnalyze);
			
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

			jobs.add(job);
		} 
		return jobs;
	}

}