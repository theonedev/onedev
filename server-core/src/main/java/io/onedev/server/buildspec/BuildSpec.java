package io.onedev.server.buildspec;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.buildspec.step.StepTemplate;
import io.onedev.server.buildspec.step.UseTemplateStep;
import io.onedev.server.data.migration.VersionedYamlDoc;
import io.onedev.server.data.migration.XmlBuildSpecMigrator;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.build.JobProperty;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.WicketUtils;

@Editable
@ClassValidating
public class BuildSpec implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final LoadingCache<String, byte[]> parseCache =  
			CacheBuilder.newBuilder().softValues().build(new CacheLoader<String, byte[]>() {
	        
		@Override
        public byte[] load(String key) {
			String buildSpecString = key;
			if (buildSpecString.trim().startsWith("<?xml"))
				buildSpecString = XmlBuildSpecMigrator.migrate(buildSpecString);
			try {
				return SerializationUtils.serialize(VersionedYamlDoc.fromYaml(buildSpecString).toBean(BuildSpec.class));
			} catch (Exception e) {
				throw new BuildSpecParseException("Malformed build spec", e);
			}
        }
	        
	});
	
	public static final String BLOB_PATH = ".onedev-buildspec.yml";
	
	private static final String PROP_JOBS = "jobs";
	
	private static final String PROP_SERVICES = "services";
	
	private static final String PROP_STEP_TEMPLATES = "stepTemplates";
	
	private static final String PROP_PROPERTIES = "properties";
	
	private static final String PROP_IMPORTS = "imports";
	
	private List<Job> jobs = new ArrayList<>();
	
	private List<StepTemplate> stepTemplates = new ArrayList<>();

	private List<Service> services = new ArrayList<>();
	
	private List<JobProperty> properties = new ArrayList<>();
	
	private List<Import> imports = new ArrayList<>();
	
	private transient List<BuildSpec> importedBuildSpecs;
	
	private transient Map<String, Job> jobMap;
	
	private transient Map<String, StepTemplate> stepTemplateMap;
	
	private transient Map<String, Service> serviceMap;
	
	private transient Map<String, JobProperty> propertyMap;
	
	@Editable
	@Valid
	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
		jobMap = null;
	}
	
	@Editable
	@Valid
	public List<StepTemplate> getStepTemplates() {
		return stepTemplates;
	}

	public void setStepTemplates(List<StepTemplate> stepTemplates) {
		this.stepTemplates = stepTemplates;
		stepTemplateMap = null;
	}

	@Editable
	@Valid
	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
		serviceMap = null;
	}

	@Editable
	@Valid
	public List<JobProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<JobProperty> properties) {
		this.properties = properties;
		propertyMap = null;
	}

	@Editable
	@Valid
	public List<Import> getImports() {
		return imports;
	}

	public void setImports(List<Import> imports) {
		this.imports = imports;
		importedBuildSpecs = null;
	}
	
	private List<BuildSpec> getImportedBuildSpecs(Collection<String> commitChain) {
		if (importedBuildSpecs == null) {
			importedBuildSpecs = new ArrayList<>();
			for (Import aImport: getImports()) {
				try {
					var importCommit = aImport.getCommit();
					if (!commitChain.contains(importCommit.name())) {
						Collection<String> newCommitChain = new HashSet<>(commitChain);
						newCommitChain.add(importCommit.name());
						BuildSpec importedBuildSpec = aImport.getBuildSpec();
						JobAuthorizationContext.push(new JobAuthorizationContext(aImport.getProject(), importCommit, null));
						try {
							importedBuildSpecs.addAll(importedBuildSpec.getImportedBuildSpecs(newCommitChain));
						} finally {
							JobAuthorizationContext.pop();
						}
						importedBuildSpecs.add(importedBuildSpec);
					}
				} catch (Exception e) {
					// Ignore here as we rely on this method to show viewer/editor 
					// Errors relating to this will be shown when validated
				}
			}
		}
		return importedBuildSpecs;
	}
	
	public Map<String, Job> getJobMap() {
		if (jobMap == null) { 
			jobMap = new LinkedHashMap<>();
			for (BuildSpec buildSpec: getImportedBuildSpecs(new HashSet<>())) {  
				for (Job job: buildSpec.getJobs())
					jobMap.put(job.getName(), job);
			}
			for (Job job: getJobs())
				jobMap.put(job.getName(), job);
		}
		return jobMap;
	}
	
	public Map<String, JobProperty> getPropertyMap() {
		if (propertyMap == null) { 
			propertyMap = new LinkedHashMap<>();
			for (BuildSpec buildSpec: getImportedBuildSpecs(new HashSet<>())) { 
				for (JobProperty property : buildSpec.getProperties())
					propertyMap.put(property.getName(), property);
			}
			for (JobProperty property : getProperties())
				propertyMap.put(property.getName(), property);
		}
		return propertyMap;
	}
	
	public Map<String, StepTemplate> getStepTemplateMap() {
		if (stepTemplateMap == null) { 
			stepTemplateMap = new LinkedHashMap<>();
			for (BuildSpec buildSpec: getImportedBuildSpecs(new HashSet<>())) {
				for (StepTemplate template: buildSpec.getStepTemplates())
					stepTemplateMap.put(template.getName(), template);
			}
			for (StepTemplate template: getStepTemplates())
				stepTemplateMap.put(template.getName(), template);
		}
		return stepTemplateMap;
	}
	
	public Map<String, Service> getServiceMap() {
		if (serviceMap == null) { 
			serviceMap = new LinkedHashMap<>();
			for (BuildSpec buildSpec: getImportedBuildSpecs(new HashSet<>())) {
				for (Service service: buildSpec.getServices())
					serviceMap.put(service.getName(), service);
			}
			for (Service service: services)
				serviceMap.put(service.getName(), service);
		}
		return serviceMap;
	}
	
	private <T> int getImportIndex(String namedElementName, Function<BuildSpec, Map<String, T>> namedElementMapProvider) {
		for (int i=imports.size()-1; i>=0; i--) {
			if (namedElementMapProvider.apply(imports.get(i).getBuildSpec()).containsKey(namedElementName))
				return i;
		}
		return -1;
	}
	
	private <T extends NamedElement> boolean validateImportedElements(ConstraintValidatorContext context, 
			List<T> namedElements, Function<BuildSpec, Map<String, T>> namedElementMapProvider, String elementTypeName) {
		boolean isValid = true;
		Validator validator = OneDev.getInstance(Validator.class);
		for (T element: namedElementMapProvider.apply(this).values()) {
			int elementIndex = namedElements.indexOf(element);
			if (elementIndex == -1) {
				int importIndex = getImportIndex(element.getName(), namedElementMapProvider);
				for (ConstraintViolation<T> violation: validator.validate(element)) {
					String location;
					if (violation.getPropertyPath().toString().length() != 0)
						location = "location: " + violation.getPropertyPath() + ", ";
					else
						location = "";

					String errorMessage = String.format("Error validating imported %s (%s: %s, %serror message: %s)", 
							elementTypeName, elementTypeName, element.getName(), location, violation.getMessage());
					context.buildConstraintViolationWithTemplate(errorMessage)
							.addPropertyNode(PROP_IMPORTS)
							.addBeanNode()
								.inIterable().atIndex(importIndex)
							.addConstraintViolation();
					isValid = false;
				}
			}
		}
		return isValid;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;

		Set<String> jobNames = new HashSet<>();
		for (Job job: jobs) {
			if (!jobNames.add(job.getName())) {
				context.buildConstraintViolationWithTemplate("Duplicate job name (" + job.getName() + ")")
						.addPropertyNode(PROP_JOBS).addConstraintViolation();
				isValid = false;
			}
		}
		
		Set<String> serviceNames = new HashSet<>();
		for (Service service: services) {
			if (!serviceNames.add(service.getName())) {
				context.buildConstraintViolationWithTemplate("Duplicate service name (" + service.getName() + ")")
						.addPropertyNode(PROP_SERVICES).addConstraintViolation();
				isValid = false;
			}
		}
		
		Set<String> stepTemplateNames = new HashSet<>();
		for (StepTemplate template: stepTemplates) {
			if (!stepTemplateNames.add(template.getName())) {
				context.buildConstraintViolationWithTemplate("Duplicate template name (" + template.getName() + ")")
						.addPropertyNode(PROP_STEP_TEMPLATES).addConstraintViolation();
				isValid = false;
			}
		}
		
		Set<String> propertyNames = new HashSet<>();
		for (JobProperty property : properties) {
			if (!propertyNames.add(property.getName())) {
				context.buildConstraintViolationWithTemplate("Duplicate property name (" + property.getName() + ")")
						.addPropertyNode(PROP_PROPERTIES).addConstraintViolation();
				isValid = false;
			}
		}
		
		Set<String> importProjectAndRevisions = new HashSet<>();
		for (Import aImport: imports) {
			if (!importProjectAndRevisions.add(aImport.getProjectPath() + ":" + aImport.getRevision())) {
				context.buildConstraintViolationWithTemplate(String.format("Duplicate import (project: %s, revision: %s)", aImport.getProjectPath(), aImport.getRevision()))
						.addPropertyNode(PROP_IMPORTS).addConstraintViolation();
				isValid = false;
			}
		}
		
		if (isValid) {
			if (!validateImportedElements(context, jobs, it -> it.getJobMap(), "job"))
				isValid = false;
			if (!validateImportedElements(context, services, it -> it.getServiceMap(), "service"))
				isValid = false;
			if (!validateImportedElements(context, stepTemplates, it -> it.getStepTemplateMap(), "step template"))
				isValid = false;
			if (!validateImportedElements(context, properties, it -> it.getPropertyMap(), "property"))
				isValid = false;
		}
		
		if (isValid) {
			for (StepTemplate template: getStepTemplateMap().values()) {
				int templateIndex = stepTemplates.indexOf(template);
				for (int stepIndex=0; stepIndex<template.getSteps().size(); stepIndex++) {
					Step step = template.getSteps().get(stepIndex);
					if (step instanceof UseTemplateStep) {
						try {
							checkTemplateUsages((UseTemplateStep) step, new ArrayList<>());
						} catch (Exception e) {
							if (templateIndex != -1) {
								context.buildConstraintViolationWithTemplate(e.getMessage())
										.addPropertyNode(PROP_STEP_TEMPLATES)
										.addPropertyNode(StepTemplate.PROP_STEPS)
											.inIterable().atIndex(templateIndex)
										.addPropertyNode(UseTemplateStep.PROP_TEMPLATE_NAME)
											.inIterable().atIndex(stepIndex)
										.addConstraintViolation();
							} else {
								int importIndex = getImportIndex(template.getName(), it->it.getStepTemplateMap());
								String errorMessage = String.format("Error validating imported step template (step template: %s, error message: %s)", 
											template.getName(), e.getMessage());
								context.buildConstraintViolationWithTemplate(errorMessage)
										.addPropertyNode(PROP_IMPORTS)
										.addBeanNode()
											.inIterable().atIndex(importIndex)
										.addConstraintViolation();
							}
							isValid = false;
						}
					}
				}
			}
			
			for (Job job: getJobMap().values()) {
				int jobIndex = jobs.indexOf(job);
				try {
					checkDependencies(job, new ArrayList<>());
				} catch (Exception e) {
					if (jobIndex != -1) {
						context.buildConstraintViolationWithTemplate(e.getMessage())
								.addPropertyNode(PROP_JOBS)
								.addPropertyNode(Job.PROP_JOB_DEPENDENCIES)
									.inIterable().atIndex(jobIndex)
								.addConstraintViolation();
					} else {
						int importIndex = getImportIndex(job.getName(), it->it.getJobMap());
						String errorMessage = String.format("Error validating imported job (job: %s, error message: %s)", 
								job.getName(), e.getMessage());
						context.buildConstraintViolationWithTemplate(errorMessage)
							.addPropertyNode(PROP_IMPORTS)
							.addBeanNode()
								.inIterable().atIndex(importIndex)
							.addConstraintViolation();
					}
					isValid = false;
				}
				
				for (String serviceName: job.getRequiredServices()) {
					if (!getServiceMap().containsKey(serviceName)) {
						context.buildConstraintViolationWithTemplate("Undefined service (" + serviceName + ")")
								.addPropertyNode(PROP_JOBS)
								.addPropertyNode(Job.PROP_REQUIRED_SERVICES)
									.inIterable().atIndex(jobIndex)
								.addConstraintViolation();
						isValid = false;
					}
				}
				
				for (int stepIndex=0; stepIndex<job.getSteps().size(); stepIndex++) {
					Step step = job.getSteps().get(stepIndex);
					if (step instanceof UseTemplateStep) {
						try {
							checkTemplateUsages((UseTemplateStep) step, new ArrayList<>());
						} catch (Exception e) {
							if (jobIndex != -1) {
								context.buildConstraintViolationWithTemplate(e.getMessage())
										.addPropertyNode(PROP_JOBS)
										.addPropertyNode(Job.PROP_STEPS)
											.inIterable().atIndex(jobIndex)
										.addPropertyNode(UseTemplateStep.PROP_TEMPLATE_NAME)
											.inIterable().atIndex(stepIndex)
										.addConstraintViolation();
							} else {
								int importIndex = getImportIndex(job.getName(), it->it.getJobMap());
								String errorMessage = String.format("Error validating imported job (job: %s, location: steps[%d].templateName, error message: %s)", 
										job.getName(), stepIndex, e.getMessage());
								context.buildConstraintViolationWithTemplate(errorMessage)
										.addPropertyNode(PROP_IMPORTS)
										.addBeanNode()
											.inIterable().atIndex(importIndex)
										.addConstraintViolation();
							}
							isValid = false;
						}
					}
				}
			}
		}
		
		if (isValid) {
			// Build spec and jobs are valid so far, we can do more validations with them safely
			for (Job job: getJobMap().values()) {
				int jobIndex = jobs.indexOf(job);
				for (int actionIndex=0; actionIndex<job.getPostBuildActions().size(); actionIndex++) {
					try {
						job.getPostBuildActions().get(actionIndex).validateWith(this, job);
					} catch (Exception e) {
						if (jobIndex != -1) {
							String errorMessage = String.format("Error validating post build action (index: %d, error message: %s)", 
									actionIndex+1, e.getMessage());
							context.buildConstraintViolationWithTemplate(errorMessage)
									.addPropertyNode(PROP_JOBS)
									.addPropertyNode(Job.PROP_POST_BUILD_ACTIONS)
										.inIterable().atIndex(jobIndex)
									.addConstraintViolation();
						} else {
							int importIndex = getImportIndex(job.getName(), it->it.getJobMap());
							String errorMessage = String.format("Error validating imported job (job: %s, error message: %s)", 
									job.getName(), e.getMessage());
							context.buildConstraintViolationWithTemplate(errorMessage)
									.addPropertyNode(PROP_IMPORTS)
									.addBeanNode()
										.inIterable().atIndex(importIndex)
									.addConstraintViolation();
						}
						isValid = false;
					}
				}
			}
		}
		
		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}
	
	private void checkTemplateUsages(UseTemplateStep step, List<String> templateChain) {
		if(templateChain.contains(step.getTemplateName())) {
			templateChain.add(step.getTemplateName());
			throw new ValidationException("Circular template usages (" + templateChain + ")");
		} else {
			StepTemplate template = getStepTemplateMap().get(step.getTemplateName());
			if (template != null) {
				if (templateChain.isEmpty()) {
					try {
						ParamUtils.validateParamMatrix(template.getParamSpecs(), step.getParamMatrix());
						for (var paramMap: step.getExcludeParamMaps())
							ParamUtils.validateParamMap(template.getParamSpecs(), paramMap.getParams());
					} catch (Exception e) {
						throw new ValidationException(String.format("Error validating step template parameters (%s)", e.getMessage()));
					}
				}
				templateChain.add(step.getTemplateName());
				for (Step templateStep: template.getSteps()) {
					if (templateStep instanceof UseTemplateStep) 
						checkTemplateUsages((UseTemplateStep) templateStep, new ArrayList<>(templateChain));
				}
			} else if (templateChain.isEmpty()) {
				throw new ValidationException("Step template not found (" + step.getTemplateName() + ")");
			}
		}
	}
	
	private void checkDependencies(Job job, List<String> dependencyChain) {
		for (JobDependency dependency: job.getJobDependencies()) {
			if (dependencyChain.contains(dependency.getJobName())) {
				dependencyChain.add(dependency.getJobName());
				throw new ValidationException("Circular dependencies (" + dependencyChain + ")");
			} else {
				Job dependencyJob = getJobMap().get(dependency.getJobName());
				if (dependencyJob != null) {
					if (dependencyChain.isEmpty()) {
						try {
							ParamUtils.validateParamMatrix(dependencyJob.getParamSpecs(), dependency.getParamMatrix());
							for (var paramMap: dependency.getExcludeParamMaps())
								ParamUtils.validateParamMap(dependencyJob.getParamSpecs(), paramMap.getParams());
						} catch (ValidationException e) {
							String message = String.format("Error validating dependency job parameters (dependency job: %s, error message: %s)", 
									dependencyJob.getName(), e.getMessage());
							throw new ValidationException(message);
						}
					}
					List<String> newDependencyChain = new ArrayList<>(dependencyChain);
					newDependencyChain.add(dependency.getJobName());
					checkDependencies(dependencyJob, newDependencyChain);
				} else if (dependencyChain.isEmpty()) {
					throw new ValidationException("Dependency job not found (" + dependency.getJobName() + ")");
				}
			}
		}
	}
	
	public static List<InputCompletion> suggestOverrides(List<String> imported, InputStatus status) {
		List<InputCompletion> completions = new ArrayList<>();
		String matchWith = status.getContentBeforeCaret().toLowerCase();
		for (String each: imported) {
			LinearRange match = LinearRange.match(each, matchWith);
			if (match != null) { 
				completions.add(new InputCompletion(each, each + status.getContentAfterCaret(), 
						each.length(), "override", match));
			}
		}
		
		return completions;
	}
	
	@Nullable
	public static BuildSpec get() {
		Component component = ComponentContext.get().getComponent();
		BuildSpecAware buildSpecAware = WicketUtils.findInnermost(component, BuildSpecAware.class);
		if (buildSpecAware != null) 
			return buildSpecAware.getBuildSpec();
		else
			return null;
	}
	
	public static List<InputSuggestion> suggestVariables(String matchWith, 
			boolean withBuildVersion, boolean withDynamicVariables, boolean withPauseCommand) {
		List<InputSuggestion> suggestions = new ArrayList<>();
		BuildSpec buildSpec = get();
		if (buildSpec != null) {
			ProjectBlobPage page = (ProjectBlobPage) WicketUtils.getPage();
			suggestions.addAll(SuggestionUtils.suggestVariables(
					page.getProject(), buildSpec, ParamSpec.list(), 
					matchWith, withBuildVersion, withDynamicVariables, withPauseCommand));
		}
		return suggestions;
	}
	
	@Nullable
	public static BuildSpec parse(byte[] bytes) {
		String buildSpecString = new String(bytes, StandardCharsets.UTF_8); 
		if (StringUtils.isNotBlank(buildSpecString)) {
			try {
				return SerializationUtils.deserialize(parseCache.getUnchecked(buildSpecString));
			} catch (Exception e) {
				BuildSpecParseException parseException = ExceptionUtils.find(e, BuildSpecParseException.class);
				if (parseException != null)
					throw parseException;
				else 
					throw e;
			}
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unused")
	private void migrate1(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			if (((ScalarNode)specTuple.getKeyNode()).getValue().equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (Iterator<NodeTuple> itJobTuple = jobNode.getValue().iterator(); itJobTuple.hasNext();) {
						NodeTuple jobTuple = itJobTuple.next();
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("submoduleCredentials")) {
							itJobTuple.remove();
						} else if (jobTupleKey.equals("projectDependencies")) {
							SequenceNode projectDependenciesNode = (SequenceNode) jobTuple.getValueNode();
							for (Node projectDependenciesItem: projectDependenciesNode.getValue()) {
								MappingNode projectDependencyNode = (MappingNode) projectDependenciesItem;
								for (Iterator<NodeTuple> itProjectDependencyTuple = projectDependencyNode.getValue().iterator(); 
										itProjectDependencyTuple.hasNext();) {
									NodeTuple projectDependencyTuple = itProjectDependencyTuple.next();
									if (((ScalarNode)projectDependencyTuple.getKeyNode()).getValue().equals("authentication"))
										itProjectDependencyTuple.remove();
								}								
							}
						}
					}
					NodeTuple cloneCredentialTuple = new NodeTuple(
							new ScalarNode(Tag.STR, "cloneCredential"), 
							new MappingNode(new Tag("!DefaultCredential"), Lists.newArrayList(), FlowStyle.BLOCK));
					jobNode.getValue().add(cloneCredentialTuple);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void migrate2(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			if (((ScalarNode)specTuple.getKeyNode()).getValue().equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (Iterator<NodeTuple> itJobTuple = jobNode.getValue().iterator(); itJobTuple.hasNext();) {
						NodeTuple jobTuple = itJobTuple.next();
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("defaultFixedIssuesFilter")) {
							itJobTuple.remove();
						} else if (jobTupleKey.equals("reports")) {
							SequenceNode reportsNode = (SequenceNode) jobTuple.getValueNode();
							for (Iterator<Node> itReportsItem = reportsNode.getValue().iterator(); itReportsItem.hasNext();) {
								MappingNode reportNode = (MappingNode) itReportsItem.next();
								if (reportNode.getTag().getValue().equals("!JobHtmlReport"))
									itReportsItem.remove();
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate3(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			if (((ScalarNode)specTuple.getKeyNode()).getValue().equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("reports")) {
							SequenceNode reportsNode = (SequenceNode) jobTuple.getValueNode();
							for (Node reportNode: reportsNode.getValue()) {
								if (reportNode.getTag().getValue().equals("!JobJestReport"))
									reportNode.setTag(new Tag("!JobJestTestReport"));
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void migrate4(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			if (((ScalarNode)specTuple.getKeyNode()).getValue().equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("triggers")) {
							SequenceNode triggersNode = (SequenceNode) jobTuple.getValueNode();
							for (Node triggerNode: triggersNode.getValue()) {
								if (triggerNode.getTag().getValue().equals("!PullRequestTrigger"))
									triggerNode.setTag(new Tag("!PullRequestUpdateTrigger"));
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void migrate5(VersionedYamlDoc doc, Stack<Integer> versions) {
		List<Node> newServiceNodes = new ArrayList<>();
		for (NodeTuple specTuple: doc.getValue()) {
			if (((ScalarNode)specTuple.getKeyNode()).getValue().equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					Node imageNode = null;
					Node commandsNode = null;
					Node servicesNode = null;
					String jobName = null;
					for (Iterator<NodeTuple> itJobTuple = jobNode.getValue().iterator(); itJobTuple.hasNext();) {
						NodeTuple jobTuple = itJobTuple.next();
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("name")) {
							jobName = ((ScalarNode)jobTuple.getValueNode()).getValue();
						} else if (jobTupleKey.equals("image")) {
							imageNode = jobTuple.getValueNode();
							itJobTuple.remove();
						} else if (jobTupleKey.equals("commands")) { 
							commandsNode = jobTuple.getValueNode();
							itJobTuple.remove();
						} else if (jobTupleKey.equals("services")) { 
							servicesNode = jobTuple.getValueNode();
							itJobTuple.remove();
						}
					}
					
					Preconditions.checkState(jobName != null && imageNode != null && commandsNode != null);
					
					List<NodeTuple> stepTuples = new ArrayList<>();
					stepTuples.add(new NodeTuple(new ScalarNode(Tag.STR, "image"), imageNode));
					stepTuples.add(new NodeTuple(new ScalarNode(Tag.STR, "commands"), commandsNode));
					stepTuples.add(new NodeTuple(
							new ScalarNode(Tag.STR, "condition"), 
							new ScalarNode(Tag.STR, "ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL")));
					Node stepNode = new MappingNode(new Tag("!CommandStep"), stepTuples, FlowStyle.BLOCK);
					Node stepsNode = new SequenceNode(Tag.SEQ, Lists.newArrayList(stepNode), FlowStyle.BLOCK);
					NodeTuple stepsTuple = new NodeTuple(new ScalarNode(Tag.STR, "steps"), stepsNode);
					jobNode.getValue().add(stepsTuple);
					
					if (servicesNode != null) {
						List<Node> serviceNameNodes = new ArrayList<>();
						for (Node serviceNodeItem: ((SequenceNode) servicesNode).getValue()) {
							MappingNode serviceNode = (MappingNode) serviceNodeItem;
							List<NodeTuple> newServiceTuples = new ArrayList<>();
							for (NodeTuple serviceTuple: serviceNode.getValue()) {
								if (((ScalarNode)serviceTuple.getKeyNode()).getValue().equals("name")) {
									String newServiceName = jobName + "-" 
											+ ((ScalarNode)serviceTuple.getValueNode()).getValue();
									serviceNameNodes.add(new ScalarNode(Tag.STR, newServiceName));
									newServiceTuples.add(new NodeTuple(
											new ScalarNode(Tag.STR, "name"), 
											new ScalarNode(Tag.STR, newServiceName)));
								} else {
									newServiceTuples.add(serviceTuple);
								}
							}
							newServiceNodes.add(new MappingNode(Tag.MAP, newServiceTuples, FlowStyle.BLOCK));
						}
						jobNode.getValue().add(new NodeTuple(
								new ScalarNode(Tag.STR, "requiredServices"), 
								new SequenceNode(Tag.SEQ, serviceNameNodes, FlowStyle.BLOCK)));
					}
				}
			}
		}
		
		if (!newServiceNodes.isEmpty()) {
			doc.getValue().add(new NodeTuple(
					new ScalarNode(Tag.STR, "services"), 
					new SequenceNode(Tag.SEQ, newServiceNodes, FlowStyle.BLOCK)));
		}
	}
	
	@SuppressWarnings("unused")
	private void migrate6(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			if (((ScalarNode)specTuple.getKeyNode()).getValue().equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					boolean retrieveSource = false;
					Node cloneCredentialNode = null;
					Node cloneDepthNode = null;
					Node artifactsNode = null;
					SequenceNode reportsNode = null;
					SequenceNode stepsNode = null;
					List<MappingNode> actionNodes = new ArrayList<>();
					for (Iterator<NodeTuple> itJobTuple = jobNode.getValue().iterator(); itJobTuple.hasNext();) {
						NodeTuple jobTuple = itJobTuple.next();
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("retrieveSource")) {
							retrieveSource = ((ScalarNode)jobTuple.getValueNode()).getValue().equals("true");
							itJobTuple.remove();
						} else if (jobTupleKey.equals("cloneCredential")) {
							cloneCredentialNode = jobTuple.getValueNode();
							itJobTuple.remove();
						} else if (jobTupleKey.equals("cloneDepth")) { 
							cloneDepthNode = jobTuple.getValueNode();
							itJobTuple.remove();
						} else if (jobTupleKey.equals("artifacts")) { 
							artifactsNode = jobTuple.getValueNode();
							itJobTuple.remove();
						} else if (jobTupleKey.equals("reports")) {
							reportsNode = (SequenceNode) jobTuple.getValueNode();
							itJobTuple.remove();
						} else if (jobTupleKey.equals("steps")) {
							stepsNode = (SequenceNode) jobTuple.getValueNode();
						} else if (jobTupleKey.equals("postBuildActions")) {
							SequenceNode actionsNode = (SequenceNode) jobTuple.getValueNode();
							for (Iterator<Node> itActionNode = actionsNode.getValue().iterator(); itActionNode.hasNext();) {
								MappingNode actionNode = (MappingNode) itActionNode.next();
								String tagName = actionNode.getTag().getValue();
								if (tagName.equals("!CreateTagAction") || tagName.equals("!CloseMilestoneAction")) { 
									actionNodes.add(actionNode);
									itActionNode.remove();
								}
							}
							if (actionsNode.getValue().isEmpty())
								itJobTuple.remove();
						}
					}
					Preconditions.checkState(cloneCredentialNode != null && stepsNode != null);
					if (retrieveSource) {
						List<NodeTuple> stepTuples = new ArrayList<>();
						stepTuples.add(new NodeTuple(new ScalarNode(Tag.STR, "cloneCredential"), cloneCredentialNode));
						if (cloneDepthNode != null)
							stepTuples.add(new NodeTuple(new ScalarNode(Tag.STR, "cloneDepth"), cloneDepthNode));
						stepTuples.add(new NodeTuple(
								new ScalarNode(Tag.STR, "condition"), 
								new ScalarNode(Tag.STR, "ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL")));
						Node stepNode = new MappingNode(new Tag("!CheckoutStep"), stepTuples, FlowStyle.BLOCK);
						stepsNode.getValue().add(0, stepNode);
					}
					if (artifactsNode != null) {
						List<NodeTuple> stepTuples = new ArrayList<>();
						stepTuples.add(new NodeTuple(new ScalarNode(Tag.STR, "artifacts"), artifactsNode));
						stepTuples.add(new NodeTuple(
								new ScalarNode(Tag.STR, "condition"), 
								new ScalarNode(Tag.STR, "ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL")));
						Node stepNode = new MappingNode(new Tag("!PublishArtifactStep"), stepTuples, FlowStyle.BLOCK);
						stepsNode.getValue().add(stepNode);
					}
					if (reportsNode != null) {
						for (Node reportsNodeItem: reportsNode.getValue()) {
							MappingNode reportNode = (MappingNode) reportsNodeItem;
							List<NodeTuple> stepTuples = new ArrayList<>();
							stepTuples.addAll(reportNode.getValue());
							stepTuples.add(new NodeTuple(
									new ScalarNode(Tag.STR, "condition"), 
									new ScalarNode(Tag.STR, "ALWAYS")));
							String tagName = reportNode.getTag().getValue();
							tagName = tagName.replaceFirst("Job", "Publish") + "Step";
							Node stepNode = new MappingNode(new Tag(tagName), stepTuples, FlowStyle.BLOCK);
							stepsNode.getValue().add(stepNode);
						}
					}
					for (MappingNode actionNode: actionNodes) {
						String tagName = actionNode.getTag().getValue().replace("Action", "Step");
						List<NodeTuple> stepTuples = new ArrayList<>();
						for (NodeTuple tuple: actionNode.getValue()) {
							String key = ((ScalarNode)tuple.getKeyNode()).getValue();
							if (!key.equals("condition"))
								stepTuples.add(tuple);
						}
						stepTuples.add(new NodeTuple(
								new ScalarNode(Tag.STR, "condition"), 
								new ScalarNode(Tag.STR, "ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL")));
						Node stepNode = new MappingNode(new Tag(tagName), stepTuples, FlowStyle.BLOCK);
						stepsNode.getValue().add(stepNode);
					}
				}
			}
		}
		
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) {
							SequenceNode stepsNode = (SequenceNode) jobTuple.getValueNode();
							for (Node stepsNodeItem: stepsNode.getValue()) {
								MappingNode stepNode = (MappingNode) stepsNodeItem;
								String tagName = stepNode.getTag().getValue();
								String stepName = WordUtils.uncamel(tagName.substring(1).replace("Step", "")).toLowerCase();
								stepNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "name"), 
										new ScalarNode(Tag.STR, stepName)));
							}
						}
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps")) {
							SequenceNode stepsNode = (SequenceNode) stepTemplateTuple.getValueNode();
							for (Node stepsNodeItem: stepsNode.getValue()) {
								MappingNode stepNode = (MappingNode) stepsNodeItem;
								String tagName = stepNode.getTag().getValue();
								String stepName = WordUtils.uncamel(tagName.substring(1).replace("Step", "")).toLowerCase();
								stepNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "name"), 
										new ScalarNode(Tag.STR, stepName)));
							}
						}
					}
				}				
			}
		}			
	}	

	@SuppressWarnings("unused")
	private void migrate7(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("paramSpecs")) {
							SequenceNode paramsNode = (SequenceNode) jobTuple.getValueNode();
							for (Node paramsNodeItem: paramsNode.getValue()) {
								MappingNode paramNode = (MappingNode) paramsNodeItem;
								String paramType = paramNode.getTag().getValue();
								if (paramType.equals("!NumberParam")) {
									paramNode.setTag(new Tag("!IntegerParam"));
								} else if (paramType.equals("!TextParam")) {
									NodeTuple multilineTuple = new NodeTuple(
											new ScalarNode(Tag.STR, "multiline"), 
											new ScalarNode(Tag.STR, "false"));
									paramNode.getValue().add(multilineTuple);
								}
							}
						}
					}
				}
			} else if (specKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stemTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stemTemplateTupleKey.equals("paramSpecs")) {
							SequenceNode paramsNode = (SequenceNode) stepTemplateTuple.getValueNode();
							for (Node paramsNodeItem: paramsNode.getValue()) {
								MappingNode paramNode = (MappingNode) paramsNodeItem;
								String paramType = paramNode.getTag().getValue();
								if (paramType.equals("!NumberParam")) {
									paramNode.setTag(new Tag("!IntegerParam"));
								} else if (paramType.equals("!TextParam")) {
									NodeTuple multilineTuple = new NodeTuple(
											new ScalarNode(Tag.STR, "multiline"), 
											new ScalarNode(Tag.STR, "false"));
									paramNode.getValue().add(multilineTuple);
								}
							}
						}
					}
				}
			}
		}			
	}	
	
	@SuppressWarnings("unused")
	private void migrate8(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("projectDependencies")) {
							SequenceNode projectDependenciesNode = (SequenceNode) jobTuple.getValueNode();
							for (Node projectDependenciesNodeItem: projectDependenciesNode.getValue()) {
								MappingNode projectDependencyNode = (MappingNode) projectDependenciesNodeItem;
								String buildNumber = null;
								for (Iterator<NodeTuple> itProjectDependencyTuple = projectDependencyNode.getValue().iterator(); itProjectDependencyTuple.hasNext();) {
									NodeTuple projectDependencyTuple = itProjectDependencyTuple.next();
									String projectDependencyTupleKey = ((ScalarNode)projectDependencyTuple.getKeyNode()).getValue();
									if (projectDependencyTupleKey.equals("buildNumber")) {
										buildNumber = ((ScalarNode)projectDependencyTuple.getValueNode()).getValue();
										itProjectDependencyTuple.remove();
										break;
									}
								}
								Preconditions.checkNotNull(buildNumber);
								
								List<NodeTuple> buildProviderTuples = new ArrayList<>();
								buildProviderTuples.add(new NodeTuple(
										new ScalarNode(Tag.STR, "buildNumber"), 
										new ScalarNode(Tag.STR, buildNumber)));
								Node buildProviderNode = new MappingNode(new Tag("!SpecifiedBuild"), buildProviderTuples, FlowStyle.BLOCK);
								projectDependencyNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "buildProvider"), buildProviderNode));
							}
						}
					}
				}
			}
		}			
	}	
	
	@SuppressWarnings("unused")
	private void migrate9(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) {
							SequenceNode stepsNode = (SequenceNode) jobTuple.getValueNode();
							for (Node stepsNodeItem: stepsNode.getValue()) {
								MappingNode stepNode = (MappingNode) stepsNodeItem;
								if (stepNode.getTag().getValue().equals("!CommandStep")) {
									stepNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "useTTY"), 
											new ScalarNode(Tag.BOOL, "false")));
								}
							}
						}
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps")) {
							SequenceNode stepsNode = (SequenceNode) stepTemplateTuple.getValueNode();
							for (Node stepsNodeItem: stepsNode.getValue()) {
								MappingNode stepNode = (MappingNode) stepsNodeItem;
								if (stepNode.getTag().getValue().equals("!CommandStep")) {
									stepNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "useTTY"), 
											new ScalarNode(Tag.BOOL, "false")));
								}
							}
						}
					}
				}				
			}
		}			
	}	
	
	@SuppressWarnings("unused")
	private void migrate10(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("cpuRequirement")) {
							ScalarNode cpuRequirementNode = (ScalarNode) jobTuple.getValueNode();
							String cpuRequirement = cpuRequirementNode.getValue();
							cpuRequirementNode.setValue(cpuRequirement.substring(0, cpuRequirement.length()-1));
						} else if (jobTupleKey.equals("memoryRequirement")) {
							ScalarNode memoryRequirementNode = (ScalarNode) jobTuple.getValueNode();
							String memoryRequirement = memoryRequirementNode.getValue();
							memoryRequirementNode.setValue(memoryRequirement.substring(0, memoryRequirement.length()-1));
						}
					}
				}
			} else if (specObjectKey.equals("services")) {
				SequenceNode servicesNode = (SequenceNode) specTuple.getValueNode();
				for (Node servicesNodeItem: servicesNode.getValue()) {
					MappingNode serviceNode = (MappingNode) servicesNodeItem;
					for (NodeTuple serviceTuple: serviceNode.getValue()) {
						String serviceTupleKey = ((ScalarNode)serviceTuple.getKeyNode()).getValue();
						if (serviceTupleKey.equals("cpuRequirement")) {
							ScalarNode cpuRequirementNode = (ScalarNode) serviceTuple.getValueNode();
							String cpuRequirement = cpuRequirementNode.getValue();
							cpuRequirementNode.setValue(cpuRequirement.substring(0, cpuRequirement.length()-1));
						} else if (serviceTupleKey.equals("memoryRequirement")) {
							ScalarNode memoryRequirementNode = (ScalarNode) serviceTuple.getValueNode();
							String memoryRequirement = memoryRequirementNode.getValue();
							memoryRequirementNode.setValue(memoryRequirement.substring(0, memoryRequirement.length()-1));
						}
					}
				}
			}
		}			
	}	

	@SuppressWarnings("unused")
	private void migrate11(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) {
							SequenceNode stepsNode = (SequenceNode) jobTuple.getValueNode();
							for (Node stepsNodeItem: stepsNode.getValue()) {
								MappingNode stepNode = (MappingNode) stepsNodeItem;
								if (stepNode.getTag().getValue().equals("!CheckoutStep")) {
									stepNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "withLfs"), 
											new ScalarNode(Tag.BOOL, "false")));
									stepNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "withSubmodules"), 
											new ScalarNode(Tag.BOOL, "true")));
								}
							}
						}
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps")) {
							SequenceNode stepsNode = (SequenceNode) stepTemplateTuple.getValueNode();
							for (Node stepsNodeItem: stepsNode.getValue()) {
								MappingNode stepNode = (MappingNode) stepsNodeItem;
								if (stepNode.getTag().getValue().equals("!CheckoutStep")) {
									stepNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "withLfs"), 
											new ScalarNode(Tag.BOOL, "false")));
									stepNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "withSubmodules"), 
											new ScalarNode(Tag.BOOL, "true")));
								}
							}
						}
					}
				}				
			}
		}			
	}	
	
	@SuppressWarnings("unused")
	private void migrate12(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) {
							SequenceNode stepsNode = (SequenceNode) jobTuple.getValueNode();
							for (Node stepsNodeItem: stepsNode.getValue()) {
								MappingNode stepNode = (MappingNode) stepsNodeItem;
								if (stepNode.getTag().getValue().equals("!PublishJestTestReportStep")) 
									stepNode.setTag(new Tag("!PublishJestReportStep"));
							}
						}
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps")) {
							SequenceNode stepsNode = (SequenceNode) stepTemplateTuple.getValueNode();
							for (Node stepsNodeItem: stepsNode.getValue()) {
								MappingNode stepNode = (MappingNode) stepsNodeItem;
								if (stepNode.getTag().getValue().equals("!PublishJestTestReportStep")) 
									stepNode.setTag(new Tag("!PublishJestReportStep"));
							}
						}
					}
				}				
			}
		}			
	}	
	
	@SuppressWarnings("unused")
	private void migrate13(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specTupleKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specTupleKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (Iterator<NodeTuple> itJobTuple = jobNode.getValue().iterator(); itJobTuple.hasNext();) {
						NodeTuple jobTuple = itJobTuple.next();
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("projectDependencies")) {
							SequenceNode projectDependenciesNode = (SequenceNode) jobTuple.getValueNode();
							for (Node projectDependenciesItem: projectDependenciesNode.getValue()) {
								MappingNode projectDependencyNode = (MappingNode) projectDependenciesItem;
								for (Iterator<NodeTuple> itProjectDependencyTuple = projectDependencyNode.getValue().iterator(); 
										itProjectDependencyTuple.hasNext();) {
									NodeTuple projectDependencyTuple = itProjectDependencyTuple.next();
									ScalarNode projectDependencyTupleKeyNode = ((ScalarNode)projectDependencyTuple.getKeyNode());
									if (projectDependencyTupleKeyNode.getValue().equals("projectName"))
										projectDependencyTupleKeyNode.setValue("projectPath");
								}								
							}
						}
					}
				}
			} else if (specTupleKey.equals("imports")) {
				SequenceNode importsNode = (SequenceNode) specTuple.getValueNode();
				for (Node importsNodeItem: importsNode.getValue()) {
					MappingNode importNode = (MappingNode) importsNodeItem;
					for (Iterator<NodeTuple> itImportTuple = importNode.getValue().iterator(); itImportTuple.hasNext();) {
						NodeTuple importTuple = itImportTuple.next();
						ScalarNode importTupleKeyNode = (ScalarNode)importTuple.getKeyNode();
						if (importTupleKeyNode.getValue().equals("projectName"))
							importTupleKeyNode.setValue("projectPath");
					}
				}
			}
		}
	}
	
	private void migrate14_steps(SequenceNode stepsNode) {
		for (Node stepsNodeItem: stepsNode.getValue()) {
			MappingNode stepNode = (MappingNode) stepsNodeItem;
			if (stepNode.getTag().getValue().equals("!CommandStep")) {
				Node commandsNode = null;
				for (Iterator<NodeTuple> itStepNodeTuple = stepNode.getValue().iterator(); itStepNodeTuple.hasNext();) {
					NodeTuple stepNodeTuple = itStepNodeTuple.next();
					if (((ScalarNode)stepNodeTuple.getKeyNode()).getValue().equals("commands")) {
						commandsNode = stepNodeTuple.getValueNode();
						itStepNodeTuple.remove();
						break;
					}
				}
				if (commandsNode != null) {
					List<NodeTuple> interpreterTuples = new ArrayList<>();
					interpreterTuples.add(new NodeTuple(new ScalarNode(Tag.STR, "commands"), commandsNode));
					stepNode.getValue().add(new NodeTuple(
							new ScalarNode(Tag.STR, "interpreter"), 
							new MappingNode(new Tag("!DefaultInterpreter"), interpreterTuples, FlowStyle.BLOCK)));
				}
				stepNode.getValue().add(new NodeTuple(
						new ScalarNode(Tag.STR, "runInContainer"), 
						new ScalarNode(Tag.BOOL, "true")));
			}
		}		
	}
	
	@SuppressWarnings("unused")
	private void migrate14(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) 
							migrate14_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps")) 
							migrate14_steps((SequenceNode) stepTemplateTuple.getValueNode());
					}
				}				
			}
		}			
	}	
	
	private void migrate15_steps(SequenceNode stepsNode) {
		for (Node stepsNodeItem: stepsNode.getValue()) {
			MappingNode stepNode = (MappingNode) stepsNodeItem;
			if (stepNode.getTag().getValue().equals("!BuildImageStep")) {
				for (Iterator<NodeTuple> itStepNodeTuple = stepNode.getValue().iterator(); itStepNodeTuple.hasNext();) {
					NodeTuple stepNodeTuple = itStepNodeTuple.next();
					String key = ((ScalarNode)stepNodeTuple.getKeyNode()).getValue();
					if (key.equals("useTTY") || key.equals("login"))
						itStepNodeTuple.remove();
				}
			}
		}		
	}
	
	@SuppressWarnings("unused")
	private void migrate15(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) 
							migrate15_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps")) 
							migrate15_steps((SequenceNode) stepTemplateTuple.getValueNode());
					}
				}				
			}
		}			
	}	
	
	private void migrate16_steps(SequenceNode stepsNode) {
		for (Node stepsNodeItem: stepsNode.getValue()) {
			MappingNode stepNode = (MappingNode) stepsNodeItem;
			if (stepNode.getTag().getValue().equals("!CommandStep")) {
				for (Iterator<NodeTuple> itStepNodeTuple = stepNode.getValue().iterator(); itStepNodeTuple.hasNext();) {
					NodeTuple stepNodeTuple = itStepNodeTuple.next();
					String key = ((ScalarNode)stepNodeTuple.getKeyNode()).getValue();
					if (key.equals("interpreter")) {
						MappingNode interpreterNode = (MappingNode) stepNodeTuple.getValueNode();
						if (interpreterNode.getTag().getValue().equals("!BashInterpreter")) {
							interpreterNode.setTag(new Tag("!ShellInterpreter"));
							interpreterNode.getValue().add(new NodeTuple(
									new ScalarNode(Tag.STR, "shell"), new ScalarNode(Tag.STR, "bash")));
						}
					}
				}
			}
		}		
	}
	
	@SuppressWarnings("unused")
	private void migrate16(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) 
							migrate16_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps")) 
							migrate16_steps((SequenceNode) stepTemplateTuple.getValueNode());
					}
				}				
			}
		}			
	}	
	
	@SuppressWarnings("unused")
	private void migrate17(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			if (((ScalarNode)specTuple.getKeyNode()).getValue().equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					List<MappingNode> actionNodes = new ArrayList<>();
					for (Iterator<NodeTuple> itJobTuple = jobNode.getValue().iterator(); itJobTuple.hasNext();) {
						NodeTuple jobTuple = itJobTuple.next();
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("postBuildActions")) {
							SequenceNode actionsNode = (SequenceNode) jobTuple.getValueNode();
							for (Node actionNodeItem: actionsNode.getValue()) {
								MappingNode actionNode = (MappingNode) actionNodeItem;
								if (actionNode.getTag().getValue().equals("!CreateIssueAction")) { 
									actionNode.getValue().add(new NodeTuple(
											new ScalarNode(Tag.STR, "issueConfidential"), new ScalarNode(Tag.STR, "false")));
								}
							}
						}
					}
				}
			}
		}
	}	
	
	private void migrate18_steps(SequenceNode stepsNode) {
		for (Node stepsNodeItem: stepsNode.getValue()) {
			MappingNode stepNode = (MappingNode) stepsNodeItem;
			if (stepNode.getTag().getValue().equals("!PullRepository")) {
				stepNode.getValue().add(new NodeTuple(
						new ScalarNode(Tag.STR, "syncToChildProject"), new ScalarNode(Tag.STR, "false")));
			}
		}		
	}
	
	@SuppressWarnings("unused")
	private void migrate18(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) 
							migrate18_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps")) 
							migrate18_steps((SequenceNode) stepTemplateTuple.getValueNode());
					}
				}				
			}
		}			
	}

	@SuppressWarnings("unused")
	private void migrate19(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (var it = jobNode.getValue().iterator(); it.hasNext();) {
						String jobTupleKey = ((ScalarNode)it.next().getKeyNode()).getValue();
						if (jobTupleKey.equals("cpuRequirement") || jobTupleKey.equals("memoryRequirement"))
							it.remove();
					}
				}
			} else if (specObjectKey.equals("services")) {
				SequenceNode servicesNode = (SequenceNode) specTuple.getValueNode();
				for (Node servicesNodeItem: servicesNode.getValue()) {
					MappingNode serviceNode = (MappingNode) servicesNodeItem;
					for (var it = serviceNode.getValue().iterator(); it.hasNext();) {
						String serviceTupleKey = ((ScalarNode)it.next().getKeyNode()).getValue();
						if (serviceTupleKey.equals("cpuRequirement") || serviceTupleKey.equals("memoryRequirement"))
							it.remove();
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate20(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specTupleKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specTupleKey.equals("imports")) {
				SequenceNode importsNode = (SequenceNode) specTuple.getValueNode();
				for (Node importsNodeItem: importsNode.getValue()) {
					MappingNode importNode = (MappingNode) importsNodeItem;
					for (Iterator<NodeTuple> itImportTuple = importNode.getValue().iterator(); itImportTuple.hasNext();) {
						NodeTuple importTuple = itImportTuple.next();
						ScalarNode importTupleKeyNode = (ScalarNode)importTuple.getKeyNode();
						if (importTupleKeyNode.getValue().equals("tag"))
							importTupleKeyNode.setValue("revision");
					}
				}
			}
		}
	}

	private void migrate21_steps(SequenceNode stepsNode) {
		for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
			MappingNode stepNode = (MappingNode) itStepNode.next();
			if (stepNode.getTag().getValue().equals("!PublishHtmlReportStep")) {
				itStepNode.remove();
			} else if (stepNode.getTag().getValue().equals("!PushRepository")) {
				for (var itStepNodeTuple = stepNode.getValue().iterator(); itStepNodeTuple.hasNext();) {
					String stepNodeTupleKey = ((ScalarNode)itStepNodeTuple.next().getKeyNode()).getValue();
					if (stepNodeTupleKey.equals("withLfs"))
						itStepNodeTuple.remove();
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void migrate21(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) 
							migrate21_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps")) 
							migrate21_steps((SequenceNode)stepTemplateTuple.getValueNode());
					}
				}
			}
		}
	}

	private void migrate22_steps(SequenceNode stepsNode) {
		for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
			MappingNode stepNode = (MappingNode) itStepNode.next();
			if (stepNode.getTag().getValue().equals("!BuildImageStep")) {
				for (var itStepNodeTuple = stepNode.getValue().iterator(); itStepNodeTuple.hasNext();) {
					String stepNodeTupleKey = ((ScalarNode)itStepNodeTuple.next().getKeyNode()).getValue();
					if (stepNodeTupleKey.equals("publish"))
						itStepNodeTuple.remove();
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate22(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps"))
							migrate22_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps"))
							migrate22_steps((SequenceNode)stepTemplateTuple.getValueNode());
					}
				}
			}
		}
	}

	private void migrate23_steps(SequenceNode stepsNode) {
		for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
			MappingNode stepNode = (MappingNode) itStepNode.next();
			if (stepNode.getTag().getValue().equals("!BuildImageStep")) {
				stepNode.getValue().add(new NodeTuple(
						new ScalarNode(Tag.STR, "publish"), new ScalarNode(Tag.STR, "true")));
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate23(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps"))
							migrate23_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps"))
							migrate23_steps((SequenceNode)stepTemplateTuple.getValueNode());
					}
				}
			}
		}
	}

	private void migrate24_steps(SequenceNode stepsNode) {
		for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
			MappingNode stepNode = (MappingNode) itStepNode.next();
			if (stepNode.getTag().getValue().equals("!RunContainerStep")) {
				for (var itStepNodeTuple = stepNode.getValue().iterator(); itStepNodeTuple.hasNext();) {
					String stepNodeTupleKey = ((ScalarNode)itStepNodeTuple.next().getKeyNode()).getValue();
					if (stepNodeTupleKey.equals("opts"))
						itStepNodeTuple.remove();
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate24(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps"))
							migrate24_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps"))
							migrate24_steps((SequenceNode)stepTemplateTuple.getValueNode());
					}
				}
			}
		}
	}

	private void migrate25_steps(SequenceNode stepsNode) {
		for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
			MappingNode stepNode = (MappingNode) itStepNode.next();
			if (stepNode.getTag().getValue().equals("!BuildImageStep")) {
				stepNode.getValue().add(new NodeTuple(
						new ScalarNode(Tag.STR, "removeDanglingImages"), new ScalarNode(Tag.STR, "true")));
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate25(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps"))
							migrate25_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps"))
							migrate25_steps((SequenceNode)stepTemplateTuple.getValueNode());
					}
				}
			}
		}
	}

	private void migrate26_steps(SequenceNode stepsNode) {
		for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
			MappingNode stepNode = (MappingNode) itStepNode.next();
			for (NodeTuple stepTuple: stepNode.getValue()) {
				var stepTupleKeyNode = (ScalarNode)stepTuple.getKeyNode();
				if (stepTupleKeyNode.getValue().equals("params")) {
					stepTupleKeyNode.setValue("paramMatrix");
					for (Node paramNode: ((SequenceNode)stepTuple.getValueNode()).getValue()) {
						MappingNode paramMappingNode = (MappingNode) paramNode;
						for (NodeTuple paramTuple: paramMappingNode.getValue()) {
							if (((ScalarNode)paramTuple.getKeyNode()).getValue().equals("valuesProvider")) {
								MappingNode valuesProviderNode = (MappingNode) paramTuple.getValueNode();
								if (valuesProviderNode.getTag().getValue().equals("!Ignore"))
									valuesProviderNode.setTag(new Tag("!IgnoreValues"));
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void migrate26(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps")) {
							migrate26_steps((SequenceNode) jobTuple.getValueNode());
						} else if (jobTupleKey.equals("triggers") || jobTupleKey.equals("jobDependencies")
								|| jobTupleKey.equals("postBuildActions")) {
							SequenceNode propsNode = (SequenceNode) jobTuple.getValueNode();
							for (Node propNodeItem: propsNode.getValue()) {
								MappingNode propNode = (MappingNode) propNodeItem;
								for (NodeTuple propTuple: propNode.getValue()) {
									var propTupleKeyNode = (ScalarNode)propTuple.getKeyNode();
									if (propTupleKeyNode.getValue().equals("params")
											|| propTupleKeyNode.getValue().equals("jobParams")) {
										propTupleKeyNode.setValue("paramMatrix");
										for (Node paramNode: ((SequenceNode)propTuple.getValueNode()).getValue()) {
											MappingNode paramMappingNode = (MappingNode) paramNode;
											for (NodeTuple paramTuple: paramMappingNode.getValue()) {
												if (((ScalarNode)paramTuple.getKeyNode()).getValue().equals("valuesProvider")) {
													MappingNode valuesProviderNode = (MappingNode) paramTuple.getValueNode();
													if (valuesProviderNode.getTag().getValue().equals("!Ignore")) 
														valuesProviderNode.setTag(new Tag("!IgnoreValues"));
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps"))
							migrate26_steps((SequenceNode)stepTemplateTuple.getValueNode());
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate27(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode) specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem : jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (var itJobTuple = jobNode.getValue().iterator(); itJobTuple.hasNext();) {
						String jobTupleKey = ((ScalarNode) itJobTuple.next().getKeyNode()).getValue();
						if (jobTupleKey.equals("caches")) 
							itJobTuple.remove();
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate28(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode) specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem : jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (var jobTuple: jobNode.getValue()) {
						if (((ScalarNode)jobTuple.getKeyNode()).getValue().equals("postBuildActions")) {
							for (var postBuildActionNode: ((SequenceNode)jobTuple.getValueNode()).getValue()) {
								if (postBuildActionNode.getTag().getValue().equals("!CreateIssueAction")) {
									for (var actionTuple: ((MappingNode)postBuildActionNode).getValue()) {
										if ((((ScalarNode)actionTuple.getKeyNode()).getValue()).equals("issueFields")) {
											for (var fieldNode: ((SequenceNode)actionTuple.getValueNode()).getValue()) {
												for (var fieldTuple: ((MappingNode)fieldNode).getValue()) {
													if ((((ScalarNode)fieldTuple.getKeyNode()).getValue()).equals("valueProvider")) {
														if (fieldTuple.getValueNode().getTag().getValue().equals("!Ignore"))
															fieldTuple.getValueNode().setTag(new Tag("!IgnoreValue"));
													}
												}												
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void migrate29_commands(MappingNode node) {
		var commandsBuilder = new StringBuilder();
		for (var itTuple = node.getValue().iterator(); itTuple.hasNext();) {
			var tuple = itTuple.next();
			if (((ScalarNode)tuple.getKeyNode()).getValue().equals("commands")) {
				for (var elementNode: ((SequenceNode)tuple.getValueNode()).getValue()) 
					commandsBuilder.append(((ScalarNode)elementNode).getValue()).append("\n");
				itTuple.remove();
			}
		}
		node.getValue().add(new NodeTuple(
				new ScalarNode(Tag.STR, "commands"),
				new ScalarNode(Tag.STR, commandsBuilder.toString())));
	}
	
	private void migrate29_steps(SequenceNode stepsNode) {
		for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
			MappingNode stepNode = (MappingNode) itStepNode.next();
			if (stepNode.getTag().getValue().equals("!CommandStep")) {
				for (var stepTuple: stepNode.getValue()) {
					if (((ScalarNode) stepTuple.getKeyNode()).getValue().equals("interpreter")) 
						migrate29_commands((MappingNode) stepTuple.getValueNode());
				}
			} else if (stepNode.getTag().getValue().equals("!SSHCommandStep")) {
				migrate29_commands(stepNode);
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate29(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps"))
							migrate29_steps((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps"))
							migrate29_steps((SequenceNode)stepTemplateTuple.getValueNode());
					}
				}
			}
		}
	}

	private void migrateSteps(VersionedYamlDoc doc, Stack<Integer> versions,
							  Consumer<SequenceNode> stepMigrator) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("steps"))
							stepMigrator.accept((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("steps"))
							stepMigrator.accept((SequenceNode)stepTemplateTuple.getValueNode());
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate30(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrateSteps(doc, versions, stepsNode -> {
			for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
				MappingNode stepNode = (MappingNode) itStepNode.next();
				if (stepNode.getTag().getValue().equals("!SetupCacheStep")) {
					String path = null;
					for (var itStepTuple = stepNode.getValue().iterator(); itStepTuple.hasNext();) {
						var stepTuple = itStepTuple.next();
						if (((ScalarNode) stepTuple.getKeyNode()).getValue().equals("path")) {
							path = ((ScalarNode) stepTuple.getValueNode()).getValue();
							itStepTuple.remove();
							break;
						}
					}
					if (path != null) {
						List<Node> pathNodes = new ArrayList<>();
						pathNodes.add(new ScalarNode(Tag.STR, path));
						stepNode.getValue().add(new NodeTuple(
								new ScalarNode(Tag.STR, "paths"),
								new SequenceNode(Tag.SEQ, pathNodes, FlowStyle.BLOCK)));

					}
				}
			}
		});
	}

	@SuppressWarnings("unused")
	private void migrate31(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrateSteps(doc, versions, stepsNode -> {
			for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
				MappingNode stepNode = (MappingNode) itStepNode.next();
				var stepType = stepNode.getTag().getValue();
				if (stepType.equals("!PullRepository")) {
					boolean syncToChild = false;
					String childProject = null;
					for (var itStepTuple = stepNode.getValue().iterator(); itStepTuple.hasNext();) {
						var stepTuple = itStepTuple.next();
						var propName = ((ScalarNode) stepTuple.getKeyNode()).getValue();
						if (propName.equals("syncToChildProject")) {
							syncToChild = Boolean.parseBoolean(((ScalarNode) stepTuple.getValueNode()).getValue());
							itStepTuple.remove();
						} else if (propName.equals("childProject")) {
							childProject = ((ScalarNode) stepTuple.getValueNode()).getValue();
							itStepTuple.remove();
						}
					}
					if (syncToChild) {
						var targetProject = Project.get() + "/" + childProject;
						stepNode.getValue().add(new NodeTuple(
								new ScalarNode(Tag.STR, "targetProject"),
								new ScalarNode(Tag.STR, targetProject)));
					}
				} else if (stepType.equals("!PublishCPDReportStep") || stepType.equals("!PublishCheckstyleReportStep") 
						|| stepType.equals("!PublishESLintReportStep") || stepType.equals("!PublishPMDReportStep")
						|| stepType.equals("!PublishRoslynatorReportStep") || stepType.equals("!PublishSpotBugsReportStep")) {
					stepNode.getValue().add(new NodeTuple(
							new ScalarNode(Tag.STR, "failThreshold"),
							new ScalarNode(Tag.STR, "HIGH")));
				}
			}
		});
	}

	@SuppressWarnings("unused")
	private void migrate32(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrateSteps(doc, versions, stepsNode -> {
			for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
				MappingNode stepNode = (MappingNode) itStepNode.next();
				var stepType = stepNode.getTag().getValue();
				if (stepType.equals("!SetupCacheStep")) {
					stepNode.getValue().add(new NodeTuple(
							new ScalarNode(Tag.STR, "uploadStrategy"),
							new ScalarNode(Tag.STR, "UPLOAD_IF_NOT_HIT")));
				} else if (stepType.equals("!ScanDepVulnersStep")) {
					stepNode.setTag(new Tag("!OsvVulnerScannerStep"));					
				} else if (stepType.equals("!ScanLicenseViolationsStep")) {
					stepNode.setTag(new Tag("!OsvLicenseScannerStep"));
				} else if (stepType.equals("!BuildImageStep")) {
 					String tags = "";
					for (var itStepTuple = stepNode.getValue().iterator(); itStepTuple.hasNext();) {
						var stepTuple = itStepTuple.next();						
						var propName = ((ScalarNode) stepTuple.getKeyNode()).getValue();
						if (propName.equals("tags")) {
							tags = ((ScalarNode)stepTuple.getValueNode()).getValue();
							itStepTuple.remove();
						} else if (propName.equals("publish") || propName.equals("removeDanglingImages")) {
							itStepTuple.remove();							
						}
					}
					var outputNode = new MappingNode(new Tag("!RegistryOutput"), Lists.newArrayList(), FlowStyle.BLOCK);					
					outputNode.getValue().add(new NodeTuple(
							new ScalarNode(Tag.STR, "tags"), 
							new ScalarNode(Tag.STR, tags)));
					stepNode.getValue().add(new NodeTuple(
							new ScalarNode(Tag.STR, "output"),
							outputNode
					));
				} else if (stepType.equals("!BuildImageWithKanikoStep")) {
					String destinations = "";
					for (var itStepTuple = stepNode.getValue().iterator(); itStepTuple.hasNext();) {
						var stepTuple = itStepTuple.next();
						var propName = ((ScalarNode) stepTuple.getKeyNode()).getValue();
						if (propName.equals("destinations")) {
							destinations = ((ScalarNode)stepTuple.getValueNode()).getValue();
							itStepTuple.remove();
						}
					}
					var outputNode = new MappingNode(new Tag("!RegistryOutput"), Lists.newArrayList(), FlowStyle.BLOCK);
					outputNode.getValue().add(new NodeTuple(
							new ScalarNode(Tag.STR, "destinations"),
							new ScalarNode(Tag.STR, destinations)));
					stepNode.getValue().add(new NodeTuple(
							new ScalarNode(Tag.STR, "output"),
							outputNode
					));
				}
			}
		});
	}

	@SuppressWarnings("unused")
	private void migrate33(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrateSteps(doc, versions, stepsNode -> {
			for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
				MappingNode stepNode = (MappingNode) itStepNode.next();
				var stepType = stepNode.getTag().getValue();
				if (stepType.equals("!CommandStep")) {
					for (var stepTuple: stepNode.getValue()) {
						String key = ((ScalarNode)stepTuple.getKeyNode()).getValue();
						if (key.equals("interpreter")) {
							MappingNode interpreterNode = (MappingNode) stepTuple.getValueNode();
							if (interpreterNode.getTag().getValue().equals("!PowerShellInterpreter")) {
								interpreterNode.getValue().add(new NodeTuple(
										new ScalarNode(Tag.STR, "powershell"), new ScalarNode(Tag.STR, "powershell.exe")));
							}
						}
					}
				}
			}
		});
	}

	private void migrateParamSpecs(VersionedYamlDoc doc, Stack<Integer> versions,
							  Consumer<SequenceNode> paramSpecMigrator) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("paramSpecs"))
							paramSpecMigrator.accept((SequenceNode) jobTuple.getValueNode());
					}
				}
			} else if (specObjectKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stepTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stepTemplateTupleKey.equals("paramSpecs"))
							paramSpecMigrator.accept((SequenceNode)stepTemplateTuple.getValueNode());
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate34(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrateSteps(doc, versions, stepsNode -> {
			for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
				MappingNode stepNode = (MappingNode) itStepNode.next();
				var stepType = stepNode.getTag().getValue();
				if (stepType.equals("!CloseMilestoneStep")) 
					stepNode.setTag(new Tag("!CloseIterationStep"));
			}
		});
		migrateParamSpecs(doc, versions, paramSpecsNode -> {
			for (var itParamSpecNode = paramSpecsNode.getValue().iterator(); itParamSpecNode.hasNext();) {
				MappingNode paramSpecNode = (MappingNode) itParamSpecNode.next();
				var paramSpecType = paramSpecNode.getTag().getValue();
				if (paramSpecType.equals("!MilestoneChoiceParam")) 
					paramSpecNode.setTag(new Tag("!IterationChoiceParam"));
			}
		});
	}

	@SuppressWarnings("unused")
	private void migrate35(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrateSteps(doc, versions, stepsNode -> {
			for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
				MappingNode stepNode = (MappingNode) itStepNode.next();
				var stepType = stepNode.getTag().getValue();
				if (stepType.equals("!CloseIterationStep")) {
					Node milestoneNameValueNode = null;
					for (var itStepTuple = stepNode.getValue().iterator(); itStepTuple.hasNext();) {
						var stepTuple = itStepTuple.next();
						if (((ScalarNode) stepTuple.getKeyNode()).getValue().equals("milestoneName")) {
							milestoneNameValueNode = stepTuple.getValueNode();
							itStepTuple.remove();
							break;
						}
					}
					if (milestoneNameValueNode != null) {
						stepNode.getValue().add(
								new NodeTuple(new ScalarNode(Tag.STR, "iterationName"),
										milestoneNameValueNode));
					}
				}
			}
		});
	}

	@SuppressWarnings("unused")
	private void migrate36(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrateSteps(doc, versions, stepsNode -> {
			for (var itStepNode = stepsNode.getValue().iterator(); itStepNode.hasNext();) {
				MappingNode stepNode = (MappingNode) itStepNode.next();
				var stepType = stepNode.getTag().getValue();
				if (stepType.equals("!PublishCheckstyleReportStep")) {
					stepNode.getValue().add(new NodeTuple(
							new ScalarNode(Tag.STR, "tabWidth"),
							new ScalarNode(Tag.STR, "8")));
				}
			}
		});
	}

	private void migrate37_registryLogins(MappingNode node) {
		String accessTokenSecret = null;
		for (var itTuple = node.getValue().iterator(); itTuple.hasNext();) {
			var tuple = itTuple.next();
			var tupleKey = ((ScalarNode)tuple.getKeyNode()).getValue();
			if (tupleKey.equals("builtInRegistryAccessTokenSecret")) {
				accessTokenSecret = ((ScalarNode)tuple.getValueNode()).getValue();
				itTuple.remove();
				break;
			}
		}
		if (accessTokenSecret != null) {
			var loginProps = new ArrayList<NodeTuple>();
			loginProps.add(new NodeTuple(
					new ScalarNode(Tag.STR, "registryUrl"), 
					new ScalarNode(Tag.STR, "@server_url@")));
			loginProps.add(new NodeTuple(
					new ScalarNode(Tag.STR, "userName"),
					new ScalarNode(Tag.STR, "@job_token@")));
			loginProps.add(new NodeTuple(
					new ScalarNode(Tag.STR, "passwordSecret"),
					new ScalarNode(Tag.STR, accessTokenSecret)));
			var login = new MappingNode(Tag.MAP, loginProps, FlowStyle.BLOCK);
			var logins = new SequenceNode(Tag.SEQ, Lists.newArrayList(login), FlowStyle.BLOCK);
			node.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "registryLogins"), logins));
		}
	}

	@SuppressWarnings("unused")
	private void migrate37(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specObjectKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specObjectKey.equals("services")) {
				SequenceNode servicesNode = (SequenceNode) specTuple.getValueNode();
				for (Node servicesNodeItem: servicesNode.getValue()) 
					migrate37_registryLogins((MappingNode) servicesNodeItem);
			}
		}
		
		migrateSteps(doc, versions, stepsNode -> {
			for (var stepNode: stepsNode.getValue()) 
				migrate37_registryLogins((MappingNode)stepNode);
		});
	}

	@SuppressWarnings("unused")
	private void migrate38(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrateSteps(doc, versions, stepsNode -> {
			for (var stepNode: stepsNode.getValue()) {
				if (stepNode.getTag().getValue().equals("!RenovateStep")) {
					for (var itStepTuple = ((MappingNode)stepNode).getValue().iterator(); itStepTuple.hasNext();) {
						var stepTuple = itStepTuple.next();
						var keyNode = (ScalarNode) stepTuple.getKeyNode();
						if (keyNode.getValue().equals("createDependencyDashboardIssue") 
								|| keyNode.getValue().equals("dashboardIssueConfidential") 
								|| keyNode.getValue().equals("closeDashboardIssueWhenDone")) {
							itStepTuple.remove();
						} else if (keyNode.getValue().equals("dashboardIssueFields")) {
							keyNode.setValue("issueFields");
						} else if (keyNode.getValue().equals("dashboardIssueCloseStates")) {
							keyNode.setValue("issueCloseStates");
						}
					}
				}
			}
		});
	}

	@SuppressWarnings("unused")
	private void migrate39(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (NodeTuple jobTuple: jobNode.getValue()) {
						String jobTupleKey = ((ScalarNode)jobTuple.getKeyNode()).getValue();
						if (jobTupleKey.equals("paramSpecs")) {
							SequenceNode paramsNode = (SequenceNode) jobTuple.getValueNode();
							for (Node paramsNodeItem: paramsNode.getValue()) {
								MappingNode paramNode = (MappingNode) paramsNodeItem;
								String paramType = paramNode.getTag().getValue();
								if (paramType.equals("!WorkingPeriodParam")) {
									paramNode.setTag(new Tag("!IntegerParam"));
								}
							}
						}
					}
				}
			} else if (specKey.equals("stepTemplates")) {
				SequenceNode stepTemplatesNode = (SequenceNode) specTuple.getValueNode();
				for (Node stepTemplatesNodeItem: stepTemplatesNode.getValue()) {
					MappingNode stepTemplateNode = (MappingNode) stepTemplatesNodeItem;
					for (NodeTuple stepTemplateTuple: stepTemplateNode.getValue()) {
						String stemTemplateTupleKey = ((ScalarNode)stepTemplateTuple.getKeyNode()).getValue();
						if (stemTemplateTupleKey.equals("paramSpecs")) {
							SequenceNode paramsNode = (SequenceNode) stepTemplateTuple.getValueNode();
							for (Node paramsNodeItem: paramsNode.getValue()) {
								MappingNode paramNode = (MappingNode) paramsNodeItem;
								String paramType = paramNode.getTag().getValue();
								if (paramType.equals("!WorkingPeriodParam")) {
									paramNode.setTag(new Tag("!IntegerParam"));
								}
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void migrate40(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrateSteps(doc, versions, stepsNode -> {
			for (var stepNode: stepsNode.getValue()) {
				var stepMappingNode = (MappingNode) stepNode;
				for (var itStepTuple = stepMappingNode.getValue().iterator(); itStepTuple.hasNext();) {
					var stepTuple = itStepTuple.next();
					var keyNode = (ScalarNode) stepTuple.getKeyNode();
					if (keyNode.getValue().equals("condition")) {
						var valueNode = (ScalarNode) stepTuple.getValueNode();
						if (valueNode.getValue().equals("ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL")) 
							valueNode.setValue("SUCCESSFUL");
					}
				}
				stepMappingNode.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, "optional"), new ScalarNode(Tag.STR, "false")));
			}
		});
	}

	@SuppressWarnings("unused")
	private void migrate41(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					for (var itJobTuple = jobNode.getValue().iterator(); itJobTuple.hasNext();) {
						var jobTuple = itJobTuple.next();
						var keyNode = (ScalarNode) jobTuple.getKeyNode();
						if (keyNode.getValue().equals("retryCondition")) {
							var valueNode = (ScalarNode) jobTuple.getValueNode();
							if (valueNode.getValue().equals("never")) {
								itJobTuple.remove();
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate42(VersionedYamlDoc doc, Stack<Integer> versions) {
		migrate42_processNode(doc);
	}

	private void migrate42_processNode(Node node) {
		if (node instanceof MappingNode) {
			MappingNode mappingNode = (MappingNode) node;
			
			if (mappingNode.getTag() != null && !mappingNode.getTag().equals(Tag.MAP) 
					&& mappingNode.getTag().getValue().startsWith("!")) {
				var tagValue = mappingNode.getTag().getValue().substring(1); 
				boolean hasTypeTuple = false;
				for (NodeTuple tuple : mappingNode.getValue()) {
					if (tuple.getKeyNode() instanceof ScalarNode) {
						ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
						if ("type".equals(keyNode.getValue())) {
							hasTypeTuple = true;
							break;
						}
					}
				}
				
				if (!hasTypeTuple) {
					mappingNode.getValue().add(0, new NodeTuple(
							new ScalarNode(Tag.STR, "type"),
							new ScalarNode(Tag.STR, tagValue)));
				}
				mappingNode.setTag(Tag.MAP);	
			}
			
			for (NodeTuple tuple : mappingNode.getValue()) {
				migrate42_processNode(tuple.getKeyNode());
				migrate42_processNode(tuple.getValueNode());
			}
		} else if (node instanceof SequenceNode) {
			SequenceNode sequenceNode = (SequenceNode) node;
			for (Node item : sequenceNode.getValue()) {
				migrate42_processNode(item);
			}
		}
	}

	@SuppressWarnings("unused")
	private void migrate43(VersionedYamlDoc doc, Stack<Integer> versions) {
		for (NodeTuple specTuple: doc.getValue()) {
			String specKey = ((ScalarNode)specTuple.getKeyNode()).getValue();
			if (specKey.equals("jobs")) {
				SequenceNode jobsNode = (SequenceNode) specTuple.getValueNode();
				for (Node jobsNodeItem: jobsNode.getValue()) {
					MappingNode jobNode = (MappingNode) jobsNodeItem;
					boolean hasRetryCondition = false;
					for (var itJobTuple = jobNode.getValue().iterator(); itJobTuple.hasNext();) {
						var jobTuple = itJobTuple.next();
						var keyNode = (ScalarNode) jobTuple.getKeyNode();
						if (keyNode.getValue().equals("retryCondition")) {
							var valueNode = (ScalarNode) jobTuple.getValueNode();
							if (StringUtils.isBlank(valueNode.getValue())) {
								itJobTuple.remove();
							} else {
								hasRetryCondition = true;
							}
							break;
						} else if (keyNode.getValue().equals("triggers")) {
							SequenceNode triggersNode = (SequenceNode) jobTuple.getValueNode();
							for (Node triggerNode: triggersNode.getValue()) {
								MappingNode triggerMappingNode = (MappingNode) triggerNode;
								boolean hasUserMatch = false;
								String triggerType = null;
								for (var triggerTuple: triggerMappingNode.getValue()) {
									String triggerTupleKey = ((ScalarNode)triggerTuple.getKeyNode()).getValue();
									if (triggerTupleKey.equals("type")) {
										triggerType = ((ScalarNode)triggerTuple.getValueNode()).getValue();
										break;
									}
								}
								if ("BranchUpdateTrigger".equals(triggerType)) {
									for (var itTriggerTuple = triggerMappingNode.getValue().iterator(); itTriggerTuple.hasNext();) {
										var triggerTuple = itTriggerTuple.next();
										String triggerTupleKey = ((ScalarNode)triggerTuple.getKeyNode()).getValue();
										if (triggerTupleKey.equals("userMatch")) {
											var valueNode = (ScalarNode) triggerTuple.getValueNode();
											if (StringUtils.isBlank(valueNode.getValue())) {
												itTriggerTuple.remove();
											} else {
												hasUserMatch = true;
											}
											break;
										}
									}
									if (!hasUserMatch) {
										triggerMappingNode.getValue().add(new NodeTuple(
												new ScalarNode(Tag.STR, "userMatch"),
												new ScalarNode(Tag.STR, "anyone")));
									}
								}
							}
						}
					}					
					if (!hasRetryCondition) {
						jobNode.getValue().add(new NodeTuple(
								new ScalarNode(Tag.STR, "retryCondition"),
								new ScalarNode(Tag.STR, "never")));
					}
				}
			}
		}
	}

}
