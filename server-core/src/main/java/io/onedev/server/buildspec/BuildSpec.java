package io.onedev.server.buildspec;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.buildspec.job.retrycondition.RetryCondition;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class BuildSpec implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(BuildSpec.class);
	
	public static final String BLOB_PATH = ".onedev-buildspec";
	
	public static final String PROP_JOBS = "jobs";
	
	private List<Job> jobs = new ArrayList<>();
	
	private List<Property> properties = new ArrayList<>();
	
	private transient Map<String, Job> jobMap;
	
	private transient Map<String, String> propertyMap;
	
	@Editable
	@Valid
	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
	
	@Editable
	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public Map<String, Job> getJobMap() {
		if (jobMap == null) { 
			jobMap = new LinkedHashMap<>();
			for (Job job: jobs)
				jobMap.put(job.getName(), job);
		}
		return jobMap;
	}
	
	public Map<String, String> getPropertyMap() {
		if (propertyMap == null) { 
			propertyMap = new LinkedHashMap<>();
			for (Property property: properties)
				propertyMap.put(property.getName(), property.getValue());
		}
		return propertyMap;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean valid = true;

		Set<String> jobNames = new HashSet<>();
		for (Job job: jobs) {
			if (!jobNames.add(job.getName())) {
				context.buildConstraintViolationWithTemplate("Duplicate names found: " + job.getName())
						.addPropertyNode(PROP_JOBS).addConstraintViolation();
				valid = false;
			}
		}

		for (int i=0; i<jobs.size(); i++) {
			Job job = jobs.get(i);
			
			int j=1;
			for (JobDependency dependency: job.getJobDependencies()) {
				Job dependencyJob = getJobMap().get(dependency.getJobName());
				if (dependencyJob != null) {
					try {
						ParamSupply.validateParams(dependencyJob.getParamSpecs(), dependency.getJobParams());
					} catch (ValidationException e) {
						String message = "Item #" + j + ": Error validating parameters of dependency job '" 
								+ dependencyJob.getName() + "': " + e.getMessage();
						context.buildConstraintViolationWithTemplate(message)
								.addPropertyNode(PROP_JOBS).addPropertyNode(Job.PROP_JOB_DEPENDENCIES)
									.inIterable().atIndex(i)
								.addConstraintViolation();
						valid = false;
					}
				} else {
					context.buildConstraintViolationWithTemplate("Dependency job not found: " + dependency.getJobName())
							.addPropertyNode("jobs").addPropertyNode(Job.PROP_JOB_DEPENDENCIES)
								.inIterable().atIndex(i)
							.addConstraintViolation();
					valid = false;
				}
				List<String> dependencyChain = Lists.newArrayList(job.getName());
				if (hasCircularDependencies(dependencyChain, dependency.getJobName())) {
					context.buildConstraintViolationWithTemplate("Circular dependencies found: " + dependencyChain)
							.addPropertyNode(PROP_JOBS).addPropertyNode(Job.PROP_JOB_DEPENDENCIES)
								.inIterable().atIndex(i)
							.addConstraintViolation();
					valid = false;
				}
				j++;
			}
			
			j=1;
			for (JobTrigger trigger: job.getTriggers()) {
				try {
					ParamSupply.validateParams(job.getParamSpecs(), trigger.getParams());
				} catch (Exception e) {
					String message = "Item #" + j + ": Error validating job parameters: " + e.getMessage();
					context.buildConstraintViolationWithTemplate(message)
							.addPropertyNode(PROP_JOBS).addPropertyNode(Job.PROP_TRIGGERS)
								.inIterable().atIndex(i)
							.addConstraintViolation();
					valid = false;
				}
				j++;
			}
			
			if (job.getRetryCondition() != null) { 
				try {
					RetryCondition.parse(job, job.getRetryCondition());
				} catch (Exception e) {
					String message = e.getMessage();
					if (message == null)
						message = "Malformed retry condition";
					context.buildConstraintViolationWithTemplate(message)
							.addPropertyNode(PROP_JOBS).addPropertyNode(Job.PROP_RETRY_CONDITION)
								.inIterable().atIndex(i)
							.addConstraintViolation();
					valid = false;
				}
			}
			
			j=1;
			for (PostBuildAction action: job.getPostBuildActions()) {
				try {
					action.validateWithContext(this, job);
				} catch (Exception e) {
					if (e.getMessage() == null)
						logger.error("Error validating post build action", e);
					context.buildConstraintViolationWithTemplate("Item #" + j + ": " + e.getMessage())
							.addPropertyNode(PROP_JOBS).addPropertyNode(Job.PROP_POST_BUILD_ACTIONS)
								.inIterable().atIndex(i)
							.addConstraintViolation();
					valid = false;
				}
				j++;
			}
		}
		
		if (!valid)
			context.disableDefaultConstraintViolation();
		return valid;
	}
	
	private boolean hasCircularDependencies(List<String> dependencyChain, String jobName) {
		if (dependencyChain.iterator().next().equals(jobName)) {
			dependencyChain.add(jobName);
			return true;
		} else if (dependencyChain.contains(jobName)) {
			// loop not at start of chain will be detected as a separate violation
			return false;
		} else {
			dependencyChain.add(jobName);
			Job job = getJobMap().get(jobName);
			if (job != null) {
				for (JobDependency dependency: job.getJobDependencies()) {
					if (hasCircularDependencies(new ArrayList<>(dependencyChain), dependency.getJobName()))
						return true;
				}
			} 
			return false;
		}
	}
	
	@Nullable
	public static BuildSpec parse(byte[] bytes) {
		String buildSpecString = new String(bytes, StandardCharsets.UTF_8); 
		if (StringUtils.isNotBlank(buildSpecString)) {
			try {
				return (BuildSpec) VersionedDocument.fromXML(buildSpecString).toBean();
			} catch (Exception e) {
				throw new InvalidBuildSpecException("Invalid build spec", e);
			}
		} else {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
		dom.getRootElement().addElement("properties");
	}
	
}
