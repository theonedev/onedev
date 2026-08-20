package io.onedev.server.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.field.spec.BooleanField;
import io.onedev.server.model.support.issue.field.spec.DateField;
import io.onedev.server.model.support.issue.field.spec.DateTimeField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.FloatField;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.IntegerField;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IterationService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UrlService;

public class IssueHelper {
    
    private static ObjectMapper getObjectMapper() {
        return OneDev.getInstance(ObjectMapper.class);
    }

    private static UrlService getUrlService() {
        return OneDev.getInstance(UrlService.class);
    }

    public static Map<String, Object> getSummary(Project currentProject, Issue issue) {        
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var summary = getObjectMapper().convertValue(issue, typeReference);
        summary.remove("id");
        summary.remove("stateOrdinal");
        summary.remove("uuid");
        summary.remove("messageId");
        summary.remove("pinDate");
        summary.remove("boardPosition");
        summary.remove("numberScopeId");
        summary.remove("totalEstimatedTime");
        summary.remove("totalSpentTime");
        summary.remove("ownEstimatedTime");
        summary.remove("ownSpentTime");
        summary.remove("progress");
        summary.put("reference", issue.getReference().toString(currentProject));
        summary.remove("submitterId");
        summary.put("submitter", issue.getSubmitter().getName());
        summary.put("Project", issue.getProject().getPath());
        summary.remove("lastActivity");
        summary.put("lastActivityDate", issue.getLastActivity().getDate());
        for (var it = summary.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (entry.getKey().endsWith("Count"))
                it.remove();
        }
        return summary;
    }

    public static List<Map<String, Object>> getComments(Issue issue) {
        var comments = new ArrayList<Map<String, Object>>();
        issue.getComments().stream().sorted(Comparator.comparing(IssueComment::getId)).forEach(comment -> {
            var commentMap = new HashMap<String, Object>();
            commentMap.put("user", comment.getUser().getName());
            commentMap.put("onBehalfOf", comment.getOnBehalfOf());
            commentMap.put("date", comment.getDate());
            commentMap.put("content", comment.getContent());            
            comments.add(commentMap);
        });
        return comments;
    }

    public static Map<String, Object> getDetail(Project currentProject, Issue issue) {
        var data = getSummary(currentProject, issue);

        data.put("branch", issue.getBranch());
        var fieldBuild = issue.getFieldBuild();
        if (fieldBuild != null) {
            if (fieldBuild.getRequest() != null) {
                if (fieldBuild.getRequest().getSourceHead() != null) {
                    data.put("defaultPullRequestTargetProject", fieldBuild.getRequest().getSourceProject().getPath());
                    data.put("defaultPullRequestTargetBranch", fieldBuild.getRequest().getSourceBranch());
                }
            } else {
                var branch = GitUtils.ref2branch(fieldBuild.getRefName());
                if (branch != null) {
                    data.put("defaultPullRequestTargetProject", issue.getProject().getPath());
                    data.put("defaultPullRequestTargetBranch", branch);
                }
            }
        } else {
            data.put("defaultPullRequestTargetProject", issue.getProject().getPath());
            data.put("defaultPullRequestTargetBranch", issue.getProject().getDefaultBranch());
        }

        for (var entry : issue.getFieldInputs().entrySet()) {
            data.put(entry.getKey(), entry.getValue().getValues());
        }
        
        Map<String, Collection<String>> linkedIssues = new HashMap<>();
        for (var link: issue.getTargetLinks()) {
            linkedIssues.computeIfAbsent(link.getSpec().getName(), k -> new ArrayList<>())
                    .add(link.getTarget().getReference().toString(currentProject));
        }
        for (var link : issue.getSourceLinks()) {
            if (link.getSpec().getOpposite() != null) {
                linkedIssues.computeIfAbsent(link.getSpec().getOpposite().getName(), k -> new ArrayList<>())
                        .add(link.getSource().getReference().toString(currentProject));
            } else {
                linkedIssues.computeIfAbsent(link.getSpec().getName(), k -> new ArrayList<>())
                        .add(link.getSource().getReference().toString(currentProject));
            }
        }
        data.putAll(linkedIssues);
        data.put("link", getUrlService().urlFor(issue, true));

        return data;
    }

    public static String getParamName(String fieldName) {
        return fieldName.replace(' ', '-');
    }

    public static String appendDescription(String description, String additional) {
        if (description.isEmpty())
            return additional;
        if (description.endsWith("."))
            return description + " " + additional;
        return description + ". " + additional;
    }

    public static Map<String, Object> getFieldProperties(FieldSpec field) {
        var description = field.getDescription() != null ? field.getDescription().replace('\n', ' ') : "";
        if (field instanceof ChoiceField) {
            var choiceField = (ChoiceField) field;
            if (field.isAllowMultiple())
                description = appendDescription(description, "Expects one or more of: " + String.join(", ", choiceField.getPossibleValues()));
            else
                description = appendDescription(description, "Expects one of: " + String.join(", ", choiceField.getPossibleValues()));
        } else if (field instanceof UserChoiceField) {
            description = appendDescription(description, field.isAllowMultiple() ? "Expects user login names" : "Expects user login name");
        } else if (field instanceof GroupChoiceField) {
            description = appendDescription(description, field.isAllowMultiple() ? "Expects group names" : "Expects group name");
        } else if (field instanceof BooleanField) {
            description = appendDescription(description, "Expects boolean value, true or false");
        } else if (field instanceof IntegerField) {
            description = appendDescription(description, "Expects integer value");
        } else if (field instanceof FloatField) {
            description = appendDescription(description, "Expects float value");
        } else if (field instanceof DateField || field instanceof DateTimeField) {
            description = appendDescription(description, field.isAllowMultiple()
                    ? "Expects unix timestamps in milliseconds since epoch"
                    : "Expects unix timestamp in milliseconds since epoch");
        }

        var fieldProperties = new HashMap<String, Object>();
        if (field.isAllowMultiple()) {
            fieldProperties.put("type", "array");
            fieldProperties.put("items", Map.of("type", "string"));
            fieldProperties.put("uniqueItems", true);
            fieldProperties.put("description", description);
        } else {
            fieldProperties.put("type", "string");
            fieldProperties.put("description", description);
        }
        return fieldProperties;
    }

