package io.onedev.server.ai;

import java.io.Serializable;
import java.text.MessageFormat;
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
import javax.persistence.EntityNotFoundException;
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
import org.unbescape.html.HtmlEscape;

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
import io.onedev.server.entitymanager.PullRequestAssignmentManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.entitymanager.PullRequestLabelManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.entityreference.PullRequestReference;
import io.onedev.server.exception.InvalidIssueFieldsException;
import io.onedev.server.exception.InvalidReferenceException;
import io.onedev.server.exception.IssueLinkValidationException;
import io.onedev.server.exception.PullRequestReviewRejectedException;
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
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
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
import io.onedev.server.model.support.pullrequest.AutoMerge;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.UrlManager;

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

    private final PullRequestChangeManager pullRequestChangeManager;

    private final PullRequestAssignmentManager pullRequestAssignmentManager;

    private final PullRequestReviewManager pullRequestReviewManager;

    private final PullRequestLabelManager pullRequestLabelManager;

    private final PullRequestCommentManager pullRequestCommentManager;

    private final GitService gitService;

    private final LabelSpecManager labelSpecManager;

    private final UrlManager urlManager;

    @Inject
    public McpHelperResource(ObjectMapper objectMapper, SettingManager settingManager, 
            UserManager userManager, IssueManager issueManager, ProjectManager projectManager, 
            LinkSpecManager linkSpecManager, IssueCommentManager issueCommentManager, 
            IterationManager iterationManager, SubscriptionManager subscriptionManager, 
            IssueChangeManager issueChangeManager, IssueLinkManager issueLinkManager, 
            IssueWorkManager issueWorkManager, PullRequestManager pullRequestManager, 
            PullRequestChangeManager pullRequestChangeManager, GitService gitService, 
            LabelSpecManager labelSpecManager, PullRequestReviewManager pullRequestReviewManager, 
            PullRequestAssignmentManager pullRequestAssignmentManager, 
            PullRequestLabelManager pullRequestLabelManager, UrlManager urlManager,
            PullRequestCommentManager pullRequestCommentManager) {
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
        this.pullRequestChangeManager = pullRequestChangeManager;
        this.pullRequestAssignmentManager = pullRequestAssignmentManager;
        this.pullRequestReviewManager = pullRequestReviewManager;
        this.gitService = gitService;
        this.labelSpecManager = labelSpecManager;
        this.pullRequestLabelManager = pullRequestLabelManager;
        this.urlManager = urlManager;
        this.pullRequestCommentManager = pullRequestCommentManager;
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

        var description = 
                "A query string is one of below criteria:\n" +
                "- issue with specified number in form of: \"Number\" is \"#<issue number>\", or in form of: \"Number\" is \"<project key>-<issue number>\" (quotes are required)\n" +
                "- criteria to check if title/description/comment contains specified text in form of: ~<containing text>~\n" +
                "- state criteria in form of: \"State\" is \"<state name>\" (quotes are required), where <state name> is one of below:\n" +
                stateNames + 
                fieldCriterias + 
                linkCriterias + 
                "- submitted by specified user criteria in form of: submitted by \"<login name of a user>\" (quotes are required)\n" +
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
                "Leave empty to list all accessible issues";

        return HtmlEscape.escapeHtml5(description);
    }

    private String getPullRequestQueryStringDescription() {
        var orderFields = new StringBuilder();
        for (var field : PullRequest.SORT_FIELDS.keySet()) {
            orderFields.append("- ").append(field).append("\n");
        }

        var labelNames = labelSpecManager.query().stream().map(LabelSpec::getName).collect(Collectors.joining(", "));
        var mergeStrategyNames = Arrays.stream(MergeStrategy.values()).map(MergeStrategy::name).collect(Collectors.joining(", "));

        var description = 
                "A query string is one of below criteria:\n" +
                "- pull request with specified number in form of: \"Number\" is \"#<pull request number>\", or in form of: \"Number\" is \"<project key>-<pull request number>\" (quotes are required)\n" +
                "- criteria to check if title/description/comment contains specified text in form of: ~<containing text>~\n" +
                "- open criteria in form of: open\n" +
                "- merged criteria in form of: merged\n" +
                "- discarded criteria in form of: discarded\n" +
                "- source branch criteria in form of: \"Source Branch\" is \"<branch name>\" (quotes are required)\n" +
                "- target branch criteria in form of: \"Target Branch\" is \"<branch name>\" (quotes are required)\n" +
                "- merge strategy criteria in form of: \"Merge Strategy\" is \"<merge strategy>\" (quotes are required), where <merge strategy> is one of: " + mergeStrategyNames + "\n" +
                "- label criteria in form of: \"Label\" is \"<label name>\" (quotes are required), where <label name> is one of: " + labelNames + "\n" +
                "- ready to merge criteria in form of: ready to merge\n" +
                "- waiting for someone to review criteria in form of: has pending reviews\n" +
                "- some builds are unsuccessful criteria in form of: has unsuccessful builds\n" +
                "- some builds are not finished criteria in form of: has unfinished builds\n" +
                "- has merge conflicts criteria in form of: has merge conflicts\n" +
                "- assigned to specified user criteria in form of: assigned to \"<login name of a user>\" (quotes are required)\n" +
                "- approved by specified user criteria in form of: approved by \"<login name of a user>\" (quotes are required)\n" +
                "- to be reviewed by specified user criteria in form of: to be reviewed by \"<login name of a user>\" (quotes are required)\n" +
                "- to be changed by specified user criteria in form of: to be changed by \"<login name of a user>\" (quotes are required)\n" +
                "- to be merged by specified user criteria in form of: to be merged by \"<login name of a user>\" (quotes are required)\n" +
                "- requested for changes by specified user in form of: requested for changes by \"<login name of a user>\" (quotes are required)\n" +
                "- need action of specified user criteria in form of: need action by \"<login name of a user>\" (quotes are required)\n" +
                "- assigned to current user criteria in form of: assigned to me\n" +
                "- approved by current user criteria in form of: approved by me\n" +
                "- to be reviewed by current user criteria in form of: to be reviewed by me\n" +
                "- to be changed by current user criteria in form of: to be changed by me\n" +
                "- to be merged by current user criteria in form of: to be merged by me\n" +
                "- requested for changes by current user in form of: requested for changes by me\n" +
                "- requested for changes by any user criteria in form of: someone requested for changes\n" +
                "- need action of current user criteria in form of: need my action\n" +
                "- submitted by specified user criteria in form of: submitted by \"<login name of a user>\" (quotes are required)\n" +
                "- submitted by current user criteria in form of: submitted by me (quotes are required)\n" +
                "- submitted before certain date criteria in form of: \"Submit Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- submitted after certain date criteria in form of: \"Submit Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- updated before certain date criteria in form of: \"Last Activity Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- updated after certain date criteria in form of: \"Last Activity Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- closed (merged or discarded) before certain date criteria in form of: \"Close Date\" is until \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- closed (merged or discarded) after certain date criteria in form of: \"Close Date\" is since \"<date>\" (quotes are required), where <date> is of format YYYY-MM-DD HH:mm\n" +
                "- includes specified issue criteria in form of: includes issue \"<issue reference>\" (quotes are required)\n" +
                "- includes specified commit criteria in form of: includes commit \"<commit hash>\" (quotes are required)\n" +                
                "- and criteria in form of <criteria1> and <criteria2>\n" +
                "- or criteria in form of <criteria1> or <criteria2>. Note that \"and criteria\" takes precedence over \"or criteria\", use braces to group \"or criteria\" like \"(criteria1 or criteria2) and criteria3\" if you want to override precedence\n" +
                "- not criteria in form of not(<criteria>)\n" +
                "\n" +
                "And can optionally add order clause at end of query string in form of: order by \"<field1>\" <asc|desc>,\"<field2>\" <asc|desc>,... (quotes are required), where <field> is one of below:\n" +
                orderFields +
                "\n" +
                "Leave empty to list all pull requests";

        return HtmlEscape.escapeHtml5(description);
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

    private ProjectInfo getProjectInfo(String projectPath, String currentProjectPath) {
        projectPath = StringUtils.trimToNull(projectPath);
        if (projectPath == null) 
            projectPath = currentProjectPath;

        var projectInfo = new ProjectInfo();
        projectInfo.project = getProject(projectPath);
        projectInfo.currentProject = getProject(currentProjectPath);
        
        return projectInfo;
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

        fieldDescription = HtmlEscape.escapeHtml5(fieldDescription);
        
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
    public Map<String, Object> getToolInputSchemas() {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();
        var inputSchemas = new HashMap<String, Object>();

        var queryIssuesInputSchema = new HashMap<String, Object>();
        var queryIssuesProperties = new HashMap<String, Object>();

        queryIssuesProperties.put("project", Map.of(
            "type", "string",
            "description", "Project to query issues in. Leave empty to query in current project"));
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
        createIssueProperties.put("project", Map.of(
            "type", "string",
            "description", "Project to create issue in. Leave empty to create issue in current project"));
        createIssueProperties.put("title", Map.of(
            "type", "string", 
            "description", "title of the issue"));
        createIssueProperties.put("description", Map.of(
            "type", "string", 
            "description", "description of the issue"));
        createIssueProperties.put("confidential", Map.of(
            "type", "boolean", 
            "description", "whether the issue is confidential"));

        createIssueProperties.put("iterations", getArrayProperties("iteration names"));

        if (subscriptionManager.isSubscriptionActive()) {
            createIssueProperties.put("ownEstimatedTime", Map.of(
                "type", "integer",
                "description", "Estimated time in hours for this issue only (not including linked issues)"));
        }

        for (var field: settingManager.getIssueSetting().getFieldSpecs()) {
            var paramName = getToolParamName(field.getName());
            var fieldProperties = getFieldProperties(field);
            createIssueProperties.put(paramName, fieldProperties);
        }
        
        createIssueInputSchema.put("Type", "object");
        createIssueInputSchema.put("Properties", createIssueProperties);
        createIssueInputSchema.put("Required", List.of("title"));

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

        editIssueProperties.put("iterations", getArrayProperties("iterations to schedule the issue in"));

        if (subscriptionManager.isSubscriptionActive()) {
            editIssueProperties.put("ownEstimatedTime", Map.of(
                    "type", "integer",
                    "description", "Estimated time in hours for this issue only (not including linked issues)"));
        }

        for (var field : settingManager.getIssueSetting().getFieldSpecs()) {
            var paramName = getToolParamName(field.getName());

            var fieldProperties = getFieldProperties(field);
            editIssueProperties.put(paramName, fieldProperties);
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
                var paramName = getToolParamName(field.getName());

                var fieldProperties = getFieldProperties(field);
                transitIssueProperties.put(paramName, fieldProperties);
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

        var queryPullRequestsInputSchema = new HashMap<String, Object>();
        var queryPullRequestsProperties = new HashMap<String, Object>();

        queryPullRequestsProperties.put("project", Map.of(
            "type", "string",
            "description", "Project to query pull requests in. Leave empty to query in current project"));
        queryPullRequestsProperties.put("query", Map.of(
                "type", "string",
                "description", getPullRequestQueryStringDescription()));
        queryPullRequestsProperties.put("offset", Map.of(
                "type", "integer",
                "description", "start position for the query (optional, defaults to 0)"));
        queryPullRequestsProperties.put("count", Map.of(
                "type", "integer",
                "description", "number of pull requests to return (optional, defaults to 25, max 100)"));

        queryPullRequestsInputSchema.put("Type", "object");
        queryPullRequestsInputSchema.put("Properties", queryPullRequestsProperties);
        queryPullRequestsInputSchema.put("Required", new ArrayList<>());

        inputSchemas.put("queryPullRequests", queryPullRequestsInputSchema);

        var createPullRequestInputSchema = new HashMap<String, Object>();
        var createPullRequestProperties = new HashMap<String, Object>();            
        createPullRequestProperties.put("targetProject", Map.of(
            "type", "string",
            "description", "Target project of the pull request. If left empty, it defaults to the original project when the source project is a fork, or to the source project itself otherwise"));
        createPullRequestProperties.put("sourceProject", Map.of(
            "type", "string",
            "description", "Source project of the pull request. Leave empty to use current project"));
        createPullRequestProperties.put("title", Map.of(
            "type", "string",
            "description", "Title of the pull request. Leave empty to use default title"));
        createPullRequestProperties.put("description", Map.of(
                "type", "string",
                "description", "Description of the pull request"));
        createPullRequestProperties.put("targetBranch", Map.of(
                "type", "string",
                "description", "A branch in target project to be used as target branch of the pull request. Leave empty to use default branch"));
        createPullRequestProperties.put("sourceBranch", Map.of(
                "type", "string",
                "description", "A branch in source project to be used as source branch of the pull request"));
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

        var labelSpecs = labelSpecManager.query();
        if (!labelSpecs.isEmpty()) {
            createPullRequestProperties.put("labels", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string"),
                    "uniqueItems", true,
                    "description", "Labels of the pull request. Must be one or more of: " + String.join(", ",
                            labelSpecs.stream().map(LabelSpec::getName).collect(Collectors.toList()))));
        }

        createPullRequestInputSchema.put("Type", "object");
        createPullRequestInputSchema.put("Properties", createPullRequestProperties);
        createPullRequestInputSchema.put("Required", List.of("sourceBranch"));

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
        editPullRequestProperties.put("assignees", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "uniqueItems", true,
                "description", "Assignees of the pull request. Expects user login names"));

        editPullRequestProperties.put("addReviewers", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "uniqueItems", true,
                "description", "Request review from specified users. Expects user login names"));
        editPullRequestProperties.put("removeReviewers", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "uniqueItems", true,
                "description", "Remove specified reviewers. Expects user login names"));

        if (!labelSpecs.isEmpty()) {
            editPullRequestProperties.put("labels", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "uniqueItems", true,
                "description", "Labels of the pull request. Must be one or more of: " + String.join(", ", labelSpecs.stream().map(LabelSpec::getName).collect(Collectors.toList()))));
        }

        editPullRequestProperties.put("autoMerge", Map.of(
                "type", "boolean",
                "description", "Whether to enable auto merge"));
        editPullRequestProperties.put("autoMergeCommitMessage", Map.of(
                "type", "string",
                "description", "Preset commit message for auto merge"));

        editPullRequestInputSchema.put("Type", "object");
        editPullRequestInputSchema.put("Properties", editPullRequestProperties);
        editPullRequestInputSchema.put("Required", List.of("pullRequestReference"));

        inputSchemas.put("editPullRequest", editPullRequestInputSchema);

        return inputSchemas;
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
                    throw new NotAcceptableException("Multiple users found: " + userName);
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
    public List<Map<String, Object>> queryIssues(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("project") String projectPath, 
                @QueryParam("query") String query, 
                @QueryParam("offset") int offset, 
                @QueryParam("count") int count) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var projectInfo = getProjectInfo(projectPath, currentProjectPath);

        if (count > RestConstants.MAX_PAGE_SIZE)
            throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);


        EntityQuery<Issue> parsedQuery;
        if (query != null) {
            var option = new IssueQueryParseOption();
            option.withCurrentUserCriteria(true);
            parsedQuery = IssueQuery.parse(projectInfo.project, query, option, true);
        } else {
            parsedQuery = new IssueQuery();
        }

        var issues = new ArrayList<Map<String, Object>>();
        for (var issue : issueManager.query(new ProjectScope(projectInfo.project, true, false), parsedQuery, true, offset, count)) {
            var issueMap = getIssueMap(projectInfo.currentProject, issue);
            for (var entry: issue.getFieldInputs().entrySet()) {
                issueMap.put(entry.getKey(), entry.getValue().getValues());
            }
            issueMap.put("link", urlManager.urlFor(issue, true));
            issues.add(issueMap);
        }
        return issues;
    }

    private Issue getIssue(Project currentProject, String referenceString) {
        IssueReference issueReference;
        try {
            issueReference = IssueReference.of(referenceString, currentProject);
        } catch (InvalidReferenceException e) {
            throw new NotAcceptableException(e.getMessage());
        }
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
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var issueMap = objectMapper.convertValue(issue, typeReference);
        issueMap.remove("id");
        issueMap.remove("stateOrdinal");
        issueMap.remove("uuid");
        issueMap.remove("messageId");
        issueMap.remove("pinDate");
        issueMap.remove("boardPosition");
        issueMap.remove("numberScopeId");
        issueMap.put("reference", issue.getReference().toString(currentProject));
        issueMap.remove("submitterId");
        issueMap.put("submitter", issue.getSubmitter().getName());
        issueMap.remove("projectId");
        issueMap.put("Project", issue.getProject().getPath());
        issueMap.remove("lastActivity");
        for (var it = issueMap.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (entry.getKey().endsWith("Count"))
                it.remove();
        }
        return issueMap;
    }
    
    @Path("/get-issue")
    @GET
    public Map<String, Object> getIssue(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var issue = getIssue(currentProject, issueReference);
                
        var issueMap = getIssueMap(currentProject, issue);
        for (var entry : issue.getFieldInputs().entrySet()) {
            issueMap.put(entry.getKey(), entry.getValue().getValues());
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
        issueMap.putAll(linkedIssues);
        issueMap.put("link", urlManager.urlFor(issue, true));

        return issueMap;
    }

    @Path("/get-issue-comments")
    @GET
    public List<Map<String, Object>> getIssueComments(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var issue = getIssue(currentProject, issueReference);
                
        var comments = new ArrayList<Map<String, Object>>();
        for (var comment : issue.getComments()) {
            var commentMap = new HashMap<String, Object>();
            commentMap.put("user", comment.getUser().getName());
            commentMap.put("date", comment.getDate());
            commentMap.put("content", comment.getContent());
            comments.add(commentMap);
        }
        return comments;
    }

    @Path("/add-issue-comment")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public String addIssueComment(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, 
                @NotNull String commentContent) {
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
    public String createIssue(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("project") String projectPath, 
                @NotNull @Valid Map<String, Serializable> data) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var projectInfo = getProjectInfo(projectPath, currentProjectPath);

        normalizeIssueData(data);

        var issueSetting = settingManager.getIssueSetting();

        Issue issue = new Issue();
        var title = (String) data.remove("title");
        if (title == null)
            throw new NotAcceptableException("title is required");
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
            if (!projectInfo.project.isTimeTracking())
                throw new NotAcceptableException("Time tracking needs to be enabled for the project");
            if (!SecurityUtils.canScheduleIssues(projectInfo.project))
                throw new UnauthorizedException("Issue schedule permission required to set own estimated time");
            issue.setOwnEstimatedTime(ownEstimatedTime*60);
        }

        @SuppressWarnings("unchecked")
        List<String> iterationNames = (List<String>) data.remove("iterations");
        if (iterationNames != null) {
            if (!SecurityUtils.canScheduleIssues(projectInfo.project))
                throw new UnauthorizedException("Issue schedule permission required to set iterations");
            for (var iterationName : iterationNames) {
                var iteration = iterationManager.findInHierarchy(projectInfo.project, iterationName);
                if (iteration == null)
                    throw new NotFoundException("Iteration '" + iterationName + "' not found");
                IssueSchedule schedule = new IssueSchedule();
                schedule.setIssue(issue);
                schedule.setIteration(iteration);
                issue.getSchedules().add(schedule);
            }
        }

        issue.setProject(projectInfo.project);
        issue.setSubmitDate(new Date());
        issue.setSubmitter(SecurityUtils.getAuthUser());
        issue.setState(issueSetting.getInitialStateSpec().getName());

        issue.setFieldValues(FieldUtils.getFieldValues(issue.getProject(), data));

        try {
            issueManager.open(issue);
        } catch (InvalidIssueFieldsException e) {
            throw newNotAcceptableException(e);
        }

        return "Created issue " + issue.getReference().toString(projectInfo.currentProject) + ": " + urlManager.urlFor(issue, true);
    }

    @Path("/edit-issue")
    @POST
    public String editIssue(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, 
                @NotNull Map<String, Serializable> data) {
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
            } catch (InvalidIssueFieldsException e) {
                throw newNotAcceptableException(e);
            }
        }

        return "Edited issue " + issueReference;
    }

    @Path("/transit-issue")
    @POST
    public String transitIssue(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, 
                @NotNull Map<String, Serializable> data) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var issue = getIssue(currentProject, issueReference);
        normalizeIssueData(data);
        var state = (String) data.remove("state");
        if (state == null)
            throw new NotAcceptableException("state is required");
        var comment = (String) data.remove("comment");
        ManualSpec transition = settingManager.getIssueSetting().getManualSpec(issue, state);
        if (transition == null) {
            var message = MessageFormat.format(
                "No applicable manual transition spec found for current user (issue: {0}, from state: {1}, to state: {2})",
                issue.getReference().toString(), issue.getState(), state);
            throw new NotAcceptableException(message);
        }

        var fieldValues = FieldUtils.getFieldValues(issue.getProject(), data);
        try {
            issueChangeManager.changeState(issue, state, fieldValues, transition.getPromptFields(),
                    transition.getRemoveFields(), comment);
        } catch (InvalidIssueFieldsException e) {
            throw newNotAcceptableException(e);
        }
        var feedback = "Issue " + issueReference + " transited to state \"" + state + "\"";
        var stateDescription = settingManager.getIssueSetting().getStateSpec(state).getDescription();
        if (stateDescription != null)
            feedback += ":\n\n" + stateDescription;
        return feedback;
    }

    @Path("/link-issues")
    @GET
    public String linkIssues(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
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
        } catch (IssueLinkValidationException e) {
            throw new NotAcceptableException(e.getMessage());
        }
        issueLinkManager.create(link);

        return "Issue " + targetReference + " added as \"" + linkName + "\" of " + sourceReference;
    }

    @Path("/log-work")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public String logWork(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
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

    private Map<String, Object> getPullRequestMap(Project currentProject, 
            PullRequest pullRequest, boolean checkMergeConditionIfOpen) {
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var pullRequestMap = objectMapper.convertValue(pullRequest, typeReference);
        pullRequestMap.remove("id");
        if (pullRequest.isOpen() && checkMergeConditionIfOpen) {
            var errorMessage = pullRequest.checkMergeCondition();
            if (errorMessage != null) 
                pullRequestMap.put("status", PullRequest.Status.OPEN.name() + " (" + errorMessage + ")");
            else
                pullRequestMap.put("status", PullRequest.Status.OPEN.name() + " (ready to merge)");
        } 
        pullRequestMap.remove("uuid");
        pullRequestMap.remove("baseCommitHash");
        pullRequestMap.remove("buildCommitHash");
        pullRequestMap.remove("submitTimeGroups");
        pullRequestMap.remove("closeTimeGroups");
        pullRequestMap.remove("checkError");
        pullRequestMap.remove("numberScopeId");
        pullRequestMap.put("reference", pullRequest.getReference().toString(currentProject));
        pullRequestMap.remove("submitterId");
        pullRequestMap.put("submitter", pullRequest.getSubmitter().getName());
        pullRequestMap.remove("targetProjectId");        
        pullRequestMap.put("targetProject", pullRequest.getTarget().getProject().getPath());
        pullRequestMap.remove("sourceProjectId");
        if (pullRequest.getSourceProject() != null)
            pullRequestMap.put("sourceProject", pullRequest.getSourceProject().getPath());
        pullRequestMap.remove("codeCommentsUpdateDate");
        pullRequestMap.remove("lastActivity");
        for (var it = pullRequestMap.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (entry.getKey().endsWith("Count"))
                it.remove();
        }
        return pullRequestMap;
    }

    @Path("/query-pull-requests")
    @GET
    public List<Map<String, Object>> queryPullRequests(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("project") String projectPath, 
                @QueryParam("query") String query, 
                @QueryParam("offset") int offset, 
                @QueryParam("count") int count) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var projectInfo = getProjectInfo(projectPath, currentProjectPath);

        if (!SecurityUtils.canReadCode(projectInfo.project))
            throw new UnauthorizedException("Code read permission required to query pull requests");

        if (count > RestConstants.MAX_PAGE_SIZE)
            throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

        EntityQuery<PullRequest> parsedQuery;
        if (query != null) {
            parsedQuery = PullRequestQuery.parse(projectInfo.project, query, true);
        } else {
            parsedQuery = new PullRequestQuery();
        }

        var pullRequests = new ArrayList<Map<String, Object>>();
        for (var pullRequest : pullRequestManager.query(projectInfo.project, parsedQuery, false, offset, count)) {
            var pullRequestMap = getPullRequestMap(projectInfo.currentProject, pullRequest, false);
            pullRequestMap.put("link", urlManager.urlFor(pullRequest, true));
            pullRequests.add(pullRequestMap);
        }
        return pullRequests;
    }

    @Path("/get-pull-request")
    @GET
    public Map<String, Object> getPullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var pullRequest = getPullRequest(currentProject, pullRequestReference);
                
        var pullRequestMap = getPullRequestMap(currentProject, pullRequest, true);        
        pullRequestMap.put("headCommitHash", pullRequest.getLatestUpdate().getHeadCommitHash());
        pullRequestMap.put("assignees", pullRequest.getAssignees().stream().map(it->it.getName()).collect(Collectors.toList()));
        var reviews = new ArrayList<Map<String, Object>>();
        for (var review : pullRequest.getReviews()) {
            if (review.getStatus() == PullRequestReview.Status.EXCLUDED)
                continue;
            var reviewMap = new HashMap<String, Object>();
            reviewMap.put("reviewer", review.getUser().getName());
            reviewMap.put("status", review.getStatus());
            reviews.add(reviewMap);
        }        
        pullRequestMap.put("reviews", reviews);
        pullRequestMap.put("labels", pullRequest.getLabels().stream().map(it->it.getSpec().getName()).collect(Collectors.toList()));
        pullRequestMap.put("link", urlManager.urlFor(pullRequest, true));

        return pullRequestMap;
    }

    @Path("/get-pull-request-comments")
    @GET
    public List<Map<String, Object>> getPullRequestComments(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);
                
        var comments = new ArrayList<Map<String, Object>>();
        for (var comment : pullRequest.getComments()) {
            var commentMap = new HashMap<String, Object>();
            commentMap.put("user", comment.getUser().getName());
            commentMap.put("date", comment.getDate());
            commentMap.put("content", comment.getContent());
            comments.add(commentMap);
        }
        return comments;
    }

    @Path("/get-pull-request-code-comments")
    @GET
    public List<Map<String, Object>> getPullRequestCodeComments(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var pullRequest = getPullRequest(currentProject, pullRequestReference);
                
        var comments = new ArrayList<Map<String, Object>>();
        for (var comment : pullRequest.getCodeComments()) {
            var commentMap = new HashMap<String, Object>();
            commentMap.put("user", comment.getUser().getName());
            commentMap.put("date", comment.getCreateDate());
            commentMap.put("file", comment.getMark().getPath());
            commentMap.put("content", comment.getContent());
            commentMap.put("replies", comment.getReplies().size());
            commentMap.put("status", comment.isResolved()?"resolved":"unresolved");
            commentMap.put("link", urlManager.urlFor(comment, true));
            comments.add(commentMap);
        }
        return comments;
    }

    @Path("/get-pull-request-patch-info")
    @GET
    public Map<String, String> getPullRequestPatchInfo(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference, 
                @QueryParam("sinceLastReview") boolean sinceLastReview) {
        var user = SecurityUtils.getAuthUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        var oldCommitHash = pullRequest.getBaseCommitHash();
        if (sinceLastReview) {
            Date sinceDate = null;
            for (var change: pullRequest.getChanges()) {
                if ((sinceDate == null || change.getDate().after(sinceDate)) 
                        && change.getUser().equals(user)
                        && (change.getData() instanceof PullRequestApproveData || change.getData() instanceof PullRequestRequestedForChangesData)) {
                    sinceDate = change.getDate();
                }
            }
            for (var comment: pullRequest.getComments()) {
                if ((sinceDate == null || comment.getDate().after(sinceDate)) 
                        && comment.getUser().equals(user)) {
                    sinceDate = comment.getDate();
                }
            }
            if (sinceDate != null) {
                for (PullRequestUpdate update: pullRequest.getSortedUpdates()) {
                    if (update.getDate().before(sinceDate))
                        oldCommitHash = update.getHeadCommitHash();
                }
            }
        }
        var newCommitHash = pullRequest.getLatestUpdate().getHeadCommitHash();
        var comparisonBase = pullRequestManager.getComparisonBase(
            pullRequest, ObjectId.fromString(oldCommitHash), ObjectId.fromString(newCommitHash));

        var patchInfo = new HashMap<String, String>();
        patchInfo.put("projectId", pullRequest.getProject().getId().toString());
        patchInfo.put("oldCommitHash", comparisonBase.name());
        patchInfo.put("newCommitHash", newCommitHash);        
        return patchInfo;
    }

    @Path("/create-pull-request")
    @POST
    public String createPullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("targetProject") String targetProjectPath,
                @QueryParam("sourceProject") String sourceProjectPath,
                @NotNull Map<String, Serializable> data) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();
        
        sourceProjectPath = StringUtils.trimToNull(sourceProjectPath);
        targetProjectPath = StringUtils.trimToNull(targetProjectPath);

        if (sourceProjectPath == null)
            sourceProjectPath = currentProjectPath;

        var sourceProject = getProject(sourceProjectPath);
        if (!SecurityUtils.canReadCode(sourceProject))
            throw new UnauthorizedException("No permission to read code of source project: " + sourceProjectPath);

        Project targetProject;
        if (targetProjectPath != null) {
            targetProject = getProject(targetProjectPath);
        } else {
            targetProject = sourceProject.getForkedFrom();
            if (targetProject == null)
                targetProject = sourceProject;
        }
        if (!SecurityUtils.canReadCode(targetProject))
            throw new UnauthorizedException("No permission to read code of target project: " + targetProjectPath);

        var currentProject = getProject(currentProjectPath);

        normalizePullRequestData(data);

        var targetBranch = (String) data.remove("targetBranch");
        if (targetBranch == null)
            targetBranch = targetProject.getDefaultBranch();
        if (targetBranch == null)
            throw new NotAcceptableException("No code in target project: " + targetProject.getPath());

        var sourceBranch = (String) data.remove("sourceBranch");

        var target = new ProjectAndBranch(targetProject, targetBranch);
        var source = new ProjectAndBranch(sourceProject, sourceBranch);

        if (target.equals(source))
            throw new NotAcceptableException("Target and source branches are the same");

        PullRequest request = pullRequestManager.findOpen(target, source);
        if (request != null)
            throw new NotAcceptableException("Another pull request already opened for this change");

        request = pullRequestManager.findEffective(target, source);
        if (request != null) {
            if (request.isOpen())
                throw new NotAcceptableException("Another pull request already opened for this change");
            else
                throw new NotAcceptableException("Change already merged");
        }

        request = new PullRequest();
        ObjectId baseCommitId = gitService.getMergeBase(
                target.getProject(), target.getObjectId(),
                source.getProject(), source.getObjectId());

        if (baseCommitId == null)
            throw new NotAcceptableException("No common base for source and target branches");

        request.setTitle((String) data.remove("title"));
        request.setDescription((String) data.remove("description"));
        request.setTarget(target);
        request.setSource(source);
        request.setSubmitter(SecurityUtils.getAuthUser());
        request.setBaseCommitHash(baseCommitId.name());

        var mergeStrategyName = (String) data.remove("mergeStrategy");
        if (mergeStrategyName != null) 
            request.setMergeStrategy(MergeStrategy.valueOf(mergeStrategyName));
        else
            request.setMergeStrategy(request.getProject().findDefaultPullRequestMergeStrategy());

        if (request.getBaseCommitHash().equals(source.getObjectName()))
            throw new NotAcceptableException("Change already merged");

        PullRequestUpdate update = new PullRequestUpdate();
        
        update.setDate(Date.from(request.getSubmitDate().toInstant().plusSeconds(1)));
        update.setRequest(request);
        update.setHeadCommitHash(source.getObjectName());
        update.setTargetHeadCommitHash(request.getTarget().getObjectName());
        request.getUpdates().add(update);

        request.generateTitleAndDescriptionIfEmpty();

        pullRequestManager.checkReviews(request, false);

        @SuppressWarnings("unchecked")
        var reviewerNames = (List<String>) data.remove("reviewers");
        if (reviewerNames != null) {
            for (var reviewerName : reviewerNames) {
                User reviewer = userManager.findByName(reviewerName);
                if (reviewer == null)
                    throw new NotFoundException("Reviewer not found: " + reviewerName);
                if (reviewer.equals(request.getSubmitter()))
                    throw new NotAcceptableException("Pull request submitter can not be reviewer");

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

        return "Created pull request " + request.getReference().toString(currentProject) + ": " + urlManager.urlFor(request, true);        
    }

    private PullRequest getPullRequest(Project currentProject, String referenceString) {
        PullRequestReference requestReference;        
        try {
            requestReference = PullRequestReference.of(referenceString, currentProject);
        } catch (InvalidReferenceException e) {
            throw new NotAcceptableException(e.getMessage());
        }
        var request = pullRequestManager.find(requestReference.getProject(), requestReference.getNumber());
        if (request != null) {
            if (!SecurityUtils.canReadCode(request.getProject()))
                throw new UnauthorizedException("No permission to access pull request: " + referenceString);
            return request;
        } else {
            throw new NotFoundException("Pull request not found: " + referenceString);
        }
    }

    @SuppressWarnings("unchecked")
    @Path("/edit-pull-request")
    @POST
    public String editPullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath,
                @QueryParam("reference") @NotNull String pullRequestReference, @NotNull Map<String, Serializable> data) {
        var user = SecurityUtils.getAuthUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var request = getPullRequest(currentProject, pullRequestReference);

        if (!SecurityUtils.canModifyPullRequest(request))
            throw new UnauthorizedException("No permission to edit pull request: " + pullRequestReference);

        normalizePullRequestData(data);

        var title = (String) data.remove("title");
        if (title != null) 
            pullRequestChangeManager.changeTitle(request, title);

        if (data.containsKey("description")) 
            pullRequestChangeManager.changeDescription(request, (String) data.remove("description"));

        var labelNames = (List<String>) data.remove("labels");
        if (labelNames != null) {
            try {
                pullRequestLabelManager.sync(request, labelNames);
            } catch (EntityNotFoundException e) {
                throw new NotFoundException(e.getMessage());
            }
        }

        var mergeStrategyName = (String) data.remove("mergeStrategy");
        if (mergeStrategyName != null) {
            if (!request.isOpen())
                throw new NotAcceptableException("Pull request is closed");
            pullRequestChangeManager.changeMergeStrategy(request, MergeStrategy.valueOf(mergeStrategyName));
        }

        var assigneeNames = (List<String>) data.remove("assignees");
        if (assigneeNames != null) {                        
            if (!request.isOpen())
                throw new NotAcceptableException("Pull request is closed");
            for (var assigneeName : assigneeNames) {
                User assignee = userManager.findByName(assigneeName);
                if (assignee == null)
                    throw new NotFoundException("Assignee not found: " + assigneeName);
                if (request.getAssignments().stream().noneMatch(it -> it.getUser().equals(assignee))) {
                    PullRequestAssignment assignment = new PullRequestAssignment();
                    assignment.setRequest(request);
                    assignment.setUser(assignee);
                    pullRequestAssignmentManager.create(assignment);
                }
            }
            for (var assignee : request.getAssignments()) {
                if (assigneeNames.stream().noneMatch(it -> it.equals(assignee.getUser().getName()))) {
                    pullRequestAssignmentManager.delete(assignee);
                }
            }
        }

        var addReviewerNames = (List<String>) data.remove("addReviewers");
        if (addReviewerNames != null) {
            if (!request.isOpen())
                throw new NotAcceptableException("Pull request is closed");
            for (var reviewerName : addReviewerNames) {
                User reviewer = userManager.findByName(reviewerName);
                if (reviewer == null)
                    throw new NotFoundException("Reviewer not found: " + reviewerName);
                var review = request.getReview(reviewer);
                if (review == null) {
                    review = new PullRequestReview();
                    review.setRequest(request);
                    review.setUser(reviewer);
                    request.getReviews().add(review);
                    pullRequestReviewManager.createOrUpdate(review);
                } else if (review.getStatus() != PullRequestReview.Status.PENDING) {
                    review.setStatus(PullRequestReview.Status.PENDING);
                    pullRequestReviewManager.createOrUpdate(review);
                }
            }
        }
        var removeReviewerNames = (List<String>) data.remove("removeReviewers");
        if (removeReviewerNames != null) {
            if (!request.isOpen())
                throw new NotAcceptableException("Pull request is closed");
            var excludedReviews = new ArrayList<PullRequestReview>();
            for (var reviewerName : removeReviewerNames) {
                User reviewer = userManager.findByName(reviewerName);
                if (reviewer == null)
                    throw new NotFoundException("Reviewer not found: " + reviewerName);
                var review = request.getReview(reviewer);
                if (review != null && review.getStatus() != PullRequestReview.Status.EXCLUDED) {
                    review.setStatus(PullRequestReview.Status.EXCLUDED);
                    excludedReviews.add(review);
                }
            }
            pullRequestManager.checkReviews(request, false);
            var requiredReviewers = excludedReviews.stream()
                    .filter(it -> it.getStatus() != PullRequestReview.Status.EXCLUDED)
                    .map(it -> it.getUser().getName())
                    .collect(Collectors.toList());
            if (!requiredReviewers.isEmpty())
                throw new NotAcceptableException("Unable to remove mandatory reviewers: " + String.join(", ", requiredReviewers));
            for (var review : excludedReviews) 
                pullRequestReviewManager.createOrUpdate(review);
        }


        var autoMergeEnabled = (Boolean) data.remove("autoMerge");
        if (autoMergeEnabled != null) {
            if (!SecurityUtils.canWriteCode(request.getProject()))
                throw new UnauthorizedException("Code write permission is required to edit auto merge");
            if (!request.isOpen())
                throw new NotAcceptableException("Pull request is closed");

            if (autoMergeEnabled && request.checkMergeCondition() == null) 
                throw new NotAcceptableException("This pull request is not eligible for auto-merge, as it can be merged directly now");

            var autoMerge = new AutoMerge();
            autoMerge.setEnabled(autoMergeEnabled);
            autoMerge.setCommitMessage(StringUtils.trimToNull((String) data.remove("autoMergeCommitMessage")));
            autoMerge.setUser(user);
            var errorMessage = request.checkMergeCommitMessage(user, autoMerge.getCommitMessage());
            if (errorMessage != null) 
                throw new NotAcceptableException("Error validating param auto merge commit message: " + errorMessage);

            pullRequestChangeManager.changeAutoMerge(request, autoMerge);
        }
                    
        return "Edited pull request " + pullRequestReference;
    }    

    @Path("/process-pull-request")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public String processPullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference, 
                @QueryParam("operation") String operation, 
                String comment) {
        var user = SecurityUtils.getAuthUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var pullRequest = getPullRequest(currentProject, pullRequestReference);
        comment = StringUtils.trimToNull(comment);
        
        switch (operation) {
        case "approve":
            try {
                pullRequestReviewManager.review(pullRequest, true, comment);
            } catch (PullRequestReviewRejectedException e) {
                throw new NotAcceptableException(e.getMessage());
            }
            return "Approved pull request " + pullRequestReference;
        case "requestChanges":
            try {
                pullRequestReviewManager.review(pullRequest, false, comment);
            } catch (PullRequestReviewRejectedException e) {
                throw new NotAcceptableException(e.getMessage());
            }
            return "Requested changes on pull request " + pullRequestReference;
        case "merge":
            if (!SecurityUtils.canWriteCode(user.asSubject(), pullRequest.getProject()))
                throw new UnauthorizedException();
            String errorMessage = pullRequest.checkMergeCondition();
            if (errorMessage != null)
                throw new NotAcceptableException(errorMessage);
            errorMessage = pullRequest.checkMergeCommitMessage(user, comment);
            if (errorMessage != null)
                throw new NotAcceptableException("Error validating merge commit message: " + errorMessage);

            pullRequestManager.merge(user, pullRequest, comment);

            return "Merged pull request " + pullRequestReference;
        case "discard":
            if (!SecurityUtils.canModifyPullRequest(pullRequest))
                throw new UnauthorizedException();
            if (!pullRequest.isOpen())
                throw new NotAcceptableException("Pull request already closed");
            pullRequestManager.discard(pullRequest, comment);
            return "Discarded pull request " + pullRequestReference;
        case "reopen":
            if (!SecurityUtils.canModifyPullRequest(pullRequest))
                throw new UnauthorizedException();
            errorMessage = pullRequest.checkReopenCondition();
            if (errorMessage != null)
                throw new NotAcceptableException(errorMessage);
            pullRequestManager.reopen(pullRequest, comment);
            return "Reopened pull request " + pullRequestReference;
        case "deleteSourceBranch":
            if (!SecurityUtils.canModifyPullRequest(pullRequest) 
                    || !SecurityUtils.canDeleteBranch(pullRequest.getSourceProject(), pullRequest.getSourceBranch())) {
                throw new UnauthorizedException();
            }
            
            errorMessage = pullRequest.checkDeleteSourceBranchCondition();
            if (errorMessage != null)
                throw new NotAcceptableException(errorMessage); 		
            
            pullRequestManager.deleteSourceBranch(pullRequest, comment);

            return "Deleted source branch of pull request " + pullRequestReference;
        case "restoreSourceBranch":
            if (!SecurityUtils.canModifyPullRequest(pullRequest) || 
                    !SecurityUtils.canWriteCode(pullRequest.getSourceProject())) {
                throw new UnauthorizedException();
            }
            
            errorMessage = pullRequest.checkRestoreSourceBranchCondition();
            if (errorMessage != null)
                throw new NotAcceptableException(errorMessage);
            
            pullRequestManager.restoreSourceBranch(pullRequest, comment);

            return "Restored source branch of pull request " + pullRequestReference;
        default:
            throw new NotAcceptableException("Invalid operation: " + operation);
        }
    }

    @Path("/add-pull-request-comment")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public String addPullRequestComment(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference, 
                @NotNull String commentContent) {
        if (SecurityUtils.getAuthUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var pullRequest = getPullRequest(currentProject, pullRequestReference);
        var comment = new PullRequestComment();
        comment.setRequest(pullRequest);
        comment.setContent(commentContent);
        comment.setUser(SecurityUtils.getAuthUser());
        comment.setDate(new Date());
        pullRequestCommentManager.create(comment);

        return "Commented on pull request " + pullRequestReference;
    }
    
    private void normalizePullRequestData(Map<String, Serializable> data) {
        for (var entry : data.entrySet()) {
            if (entry.getValue() instanceof String)
                entry.setValue(StringUtils.trimToNull((String) entry.getValue()));
        }
    }

    private NotAcceptableException newNotAcceptableException(InvalidIssueFieldsException e) {
        var invaliParams = new ArrayList<String>();
        for (var entry: e.getInvalidFields().entrySet()) {
            invaliParams.add(getToolParamName(entry.getKey()) + ": " + entry.getValue());
        }
        return new NotAcceptableException("Invalid tool parameters:\n\n" + String.join("\n", invaliParams));
    }

    private static class ProjectInfo {
        
        Project project;

        Project currentProject;
    }

 }