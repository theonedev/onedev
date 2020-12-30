package io.onedev.server.model.support.administration.jobexecutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.buildspec.job.JobExecutorDiscoverer;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;

@Editable(order=10000, description="Discover appropriate job executor automatically")
@Horizontal
public class AutoDiscoveredJobExecutor extends JobExecutor {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void execute(String jobToken, JobContext context) {
		context.getLogger().log("Discovering job executor...");
		List<JobExecutor> jobExecutors = new ArrayList<>();
		for (JobExecutorDiscoverer discoverer: OneDev.getExtensions(JobExecutorDiscoverer.class)) {
			JobExecutor jobExecutor = discoverer.discover();
			if (jobExecutor != null)
				jobExecutors.add(jobExecutor);
		}
		if (!jobExecutors.isEmpty()) {
			Collections.sort(jobExecutors, new Comparator<JobExecutor>() {

				@Override
				public int compare(JobExecutor o1, JobExecutor o2) {
					return EditableUtils.getOrder(o1.getClass()) - EditableUtils.getOrder(o2.getClass());
				}
				
			});
			JobExecutor jobExecutor = jobExecutors.iterator().next();
			context.getLogger().log("Discovered job executor type: " 
					+ EditableUtils.getDisplayName(jobExecutor.getClass()));
			jobExecutor.setName(getName());
			jobExecutor.setCacheTTL(getCacheTTL());
			jobExecutor.setEnabled(isEnabled());
			jobExecutor.setJobMatch(getJobMatch());
			jobExecutor.execute(jobToken, context);
		} else {
			throw new ExplicitException("No job executors discovered");
		}
	}

}
