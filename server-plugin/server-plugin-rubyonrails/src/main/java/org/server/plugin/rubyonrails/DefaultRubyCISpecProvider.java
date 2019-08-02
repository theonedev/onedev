package org.server.plugin.rubyonrails;

import org.apache.commons.lang3.StringUtils;
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

public class DefaultRubyCISpecProvider implements DefaultCISpecProvider {

	private static final Logger logger = LoggerFactory.getLogger(DefaultRubyCISpecProvider.class);
	
	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId) {

			Blob gemFileLockBlob = project.getBlob(new BlobIdent(commitId.name(), "Gemfile.lock", FileMode.TYPE_FILE), false);
			if(gemFileLockBlob != null) {
				
				return getCISpec(gemFileLockBlob,project,commitId);

			}else {
				Blob gemFileBlob = project.getBlob(new BlobIdent(commitId.name(), "Gemfile", FileMode.TYPE_FILE), false);
				if(gemFileBlob != null) {
					
					return getCISpec(gemFileBlob,project,commitId);
					
				}
				
			}
			return null; 	
	}
			
	private CISpec getCISpec(Blob blob,Project project,ObjectId commitId) {
		
		String version = blob.getText().getContent();
		
		if(version.contains("rails") || version.contains("ruby")) {
			CISpec ciSpec = new CISpec();
			
			Job job = new Job();

			job.setName("ci");
			
			Blob rubyVersionBlob = project.getBlob(new BlobIdent(commitId.name(), ".ruby-version", FileMode.TYPE_FILE), false);
			
			if (rubyVersionBlob != null) {
				
				String rubyVersion = rubyVersionBlob.getText().getContent();

				job.setEnvironment(StringUtils.remove(rubyVersion.replace("-", ":"), " "));
				
			}else {
				job.setEnvironment("ruby");
			}

			setJob(job, ciSpec);
			
			return ciSpec;
			
		}
		return null;
	}
	
	private void setJob(Job job,CISpec ciSpec) {
		job.setCommands(""
				+ "echo \"Detecting project version (may require some time while downloading rails dependencies)...\"\n"+
				"bundle install"+ "\n"
				+"rails test");

		// Trigger the job automatically when there is a push to the branch			
		BranchUpdateTrigger trigger = new BranchUpdateTrigger();
		job.getTriggers().add(trigger);
		
		/*
		 * Cache Gemfile local repository in order not to download rails dependencies all over again for 
		 * subsequent builds
		 */
		CacheSpec cache = new CacheSpec();
		cache.setKey("bundle-local-repository");
		cache.setPath("/usr/local/bundle");
		job.getCaches().add(cache);
		
		ciSpec.getJobs().add(job);
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
