package org.server.plugin.django;

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

public class DefaultDjangoCISpecProvider implements DefaultCISpecProvider {

	private static final Logger logger = LoggerFactory.getLogger(DefaultDjangoCISpecProvider.class);
	
	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId) {
		
		Blob manageBlob = project.getBlob(new BlobIdent(commitId.name(), "manage.py", FileMode.TYPE_FILE), false);
		
		Blob requirementBlob = project.getBlob(new BlobIdent(commitId.name(), "requirements.txt", FileMode.TYPE_FILE), false);
		
		if (manageBlob != null && requirementBlob != null ) {
			
			CISpec ciSpec = new CISpec();
			
			Job job = new Job();

			job.setName("ci");
			
			job.setEnvironment("python:3.6");
				
			Blob projectVersionBlob = project.getBlob(new BlobIdent(commitId.name(), "setup.py", FileMode.TYPE_FILE), false);
			
			String version = null;
			
			if (projectVersionBlob != null) {
				String string = projectVersionBlob.getText().getContent();
				String[] strings = string.split("\n");
				for(String string2:strings) {
					if(string2.contains("version")) {
						int first = string2.indexOf("'");
						int last = string2.lastIndexOf("'");
						version = string2.substring(first+1, last);
						break;
					}
				}
				if(version == null)
					version = "0.0.0";
				
				job.setCommands("set -e\n"+"buildVersion="+version+"\n"
						+ "echo \"##onedev[SetBuildVersion '$buildVersion']\"\n"
						+ "python -m ensurepip \n"
						+ "pip3 install -r requirements.txt \n"
						+ "python manage.py test \n"
						+ "python setup.py sdist");
			}else {
				version = "0.0.0";
				
				job.setCommands("set -e\n"+"buildVersion="+version+"\n"
						+ "echo \"##onedev[SetBuildVersion '$buildVersion']\"\n"
						+ "python -m ensurepip \n"
						+ "pip3 install -r requirements.txt \n"
						+ "python manage.py test \n");
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

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
