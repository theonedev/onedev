package org.server.plugin.gradle;

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

public class DefaultGradleCISpecProvider implements DefaultCISpecProvider {

		
	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId) {
		
		Blob gradleBlob = project.getBlob(new BlobIdent(commitId.name(), "build.gradle", FileMode.TYPE_FILE), false);
		Blob kotlinBlob = project.getBlob(new BlobIdent(commitId.name(), "build.gradle.kts", FileMode.TYPE_FILE), false);
		if (gradleBlob != null ) {
			return getJob(project,commitId,gradleBlob);
		}
		if(kotlinBlob != null) {
			return getJob(project,commitId,kotlinBlob);
		}
		return null;

	}
	
	private CISpec getJob(Project project, ObjectId commitId,Blob blob) {
		
		CISpec ciSpec = new CISpec();
		
		Job job = new Job();

		job.setName("ci");
		
		String version = null;
		
		String jdkVersion = null;
		
		Blob androidBlob = project.getBlob(new BlobIdent(commitId.name(), "app/build.gradle", FileMode.TYPE_FILE), false);
		
		if(androidBlob != null) {
			version = getVersion(androidBlob, "versionName");
			job.setEnvironment("ekreative/android");
			job.setCommands("set -e\n"
					+"buildVersion="+version+"\n"
					+ "echo \"##onedev[SetBuildVersion '$buildVersion']\"\n"
					+ "./gradlew test\n"
					+ "./gradlew assembleDebug");

		}else {
			version = getVersion(blob, "version");
			jdkVersion = getVersion(blob, "sourceCompatibility");
			if(isValidInt(jdkVersion)) {
				int jdkVersionInt=Integer.parseInt(jdkVersion);
				if(jdkVersionInt>8)
				{
					job.setEnvironment("gradle");
				}
				else
				{
					job.setEnvironment("gradle:5.5.1-jdk8");
				}				
			
			}else {
				job.setEnvironment("gradle:5.5.1-jdk8");
			}
			
			job.setCommands("set -e\n"
					+"buildVersion="+version+"\n"
					+ "echo \"##onedev[SetBuildVersion '$buildVersion']\"\n"
					+ "gradle clean \n"
					+ "gradle build"
					);

		}
		
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
		
	}
	
	private String getVersion(Blob blob,String containName) {
		String projectVersion = null;
		String blobString = blob.getText().getContent();
		String[] blobStrings = blobString.split("\n");
		for(String targetString : blobStrings) {
			targetString = StringUtils.trim(targetString);
			if(targetString.startsWith(containName)) {				
				projectVersion = StringUtils.substringAfter(targetString,"=");
				projectVersion = StringUtils.strip(projectVersion.trim(),"'\"");
				break;
			}
		}
		if(projectVersion == null)
			projectVersion = " ";
		return projectVersion;
	}
	
	//判断字符串是否是整数
	private boolean isValidInt(String value) {  
        try {  
            Integer.parseInt(value);  
        } catch (NumberFormatException e) {  
            return false;  
        }  
        return true;  
    }  

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
