package org.server.plugin.android;

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

public class DefaultAndriodCISpecProvider implements DefaultCISpecProvider {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAndriodCISpecProvider.class);
	
	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId) {
		
		Blob androidBlob = project.getBlob(new BlobIdent(commitId.name(), "build.gradle", FileMode.TYPE_FILE), false);
		
		if (androidBlob != null ) {
			
			CISpec ciSpec = new CISpec();
			
			Job job = new Job();

			job.setName("ci");
			
			job.setEnvironment("ekreative/android");
			
			String version = null;
			
			Blob versionBlob = project.getBlob(new BlobIdent(commitId.name(), "app/build.gradle", FileMode.TYPE_FILE), false);
			
			if(versionBlob != null) {
				String string = versionBlob.getText().getContent();
				String[] strings = string.split("\n");
				for(String string2:strings) {
					if(string2.contains("versionName")) {
						int first = string2.indexOf("\"");
						int last = string2.lastIndexOf("\"");
						version = string2.substring(first+1, last);
						break;
					}
				}
				if(version == null)
					version = "0.0.0";
			}else {
				version = "0.0.0";
			}
			job.setCommands("set -e\n"
					+"buildVersion="+version+"\n"
					+ "echo \"##onedev[SetBuildVersion '$buildVersion']\"\n"
					+ "./gradlew test\n"
					+ "./gradlew assembleDebug");

			// Trigger the job automatically when there is a push to the branch			
			BranchUpdateTrigger trigger = new BranchUpdateTrigger();
			job.getTriggers().add(trigger);
			
			/*
			 * Cache Django local repository in order not to download Django dependencies all over again for 
			 * subsequent builds
			 */
			CacheSpec cache = new CacheSpec();
			cache.setKey("android-local-repository");
			cache.setPath("/root/.gradle");
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
