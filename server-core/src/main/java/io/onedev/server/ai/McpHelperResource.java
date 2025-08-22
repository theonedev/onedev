package io.onedev.server.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.IssueLinkManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueWorkManager;
import io.onedev.server.entitymanager.IterationManager;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.exception.LinkValidationException;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.field.EmptyFieldsException;
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
import io.onedev.server.model.support.issue.transitionspec.ManualSpec;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.rest.InvalidParamsException;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectScope;

@Api(internal = true)
@Path("/mcp-helper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class McpHelperResource {

    private final ObjectMapper objectMapper;

    private final SettingManager settingManager;

    private final UserManager userManager;
    
    private final IssueManager issueManager;    
    
    private final ProjectManager projectManager;

    private final LinkSpecManager linkSpecManager;

    private final IssueLinkManager issueLinkManager;

    private final IssueCommentManager issueCommentManager;

    private final IterationManager iterationManager;

    private final IssueChangeManager issueChangeManager;

    private final IssueWorkManager issueWorkManager;

    private final SubscriptionManager subscriptionManager;

    private final PullRequestManager pullRequestManager;

    private final GitService gitService;

    private final LabelSpecManager labelSpecManager;

    @Inject
    public McpHelperResource(ObjectMapper objectMapper, SettingManager settingManager, 
            UserManager userManager, IssueManager issueManager, ProjectManager projectManager, 
            LinkSpecManager linkSpecManager, IssueCommentManager issueCommentManager, 
            IterationManager iterationManager, SubscriptionManager subscriptionManager, 
            IssueChangeManager issueChangeManager, IssueLinkManager issueLinkManager, 
            IssueWorkManager issueWorkManager, PullRequestManager pullRequestManager, 
            GitService gitService, LabelSpecManager labelSpecManager) {
        this.objectMapper = objectMapper;
        this.settingManager = settingManager;
        this.issueManager = issueManager;
        this.userManager = userManager;
        this.projectManager = projectManager;
        this.linkSpecManager = linkSpecManager;
        this.issueCommentManager = issueCommentManager;
        this.iterationManager = iterationManager;
        this.subscriptionManager = subscriptionManager;
        this.issueChangeManager = issueChangeManager;
        this.issueLinkManager = issueLinkManager;
        this.issueWorkManager = issueWorkManager;
        this.pullRequestManager = pullRequestManager;
        this.gitService = gitService;
        this.labelSpecManager = labelSpecManager;
    }

    private String getIssueQueryStringDescription() {
        var stateNames = new StringBuilder();
        for (var state: settingManager.getIssueSetting().getStateSpecs()) {
            stateNames.append("  - ");
            stateNames.append(state.getName());
            if (state.getDescription() != null) {
                stateNames.append(": ").append(state.getDescription().replace("\n", " "));
            }
            stateNames.append("\n");
        }
        var fieldCriterias = new StringBuilder();
        for (var field: settingManager.getIssueSetting().getFieldSpecs()) {
            if (field instanceof ChoiceField) {
                var choiceField = (ChoiceField) field;
                fieldCriterias.append("- " + field.getName().toLowerCase() + " criteria in form of: \""
                        + field.getName() + "\" is \"<" + field.getName().toLowerCase()
                        + " value>\" (quotes are required), where <" + field.getName().toLowerCase()
                        + " value> is one of below:\n");
                for (var choice : choiceField.getPossibleValues())
                    fieldCriterias.append("  - " + choice).append("\n");
            } else if (field instanceof UserChoiceField) {
                fieldCriterias.append("- " + field.getName().toLowerCase() + " criteria in form of: \""
                        + field.getName() + "\" is \"<login name of a user>\" (quotes are required)\n");
                fieldCriterias.append(
                        "- " + field.getName().toLowerCase() + " criteria for current user in form of: \""
                                + field.getName() + "\" is me (quotes are required)\n");
            } else if (field instanceof GroupChoiceField) {
                fieldCriterias.append("- " + field.getName().toLowerCase() + " criteria in form of: \""
                        + field.getName() + "\" is \"<group name>\" (quotes are required)\n");
            } else if (field instanceof BooleanField) {
                fieldCriterias.append("- " + field.getName().toLowerCase() + " is true criteria in form of: \""
                        + field.getName() + "\" is \"true\" (quotes are required)\n");
                fieldCriterias.append("- " + field.getName().toLowerCase() + " is false criteria in form of: \""
                        + field.getName() + "\" is \"false\" (quotes are required)\n");
            } else if (field instanceof DateField) {
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is before certain date criteria in form of: \"" + field.getName()
                        + "\" is before \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD\n");
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is after certain date criteria in form of: \"" + field.getName()
                        + "\" is after \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD\n");
            } else if (field instanceof DateTimeField) {
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is before certain date time criteria in form of: \"" + field.getName()
                        + "\" is before \"<date time>\" (quotes are required), where <date time> is of format YYYY-MM-DD HH:mm\n");
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is after certain date time criteria in form of: \"" + field.getName()
                        + "\" is after \"<date time>\" (quotes are required), where <date time> is of format YYYY-MM-DD HH:mm\n");
            } else if (field instanceof IntegerField) {
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is equal to certain integer criteria in form of: \"" + field.getName()
                        + "\" is \"<integer>\" (quotes are required), where <integer> is an integer\n");
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is greater than certain integer criteria in form of: \"" + field.getName()
                        + "\" is greater than \"<integer>\" (quotes are required), where <integer> is an integer\n");
                fieldCriterias.append("- " + field.getName().toLowerCase()
                        + " is less than certain integer criteria in form of: \"" + field.getName()
                        + "\" is less than \"<integer>\" (quotes are required), where <integer> is an integer\n");
            }
            fieldCriterias.append("- " + field.getName().toLowerCase() + " is not set criteria in form of: \""
                    + field.getName() + "\" is empty (quotes are required)\n");
        }
        var linkCriterias = new StringBuilder();
        for (var linkSpec: linkSpecManager.query()) {
            linkCriterias.append("- criteria to list issues with any " + linkSpec.getName().toLowerCase()
                    + " issues matching certain criteria in form of: any \"" + linkSpec.getName()
                    + "\" matching(another criteria) (quotes are required)\n");
            linkCriterias.append("- criteria to list issues with all " + linkSpec.getName().toLowerCase()
                    + " issues matching certain criteria in form of: all \"" + linkSpec.getName()
                    + "\" matching(another criteria) (quotes are required)\n");
            linkCriterias.append("- criteria to list issues with some " + linkSpec.getName().toLowerCase()
                    + " issues in form of: has any \"" + linkSpec.getName() + "\" (quotes are required)\n");
            if (linkSpec.getOpposite() != null) {
                linkCriterias.append("- criteria to list issues with any "
                        + linkSpec.getOpposite().getName().toLowerCase()
                        + " issues matching certain criteria in form of: any \"" + linkSpec.getOpposite().getName()
                        + "\" matching(another criteria) (quotes are required)\n");
                linkCriterias.append("- criteria to list issues with all "
                        + linkSpec.getOpposite().getName().toLowerCase()
                        + " issues matching certain criteria in form of: all \"" + linkSpec.getOpposite().getName()
                        + "\" matching(another criteria) (quotes are required)\n");
                linkCriterias.append("- criteria to list issues with some " + linkSpec.getOpposite().getName().toLowerCase()
                        + " issues in form of: has any \"" + linkSpec.getOpposite().getName() + "\" (quotes are required)\n");
            }
        }
        var orderFields = new StringBuilder();
        for (var field: Issue.SORT_FIELDS.keySet()) {
            orderFields.append("- ").append(field).append("\n");
        }

        return
                "A query string is one of below criteria:\n" +
                "- Issue with specified number in form of: \"Number\" is \"#<issue number>\", or in form of: \"Number\" is \"<project key>-<issue number>\" (quotes are required)\n" +
                "- Text based criteria in form of: ~<containing text>~\n" +
                "- State criteria in form of: \"State\" is \"<state name>\" (quotes are required), where <state name> is one of below:\n" +
                stateNames + 
                fieldCriterias + 
                linkCriterias + 
                "- submitter criteria in form of: \"Submitter\" is \"<login name of a user>\" (quotes are required)\n" +
                "- submitted by current user criteria in form of: submitted by me (quotes are required)\n" +
                "- submitted before certain date criteria in form of: \"Submit Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- submitted after certain date criteria in form of: \"Submit Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- updated before certain date criteria in form of: \"Last Activity Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- updated after certain date criteria in form of: \"Last Activity Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- confidential criteria in form of: confidential\n" +
                "- iteration criteria in form of: \"Iteration\" is \"<iteration name>\" (quotes are required)\n" +
                "- and criteria in form of <criteria1> and <criteria2>\n" + 
                "- or criteria in form of <criteria1> or <criteria2>. Note that \"and criteria\" takes precedence over \"or criteria\", use braces to group \"or criteria\" like \"(criteria1 or criteria2) and criteria3\" if you want to override precedence\n" +
                "- not criteria in form of not(<criteria>)\n" +
                "\n" +
                "And can optionally add order clause at end of query string in form of: order by \"<field1>\" <asc|desc>,\"<field2>\" <asc|desc>,... (quotes are required), where <field> is one of below:\n" +
                orderFields + 
                "\n" +
                "Leave empty to search all issues";
    }

    private String getToolParamName(String fieldName) {
        return fieldName.replace(" ", "_");
    }

    private String appendDescription(String description, String additionalDescription) {
        if (description.length() > 0) {
            if (description.endsWith("."))
                return description + " " + additionalDescription;
            else
                return description + ". " + additionalDescription;
        } else {
            return additionalDescription;
        }
    }

    private Project getProject(String projectPath) {
        var project = projectManager.findByPath(projectPath);
        if (project == null)
            throw new NotFoundException("Project not found: " + projectPath);
        if (!SecurityUtils.canAccessProject(project))
            throw new UnauthorizedException("Unable to access project: " + projectPath);
        return project;
    }

    private Map<String, Object> getFieldProperties(FieldSpec field) {
        String fieldDescription;
        if (field.getDescription() != null)
            fieldDescription = field.getDescription().replace("\n", " ");
        else
            fieldDescription = "";
        if (field instanceof ChoiceField) {
            var choiceField = (ChoiceField) field;
            if (field.isAllowMultiple())
                fieldDescription = appendDescription(fieldDescription,
                        "Expects one or more of: " + String.join(", ", choiceField.getPossibleValues()));
            else
                fieldDescription = appendDescription(fieldDescription,
                        "Expects one of: " + String.join(", ", choiceField.getPossibleValues()));
        } else if (field instanceof UserChoiceField) {
            if (field.isAllowMultiple())
                fieldDescription = appendDescription(fieldDescription, "Expects user login names");
            else
                fieldDescription = appendDescription(fieldDescription, "Expects user login name");
        } else if (field instanceof GroupChoiceField) {
        } else if (field instanceof BooleanField) {
            fieldDescription = appendDescription(fieldDescription, "Expects boolean value, true or false");
        } else if (field instanceof IntegerField) {
            fieldDescription = appendDescription(fieldDescription, "Expects integer value");
        } else if (field instanceof FloatField) {
            fieldDescription = appendDescription(fieldDescription, "Expects float value");
        } else if (field instanceof DateField || field instanceof DateTimeField) {
            if (field.isAllowMultiple())
                fieldDescription = appendDescription(fieldDescription,
                        "Expects unix timestamps in milliseconds since epoch");
            else
                fieldDescription = appendDescription(fieldDescription,
                        "Expects unix timestamp in milliseconds since epoch");
        }

        var fieldProperties = new HashMap<String, Object>();
        if (field.isAllowMultiple()) {
            fieldProperties.putAll(getArrayProperties(fieldDescription));
        } else {
            fieldProperties.put("type", "string");
            fieldProperties.put("description", fieldDescription);
        }
        return fieldProperties;
    }

    private Map<String, Object> getArrayProperties(String description) {
        return Map.of(
            "type", "array",
            "items", Map.of("type", "string"),
            "uniqueItems", true,
            "description", description);
    }

    @Path("/get-tool-input-schemas")
    @GET
    public Map<String, Object> getToolInputSchemas(@QueryParam("currentProject") @NotNull String currentProjectPath) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();
        var currentProject = getProject(currentProjectPath);

        Project.push(currentProject);
        try {
            var inputSchemas = new HashMap<String, Object>();

            var queryIssuesInputSchema = new HashMap<String, Object>();
            var queryIssuesProperties = new HashMap<String, Object>();

            queryIssuesProperties.put("query", Map.of(
                "type", "string",
                "description", getIssueQueryStringDescription()));
            queryIssuesProperties.put("offset", Map.of(
                "type", "integer",
                "description", "start position for the query (optional, defaults to 0)"));
            queryIssuesProperties.put("count", Map.of(
                "type", "integer",
                "description", "number of issues to return (optional, defaults to 25, max 100)"));

            queryIssuesInputSchema.put("Type", "object");
            queryIssuesInputSchema.put("Properties", queryIssuesProperties);
            queryIssuesInputSchema.put("Required", new ArrayList<>());

            inputSchemas.put("queryIssues", queryIssuesInputSchema);

            var createIssueInputSchema = new HashMap<String, Object>();
            var createIssueProperties = new HashMap<String, Object>();
            createIssueProperties.put("title", Map.of(
                "type", "string", 
                "description", "title of the issue"));
            createIssueProperties.put("description", Map.of(
                "type", "string", 
                "description", "description of the issue"));
            createIssueProperties.put("confidential", Map.of(
                "type", "boolean", 
                "description", "whether the issue is confidential"));

            if (SecurityUtils.canScheduleIssues(currentProject)) {                
                createIssueProperties.put("iterations", getArrayProperties("iteration names"));

                if (subscriptionManager.isSubscriptionActive() && currentProject.isTimeTracking()) {
                    createIssueProperties.put("ownEstimatedTime", Map.of(
                        "type", "integer",
                        "description", "Estimated time in hours for this issue only (not including linked issues)"));
                }
            }

            var createIssueRequiredProperties = new ArrayList<String>();
            createIssueRequiredProperties.add("title");

            for (var field: settingManager.getIssueSetting().getFieldSpecs()) {
                if (field.isApplicable(currentProject) 
                        && field.isPromptUponIssueOpen() 
                        && SecurityUtils.canEditIssueField(currentProject, field.getName())) {
                    var paramName = getToolParamName(field.getName());
                    var fieldProperties = getFieldProperties(field);
                    createIssueProperties.put(paramName, fieldProperties);

                    if (!field.isAllowEmpty() && field.getShowCondition() == null) {
                        createIssueRequiredProperties.add(paramName);
                    }
                }
            }
            
            createIssueInputSchema.put("Type", "object");
            createIssueInputSchema.put("Properties", createIssueProperties);
            createIssueInputSchema.put("Required", createIssueRequiredProperties);

            inputSchemas.put("createIssue", createIssueInputSchema);

            var editIssueInputSchema = new HashMap<String, Object>();
            var editIssueProperties = new HashMap<String, Object>();
            editIssueProperties.put("issueReference", Map.of(
                "type", "string",
                "description", "reference of the issue to update"));
            editIssueProperties.put("title", Map.of(
                    "type", "string",
                    "description", "title of the issue"));
            editIssueProperties.put("description", Map.of(
                    "type", "string",
                    "description", "description of the issue"));
            editIssueProperties.put("confidential", Map.of(
                    "type", "boolean",
                    "description", "whether the issue is confidential"));

            if (SecurityUtils.canScheduleIssues(currentProject)) {
                editIssueProperties.put("iterations", getArrayProperties("iterations to schedule the issue in"));

                if (subscriptionManager.isSubscriptionActive() && currentProject.isTimeTracking()) {
                    editIssueProperties.put("ownEstimatedTime", Map.of(
                            "type", "integer",
                            "description", "Estimated time in hours for this issue only (not including linked issues)"));
                }
            }

            for (var field : settingManager.getIssueSetting().getFieldSpecs()) {
                if (SecurityUtils.canEditIssueField(currentProject, field.getName())) {
                    var paramName = getToolParamName(field.getName());

                    var fieldProperties = getFieldProperties(field);
                    editIssueProperties.put(paramName, fieldProperties);
               }
            }

            editIssueInputSchema.put("Type", "object");
            editIssueInputSchema.put("Properties", editIssueProperties);
            editIssueInputSchema.put("Required", List.of("issueReference"));

            inputSchemas.put("editIssue", editIssueInputSchema);            

            var toStates = new HashSet<String>();
            for (var transition: settingManager.getIssueSetting().getTransitionSpecs()) {
                if (transition instanceof ManualSpec) {
                    if (transition.getToStates().isEmpty()) {
                        toStates.addAll(settingManager.getIssueSetting().getStateSpecMap().keySet());
                        break;
                    } else {
                        toStates.addAll(transition.getToStates());
                    }
                }
            }
            if (toStates.size() > 0) {
                var transitionInputSchema = new HashMap<String, Object>();
                transitionInputSchema.put("Type", "object");

                var transitIssueProperties = new HashMap<String, Object>();
                transitIssueProperties.put("issueReference", Map.of(
                    "type", "string",
                    "description", "reference of the issue to transit state"));
                transitIssueProperties.put("state", Map.of(
                        "type", "string",
                        "description", "new state of the issue after transition. Must be one of: " + String.join(", ", toStates)));
                transitIssueProperties.put("comment", Map.of(
                        "type", "string",
                        "description", "comment of the transition"));

                for (var field : settingManager.getIssueSetting().getFieldSpecs()) {
                    if (SecurityUtils.canEditIssueField(currentProject, field.getName())) {
                        var paramName = getToolParamName(field.getName());

                        var fieldProperties = getFieldProperties(field);
                        transitIssueProperties.put(paramName, fieldProperties);
                    }
                }                

                transitionInputSchema.put("Properties", transitIssueProperties);
                transitionInputSchema.put("Required", List.of("issueReference", "state"));

                inputSchemas.put("transitIssue", transitionInputSchema);
            }

            var linkSpecs = linkSpecManager.query();
            if (!linkSpecs.isEmpty()) {
                var linkInputSchema = new HashMap<String, Object>();
                linkInputSchema.put("Type", "object");

                var linkProperties = new HashMap<String, Object>();
                linkProperties.put("sourceIssueReference", Map.of(
                    "type", "string", 
                    "description", "Issue reference as source of the link"));
                linkProperties.put("targetIssueReference", Map.of(
                    "type", "string", 
                    "description", "Issue reference as target of the link"
                ));
                var linkNames = new ArrayList<String>();
                for (var linkSpec: linkSpecs) {
                    linkNames.add(linkSpec.getName());
                    if (linkSpec.getOpposite() != null)
                        linkNames.add(linkSpec.getOpposite().getName());
                }
                linkProperties.put("linkName", Map.of(
                    "type", "string", 
                    "description", "Name of the link. Must be one of: " + String.join(", ", linkNames)));
                linkInputSchema.put("Properties", linkProperties);
                linkInputSchema.put("Required", List.of("sourceIssueReference", "targetIssueReference", "linkName"));

                inputSchemas.put("linkIssues", linkInputSchema);
            }

            var createPullRequestInputSchema = new HashMap<String, Object>();
            var createPullRequestProperties = new HashMap<String, Object>();            
            createPullRequestProperties.put("title", Map.of(
                    "type", "string",
                    "description", "Title of the pull request"));
            createPullRequestProperties.put("description", Map.of(
                    "type", "string",
                    "description", "Description of the pull request"));
            createPullRequestProperties.put("baseBranch", Map.of(
                    "type", "string",
                    "description", "A branch in current project to be used as base branch of the pull request"));
            createPullRequestProperties.put("headBranch", Map.of(
                    "type", "string",
                    "description", "A branch in current project to be used as head branch of the pull request"));
            createPullRequestProperties.put("mergeStrategy", Map.of(
                    "type", "string",
                    "description", "Merge strategy of the pull request. Must be one of: " + 
                        Arrays.stream(MergeStrategy.values()).map(Enum::name).collect(Collectors.joining(", "))));
            createPullRequestProperties.put("reviewers", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string"),
                    "uniqueItems", true,
                    "description", "Reviewers of the pull request. Expects user login names"));
            createPullRequestProperties.put("assignees", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string"),
                    "uniqueItems", true,
                    "description", "Assignees of the pull request. Expects user login names"));

            createPullRequestInputSchema.put("Type", "object");
            createPullRequestInputSchema.put("Properties", createPullRequestProperties);
            createPullRequestInputSchema.put("Required", List.of("title", "baseBranch", "headBranch"));

            inputSchemas.put("createPullRequest", createPullRequestInputSchema);

            var editPullRequestInputSchema = new HashMap<String, Object>();
            var editPullRequestProperties = new HashMap<String, Object>();
            editPullRequestProperties.put("pullRequestReference", Map.of(
                    "type", "string",
                    "description", "Reference of the pull request to edit"));
            editPullRequestProperties.put("title", Map.of(
                    "type", "string",
                    "description", "Title of the pull request"));
            editPullRequestProperties.put("description", Map.of(
                    "type", "string",
                    "description", "Description of the pull request"));
            editPullRequestProperties.put("mergeStrategy", Map.of(
                    "type", "string",
                    "description", "Merge strategy of the pull request. Must be one of: " +
                            Arrays.stream(MergeStrategy.values()).map(Enum::name).collect(Collectors.joining(", "))));
            editPullRequestProperties.put("reviewers", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string"),
                    "uniqueItems", true,
                    "description", "Reviewers of the pull request. Expects user login names"));
            editPullRequestProperties.put("assignees", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string"),
                    "uniqueItems", true,
                    "description", "Assignees of the pull request. Expects user login names"));

            var labelSpecs = labelSpecManager.query();
            if (!labelSpecs.isEmpty()) {
                editPullRequestProperties.put("labels", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string"),
                    "uniqueItems", true,
                    "description", "Labels of the pull request. Must be one or more of: " + String.join(", ", labelSpecs.stream().map(LabelSpec::getName).collect(Collectors.toList()))));
            }

            editPullRequestInputSchema.put("Type", "object");
            editPullRequestInputSchema.put("Properties", editPullRequestProperties);
            editPullRequestInputSchema.put("Required", List.of("pullRequestReference"));

            inputSchemas.put("editPullRequest", editPullRequestInputSchema);

            return inputSchemas;
        } finally {
            Project.pop();
        }
    }

    @Path("/get-prompt-arguments")
    @GET
    public Map<String, Object> getPromptArguments(@QueryParam("currentProject") @NotNull String currentProjectPath) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        Project.push(currentProject);
        try {
            var arguments = new LinkedHashMap<String, Object>();
            var createIssueArguments = new ArrayList<Map<String, Object>>();
            createIssueArguments.add(Map.of(
                    "name", "title",
                    "description", "title of the issue",
                    "required", true));
            createIssueArguments.add(Map.of(
                    "name", "description",
                    "description", "description of the issue",
                    "required", false));
            createIssueArguments.add(Map.of(
                    "name", "confidential",
                    "description", "whether the issue is confidential",
                    "required", false));
            
            if (SecurityUtils.canScheduleIssues(currentProject)) {
                createIssueArguments.add(Map.of(
                        "name", "iterations",
                        "description", "iterations to schedule the issue in",
                        "required", false));

                if (subscriptionManager.isSubscriptionActive() && currentProject.isTimeTracking()) {
                    createIssueArguments.add(Map.of(
                        "name", "ownEstimatedTime",
                        "description", "estimated time for this issue only in hours (excluding linked issues)", 
                        "required", false));
                }
            }

            for (var field: settingManager.getIssueSetting().getFieldSpecs()) {
                if (field.isApplicable(currentProject) 
                        && field.isPromptUponIssueOpen()
                        && field.getShowCondition() == null
                        && SecurityUtils.canEditIssueField(currentProject, field.getName())) {
                    var paramName = getToolParamName(field.getName());
                    String description = "";
                    if (field.getDescription() != null)
                        description = field.getDescription().replace("\n", " ");
                    if (field instanceof ChoiceField) {
                        var choiceField = (ChoiceField) field;
                        if (field.isAllowMultiple())
                            description = appendDescription(description, "Expects one or more of: " + String.join(", ", choiceField.getPossibleValues()));
                        else
                            description = appendDescription(description, "Expects one of: " + String.join(", ", choiceField.getPossibleValues()));
                    }
                    var argumentMap = new HashMap<String, Object>();
                    argumentMap.put("name", paramName);
                    argumentMap.put("required", !field.isAllowEmpty());
                    if (description != null)
                        argumentMap.put("description", description);
                    createIssueArguments.add(argumentMap);
                }
            }

            arguments.put("createIssue", createIssueArguments);
            return arguments;
        } finally {
            Project.pop();
        } 
    }

    @Path("/get-login-name")
    @GET
    public String getLoginName(@QueryParam("userName") String userName) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        User user;                
        userName = StringUtils.trimToNull(userName);
        if (userName != null) {
            user = userManager.findByName(userName);
            if (user == null)
                user = userManager.findByFullName(userName);
            if (user == null) {
                var matchingUsers = new ArrayList<User>();
                var lowerCaseUserName = userName.toLowerCase();
                for (var eachUser: userManager.query()) {
                    if (eachUser.getFullName() != null) {
                        if (Splitter.on(" ").trimResults().omitEmptyStrings().splitToList(eachUser.getFullName().toLowerCase()).contains(lowerCaseUserName)) {
                            matchingUsers.add(eachUser);
                        }
                    }
                }
                if (matchingUsers.size() == 1) {
                    user = matchingUsers.get(0);
                } else if (matchingUsers.size() > 1) {
                    throw new InvalidParamsException("Multiple users found: " + userName);
                }
            }
            if (user == null) 
                throw new NotFoundException("User not found: " + userName);
        } else {
            user = SecurityUtils.getUser();
        }
        return user.getName();
    }

    @Path("/get-unix-timestamp")
    @GET
    public long getUnixTimestamp(@QueryParam("dateTimeDescription") @NotNull String dateTimeDescription) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        return DateUtils.parseRelaxed(dateTimeDescription).getTime();
    }

    @Path("/query-issues")
    @GET
    public List<Map<String, Object>> queryIssues(@QueryParam("currentProject") @NotNull String currentProjectPath, 
            @QueryParam("query") String query, @QueryParam("offset") int offset, @QueryParam("count") int count) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        if (count > RestConstants.MAX_PAGE_SIZE)
            throw new InvalidParamsException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

        var currentProject = getProject(currentProjectPath);

        EntityQuery<Issue> parsedQuery;
        if (query != null) {
            var option = new IssueQueryParseOption();
            option.withCurrentUserCriteria(true);
            parsedQuery = IssueQuery.parse(currentProject, query, option, true);
        } else {
            parsedQuery = new IssueQuery();
        }

        var issues = new ArrayList<Map<String, Object>>();
        for (var issue : issueManager.query(new ProjectScope(currentProject, true, false), parsedQuery, true, offset, count)) {
            var issueMap = getIssueMap(currentProject, issue);
            for (var entry: issue.getFieldInputs().entrySet()) {
                issueMap.put(entry.getKey(), entry.getValue().getValues());
            }
            issues.add(issueMap);
        }
        return issues;
    }

    private Issue getIssue(Project currentProject, String referenceString) {
        var issueReference = IssueReference.of(referenceString, currentProject);
        var issue = issueManager.find(issueReference.getProject(), issueReference.getNumber());
        if (issue != null) {
            if (!SecurityUtils.canAccessIssue(issue))
                throw new UnauthorizedException("No permission to access issue: " + referenceString);
            return issue;
        } else {
            throw new NotFoundException("Issue not found: " + referenceString);
        }
    }

    private Map<String, Object> getIssueMap(Project currentProject, Issue issue) {
        var typeReference = new TypeReference<Map<String, Object>>() {};
        var issueMap = objectMapper.convertValue(issue, typeReference);
        issueMap.put("reference", issue.getReference().toString(currentProject));
        issueMap.remove("submitterId");
        issueMap.put("Submitter", issue.getSubmitter().getName());
        issueMap.remove("projectId");
        issueMap.put("Project", issue.getProject().getPath());
        return issueMap;
    }
    
    @Path("/get-issue-detail")
    @GET
    public Map<String, Object> getIssueDetail(@QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var issue = getIssue(currentProject, issueReference);
                
        var issueMap = getIssueMap(currentProject, issue);
        for (var entry : issue.getFieldInputs().entrySet()) {
            issueMap.put(entry.getKey(), entry.getValue().getValues());
        }
        
        issueMap.put("comments", issue.getComments());

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
        issueMap.putAll(linkedIssues);

        return issueMap;
    }

    @Path("/add-issue-comment")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public String addIssueComment(@QueryParam("currentProject") String currentProjectPath, 
            @QueryParam("reference") @NotNull String issueReference, @NotNull String commentContent) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var issue = getIssue(currentProject, issueReference);
        var comment = new IssueComment();
        comment.setIssue(issue);
        comment.setContent(commentContent);
        comment.setUser(SecurityUtils.getAuthUser());
        comment.setDate(new Date());
        issueCommentManager.create(comment);

        return "Commented on issue " + issueReference;
    }

    @Path("/create-issue")
    @POST
    public String createIssue(@QueryParam("currentProject") @NotNull String currentProjectPath, 
                @NotNull @Valid Map<String, Serializable> data) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        normalizeIssueData(data);

        var issueSetting = settingManager.getIssueSetting();

        Issue issue = new Issue();
        var title = (String) data.remove("title");
        if (title == null)
            throw new InvalidParamsException("title is required");
        issue.setTitle(title);
        var description = (String) data.remove("description");
        issue.setDescription(description);
        var confidential = (Boolean) data.remove("confidential");
        if (confidential != null)
            issue.setConfidential(confidential);

        Integer ownEstimatedTime = (Integer) data.remove("ownEstimatedTime");
        if (ownEstimatedTime != null) {
            if (!subscriptionManager.isSubscriptionActive())
                throw new NotAcceptableException("An active subscription is required for this feature");
            if (!currentProject.isTimeTracking())
                throw new NotAcceptableException("Time tracking needs to be enabled for the project");
            if (!SecurityUtils.canScheduleIssues(currentProject))
                throw new UnauthorizedException("Issue schedule permission required to set own estimated time");
            issue.setOwnEstimatedTime(ownEstimatedTime*60);
        }

        @SuppressWarnings("unchecked")
        List<String> iterationNames = (List<String>) data.remove("iterations");
        if (iterationNames != null) {
            if (!SecurityUtils.canScheduleIssues(currentProject))
                throw new UnauthorizedException("Issue schedule permission required to set iterations");
            for (var iterationName : iterationNames) {
                var iteration = iterationManager.findInHierarchy(currentProject, iterationName);
                if (iteration == null)
                    throw new NotFoundException("Iteration '" + iterationName + "' not found");
                IssueSchedule schedule = new IssueSchedule();
                schedule.setIssue(issue);
                schedule.setIteration(iteration);
                issue.getSchedules().add(schedule);
            }
        }

        issue.setProject(currentProject);
        issue.setSubmitDate(new Date());
        issue.setSubmitter(SecurityUtils.getAuthUser());
        issue.setState(issueSetting.getInitialStateSpec().getName());

        issue.setFieldValues(FieldUtils.getFieldValues(issue.getProject(), data));

        try {
            issueManager.open(issue);
        } catch (EmptyFieldsException e) {
            throw new InvalidParamsException("Missing parameters: " + String.join(", ", e.getEmptyFields()));
        }

        return "Created issue " + issue.getReference().toString(currentProject);
    }

    @Path("/edit-issue")
    @POST
    public String editIssue(@QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, @NotNull Map<String, Serializable> data) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var issue = getIssue(currentProject, issueReference);
        if (!SecurityUtils.canModifyIssue(issue))
            throw new UnauthorizedException();

        normalizeIssueData(data);

        var title = (String) data.remove("title");
        if (title != null) 
            issueChangeManager.changeTitle(issue, title);

        if (data.containsKey("description")) 
            issueChangeManager.changeDescription(issue, (String) data.remove("description"));

        var confidential = (Boolean) data.remove("confidential");
        if (confidential != null)
            issueChangeManager.changeConfidential(issue, confidential);

        Integer ownEstimatedTime = (Integer) data.remove("ownEstimatedTime");
        if (ownEstimatedTime != null) {
            if (!subscriptionManager.isSubscriptionActive())
                throw new NotAcceptableException("An active subscription is required for this feature");
            if (!issue.getProject().isTimeTracking())
                throw new NotAcceptableException("Time tracking needs to be enabled for the project");
            if (!SecurityUtils.canScheduleIssues(issue.getProject()))
                throw new UnauthorizedException("Issue schedule permission required to set own estimated time");
            issueChangeManager.changeOwnEstimatedTime(issue, ownEstimatedTime*60);
        }

        @SuppressWarnings("unchecked")
        List<String> iterationNames = (List<String>) data.remove("iterations");
        if (iterationNames != null) {
            if (!SecurityUtils.canScheduleIssues(issue.getProject()))
                throw new UnauthorizedException("Issue schedule permission required to set iterations");
            var iterations = new ArrayList<Iteration>();
            for (var iterationName : iterationNames) {
                var iteration = iterationManager.findInHierarchy(issue.getProject(), iterationName);
                if (iteration == null)
                    throw new NotFoundException("Iteration '" + iterationName + "' not found");
                iterations.add(iteration);
            }
            issueChangeManager.changeIterations(issue, iterations);
        }

        if (!data.isEmpty()) {
            var issueSetting = settingManager.getIssueSetting();
            String initialState = issueSetting.getInitialStateSpec().getName();

            if (!SecurityUtils.canManageIssues(issue.getProject())
                    && !(issue.getSubmitter().equals(SecurityUtils.getAuthUser())
                            && issue.getState().equals(initialState))) {
                throw new UnauthorizedException("No permission to update issue fields");
            }

            try {
                issueChangeManager.changeFields(issue, FieldUtils.getFieldValues(issue.getProject(), data));
            } catch (EmptyFieldsException e) {
                throw new InvalidParamsException("Missing parameters: " + String.join(", ", e.getEmptyFields()));
            }
        }

        return "Updated issue " + issueReference;
    }

    @Path("/transit-issue")
    @POST
    public String transitIssue(@QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, @NotNull Map<String, Serializable> data) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var issue = getIssue(currentProject, issueReference);
        normalizeIssueData(data);
        var state = (String) data.remove("state");
        if (state == null)
            throw new InvalidParamsException("state is required");
        var comment = (String) data.remove("comment");
        ManualSpec transition = settingManager.getIssueSetting().getManualSpec(issue, state);

        var fieldValues = FieldUtils.getFieldValues(issue.getProject(), data);
        try {
            issueChangeManager.changeState(issue, state, fieldValues, transition.getPromptFields(),
                    transition.getRemoveFields(), comment);
        } catch (EmptyFieldsException e) {
            throw new InvalidParamsException("Missing parameters: " + String.join(", ", e.getEmptyFields()));
        }
        return "Issue " + issueReference + " transited to state \"" + state + "\"";
    }

    @Path("/link-issues")
    @GET
    public String linkIssues(@QueryParam("currentProject") @NotNull String currentProjectPath, 
            @QueryParam("sourceReference") @NotNull String sourceReference, 
            @QueryParam("linkName") @Nullable String linkName, 
            @QueryParam("targetReference") @NotNull String targetReference) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var sourceIssue = getIssue(currentProject, sourceReference);
        var targetIssue = getIssue(currentProject, targetReference);
        var linkSpec = linkSpecManager.find(linkName);
        if (linkSpec == null)
            throw new NotFoundException("Link spec not found: " + linkName);
        if (!SecurityUtils.canEditIssueLink(sourceIssue.getProject(), linkSpec) 
                || !SecurityUtils.canEditIssueLink(targetIssue.getProject(), linkSpec)) {
            throw new UnauthorizedException("No permission to add specified link for specified issues");
        }
        
        var link = new IssueLink();
        link.setSpec(linkSpec);
        if (linkName.equals(linkSpec.getName())) {
            link.setSource(sourceIssue);
            link.setTarget(targetIssue);
        } else {
            link.setSource(targetIssue);
            link.setTarget(sourceIssue);
        }
        try {
            link.validate();
        } catch (LinkValidationException e) {
            throw new NotAcceptableException(e.getMessage());
        }
        issueLinkManager.create(link);

        return "Issue " + targetReference + " added as \"" + linkName + "\" of " + sourceReference;
    }

    @Path("/log-work")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public String logWork(@QueryParam("currentProject") @NotNull String currentProjectPath, 
            @QueryParam("reference") @NotNull String issueReference, 
            @QueryParam("spentHours") int spentHours, String comment) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();
        var currentProject = getProject(currentProjectPath);
        var issue = getIssue(currentProject, issueReference);

        if (!subscriptionManager.isSubscriptionActive())
            throw new NotAcceptableException("An active subscription is required for this feature");
        if (!issue.getProject().isTimeTracking())
            throw new NotAcceptableException("Time tracking needs to be enabled for the project");
        if (!SecurityUtils.canAccessIssue(issue))
            throw new UnauthorizedException("No permission to access issue: " + issueReference);

        var work = new IssueWork();
        work.setIssue(issue);
        work.setUser(SecurityUtils.getAuthUser());
        work.setMinutes(spentHours * 60);
        work.setNote(StringUtils.trimToNull(comment));
        issueWorkManager.createOrUpdate(work);
        return "Work logged for issue " + issueReference;
    }

    private void normalizeIssueData(Map<String, Serializable> data) {
        for (var entry: data.entrySet()) {
            if (entry.getValue() instanceof String) 
                entry.setValue(StringUtils.trimToNull((String) entry.getValue()));
        }
        for (var field: settingManager.getIssueSetting().getFieldSpecs()) {
            var paramName = getToolParamName(field.getName());
            if (!paramName.equals(field.getName()) && data.containsKey(paramName)) {
                data.put(field.getName(), data.get(paramName));
                data.remove(paramName);
            }
        }        
    }    

    @Path("/create-pull-request")
    @POST
    public String createPullRequest(@QueryParam("currentProject") @NotNull String currentProjectPath,
                @NotNull Map<String, Serializable> data) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();
        var currentProject = getProject(currentProjectPath);        
        if (!SecurityUtils.canReadCode(currentProject))
            throw new UnauthorizedException("No permission to read code of project: " + currentProjectPath);

        normalizePullRequestData(data);

        var baseBranch = (String) data.remove("baseBranch");
        var headBranch = (String) data.remove("headBranch");

        var target = new ProjectAndBranch(currentProject, baseBranch);
        var source = new ProjectAndBranch(currentProject, headBranch);

        if (target.equals(source))
            throw new InvalidParamsException("Base and head branches are the same");

        PullRequest request = pullRequestManager.findOpen(target, source);
        if (request != null)
            throw new InvalidParamsException("Another pull request already opened for this change");

        request = pullRequestManager.findEffective(target, source);
        if (request != null) {
            if (request.isOpen())
                throw new InvalidParamsException("Another pull request already opened for this change");
            else
                throw new InvalidParamsException("Change already merged");
        }

        request = new PullRequest();
        ObjectId baseCommitId = gitService.getMergeBase(
                target.getProject(), target.getObjectId(),
                source.getProject(), source.getObjectId());

        if (baseCommitId == null)
            throw new InvalidParamsException("No common base for base and head branches");

        request.setTitle((String) data.remove("title"));
        request.setTarget(target);
        request.setSource(source);
        request.setSubmitter(SecurityUtils.getAuthUser());
        request.setBaseCommitHash(baseCommitId.name());
        request.setDescription((String) data.remove("description"));

        var mergeStrategyName = (String) data.remove("mergeStrategy");
        if (mergeStrategyName != null) 
            request.setMergeStrategy(MergeStrategy.valueOf(mergeStrategyName));
        else
            request.setMergeStrategy(request.getProject().findDefaultPullRequestMergeStrategy());

        if (request.getBaseCommitHash().equals(source.getObjectName()))
            throw new InvalidParamsException("Change already merged");

        PullRequestUpdate update = new PullRequestUpdate();
        
        update.setDate(Date.from(request.getSubmitDate().toInstant().plusSeconds(1)));
        update.setRequest(request);
        update.setHeadCommitHash(source.getObjectName());
        update.setTargetHeadCommitHash(request.getTarget().getObjectName());
        request.getUpdates().add(update);

        pullRequestManager.checkReviews(request, false);

        @SuppressWarnings("unchecked")
        var reviewerNames = (List<String>) data.remove("reviewers");
        if (reviewerNames != null) {
            for (var reviewerName : reviewerNames) {
                User reviewer = userManager.findByName(reviewerName);
                if (reviewer == null)
                    throw new NotFoundException("Reviewer not found: " + reviewerName);
                if (reviewer.equals(request.getSubmitter()))
                    throw new InvalidParamsException("Pull request submitter can not be reviewer");

                if (request.getReview(reviewer) == null) {
                    PullRequestReview review = new PullRequestReview();
                    review.setRequest(request);
                    review.setUser(reviewer);
                    request.getReviews().add(review);
                }
            }
        }

        @SuppressWarnings("unchecked")
        var assigneeNames = (List<String>) data.remove("assignees");
        if (assigneeNames != null) {
            for (var assigneeName : assigneeNames) {
                User assignee = userManager.findByName(assigneeName);
                if (assignee == null)
                    throw new NotFoundException("Assignee not found: " + assigneeName);
                PullRequestAssignment assignment = new PullRequestAssignment();
                assignment.setRequest(request);
                assignment.setUser(assignee);
                request.getAssignments().add(assignment);
            }
        } else {
            for (var assignee : target.getProject().findDefaultPullRequestAssignees()) {
                PullRequestAssignment assignment = new PullRequestAssignment();
                assignment.setRequest(request);
                assignment.setUser(assignee);
                request.getAssignments().add(assignment);
            }
        }

        pullRequestManager.open(request);

        return "Created pull request " + request.getReference().toString(currentProject);        
    }
    
    private void normalizePullRequestData(Map<String, Serializable> data) {
        for (var entry : data.entrySet()) {
            if (entry.getValue() instanceof String)
                entry.setValue(StringUtils.trimToNull((String) entry.getValue()));
        }
    }

}