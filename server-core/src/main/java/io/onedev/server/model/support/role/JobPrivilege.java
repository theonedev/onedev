package io.onedev.server.model.support.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.ShowCondition;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class JobPrivilege implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobNames;
	
	private boolean manageJob;
	
	private boolean runJob;
	
	private boolean accessLog;
	
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
		List<String> jobNames = new ArrayList<>(OneDev.getInstance(BuildManager.class).getJobNames(null));
		Collections.sort(jobNames);
		return SuggestionUtils.suggest(jobNames, matchWith);
	}

	@Editable(order=100, description="This permission is required to delete builds. It implies all other job permissions")
	public boolean isManageJob() {
		return manageJob;
	}

	public void setManageJob(boolean manageJob) {
		this.manageJob = manageJob;
	}
	
	@SuppressWarnings("unused")
	private static boolean isManageJobDisabled() {
		return !(boolean) EditContext.get().getInputValue("manageJob");
	}

	@Editable(order=200, description="The permission to run job manually. It also implies the permission "
			+ "to access build log, and all published reports")
	@ShowCondition("isManageJobDisabled")
	public boolean isRunJob() {
		return runJob;
	}

	public void setRunJob(boolean runJob) {
		this.runJob = runJob;
	}

	@SuppressWarnings("unused")
	private static boolean isRunJobDisabled() {
		return !(boolean) EditContext.get().getInputValue("runJob");
	}
	
	@Editable(order=300, name="Access Build Log", description="The permission to access build log. "
			+ "It also implies the permission to access published reports")
	@ShowCondition("isRunJobDisabled")
	public boolean isAccessLog() {
		return accessLog;
	}

	public void setAccessLog(boolean accessLog) {
		this.accessLog = accessLog;
	}

	@SuppressWarnings("unused")
	private static boolean isAccessLogDisabled() {
		return !(boolean) EditContext.get().getInputValue("accessLog");
	}
	
	@Editable(order=400, name="Access Build Reports", description="Optionally specify space-separated reports. "
			+ "Use '*' or '?' for wildcard match. Prefix with '-' to exclude. Leave empty to match all")
	@ShowCondition("isAccessLogDisabled")
	@Patterns
	@Nullable
	public String getAccessibleReports() {
		return accessibleReports;
	}

	public void setAccessibleReports(String accessibleReports) {
		this.accessibleReports = accessibleReports;
	}

}
