package io.onedev.server.ci.detector;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.cache.JobCache;
import io.onedev.server.ci.job.trigger.BranchPushedTrigger;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;

public class MavenDetector implements CISpecDetector {

	@Override
	public CISpec detect(Project project, ObjectId commitId) {
		Blob blob = project.getBlob(new BlobIdent(commitId.name(), "pom.xml", FileMode.TYPE_FILE), false);

		if (blob != null) {
			CISpec ciSpec = new CISpec();

			Job job = new Job();

			job.setName("ci");
			job.setEnvironment("maven:3.6.1-jdk-8");
			job.setCommands(Lists.newArrayList(
					"buildVersion=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout)",
					"echo \"##onedev[SetBuildVersion '$buildVersion']\"",
					"echo",
					"mvn clean test"
					));

			BranchPushedTrigger trigger = new BranchPushedTrigger();
			job.getTriggers().add(trigger);
			
			JobCache cache = new JobCache();
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
