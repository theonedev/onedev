package io.onedev.server.migration;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.serializer.Serializer;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.StringUtils;

public class XmlBuildSpecMigrator {

	private static Node migrateParamSpec(Element paramSpecElement) {
		String classTag = getClassTag(paramSpecElement.getName());
		List<NodeTuple> tuples = new ArrayList<>();
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "name"), 
				new ScalarNode(Tag.STR, paramSpecElement.elementText("name").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "allowEmpty"), 
				new ScalarNode(Tag.STR, paramSpecElement.elementText("allowEmpty").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "allowMultiple"), 
				new ScalarNode(Tag.STR, paramSpecElement.elementText("allowMultiple").trim())));
		Element patternElement = paramSpecElement.element("pattern");
		if (patternElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "pattern"), 
					new ScalarNode(Tag.STR, patternElement.getText().trim())));
		}
		Element descriptionElement = paramSpecElement.element("description");
		if (descriptionElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "description"), 
					new ScalarNode(Tag.STR, descriptionElement.getText().trim())));
		}
		Element minValueElement = paramSpecElement.element("minValue");
		if (minValueElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "minValue"), 
					new ScalarNode(Tag.STR, minValueElement.getText().trim())));
		}
		Element maxValueElement = paramSpecElement.element("maxValue");
		if (maxValueElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "maxValue"), 
					new ScalarNode(Tag.STR, maxValueElement.getText().trim())));
		}
		Element showConditionElement = paramSpecElement.element("showCondition");
		if (showConditionElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "showCondition"), 
					migrateShowCondition(showConditionElement)));
		}
		Element defaultValueProviderElement = paramSpecElement.element("defaultValueProvider");
		if (defaultValueProviderElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "defaultValueProvider"), 
					migrateDefaultValueProvider(defaultValueProviderElement)));
		}
		Element defaultMultiValueProviderElement = paramSpecElement.element("defaultMultiValueProvider");
		if (defaultMultiValueProviderElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "defaultMultiValueProvider"), 
					migrateDefaultMultiValueProvider(defaultMultiValueProviderElement)));
		}
		Element choiceProviderElement = paramSpecElement.element("choiceProvider");
		if (choiceProviderElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "choiceProvider"), 
					migrateChoiceProvider(choiceProviderElement)));
		}
		return new MappingNode(new Tag(classTag), tuples, FlowStyle.BLOCK);
	}
	
	private static String getClassTag(String className) {
		return "!" + StringUtils.substringAfterLast(className, ".");
	}
	
	private static Node migrateChoiceProvider(Element choiceProviderElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		String classTag = getClassTag(choiceProviderElement.attributeValue("class"));

		Element scriptNameElement = choiceProviderElement.element("scriptName");
		if (scriptNameElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "scriptName"), 
					new ScalarNode(Tag.STR, scriptNameElement.getText().trim())));
		}
		Element choicesElement = choiceProviderElement.element("choices");
		if (choicesElement != null) {
			List<Node> choiceNodes = new ArrayList<>();
			for (Element choiceElement: choicesElement.elements()) {
				List<NodeTuple> choiceTuples = new ArrayList<>();
				choiceTuples.add(new NodeTuple(
						new ScalarNode(Tag.STR, "value"), 
						new ScalarNode(Tag.STR, choiceElement.elementText("value").trim())));
				choiceTuples.add(new NodeTuple(
						new ScalarNode(Tag.STR, "color"), 
						new ScalarNode(Tag.STR, choiceElement.elementText("color").trim())));
				choiceNodes.add(new MappingNode(Tag.MAP, choiceTuples, FlowStyle.BLOCK));
			}
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "choices"), 
					new SequenceNode(Tag.SEQ, choiceNodes, FlowStyle.BLOCK)));
		}
		
		return new MappingNode(new Tag(classTag), tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateDefaultMultiValueProvider(Element defaultMultiValueProviderElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		String classTag = getClassTag(defaultMultiValueProviderElement.attributeValue("class"));
		Element scriptNameElement = defaultMultiValueProviderElement.element("scriptName");
		if (scriptNameElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "scriptName"), 
					new ScalarNode(Tag.STR, scriptNameElement.getText().trim())));
		}
		Element valueElement = defaultMultiValueProviderElement.element("value");
		if (valueElement != null) {
			List<Node> valueItemNodes = new ArrayList<>();
			for (Element valueItemElement: valueElement.elements())
				valueItemNodes.add(new ScalarNode(Tag.STR, valueItemElement.getText().trim()));
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "value"), 
					new SequenceNode(Tag.SEQ, valueItemNodes, FlowStyle.BLOCK)));
		}
		
		return new MappingNode(new Tag(classTag), tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateDefaultValueProvider(Element defaultValueProviderElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		String classTag = getClassTag(defaultValueProviderElement.attributeValue("class"));
		Element scriptNameElement = defaultValueProviderElement.element("scriptName");
		if (scriptNameElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "scriptName"), 
					new ScalarNode(Tag.STR, scriptNameElement.getText().trim())));
		}
		Element valueElement = defaultValueProviderElement.element("value");
		if (valueElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "value"), 
					new ScalarNode(Tag.STR, valueElement.getText().trim())));
		}
		
		return new MappingNode(new Tag(classTag), tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateShowCondition(Element showConditionElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "inputName"), 
				new ScalarNode(Tag.STR, showConditionElement.elementText("inputName").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "valueMatcher"), 
				migrateValueMatcher(showConditionElement.element("valueMatcher"))));
		return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateValueMatcher(Element valueMatcherElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		String classTag = getClassTag(valueMatcherElement.attributeValue("class"));
		Element valuesElement = valueMatcherElement.element("values");
		if (valuesElement != null) {
			List<Node> valueNodes = new ArrayList<>();
			for (Element valueElement: valuesElement.elements()) 
				valueNodes.add(new ScalarNode(Tag.STR, valueElement.getText().trim()));
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "values"), 
					new SequenceNode(Tag.SEQ, valueNodes, FlowStyle.BLOCK)));
		}
		
		return new MappingNode(new Tag(classTag), tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateJob(Element jobElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "name"), 
				new ScalarNode(Tag.STR, jobElement.elementText("name").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "image"), 
				new ScalarNode(Tag.STR, jobElement.elementText("image").trim())));
		List<Node> commandNodes = new ArrayList<>();
		for (Element commandElement: jobElement.element("commands").elements()) 
			commandNodes.add(new ScalarNode(Tag.STR, commandElement.getText().trim()));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "commands"), 
				new SequenceNode(Tag.SEQ, commandNodes, FlowStyle.BLOCK)));
		
		List<Node> paramSpecNodes = new ArrayList<>();
		for (Element paramSpecElement: jobElement.element("paramSpecs").elements()) 
			paramSpecNodes.add(migrateParamSpec(paramSpecElement));

		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "paramSpecs"), 
				new SequenceNode(Tag.SEQ, paramSpecNodes, FlowStyle.BLOCK)));
		
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "retrieveSource"),
				new ScalarNode(Tag.STR, jobElement.elementText("retrieveSource").trim())));
		Element cloneDepthElement = jobElement.element("cloneDepth");
		if (cloneDepthElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "cloneDepth"),
					new ScalarNode(Tag.STR, cloneDepthElement.getText().trim())));
		}
		
		List<Node> submoduleCredentialNodes = new ArrayList<>();
		for (Element submoduleCredentialElement: jobElement.element("submoduleCredentials").elements()) {
			List<NodeTuple> submoduleCredentialTuples = new ArrayList<>();
			submoduleCredentialTuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "url"), 
					new ScalarNode(Tag.STR, submoduleCredentialElement.elementText("url").trim())));
			submoduleCredentialTuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "userName"), 
					new ScalarNode(Tag.STR, submoduleCredentialElement.elementText("userName").trim())));
			submoduleCredentialTuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "passwordSecret"), 
					new ScalarNode(Tag.STR, submoduleCredentialElement.elementText("passwordSecret").trim())));
			submoduleCredentialNodes.add(new MappingNode(Tag.MAP, submoduleCredentialTuples, FlowStyle.BLOCK));
		}
		if (!submoduleCredentialNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "submoduleCredentials"), 
					new SequenceNode(Tag.SEQ, submoduleCredentialNodes, FlowStyle.BLOCK)));
		}
		
		List<Node> jobDependencyNodes = new ArrayList<>();
		for (Element jobDependencyElement: jobElement.element("jobDependencies").elements()) 
			jobDependencyNodes.add(migrateJobDependency(jobDependencyElement));
		if (!jobDependencyNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "jobDependencies"), 
					new SequenceNode(Tag.SEQ, jobDependencyNodes, FlowStyle.BLOCK)));
		}
		
		List<Node> projectDependencyNodes = new ArrayList<>();
		for (Element projectDependencyElement: jobElement.element("projectDependencies").elements()) 
			projectDependencyNodes.add(migrateProjectDependency(projectDependencyElement));
		if (!projectDependencyNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "projectDependencies"), 
					new SequenceNode(Tag.SEQ, projectDependencyNodes, FlowStyle.BLOCK)));
		}
		
		List<Node> serviceNodes = new ArrayList<>();
		for (Element serviceElement: jobElement.element("services").elements()) 
			serviceNodes.add(migrateService(serviceElement));
		if (!serviceNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "services"), 
					new SequenceNode(Tag.SEQ, serviceNodes, FlowStyle.BLOCK)));
		}
		
		Element artifactsElement = jobElement.element("artifacts");
		if (artifactsElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "artifacts"), 
					new ScalarNode(Tag.STR, artifactsElement.getText().trim())));
		}
		
		List<Node> reportNodes = new ArrayList<>();
		for (Element reportElement: jobElement.element("reports").elements()) 
			reportNodes.add(migrateReport(reportElement));
		if (!reportNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "reports"), 
					new SequenceNode(Tag.SEQ, reportNodes, FlowStyle.BLOCK)));
		}
		
		List<Node> triggerNodes = new ArrayList<>();
		for (Element triggerElement: jobElement.element("triggers").elements()) 
			triggerNodes.add(migrateTrigger(triggerElement));
		if (!triggerNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "triggers"), 
					new SequenceNode(Tag.SEQ, triggerNodes, FlowStyle.BLOCK)));
		}
		
		List<Node> cacheNodes = new ArrayList<>();
		for (Element cacheElement: jobElement.element("caches").elements()) 
			cacheNodes.add(migrateCache(cacheElement));
		if (!cacheNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "caches"), 
					new SequenceNode(Tag.SEQ, cacheNodes, FlowStyle.BLOCK)));
		}
		
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "cpuRequirement"), 
				new ScalarNode(Tag.STR, jobElement.elementText("cpuRequirement").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "memoryRequirement"), 
				new ScalarNode(Tag.STR, jobElement.elementText("memoryRequirement").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "timeout"), 
				new ScalarNode(Tag.STR, jobElement.elementText("timeout").trim())));
		
		List<Node> postBuildActionNodes = new ArrayList<>();
		for (Element postBuildActionElement: jobElement.element("postBuildActions").elements())
			postBuildActionNodes.add(migratePostBuildAction(postBuildActionElement));
		if (!postBuildActionNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "postBuildActions"), 
					new SequenceNode(Tag.SEQ, postBuildActionNodes, FlowStyle.BLOCK)));
		}
		
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "retryCondition"), 
				new ScalarNode(Tag.STR, jobElement.elementText("retryCondition").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "maxRetries"), 
				new ScalarNode(Tag.STR, jobElement.elementText("maxRetries").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "retryDelay"), 
				new ScalarNode(Tag.STR, jobElement.elementText("retryDelay").trim())));
		
		Element defaultFixedIssuesFilterElement = jobElement.element("defaultFixedIssuesFilter");
		if (defaultFixedIssuesFilterElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "defaultFixedIssuesFilter"), 
					new ScalarNode(Tag.STR, defaultFixedIssuesFilterElement.getText().trim())));
		}
		
		MappingNode jobNode = new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
		return jobNode;
	}
	
	private static Node migratePostBuildAction(Element postBuildActionElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		String classTag = getClassTag(postBuildActionElement.getName());
		
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "condition"), 
				new ScalarNode(Tag.STR, postBuildActionElement.elementText("condition").trim())));
		
		Element milestoneNameElement = postBuildActionElement.element("milestoneName");
		if (milestoneNameElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "milestoneName"), 
					new ScalarNode(Tag.STR, milestoneNameElement.getText().trim())));
		}
		
		Element issueTitleElement = postBuildActionElement.element("issueTitle");
		if (issueTitleElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "issueTitle"), 
					new ScalarNode(Tag.STR, issueTitleElement.getText().trim())));
		}
		
		Element issueDescriptionElement = postBuildActionElement.element("issueDescription");
		if (issueDescriptionElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "issueDescription"), 
					new ScalarNode(Tag.STR, issueDescriptionElement.getText().trim())));
		}
		
		Element issueFieldsElement = postBuildActionElement.element("issueFields");
		if (issueFieldsElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "issueFields"), 
					new SequenceNode(Tag.SEQ, migrateFieldSupplies(issueFieldsElement.elements()), FlowStyle.BLOCK)));
		}
		
		Element tagNameElement = postBuildActionElement.element("tagName");
		if (tagNameElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "tagName"), 
					new ScalarNode(Tag.STR, tagNameElement.getText().trim())));
		}
		
		Element tagMessageElement = postBuildActionElement.element("tagMessage");
		if (tagMessageElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "tagMessage"), 
					new ScalarNode(Tag.STR, tagMessageElement.getText().trim())));
		}
		
		Element jobNameElement = postBuildActionElement.element("jobName");
		if (jobNameElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "jobName"), 
					new ScalarNode(Tag.STR, jobNameElement.getText().trim())));
		}
		
		Element jobParamsElement = postBuildActionElement.element("jobParams");
		if (jobParamsElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "jobParams"), 
					new SequenceNode(Tag.SEQ, migrateParamSupplies(jobParamsElement.elements()), FlowStyle.BLOCK)));
		}
		
		Element receiversElement = postBuildActionElement.element("receivers");
		if (receiversElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "receivers"), 
					new ScalarNode(Tag.STR, receiversElement.getText().trim())));
		}
		
		return new MappingNode(new Tag(classTag), tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateCache(Element cacheElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "key"), 
				new ScalarNode(Tag.STR, cacheElement.elementText("key").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "path"), 
				new ScalarNode(Tag.STR, cacheElement.elementText("path").trim())));
		return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateTrigger(Element triggerElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		String classTag = getClassTag(triggerElement.getName());

		List<Node> paramSupplyNodes = migrateParamSupplies(triggerElement.element("params").elements());
		if (!paramSupplyNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "params"), 
					new SequenceNode(Tag.SEQ, paramSupplyNodes, FlowStyle.BLOCK)));
		}
		
		Element branchesElement = triggerElement.element("branches");
		if (branchesElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "branches"), 
					new ScalarNode(Tag.STR, branchesElement.getText().trim())));
		}
		
		Element pathsElement = triggerElement.element("paths");
		if (pathsElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "paths"), 
					new ScalarNode(Tag.STR, pathsElement.getText().trim())));
		}
		
		Element tagsElement = triggerElement.element("tags");
		if (tagsElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "tags"), 
					new ScalarNode(Tag.STR, tagsElement.getText().trim())));
		}
		
		return new MappingNode(new Tag(classTag), tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateReport(Element reportElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		String classTag = getClassTag(reportElement.getName());
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "filePatterns"), 
				new ScalarNode(Tag.STR, reportElement.elementText("filePatterns").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "reportName"), 
				new ScalarNode(Tag.STR, reportElement.elementText("reportName").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "startPage"), 
				new ScalarNode(Tag.STR, reportElement.elementText("startPage").trim())));
		return new MappingNode(new Tag(classTag), tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateService(Element serviceElement) {
		List<NodeTuple> tuples = new ArrayList<>();

		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "name"), 
				new ScalarNode(Tag.STR, serviceElement.elementText("name").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "image"), 
				new ScalarNode(Tag.STR, serviceElement.elementText("image").trim())));
		Element argumentsElement = serviceElement.element("arguments");
		if (argumentsElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "arguments"), 
					new ScalarNode(Tag.STR, argumentsElement.getText().trim())));
		}
		List<Node> envVarNodes = new ArrayList<>();
		for (Element envVarElement: serviceElement.element("envVars").elements()) {
			List<NodeTuple> envVarTuples = new ArrayList<>();
			envVarTuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "name"),
					new ScalarNode(Tag.STR, envVarElement.elementText("name").trim())));
			envVarTuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "value"),
					new ScalarNode(Tag.STR, envVarElement.elementText("value").trim())));
			envVarNodes.add(new MappingNode(Tag.MAP, envVarTuples, FlowStyle.BLOCK));
		}
		if (!envVarNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "envVars"), 
					new SequenceNode(Tag.SEQ, envVarNodes, FlowStyle.BLOCK)));
		}
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "readinessCheckCommand"), 
				new ScalarNode(Tag.STR, serviceElement.elementText("readinessCheckCommand").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "cpuRequirement"), 
				new ScalarNode(Tag.STR, serviceElement.elementText("cpuRequirement").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "memoryRequirement"), 
				new ScalarNode(Tag.STR, serviceElement.elementText("memoryRequirement").trim())));
		
		return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
	}
	
	private static Node migrateProjectDependency(Element projectDependencyElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "projectName"), 
				new ScalarNode(Tag.STR, projectDependencyElement.elementText("projectName").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "buildNumber"), 
				new ScalarNode(Tag.STR, projectDependencyElement.elementText("buildNumber").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "artifacts"), 
				new ScalarNode(Tag.STR, projectDependencyElement.elementText("artifacts").trim())));

		Element authenticationElement = projectDependencyElement.element("authentication");
		if (authenticationElement != null) {
			List<NodeTuple> authenticationTuples = new ArrayList<>();
			authenticationTuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "userName"), 
					new ScalarNode(Tag.STR, authenticationElement.elementText("userName").trim())));
			authenticationTuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "passwordSecret"), 
					new ScalarNode(Tag.STR, authenticationElement.elementText("passwordSecret").trim())));
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "authentication"), 
					new MappingNode(Tag.MAP, authenticationTuples, FlowStyle.BLOCK)));
		}
		return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
	}	
	
	private static Node migrateJobDependency(Element jobDependencyElement) {
		List<NodeTuple> tuples = new ArrayList<>();
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "jobName"), 
				new ScalarNode(Tag.STR, jobDependencyElement.elementText("jobName").trim())));
		tuples.add(new NodeTuple(
				new ScalarNode(Tag.STR, "requireSuccessful"), 
				new ScalarNode(Tag.STR, jobDependencyElement.elementText("requireSuccessful").trim())));
		Element artifactsElement = jobDependencyElement.element("artifacts");
		if (artifactsElement != null) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "artifacts"), 
					new ScalarNode(Tag.STR, artifactsElement.getText().trim())));
		}
		
		List<Node> paramSupplyNodes = migrateParamSupplies(jobDependencyElement.element("jobParams").elements());
		if (!paramSupplyNodes.isEmpty()) {
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "jobParams"), 
					new SequenceNode(Tag.SEQ, paramSupplyNodes, FlowStyle.BLOCK)));
		}
		return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
	}
	
	private static List<Node> migrateParamSupplies(List<Element> paramSupplyElements) {
		List<Node> paramSupplyNodes = new ArrayList<>();
		for (Element paramSupplyElement: paramSupplyElements) {
			List<NodeTuple> tuples = new ArrayList<>();
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "name"), 
					new ScalarNode(Tag.STR, paramSupplyElement.elementText("name").trim())));
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "secret"), 
					new ScalarNode(Tag.STR, paramSupplyElement.elementText("secret").trim())));
			
			Element valuesProviderElement = paramSupplyElement.element("valuesProvider");
			String classTag = getClassTag(valuesProviderElement.attributeValue("class"));
			List<NodeTuple> valuesProviderTuples = new ArrayList<>();
			Element scriptNameElement = valuesProviderElement.element("scriptName");
			if (scriptNameElement != null) {
				valuesProviderTuples.add(new NodeTuple(
						new ScalarNode(Tag.STR, "scriptName"), 
						new ScalarNode(Tag.STR, scriptNameElement.getText().trim())));
			}
			Element valuesElement = valuesProviderElement.element("values");
			if (valuesElement != null) {
				List<Node> listNodes = new ArrayList<>();
				for (Element listElement: valuesElement.elements()) {
					List<Node> listItemNodes = new ArrayList<>();
					for (Element listItemElement: listElement.elements()) 
						listItemNodes.add(new ScalarNode(Tag.STR, listItemElement.getText().trim()));
					listNodes.add(new SequenceNode(Tag.SEQ, listItemNodes, FlowStyle.BLOCK));
				}
				valuesProviderTuples.add(new NodeTuple(
						new ScalarNode(Tag.STR, "values"), 
						new SequenceNode(Tag.SEQ, listNodes, FlowStyle.BLOCK)));
			}
			
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "valuesProvider"), 
					new MappingNode(new Tag(classTag), valuesProviderTuples, FlowStyle.BLOCK)));
			paramSupplyNodes.add(new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK));
		}
		return paramSupplyNodes;
	}
	
	private static List<Node> migrateFieldSupplies(List<Element> fieldSupplyElements) {
		List<Node> fieldSupplyNodes = new ArrayList<>();
		for (Element fieldSupplyElement: fieldSupplyElements) {
			List<NodeTuple> tuples = new ArrayList<>();
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "name"), 
					new ScalarNode(Tag.STR, fieldSupplyElement.elementText("name").trim())));
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "secret"), 
					new ScalarNode(Tag.STR, fieldSupplyElement.elementText("secret").trim())));
			
			Element valueProviderElement = fieldSupplyElement.element("valueProvider");
			String classTag = getClassTag(valueProviderElement.attributeValue("class"));
			List<NodeTuple> valueProviderTuples = new ArrayList<>();
			Element scriptNameElement = valueProviderElement.element("scriptName");
			if (scriptNameElement != null) {
				valueProviderTuples.add(new NodeTuple(
						new ScalarNode(Tag.STR, "scriptName"), 
						new ScalarNode(Tag.STR, scriptNameElement.getText().trim())));
			}
			Element valueElement = valueProviderElement.element("value");
			if (valueElement != null) {
				List<Node> valueItemNodes = new ArrayList<>();
				for (Element valueItemElement: valueElement.elements()) 
					valueItemNodes.add(new ScalarNode(Tag.STR, valueItemElement.getText().trim()));
				valueProviderTuples.add(new NodeTuple(
						new ScalarNode(Tag.STR, "value"), 
						new SequenceNode(Tag.SEQ, valueItemNodes, FlowStyle.BLOCK)));
			}
			
			tuples.add(new NodeTuple(
					new ScalarNode(Tag.STR, "valueProvider"), 
					new MappingNode(new Tag(classTag), valueProviderTuples, FlowStyle.BLOCK)));
			fieldSupplyNodes.add(new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK));
		}
		return fieldSupplyNodes;
	}
	
	public static String migrate(String xml) {
		Document xmlDoc;
		try {
			SAXReader reader = new SAXReader();
			// Prevent XXE attack as the xml might be provided by malicious users
			reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			xmlDoc = reader.read(new StringReader(xml));
		} catch (DocumentException | SAXException e) {
			throw new RuntimeException(e);
		}
		
		List<NodeTuple> tuples = new ArrayList<>();
		Node keyNode = new ScalarNode(Tag.STR, "version");
		Node valueNode = new ScalarNode(Tag.INT, "0");
		tuples.add(new NodeTuple(keyNode, valueNode));
		
		List<Node> jobNodes = new ArrayList<>();
		for (Element jobElement: xmlDoc.getRootElement().element("jobs").elements()) 
			jobNodes.add(migrateJob(jobElement));
		
		if (!jobNodes.isEmpty()) {
			keyNode = new ScalarNode(Tag.STR, "jobs");
			tuples.add(new NodeTuple(keyNode, new SequenceNode(Tag.SEQ, jobNodes, FlowStyle.BLOCK)));
		}
		
		List<Node> propertyNodes = new ArrayList<>();
		Element propertiesElement = xmlDoc.getRootElement().element("properties");
		if (propertiesElement != null) {
			for (Element propertyElement: propertiesElement.elements()) {
				Node nameNode = new ScalarNode(Tag.STR, propertyElement.elementText("name").trim());
				valueNode = new ScalarNode(Tag.STR, propertyElement.elementText("value").trim());
				List<NodeTuple> propertyTuples = Lists.newArrayList(
						new NodeTuple(new ScalarNode(Tag.STR, "name"), nameNode), 
						new NodeTuple(new ScalarNode(Tag.STR, "value"), valueNode));
				propertyNodes.add(new MappingNode(Tag.MAP, propertyTuples, FlowStyle.BLOCK));
			}
		}
		if(!propertyNodes.isEmpty()) {
			keyNode = new ScalarNode(Tag.STR, "properties");
			tuples.add(new NodeTuple(keyNode, new SequenceNode(Tag.SEQ, propertyNodes, FlowStyle.BLOCK)));
		}
		
		MappingNode rootNode = new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
		StringWriter writer = new StringWriter();
		DumperOptions dumperOptions = new DumperOptions();
		Serializer serializer = new Serializer(new Emitter(writer, dumperOptions), 
				new Resolver(), dumperOptions, Tag.MAP);
		try {
			serializer.open();
			serializer.serialize(rootNode);
			serializer.close();
			return writer.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
}
