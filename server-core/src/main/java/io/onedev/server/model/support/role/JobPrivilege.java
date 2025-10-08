package io.onedev.server.model.support.role;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.service.BuildService;
import io.onedev.server.web.util.SuggestionUtils;

import org.jspecify.annotations.Nullable;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List; 

@Editable
public class JobPrivilege implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobNames;
	
	private boolean manageJob;
	
	private boolean runJob;
	
	private boolean accessLog;
	
	private boolean accessPipeline;
	
	private String accessibleReports;
	
	@Editable(order=100, description="Specify space-separated jobs. Use '*' or '?' for wildcard match. "
			+ "Prefix with '-' to exclude. <b class='text-danger'>NOTE: </b> Permission to access build artifacts "
			+ "will be granted implicitly in matched jobs even if no other permissions are specified here")
	@Patterns(suggester = "suggestJobNames")
	@NotEmpty
	public String getJobNames() {
		return jobNames;
	}

	public void setJobNames(String jobNames) {
		this.jobNames = jobNames;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestJobNames(String matchWith) {
		List<String> jobNames = new ArrayList<>(OneDev.getInstance(BuildService.class).getJobNames(null));
		Collections.sort(jobNames);
		return SuggestionUtils.suggest(jobNames, matchWith);
	}

	@Editable(order=100, description="Job administrative permission, including deleting builds of the job. "
			+ "It implies all other job permissions")
	public boolean isManageJob() {
		return manageJob;
	}

	public void setManageJob(boolean manageJob) {
		this.manageJob = manageJob;
	}
	
	@Editable(order=200, description="The permission to run job manually. It also implies the permission "
			+ "to access build log, build pipeline and all published reports")
	@DependsOn(property="manageJob", value="false")
	public boolean isRunJob() {
		return runJob;
	}

	public void setRunJob(boolean runJob) {
		this.runJob = runJob;
	}

	@Editable(order=300, name="Access Build Log", description="The permission to access build log")
	@DependsOn(property="runJob", value="false")
	public boolean isAccessLog() {
		return accessLog;
	}

	public void setAccessLog(boolean accessLog) {
		this.accessLog = accessLog;
	}

	@Editable(order=350, name="Access Build Pipeline", description="The permission to access build pipeline")
	@DependsOn(property="runJob", value="false")
	public boolean isAccessPipeline() {
		return accessPipeline;
	}

	public void setAccessPipeline(boolean accessPipeline) {
		this.accessPipeline = accessPipeline;
	}
	
	@Editable(order=400, name="Access Build Reports", placeholder="No accessible reports", description="Optionally specify space-separated reports. "
			+ "Use '*' or '?' for wildcard match. Prefix with '-' to exclude")
	@DependsOn(property="runJob", value="false")
	@Patterns
	@Nullable
	public String getAccessibleReports() {
		return accessibleReports;
	}

	public void setAccessibleReports(String accessibleReports) {
		this.accessibleReports = accessibleReports;
	}

}
