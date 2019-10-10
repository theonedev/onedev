package org.server.plugin.cispec.django;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.DefaultCISpecProvider;
import io.onedev.server.ci.job.CacheSpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.trigger.BranchUpdateTrigger;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;

public class DefaultDjangoCISpecProvider implements DefaultCISpecProvider {

	
	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId) {
		
		Blob manageBlob = project.getBlob(new BlobIdent(commitId.name(), "manage.py", FileMode.TYPE_FILE), false);
		
		Blob requirementBlob = project.getBlob(new BlobIdent(commitId.name(), "requirements.txt", FileMode.TYPE_FILE), false);
		
		if (manageBlob != null && requirementBlob != null ) {
			
			CISpec ciSpec = new CISpec();
			
			Job job = new Job();

			job.setName("ci");
			
			job.setImage("python:3.6");
				
			Blob projectVersionBlob = project.getBlob(new BlobIdent(commitId.name(), "setup.py", FileMode.TYPE_FILE), false);
			
			String version = null;
			
			if (projectVersionBlob != null) {
				
				version = getVersion(projectVersionBlob, "version");
				//Django default version is "0.0.0"
				if(version.isEmpty())
					version = "0.0.0";
				
				job.setCommands(Lists.newArrayList(
						"set -e", 
						"buildVersion=" + version,
						"echo \"##onedev[SetBuildVersion '$buildVersion']\"",
						"python -m ensurepip",
						"pip3 install -r requirements.txt",
						"python manage.py test",
						"python setup.py sdist"));
			}else {
				version = "0.0.0";
				
				job.setCommands(Lists.newArrayList(
						"set -e",
						"buildVersion=" + version,
						"echo \"##onedev[SetBuildVersion '$buildVersion']\"",
						"python -m ensurepip",
						"pip3 install -r requirements.txt",
						"python manage.py test"));
			}

			// Trigger the job automatically when there is a push to the branch			
			BranchUpdateTrigger trigger = new BranchUpdateTrigger();
			job.getTriggers().add(trigger);
			
			/*
			 * Cache Django local repository in order not to download Django dependencies all over again for 
			 * subsequent builds
			 */
			CacheSpec cache = new CacheSpec();
			cache.setKey("python-local-repository");
			cache.setPath("/usr/local/lib/python3.6/site-packages");
			job.getCaches().add(cache);
			
			ciSpec.getJobs().add(job);
			
			return ciSpec;
		}else {
			return null;
		} 	

	}
	
	private String getVersion(Blob blob,String containName) {
		String projectVersion = null;
		String blobString = blob.getText().getContent();
		String[] blobStrings = blobString.split("\n");
		for(String targetString : blobStrings) {
			targetString = StringUtils.trim(targetString);
			if(targetString.startsWith(containName)) {
				projectVersion = StringUtils.substringAfter(targetString,"=");
				projectVersion = StringUtils.strip(projectVersion.trim(),"'\",");
				break;
			}
		}
		if(projectVersion == null)
			projectVersion = " ";
		return projectVersion;
	}
	

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
