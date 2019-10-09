package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.ValidationException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobDependency;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.trigger.JobTrigger;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class CISpec implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	public static final String BLOB_PATH = "onedev-ci.xml";
	
	private List<Job> jobs = new ArrayList<>();
	
	private transient Map<String, Job> jobMap;
	
	@Editable
	@Valid
	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
	
	public Map<String, Job> getJobMap() {
		if (jobMap == null) { 
			jobMap = new LinkedHashMap<>();
			for (Job job: jobs)
				jobMap.put(job.getName(), job);
		}
		return jobMap;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean valid = true;

		Set<String> jobNames = new HashSet<>();
		for (Job job: jobs) {
			if (!jobNames.add(job.getName())) {
				context.buildConstraintViolationWithTemplate("Duplicate names found: " + job.getName())
						.addPropertyNode("jobs").addConstraintViolation();
				valid = false;
			}
		}

		for (int i=0; i<jobs.size(); i++) {
			Job job = jobs.get(i);
			for (JobDependency dependency: job.getJobDependencies()) {
				Job dependencyJob = getJobMap().get(dependency.getJobName());
				if (dependencyJob != null) {
					try {
						JobParam.validateParams(dependencyJob.getParamSpecs(), dependency.getJobParams());
					} catch (ValidationException e) {
						String message = "Error validating job parameters of dependency '" 
								+ dependencyJob.getName() + "': " + e.getMessage();
						context.buildConstraintViolationWithTemplate(message)
								.addPropertyNode("jobs").addPropertyNode("dependencies")
									.inIterable().atIndex(i)
								.addConstraintViolation();
						valid = false;
					}
				} else {
					context.buildConstraintViolationWithTemplate("Dependency job not found: " + dependency.getJobName())
							.addPropertyNode("jobs").addPropertyNode("dependencies")
								.inIterable().atIndex(i)
							.addConstraintViolation();
					valid = false;
				}
				List<String> dependencyChain = Lists.newArrayList(job.getName());
				if (hasCircularDependencies(dependencyChain, dependency.getJobName())) {
					context.buildConstraintViolationWithTemplate("Circular dependencies found: " + dependencyChain)
							.addPropertyNode("jobs").addPropertyNode("dependencies")
								.inIterable().atIndex(i)
							.addConstraintViolation();
					valid = false;
				}
			}
			for (JobTrigger trigger: job.getTriggers()) {
				try {
					JobParam.validateParams(job.getParamSpecs(), trigger.getParams());
				} catch (Exception e) {
					String message = "Error validating job parameters: " + e.getMessage();
					context.buildConstraintViolationWithTemplate(message)
							.addPropertyNode("jobs").addPropertyNode("triggers")
								.inIterable().atIndex(i)
							.addConstraintViolation();
					valid = false;
				}
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
	public static CISpec parse(byte[] bytes) {
		String ciSpecString = new String(bytes, Charsets.UTF_8); 
		if (StringUtils.isNotBlank(ciSpecString)) {
			try {
				return (CISpec) VersionedDocument.fromXML(ciSpecString).toBean();
			} catch (Exception e) {
				throw new InvalidCISpecException("Invalid CI spec", e);
			}
		} else {
			return null;
		}
	}

}
