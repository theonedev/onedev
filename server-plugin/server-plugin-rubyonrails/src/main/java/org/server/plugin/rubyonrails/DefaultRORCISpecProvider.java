package org.server.plugin.rubyonrails;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.DefaultCISpecProvider;
import io.onedev.server.ci.job.CacheSpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.trigger.BranchUpdateTrigger;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;

public class DefaultRORCISpecProvider implements DefaultCISpecProvider {


	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId) {

		Blob gemFileLockBlob = project.getBlob(new BlobIdent(commitId.name(), "Gemfile.lock", FileMode.TYPE_FILE),
				false);
		if (gemFileLockBlob != null) {

			return getCISpec(gemFileLockBlob, project, commitId);

		} else {
			Blob gemFileBlob = project.getBlob(new BlobIdent(commitId.name(), "Gemfile", FileMode.TYPE_FILE), false);
			if (gemFileBlob != null) {

				return getCISpec(gemFileBlob, project, commitId);

			}

		}
		return null;
	}

	private CISpec getCISpec(Blob blob, Project project, ObjectId commitId) {

		String version = blob.getText().getContent();

		if (version.contains("rails") || version.contains("ruby")) {
			CISpec ciSpec = new CISpec();

			Job job = new Job();

			job.setName("ci");

			Blob rubyVersionBlob = project.getBlob(new BlobIdent(commitId.name(), ".ruby-version", FileMode.TYPE_FILE),
					false);

			if (rubyVersionBlob != null) {

				String rubyVersion = rubyVersionBlob.getText().getContent();

				rubyVersion = StringUtils.trim(rubyVersion);

				rubyVersion = StringUtils.strip(rubyVersion, "ruby-: ");

				job.setEnvironment("ruby:" + rubyVersion);

			} else {
				job.setEnvironment("ruby");
			}
			String projectVersion = null;
			
			Blob projectVersionBlob = project.getBlob(new BlobIdent(commitId.name(), "config/version.rb", FileMode.TYPE_FILE),
					false);
			if(projectVersionBlob != null) {
				
				String projectVersionBlobContext = projectVersionBlob.getText().getContent();
				
				String[] blobStrings = projectVersionBlobContext.split("\n");
				
				for(String targetString : blobStrings) {
					targetString = StringUtils.trim(targetString);
					if(targetString.startsWith("VERSION")) {
						projectVersion = StringUtils.substringAfter(targetString,"=");
						projectVersion = StringUtils.strip(projectVersion.trim(),"'\",");					
						break;
					}
				}
	
			}
			if(projectVersion == null) {
				job.setCommands("bundle install" + "\n"
						+ "rails test");
			}else {
				job.setCommands("set -e\n"+"buildVersion="+projectVersion+"\n"
						+ "echo \"##onedev[SetBuildVersion '$buildVersion']\"\n"
						+ "bundle install" + "\n"
						+ "rails test");
				
			}
			
			

			// Trigger the job automatically when there is a push to the branch
			BranchUpdateTrigger trigger = new BranchUpdateTrigger();
			job.getTriggers().add(trigger);

			/*
			 * Cache Gemfile local repository in order not to download rails dependencies
			 * all over again for subsequent builds
			 */
			CacheSpec cache = new CacheSpec();
			cache.setKey("bundle-local-repository");
			cache.setPath("/usr/local/bundle");
			job.getCaches().add(cache);

			ciSpec.getJobs().add(job);
			return ciSpec;

		}
		return null;
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
