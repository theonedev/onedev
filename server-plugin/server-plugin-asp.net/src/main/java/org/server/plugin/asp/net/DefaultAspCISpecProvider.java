package org.server.plugin.asp.net;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
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

public class DefaultAspCISpecProvider implements DefaultCISpecProvider {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAspCISpecProvider.class);
	
	@Override
	public CISpec getDefaultCISpec(Project project, ObjectId commitId) {
		Blob blob = project.getBlob(new BlobIdent(commitId.name(), project.getName()+".csproj", FileMode.TYPE_FILE), false);

		if (blob != null) {
			Document document;
			try {
				document = new SAXReader().read(new StringReader(blob.getText().getContent()));
			} catch (DocumentException e) {
				logger.debug("Error parsing .csproj(project: {}, commit: {})", 
						project.getName(), commitId.getName(), e);
				return null;
			}
			
			String aspVersion = " ";

			// Use XPath with localname as POM project element may contain xmlns definition
			Node node = document.selectSingleNode("//*[local-name()='TargetFramework']");
			if (node != null) {
				aspVersion = node.getText().trim();
			} else {
				return null;
			}
			
			CISpec ciSpec = new CISpec();
			
			Job job = new Job();

			job.setName("ci");
			
			job.setEnvironment("microsoft/dotnet:2.1-aspnetcore-runtime");

			/*
			 * Before running maven test, we extract version of the project and use LogInstruction to tell 
			 * OneDev using extracted version for current build
			 */
			job.setCommands(""
					+ "echo \"Detecting project version (may require some time while downloading maven dependencies)...\"\n"
					+ "echo\n" 
					+ "docker run -d -p 5000:5000 '$job.getEnvironment()'");

			// Trigger the job automatically when there is a push to the branch			
			BranchUpdateTrigger trigger = new BranchUpdateTrigger();
			job.getTriggers().add(trigger);
			
			/*
			 * Cache Maven local repository in order not to download Maven dependencies all over again for 
			 * subsequent builds
			 */
			CacheSpec cache = new CacheSpec();
			cache.setKey("asp-local-repository");
			cache.setPath("/root/.asp");
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
