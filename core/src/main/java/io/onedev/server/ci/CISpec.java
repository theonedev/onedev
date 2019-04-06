package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.ci.job.Job;
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
	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
	
	public Map<String, Job> getJobMap() {
		if (jobMap == null) { 
			jobMap = new HashMap<>();
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
			for (Dependency dependency: job.getDependencies()) {
				if (!getJobMap().containsKey(dependency.getJobName())) {
					context.buildConstraintViolationWithTemplate("Dependency job not found: " + dependency.getJobName())
							.addPropertyNode("jobs").addPropertyNode("dependencies")
								.inIterable().atIndex(i)
							.addConstraintViolation();
					valid = false;
				}
			}
		}
		
		for (int i=0; i<jobs.size(); i++) {
			Job job = jobs.get(i);
			for (Dependency dependency: job.getDependencies()) {
				List<String> dependencyChain = Lists.newArrayList(job.getName());
				if (hasCircularDependencies(dependencyChain, dependency.getJobName())) {
					context.buildConstraintViolationWithTemplate("Circular dependencies found: " + dependencyChain)
							.addPropertyNode("jobs").addPropertyNode("dependencies")
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
				for (Dependency dependency: job.getDependencies()) {
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
