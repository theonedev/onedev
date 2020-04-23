package io.onedev.server.model.support.administration.jobexecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.validation.annotation.DnsName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.JobMatch;
import io.onedev.server.web.util.SuggestionUtils;

@ExtensionPoint
@Editable
public abstract class JobExecutor implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String name;
	
	private String jobMatch = "all";
	
	private int cacheTTL = 7;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=10)
	@DnsName //this name may be used as namespace/network prefixes, so put a strict constraint
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=10000, name="Job Match Condition", description="Jobs applicable for this executor must match "
			+ "condition specified here")
	@JobMatch
	@NotEmpty
	public String getJobMatch() {
		return jobMatch;
	}

	public void setJobMatch(String jobMatch) {
		this.jobMatch = jobMatch;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestJobNames(String matchWith) {
		List<String> jobNames = new ArrayList<>(OneDev.getInstance(BuildManager.class).getJobNames(null));
		Collections.sort(jobNames);
		return SuggestionUtils.suggest(jobNames, matchWith);
	}

	@Editable(order=50000, group="More Settings", description="Specify job cache TTL (time to live) by days. "
			+ "OneDev may create multiple job caches even for same cache key to avoid cache conflicts when "
			+ "running jobs concurrently. This setting tells OneDev to remove caches inactive for specified "
			+ "time period to save disk space")
	public int getCacheTTL() {
		return cacheTTL;
	}

	public void setCacheTTL(int cacheTTL) {
		this.cacheTTL = cacheTTL;
	}
	
	public abstract void execute(String jobToken, JobContext context);

	public final boolean isApplicable(Build build) {
		return isEnabled() && io.onedev.server.util.jobmatch.JobMatch.parse(jobMatch).matches(build);
	}
	
	public Usage onDeleteProject(String projectName, int executorIndex) {
		Usage usage = new Usage();
		if (io.onedev.server.util.jobmatch.JobMatch.parse(jobMatch).isUsingProject(projectName))
			usage.add("job executor #" + executorIndex + ": job match" );
		return usage;
	}
	
	public void onRenameProject(String oldName, String newName) {
		io.onedev.server.util.jobmatch.JobMatch parsedJobMatch = 
				io.onedev.server.util.jobmatch.JobMatch.parse(this.jobMatch);
		parsedJobMatch.onRenameProject(oldName, newName);
		jobMatch = parsedJobMatch.toString();
	}

	public Usage onDeleteUser(String userName, int executorIndex) {
		Usage usage = new Usage();
		if (io.onedev.server.util.jobmatch.JobMatch.parse(jobMatch).isUsingUser(userName))
			usage.add("job executor #" + executorIndex + ": job match" );
		return usage;
	}
	
	public void onRenameUser(String oldName, String newName) {
		io.onedev.server.util.jobmatch.JobMatch parsedJobMatch = 
				io.onedev.server.util.jobmatch.JobMatch.parse(this.jobMatch);
		parsedJobMatch.onRenameUser(oldName, newName);
		jobMatch = parsedJobMatch.toString();
	}
	
}
