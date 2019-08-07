package io.onedev.server.plugin.laravel;

import java.util.Iterator;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.json.*;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.DefaultCISpecProvider;
import io.onedev.server.ci.job.CacheSpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.trigger.BranchUpdateTrigger;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;

public class DefaultLaravelCISpecProvider implements DefaultCISpecProvider {

	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId){
		Blob blob = project.getBlob(new BlobIdent(commitId.name(), "composer.json", FileMode.TYPE_FILE), false);

		if (blob != null) {
			String content = null;

			content = blob.getText().getContent();

			CISpec ciSpec = new CISpec();

			Job job = new Job();

			job.setName("ci");

			job.setEnvironment("1dev/php7.3:laravel");
			
			if (content.indexOf("laravel/framework") != -1) {	//Recognize react projects
				
				job.setCommands( "" 
							+ "composer install \n" 
							+ "phpunit \n" 
							);
				
				// Trigger the job automatically when there is a push to the branch
				BranchUpdateTrigger trigger = new BranchUpdateTrigger();
				job.getTriggers().add(trigger);

				CacheSpec cache = new CacheSpec();
				cache.setKey("vendor");
				cache.setPath("vendor");
				job.getCaches().add(cache);

				ciSpec.getJobs().add(job);

				return ciSpec;
			}else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
