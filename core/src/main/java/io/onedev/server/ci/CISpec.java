package io.onedev.server.ci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.StringUtils;
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

	@Editable
	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		Map<String, Job> jobMap = new HashMap<>();

		boolean valid = true;
		
		for (Job job: jobs) {
			if (jobMap.containsKey(job.getName())) {
				context.buildConstraintViolationWithTemplate("Duplicate names found: " + job.getName())
						.addPropertyNode("jobs").addConstraintViolation();
				valid = false;
			} else {
				jobMap.put(job.getName(), job);
			}
		}

		for (int i=0; i<jobs.size(); i++) {
			Job job = jobs.get(i);
			for (Dependency dependency: job.getDependencies()) {
				if (!jobMap.containsKey(dependency.getJob())) {
					context.buildConstraintViolationWithTemplate("Dependency job not found: " + dependency.getJob())
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
				if (hasCircularDependencies(jobMap, dependencyChain, dependency.getJob())) {
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
	
	private boolean hasCircularDependencies(Map<String, Job> jobMap, List<String> dependencyChain, String jobName) {
		if (dependencyChain.iterator().next().equals(jobName)) {
			dependencyChain.add(jobName);
			return true;
		} else if (dependencyChain.contains(jobName)) {
			// loop not at start of chain will be detected as a separate violation
			return false;
		} else {
			dependencyChain.add(jobName);
			Job job = jobMap.get(jobName);
			if (job != null) {
				for (Dependency dependency: job.getDependencies()) {
					if (hasCircularDependencies(jobMap, new ArrayList<>(dependencyChain), dependency.getJob()))
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
			return (CISpec) VersionedDocument.fromXML(ciSpecString).toBean();
		} else {
			return null;
		}
	}
	
}
