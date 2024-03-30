package io.onedev.server.job;

import io.onedev.server.buildspec.job.TriggerMatch;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.quartz.CronExpression;

import java.io.Serializable;

public class JobSchedule implements Serializable {

	private static final long serialVersionUID = 1L;

	private final ObjectId commitId;
	
	private final String jobName;
	
	private final CronExpression cronExpression;
	
	private final TriggerMatch match;
	
	public JobSchedule(ObjectId commitId, String jobName, CronExpression cronExpression,
					   TriggerMatch match) {
		this.commitId = commitId;
		this.jobName = jobName;
		this.cronExpression = cronExpression;
		this.match = match;
	}

	public ObjectId getCommitId() {
		return commitId;
	}

	public String getJobName() {
		return jobName;
	}

	public CronExpression getCronExpression() {
		return cronExpression;
	}

	public TriggerMatch getMatch() {
		return match;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) 
			return false; 
		if (other == this)  
			return true; 
		if (other.getClass() != getClass()) 
			return false;
		JobSchedule otherJobSchedule = (JobSchedule) other;
		return new EqualsBuilder()
				.append(commitId, otherJobSchedule.commitId)
				.append(jobName, otherJobSchedule.jobName)
				.append(cronExpression, otherJobSchedule.cronExpression)
				.append(match, otherJobSchedule.match)
				.isEquals();	
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(commitId)
				.append(jobName)
				.append(cronExpression)
				.append(match)
				.toHashCode();
	}
	
}