    public static Map<String, Object> getValidFields() {
        var issueFields = new HashMap<String, Object>();
        for (var field : OneDev.getInstance(SettingService.class).getIssueSetting().getFieldSpecs())
            issueFields.put(getParamName(field.getName()), getFieldProperties(field));
        return issueFields;
    }

    public static JsonObjectSchema getFieldsSchema() {
        var builder = JsonObjectSchema.builder()
                .description("Issue custom fields as a map of field name to value. "
                        + "Must be a JSON object, not an array");
        for (Entry<String, Object> entry : getValidFields().entrySet()) {
            @SuppressWarnings("unchecked")
            var fieldProps = (Map<String, Object>) entry.getValue();
            var description = (String) fieldProps.get("description");
            if ("array".equals(fieldProps.get("type"))) {
                builder.addProperty(entry.getKey(), JsonArraySchema.builder()
                        .description(description)
                        .items(new JsonStringSchema())
                        .build());
            } else {
                builder.addStringProperty(entry.getKey()).description(description);
            }
        }
        return builder.build();
    }

    public static void setFieldValues(Map<String, Serializable> data, JsonNode fieldsNode) {
        if (fieldsNode == null || !fieldsNode.isObject())
            return;
        var fields = fieldsNode.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            setFieldValue(data, entry.getKey(), entry.getValue());
        }
    }

    private static void setFieldValue(Map<String, Serializable> data, String fieldName, JsonNode value) {
        if (value == null || value.isNull())
            return;
        if (value.isArray()) {
            var values = new ArrayList<String>();
            for (var element : value)
                values.add(element.asText());
            data.put(fieldName, values);
        } else if (value.isTextual()) {
            var trimmed = StringUtils.trimToNull(value.asText());
            if (trimmed != null)
                data.put(fieldName, trimmed);
        } else {
            data.put(fieldName, value.asText());
        }
    }

    public static void normalizeData(Map<String, Serializable> data) {
        for (var entry : data.entrySet()) {
            if (entry.getValue() instanceof String)
                entry.setValue(StringUtils.trimToNull((String) entry.getValue()));
        }
        for (var field : OneDev.getInstance(SettingService.class).getIssueSetting().getFieldSpecs()) {
            var paramName = getParamName(field.getName());
            if (!paramName.equals(field.getName()) && data.containsKey(paramName)) {
                data.put(field.getName(), data.get(paramName));
                data.remove(paramName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Issue createIssue(Subject subject, Project project, Map<String, Serializable> data) {
        normalizeData(data);

        var issueSetting = OneDev.getInstance(SettingService.class).getIssueSetting();

        Issue issue = new Issue();
        var title = (String) data.remove("title");
        if (title == null)
            throw new ExplicitException("Title is required");
        issue.setTitle(title);
        var description = (String) data.remove("description");
        issue.setDescription(description);
        var confidential = (Boolean) data.remove("confidential");
        if (confidential != null)
            issue.setConfidential(confidential);

        Integer ownEstimatedTime = (Integer) data.remove("ownEstimatedTime");
        if (ownEstimatedTime != null) {
            var subscriptionService = OneDev.getInstance(SubscriptionService.class);
            if (!subscriptionService.isSubscriptionActive())
                throw new ExplicitException("An active subscription is required for this feature");
            if (!project.isTimeTracking())
                throw new ExplicitException("Time tracking needs to be enabled for the project");
            if (!SecurityUtils.canScheduleIssues(subject, project))
                throw new UnauthorizedException("Issue schedule permission required to set own estimated time");
            issue.setOwnEstimatedTime(ownEstimatedTime * 60);
        }

        List<String> iterationNames = (List<String>) data.remove("iterations");
        if (iterationNames != null) {
            if (!SecurityUtils.canScheduleIssues(subject, project))
                throw new UnauthorizedException("Issue schedule permission required to set iterations");
            var iterationService = OneDev.getInstance(IterationService.class);
            for (var iterationName : iterationNames) {
                var iteration = iterationService.findInHierarchy(project, iterationName);
                if (iteration == null)
                    throw new ExplicitException("Iteration '" + iterationName + "' not found");
                IssueSchedule schedule = new IssueSchedule();
                schedule.setIssue(issue);
                schedule.setIteration(iteration);
                issue.getSchedules().add(schedule);
            }
        }

        issue.setProject(project);
        issue.setSubmitDate(new Date());
        issue.setSubmitter(SecurityUtils.getUser(subject));
        issue.setState(issueSetting.getInitialStateSpec().getName());

        issue.setFieldValues(FieldUtils.getFieldValues(subject, project, data));

        OneDev.getInstance(IssueService.class).open(issue);
        return issue;
    }

}
