package io.onedev.server.plugin.buildspec.cmake;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

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
import io.onedev.server.model.Project;
import io.onedev.server.plugin.report.cobertura.PublishCoberturaReportStep;
import io.onedev.server.plugin.report.coverage.PublishCoverageReportStep;
import io.onedev.server.plugin.report.cppcheck.PublishCppcheckReportStep;

public class CmakeJobSuggestion implements JobSuggestion {
	
	private GenerateChecksumStep newChecksumGenerateStep(String files) {
		var generateChecksum = new GenerateChecksumStep();
		generateChecksum.setName("generate dependency checksum");
		generateChecksum.setFiles(files);
		generateChecksum.setTargetFile("checksum");
		return generateChecksum;
	}

	private SetupCacheStep newVcpkgCacheSetupStep() {
		var setupCache = new SetupCacheStep();
		setupCache.setName("set up dependency cache");
		setupCache.setKey("vcpkg_cache_@file:checksum@");
		setupCache.setPaths(Lists.newArrayList("/root/.cache/vcpkg"));
		setupCache.getLoadKeys().add("vcpkg_cache");
		return setupCache;
	}

	private SetupCacheStep newFetchContentCacheSetupStep() {
		var setupCache = new SetupCacheStep();
		setupCache.setName("set up dependency cache");
		setupCache.setKey("fetchcontent_cache_@file:checksum@");
		setupCache.setPaths(Lists.newArrayList("build/.deps"));
		setupCache.getLoadKeys().add("fetchcontent_cache");
		return setupCache;
	}

	private SetupCacheStep newConanCacheSetupStep() {
		var setupCache = new SetupCacheStep();
		setupCache.setName("set up dependency cache");
		setupCache.setKey("conan_cache_@file:checksum@");
		setupCache.setPaths(Lists.newArrayList("/root/.conan2/p"));
		setupCache.getLoadKeys().add("conan_cache");
		return setupCache;
	}
	
	private PublishCoverageReportStep newCoverageReportPublishStep() {
		var publishCoverageReport = new PublishCoberturaReportStep();
		publishCoverageReport.setName("publish coverage report");
		publishCoverageReport.setReportName("Coverage");
		publishCoverageReport.setFilePatterns("coverage.xml");
		publishCoverageReport.setCondition(ExecuteCondition.ALWAYS);
		return publishCoverageReport;
	}

	private PublishCppcheckReportStep newCppcheckReportPublishStep() {
		var publishCppcheckReport = new PublishCppcheckReportStep();
		publishCppcheckReport.setName("publish cppcheck report");
		publishCppcheckReport.setReportName("Cppcheck");
		publishCppcheckReport.setFilePatterns("check-result.xml");
		publishCppcheckReport.setCondition(ExecuteCondition.ALWAYS);
		return publishCppcheckReport;
	}
	
	private Job newJob() {
		Job job = new Job();
		job.setName("cmake ci");
		return job;
	}
	
	private void addCommonJobsAndTriggers(Job job) {
		var checkout = new CheckoutStep();
		checkout.setName("checkout code");
		job.getSteps().add(0, checkout);
		job.getSteps().add(newCoverageReportPublishStep());
		job.getSteps().add(newCppcheckReportPublishStep());
		job.getTriggers().add(new BranchUpdateTrigger());
		job.getTriggers().add(new PullRequestUpdateTrigger());
	}
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		List<Job> jobs = new ArrayList<>();
		var hasVcpkg = project.getBlob(new BlobIdent(commitId.name(), "vcpkg.json", FileMode.TYPE_FILE), false) != null;
		var hasConan = project.getBlob(new BlobIdent(commitId.name(), "conanfile.txt", FileMode.TYPE_FILE), false) != null;
		if (project.getBlob(new BlobIdent(commitId.name(), "CMakeLists.txt", FileMode.TYPE_FILE), false) != null) {
			Job job = newJob();
			if (hasVcpkg) {
				job.getSteps().add(newChecksumGenerateStep("vcpkg.json vcpkg-configuration.json"));
				job.getSteps().add(newVcpkgCacheSetupStep());
			} else if (hasConan) {
				job.getSteps().add(newChecksumGenerateStep("conanfile.txt"));
				job.getSteps().add(newConanCacheSetupStep());
			} else {
				job.getSteps().add(newChecksumGenerateStep("**/CMakeLists.txt"));
				job.getSteps().add(newFetchContentCacheSetupStep());
			}

			CommandStep testAndCheck = new CommandStep();
			testAndCheck.setName("test and check");
			
			// Use 1.0.0 instead of 1.0.1 (build from dev branch using cmake version 3.30.3) as the cmake 
			// vcpkg example does not work
			testAndCheck.setImage("1dev/cmake:1.0.0");
			var commandsBuilder = new StringBuilder("set -e\n");
			if (hasConan) 
				commandsBuilder.append("conan install . --output-folder=build --build=missing\n");
			commandsBuilder.append(
					"cmake -S. -Bbuild -DCMAKE_BUILD_TYPE=Release -DCMAKE_CXX_FLAGS_RELEASE=\"-g -fprofile-arcs -ftest-coverage\" -DCMAKE_C_FLAGS_RELEASE=\"-g -fprofile-arcs -ftest-coverage\"");
			if (hasVcpkg) 
				commandsBuilder.append(" -DCMAKE_TOOLCHAIN_FILE=\"$VCPKG_ROOT/scripts/buildsystems/vcpkg.cmake\"");
			else if (hasConan)
				commandsBuilder.append(" -DCMAKE_MODULE_PATH=\"$(pwd)/build\"");
			commandsBuilder.append(
					"\ncmake --build build\n" +
					"GTEST_COLOR=1 ctest --test-dir build --verbose\n" +
					"gcovr --exclude build --cobertura --cobertura-pretty > coverage.xml\n" +
					"#cppcheck -ibuild . --xml 2>check-result.xml");
			testAndCheck.getInterpreter().setCommands(commandsBuilder.toString());
			job.getSteps().add(testAndCheck);
			addCommonJobsAndTriggers(job);
			jobs.add(job);
		}
		return jobs;
	}

}