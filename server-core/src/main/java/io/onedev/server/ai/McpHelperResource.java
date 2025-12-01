package io.onedev.server.ai;

import static io.onedev.server.ai.QueryDescriptions.getBuildQueryDescription;
import static io.onedev.server.ai.QueryDescriptions.getPackQueryDescription;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.Validator;
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
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.ServerConfig;
import io.onedev.server.SubscriptionService;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.data.migration.VersionedYamlDoc;
import io.onedev.server.entityreference.BuildReference;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.entityreference.PullRequestReference;
import io.onedev.server.exception.PullRequestReviewRejectedException;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.SystemSetting;
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
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.service.IssueLinkService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueWorkService;
import io.onedev.server.service.IterationService;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestAssignmentService;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.PullRequestLabelService;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.UrlService;

@Api(internal = true)
@Path("/mcp-helper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class McpHelperResource {

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private SettingService settingService;

    @Inject
    private UserService userService;
    
    @Inject
    private IssueService issueService;    
    
    @Inject
    private ProjectService projectService;

    @Inject
    private LinkSpecService linkSpecService;

    @Inject
    private IssueLinkService issueLinkService;

    @Inject
    private IssueCommentService issueCommentService;

    @Inject
    private IterationService iterationService;

    @Inject
    private IssueChangeService issueChangeService;

    @Inject
    private IssueWorkService issueWorkService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private PullRequestService pullRequestService;

    @Inject
    private PullRequestChangeService pullRequestChangeService;

    @Inject
    private PullRequestAssignmentService pullRequestAssignmentService;

    @Inject
    private PullRequestReviewService pullRequestReviewService;

    @Inject
    private PullRequestLabelService pullRequestLabelService;

    @Inject
    private PullRequestCommentService pullRequestCommentService;

    @Inject
    private BuildService buildService;

    @Inject
    private PackService packService;

    @Inject
    private JobService jobService;

    @Inject
    private GitService gitService;

    @Inject
    private LabelSpecService labelSpecService;

    @Inject
    private UrlService urlService;

    @Inject
    private Validator validator;

    @Inject
    private ServerConfig serverConfig;

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
        var project = projectService.findByPath(projectPath);
        if (project == null || !SecurityUtils.canAccessProject(project))
            throw new NotFoundException("Project not found or inaccessible: " + projectPath);
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

        fieldDescription = escapeHtml5(fieldDescription);
        
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
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();
        var inputSchemas = new HashMap<String, Object>();

        var queryIssuesInputSchema = new HashMap<String, Object>();
        var queryIssuesProperties = new HashMap<String, Object>();

        queryIssuesProperties.put("project", Map.of(
            "type", "string",
            "description", "Project to query issues in. Leave empty to query in current project"));
        queryIssuesProperties.put("query", Map.of(
            "type", "string",
            "description", escapeHtml5(QueryDescriptions.getIssueQueryDescription())));
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

        if (subscriptionService.isSubscriptionActive()) {
            createIssueProperties.put("ownEstimatedTime", Map.of(
                "type", "integer",
                "description", "Estimated time in hours for this issue only (not including linked issues)"));
        }

        for (var field: settingService.getIssueSetting().getFieldSpecs()) {
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

        if (subscriptionService.isSubscriptionActive()) {
            editIssueProperties.put("ownEstimatedTime", Map.of(
                    "type", "integer",
                    "description", "Estimated time in hours for this issue only (not including linked issues)"));
        }

        for (var field : settingService.getIssueSetting().getFieldSpecs()) {
            var paramName = getToolParamName(field.getName());

            var fieldProperties = getFieldProperties(field);
            editIssueProperties.put(paramName, fieldProperties);
        }

        editIssueInputSchema.put("Type", "object");
        editIssueInputSchema.put("Properties", editIssueProperties);
        editIssueInputSchema.put("Required", List.of("issueReference"));

        inputSchemas.put("editIssue", editIssueInputSchema);            

        var toStates = new HashSet<String>();
        for (var transition: settingService.getIssueSetting().getTransitionSpecs()) {
            if (transition instanceof ManualSpec) {
                if (transition.getToStates().isEmpty()) {
                    toStates.addAll(settingService.getIssueSetting().getStateSpecMap().keySet());
                    break;
                } else {
                    toStates.addAll(transition.getToStates());
                }
            }
        }
        if (toStates.size() > 0) {
            var changeIssueStateInputSchema = new HashMap<String, Object>();
            changeIssueStateInputSchema.put("Type", "object");

            var changeIssueStateProperties = new HashMap<String, Object>();
            changeIssueStateProperties.put("issueReference", Map.of(
                "type", "string",
                "description", "reference of the issue to change state"));
            changeIssueStateProperties.put("state", Map.of(
                    "type", "string",
                    "description", "new state of the issue. Must be one of: " + String.join(", ", toStates)));
            changeIssueStateProperties.put("comment", Map.of(
                    "type", "string",
                    "description", "comment of the state change"));

            for (var field : settingService.getIssueSetting().getFieldSpecs()) {
                var paramName = getToolParamName(field.getName());

                var fieldProperties = getFieldProperties(field);
                changeIssueStateProperties.put(paramName, fieldProperties);
            }                

            changeIssueStateInputSchema.put("Properties", changeIssueStateProperties);
            changeIssueStateInputSchema.put("Required", List.of("issueReference", "state"));

            inputSchemas.put("changeIssueState", changeIssueStateInputSchema);
        }

        var linkSpecs = linkSpecService.query();
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
                "description", escapeHtml5(QueryDescriptions.getPullRequestQueryDescription())));
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

        var queryBuildsInputSchema = new HashMap<String, Object>();
        var queryBuildsProperties = new HashMap<String, Object>();

        queryBuildsProperties.put("project", Map.of(
            "type", "string",
            "description", "Project to query builds in. Leave empty to query in current project"));
        queryBuildsProperties.put("query", Map.of(
                "type", "string",
                "description", escapeHtml5(getBuildQueryDescription())));
        queryBuildsProperties.put("offset", Map.of(
                "type", "integer",
                "description", "start position for the query (optional, defaults to 0)"));
        queryBuildsProperties.put("count", Map.of(
                "type", "integer",
                "description", "number of builds to return (optional, defaults to 25, max 100)"));

        queryBuildsInputSchema.put("Type", "object");
        queryBuildsInputSchema.put("Properties", queryBuildsProperties);
        queryBuildsInputSchema.put("Required", new ArrayList<>());

        inputSchemas.put("queryBuilds", queryBuildsInputSchema);

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

        var labelSpecs = labelSpecService.query();
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

        var queryPacksInputSchema = new HashMap<String, Object>();
        var queryPacksProperties = new HashMap<String, Object>();

        queryPacksProperties.put("project", Map.of(
            "type", "string",
            "description", "Project to query packages in. Leave empty to query in current project"));
        queryPacksProperties.put("query", Map.of(
                "type", "string",
                "description", escapeHtml5(getPackQueryDescription())));
        queryPacksProperties.put("offset", Map.of(
                "type", "integer",
                "description", "start position for the query (optional, defaults to 0)"));
        queryPacksProperties.put("count", Map.of(
                "type", "integer",
                "description", "number of packages to return (optional, defaults to 25, max 100)"));

        queryPacksInputSchema.put("Type", "object");
        queryPacksInputSchema.put("Properties", queryPacksProperties);
        queryPacksInputSchema.put("Required", new ArrayList<>());

        inputSchemas.put("queryPacks", queryPacksInputSchema);
        
        return inputSchemas;
    }

    @Path("/get-login-name")
    @GET
    public String getLoginName(@QueryParam("userName") String userName) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        User user;                
        userName = StringUtils.trimToNull(userName);
        if (userName != null) {
            user = userService.findByName(userName);
            if (user == null)
                user = userService.findByFullName(userName);
            if (user == null) {
                var matchingUsers = new ArrayList<User>();
                var lowerCaseUserName = userName.toLowerCase();
                for (var eachUser: userService.query()) {
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
        if (SecurityUtils.getUser() == null)
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
        var subject = SecurityUtils.getSubject();
        if (SecurityUtils.getUser(subject) == null)
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

        var summaries = new ArrayList<Map<String, Object>>();
        for (var issue : issueService.query(subject, new ProjectScope(projectInfo.project, true, false), parsedQuery, true, offset, count)) {
            var summary = IssueHelper.getSummary(projectInfo.currentProject, issue);
            for (var entry: issue.getFieldInputs().entrySet()) {
                summary.put(entry.getKey(), entry.getValue().getValues());
            }
            summary.put("link", urlService.urlFor(issue, true));
            summaries.add(summary);
        }
        return summaries;
    }

    private Issue getIssue(Project currentProject, String referenceString) {
        var issueReference = IssueReference.of(referenceString, currentProject);
        var issue = issueService.find(issueReference.getProject(), issueReference.getNumber());
        if (issue != null) {
            if (!SecurityUtils.canAccessIssue(issue))
                throw new UnauthorizedException("No permission to access issue: " + referenceString);
            return issue;
        } else {
            throw new NotFoundException("Issue not found: " + referenceString);
        }
    }
    
    @Path("/get-issue")
    @GET
    public Map<String, Object> getIssue(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var issue = getIssue(currentProject, issueReference);                
        return IssueHelper.getDetail(currentProject, issue);
    }

    @Path("/get-issue-comments")
    @GET
    public List<Map<String, Object>> getIssueComments(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();
        var currentProject = getProject(currentProjectPath);
        var issue = getIssue(currentProject, issueReference);
        return IssueHelper.getComments(issue);
    }

    @Path("/add-issue-comment")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public String addIssueComment(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, 
                @NotNull String commentContent) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var issue = getIssue(currentProject, issueReference);
        var comment = new IssueComment();
        comment.setIssue(issue);
        comment.setContent(commentContent);
        comment.setUser(SecurityUtils.getUser());
        comment.setDate(new Date());
        issueCommentService.create(comment);

        return "Commented on issue " + issueReference;
    }

    @Path("/create-issue")
    @POST
    public String createIssue(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("project") String projectPath, 
                @NotNull @Valid Map<String, Serializable> data) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var projectInfo = getProjectInfo(projectPath, currentProjectPath);

        normalizeIssueData(data);

        var issueSetting = settingService.getIssueSetting();

        Issue issue = new Issue();
        var title = (String) data.remove("title");
        if (title == null)
            throw new NotAcceptableException("Title is required");
        issue.setTitle(title);
        var description = (String) data.remove("description");
        issue.setDescription(description);
        var confidential = (Boolean) data.remove("confidential");
        if (confidential != null)
            issue.setConfidential(confidential);

        Integer ownEstimatedTime = (Integer) data.remove("ownEstimatedTime");
        if (ownEstimatedTime != null) {
            if (!subscriptionService.isSubscriptionActive())
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
                var iteration = iterationService.findInHierarchy(projectInfo.project, iterationName);
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
        issue.setSubmitter(SecurityUtils.getUser());
        issue.setState(issueSetting.getInitialStateSpec().getName());

        issue.setFieldValues(FieldUtils.getFieldValues(issue.getProject(), data));

        issueService.open(issue);

        return "Created issue " + issue.getReference().toString(projectInfo.currentProject) + ": " + urlService.urlFor(issue, true);
    }

    @Path("/edit-issue")
    @POST
    public String editIssue(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, 
                @NotNull Map<String, Serializable> data) {
        var subject = SecurityUtils.getSubject();
        var user = SecurityUtils.getUser(subject);

        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var issue = getIssue(currentProject, issueReference);
        if (!SecurityUtils.canModifyIssue(issue))
            throw new UnauthorizedException();

        normalizeIssueData(data);

        var title = (String) data.remove("title");
        if (title != null) 
            issueChangeService.changeTitle(user, issue, title);

        if (data.containsKey("description")) 
            issueChangeService.changeDescription(user, issue, (String) data.remove("description"));

        var confidential = (Boolean) data.remove("confidential");
        if (confidential != null)
            issueChangeService.changeConfidential(user, issue, confidential);

        Integer ownEstimatedTime = (Integer) data.remove("ownEstimatedTime");
        if (ownEstimatedTime != null) {
            if (!subscriptionService.isSubscriptionActive())
                throw new NotAcceptableException("An active subscription is required for this feature");
            if (!issue.getProject().isTimeTracking())
                throw new NotAcceptableException("Time tracking needs to be enabled for the project");
            if (!SecurityUtils.canScheduleIssues(subject, issue.getProject()))
                throw new UnauthorizedException("Issue schedule permission required to set own estimated time");
            issueChangeService.changeOwnEstimatedTime(user, issue, ownEstimatedTime*60);
        }

        @SuppressWarnings("unchecked")
        List<String> iterationNames = (List<String>) data.remove("iterations");
        if (iterationNames != null) {
            if (!SecurityUtils.canScheduleIssues(subject, issue.getProject()))
                throw new UnauthorizedException("Issue schedule permission required to set iterations");
            var iterations = new ArrayList<Iteration>();
            for (var iterationName : iterationNames) {
                var iteration = iterationService.findInHierarchy(issue.getProject(), iterationName);
                if (iteration == null)
                    throw new NotFoundException("Iteration '" + iterationName + "' not found");
                iterations.add(iteration);
            }
            issueChangeService.changeIterations(user, issue, iterations);
        }

        if (!data.isEmpty()) {
            var issueSetting = settingService.getIssueSetting();
            String initialState = issueSetting.getInitialStateSpec().getName();

            if (!SecurityUtils.canManageIssues(subject, issue.getProject())
                    && !(issue.getSubmitter().equals(user) && issue.getState().equals(initialState))) {
                throw new UnauthorizedException("No permission to update issue fields");
            }

            issueChangeService.changeFields(user, issue, FieldUtils.getFieldValues(issue.getProject(), data));
        }

        return "Edited issue " + issueReference;
    }

    @Path("/change-issue-state")
    @POST
    public String changeIssueState(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, 
                @NotNull Map<String, Serializable> data) {
        var subject = SecurityUtils.getSubject();
        var user = SecurityUtils.getUser(subject);
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var issue = getIssue(currentProject, issueReference);
        normalizeIssueData(data);
        var state = (String) data.remove("state");
        if (state == null)
            throw new NotAcceptableException("State is required");
        var comment = (String) data.remove("comment");
        ManualSpec transition = settingService.getIssueSetting().getManualSpec(subject, issue, state);
        if (transition == null) {
            var message = MessageFormat.format(
                "No applicable manual transition spec found for current user (issue: {0}, from state: {1}, to state: {2})",
                issue.getReference().toString(), issue.getState(), state);
            throw new NotAcceptableException(message);
        }

        var fieldValues = FieldUtils.getFieldValues(issue.getProject(), data);
        issueChangeService.changeState(user, issue, state, fieldValues, transition.getPromptFields(),
                transition.getRemoveFields(), comment);
        var feedback = "Issue " + issueReference + " transited to state \"" + state + "\"";
        var stateDescription = settingService.getIssueSetting().getStateSpec(state).getDescription();
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
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var sourceIssue = getIssue(currentProject, sourceReference);
        var targetIssue = getIssue(currentProject, targetReference);

        var linkSpec = linkSpecService.find(linkName);
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
        link.validate();
        issueLinkService.create(link);

        return "Issue " + targetReference + " added as \"" + linkName + "\" of " + sourceReference;
    }

    @Path("/log-work")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public String logWork(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, 
                @QueryParam("spentHours") int spentHours, String comment) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var issue = getIssue(currentProject, issueReference);

        if (!subscriptionService.isSubscriptionActive())
            throw new NotAcceptableException("An active subscription is required for this feature");
        if (!issue.getProject().isTimeTracking())
            throw new NotAcceptableException("Time tracking needs to be enabled for the project");
        if (!SecurityUtils.canAccessIssue(issue))
            throw new UnauthorizedException("No permission to access issue: " + issueReference);

        var work = new IssueWork();
        work.setIssue(issue);
        work.setUser(SecurityUtils.getUser());
        work.setMinutes(spentHours * 60);
        work.setNote(StringUtils.trimToNull(comment));
        issueWorkService.createOrUpdate(work);
        return "Work logged for issue " + issueReference;
    }

    private void normalizeIssueData(Map<String, Serializable> data) {
        for (var entry: data.entrySet()) {
            if (entry.getValue() instanceof String) 
                entry.setValue(StringUtils.trimToNull((String) entry.getValue()));
        }
        for (var field: settingService.getIssueSetting().getFieldSpecs()) {
            var paramName = getToolParamName(field.getName());
            if (!paramName.equals(field.getName()) && data.containsKey(paramName)) {
                data.put(field.getName(), data.get(paramName));
                data.remove(paramName);
            }
        }        
    }    

    @Path("/query-pull-requests")
    @GET
    public List<Map<String, Object>> queryPullRequests(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("project") String projectPath, 
                @QueryParam("query") String query, 
                @QueryParam("offset") int offset, 
                @QueryParam("count") int count) {
        var subject = SecurityUtils.getSubject();
        if (SecurityUtils.getUser(subject) == null)
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

        var summaries = new ArrayList<Map<String, Object>>();
        for (var pullRequest : pullRequestService.query(subject, projectInfo.project, parsedQuery, false, offset, count)) {
            var summary = PullRequestHelper.getSummary(projectInfo.currentProject, pullRequest, false);
            summary.put("link", urlService.urlFor(pullRequest, true));
            summaries.add(summary);
        }
        return summaries;
    }

    @Path("/query-builds")
    @GET
    public List<Map<String, Object>> queryBuilds(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("project") String projectPath, 
                @QueryParam("query") String query, 
                @QueryParam("offset") int offset, 
                @QueryParam("count") int count) {
        var subject = SecurityUtils.getSubject();
        if (SecurityUtils.getUser(subject) == null)
            throw new UnauthenticatedException();

        var projectInfo = getProjectInfo(projectPath, currentProjectPath);

        if (count > RestConstants.MAX_PAGE_SIZE)
            throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

        EntityQuery<Build> parsedQuery;
        if (query != null) {
            parsedQuery = BuildQuery.parse(projectInfo.project, query, true, true);
        } else {
            parsedQuery = new BuildQuery();
        }

        var builds = new ArrayList<Map<String, Object>>();
        for (var build : buildService.query(subject, projectInfo.project, parsedQuery, false, offset, count)) {
            var buildMap = getBuildMap(projectInfo.currentProject, build);
            buildMap.put("link", urlService.urlFor(build, true));
            builds.add(buildMap);
        }
        return builds;
    }

    @Path("/get-build")
    @GET
    public Map<String, Object> getBuild(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String buildReference) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var build = getBuild(currentProject, buildReference);
                
        var buildMap = getBuildMap(currentProject, build);     
        buildMap.put("params", build.getParamMap());
        buildMap.put("labels", build.getLabels().stream().map(it->it.getSpec().getName()).collect(Collectors.toList()));
        buildMap.put("link", urlService.urlFor(build, true));

        return buildMap;
    }
    
    @Path("/get-previous-successful-similar-build")
    @GET
    public Map<String, Object> getPreviousSuccessfulSimilarBuild(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String buildReference) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var build = getBuild(currentProject, buildReference);
                
        var foundBuild = buildService.findPreviousSuccessfulSimilar(build);
        if (foundBuild != null) {
            var buildMap = getBuildMap(currentProject, foundBuild);     
            buildMap.put("params", foundBuild.getParamMap());
            buildMap.put("labels", foundBuild.getLabels().stream().map(it->it.getSpec().getName()).collect(Collectors.toList()));
            buildMap.put("link", urlService.urlFor(foundBuild, true));
            return buildMap;    
        } else {
            throw new NotFoundException("Previous successful similar build not found");
        }
    }

    @Path("/get-pull-request")
    @GET
    public Map<String, Object> getPullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);
        return PullRequestHelper.getDetail(currentProject, pullRequest);
    }

    @Path("/get-pull-request-comments")
    @GET
    public List<Map<String, Object>> getPullRequestComments(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);
                
        return PullRequestHelper.getComments(pullRequest);
    }

    @Path("/get-pull-request-code-comments")
    @GET
    public List<Map<String, Object>> getPullRequestCodeComments(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference) {
        if (SecurityUtils.getUser() == null)
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
            commentMap.put("link", urlService.urlFor(comment, true));
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
        var user = SecurityUtils.getUser();
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
        var comparisonBase = pullRequestService.getComparisonBase(
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
        if (SecurityUtils.getUser() == null)
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
        if (sourceBranch == null)
            throw new NotAcceptableException("Source branch is required");

        var target = new ProjectAndBranch(targetProject, targetBranch);
        var source = new ProjectAndBranch(sourceProject, sourceBranch);

        if (target.equals(source))
            throw new NotAcceptableException("Target and source branches are the same");

        PullRequest request = pullRequestService.findOpen(target, source);
        if (request != null)
            throw new NotAcceptableException("Another pull request already opened for this change");

        request = pullRequestService.findEffective(target, source);
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

        request.setTarget(target);
        request.setSource(source);
        request.setSubmitter(SecurityUtils.getUser());
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

        var title = (String) data.remove("title");
        if (title == null)
            title = request.generateTitleFromCommits();
        else
            title = request.cleanTitle(title);
            
        if (title == null)
            title = request.generateTitleFromBranch();

        request.setTitle(title);

        var description = (String) data.remove("description");
        if (description == null)
            description = request.generateDescriptionFromCommits();
        request.setDescription(description);

        pullRequestService.checkReviews(request, false);

        @SuppressWarnings("unchecked")
        var reviewerNames = (List<String>) data.remove("reviewers");
        if (reviewerNames != null) {
            for (var reviewerName : reviewerNames) {
                User reviewer = userService.findByName(reviewerName);
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
                User assignee = userService.findByName(assigneeName);
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

        pullRequestService.open(request);

        return "Created pull request " + request.getReference().toString(currentProject) + ": " + urlService.urlFor(request, true);        
    }

    private PullRequest getPullRequest(Project currentProject, String referenceString) {
        var requestReference = PullRequestReference.of(referenceString, currentProject);
        var request = pullRequestService.find(requestReference.getProject(), requestReference.getNumber());
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
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var request = getPullRequest(currentProject, pullRequestReference);

        if (!SecurityUtils.canModifyPullRequest(request))
            throw new UnauthorizedException("No permission to edit pull request: " + pullRequestReference);

        normalizePullRequestData(data);

        var title = (String) data.remove("title");
        if (title != null) 
            pullRequestChangeService.changeTitle(user, request, title);

        if (data.containsKey("description")) 
            pullRequestChangeService.changeDescription(user, request, (String) data.remove("description"));

        var labelNames = (List<String>) data.remove("labels");
        if (labelNames != null) {
            try {
                pullRequestLabelService.sync(request, labelNames);
            } catch (EntityNotFoundException e) {
                throw new NotFoundException(e.getMessage());
            }
        }

        var mergeStrategyName = (String) data.remove("mergeStrategy");
        if (mergeStrategyName != null) {
            if (!request.isOpen())
                throw new NotAcceptableException("Pull request is closed");
            pullRequestChangeService.changeMergeStrategy(user, request, MergeStrategy.valueOf(mergeStrategyName));
        }

        var assigneeNames = (List<String>) data.remove("assignees");
        if (assigneeNames != null) {                        
            if (!request.isOpen())
                throw new NotAcceptableException("Pull request is closed");
            for (var assigneeName : assigneeNames) {
                User assignee = userService.findByName(assigneeName);
                if (assignee == null)
                    throw new NotFoundException("Assignee not found: " + assigneeName);
                if (request.getAssignments().stream().noneMatch(it -> it.getUser().equals(assignee))) {
                    PullRequestAssignment assignment = new PullRequestAssignment();
                    assignment.setRequest(request);
                    assignment.setUser(assignee);
                    pullRequestAssignmentService.create(assignment);
                }
            }
            for (var assignee : request.getAssignments()) {
                if (assigneeNames.stream().noneMatch(it -> it.equals(assignee.getUser().getName()))) {
                    pullRequestAssignmentService.delete(assignee);
                }
            }
        }

        var addReviewerNames = (List<String>) data.remove("addReviewers");
        if (addReviewerNames != null) {
            if (!request.isOpen())
                throw new NotAcceptableException("Pull request is closed");
            
            for (var reviewerName : addReviewerNames) {
                User reviewer = userService.findByName(reviewerName);
                if (reviewer == null)
                    throw new NotFoundException("Reviewer not found: " + reviewerName);
                var review = request.getReview(reviewer);
                if (review == null) {
                    review = new PullRequestReview();
                    review.setRequest(request);
                    review.setUser(reviewer);
                    request.getReviews().add(review);
                    pullRequestReviewService.createOrUpdate(user, review);
                } else if (review.getStatus() != PullRequestReview.Status.PENDING) {
                    review.setStatus(PullRequestReview.Status.PENDING);
                    pullRequestReviewService.createOrUpdate(user, review);
                }
            }
        }
        var removeReviewerNames = (List<String>) data.remove("removeReviewers");
        if (removeReviewerNames != null) {
            if (!request.isOpen())
                throw new NotAcceptableException("Pull request is closed");
            var excludedReviews = new ArrayList<PullRequestReview>();
            for (var reviewerName : removeReviewerNames) {
                User reviewer = userService.findByName(reviewerName);
                if (reviewer == null)
                    throw new NotFoundException("Reviewer not found: " + reviewerName);
                var review = request.getReview(reviewer);
                if (review != null && review.getStatus() != PullRequestReview.Status.EXCLUDED) {
                    review.setStatus(PullRequestReview.Status.EXCLUDED);
                    excludedReviews.add(review);
                }
            }
            pullRequestService.checkReviews(request, false);
            var requiredReviewers = excludedReviews.stream()
                    .filter(it -> it.getStatus() != PullRequestReview.Status.EXCLUDED)
                    .map(it -> it.getUser().getName())
                    .collect(Collectors.toList());
            if (!requiredReviewers.isEmpty())
                throw new NotAcceptableException("Unable to remove mandatory reviewers: " + String.join(", ", requiredReviewers));
            for (var review : excludedReviews) 
                pullRequestReviewService.createOrUpdate(user, review);
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
            var errorMessage = request.checkMergeCommitMessage(user, autoMerge.getCommitMessage());
            if (errorMessage != null) 
                throw new NotAcceptableException("Error validating param auto merge commit message: " + errorMessage);

            pullRequestChangeService.changeAutoMerge(user, request, autoMerge);
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
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var pullRequest = getPullRequest(currentProject, pullRequestReference);
        comment = StringUtils.trimToNull(comment);
        
        switch (operation) {
        case "approve":
            try {
                pullRequestReviewService.review(user, pullRequest, true, comment);
            } catch (PullRequestReviewRejectedException e) {
                throw new NotAcceptableException(e.getMessage());
            }
            return "Approved pull request " + pullRequestReference;
        case "requestChanges":
            try {
                pullRequestReviewService.review(user, pullRequest, false, comment);
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

            pullRequestService.merge(user, pullRequest, comment);

            return "Merged pull request " + pullRequestReference;
        case "discard":
            if (!SecurityUtils.canModifyPullRequest(pullRequest))
                throw new UnauthorizedException();
            if (!pullRequest.isOpen())
                throw new NotAcceptableException("Pull request already closed");
            pullRequestService.discard(user, pullRequest, comment);
            return "Discarded pull request " + pullRequestReference;
        case "reopen":
            if (!SecurityUtils.canModifyPullRequest(pullRequest))
                throw new UnauthorizedException();
            errorMessage = pullRequest.checkReopenCondition();
            if (errorMessage != null)
                throw new NotAcceptableException(errorMessage);
            pullRequestService.reopen(user, pullRequest, comment);
            return "Reopened pull request " + pullRequestReference;
        case "deleteSourceBranch":
            if (!SecurityUtils.canModifyPullRequest(pullRequest) 
                    || !SecurityUtils.canDeleteBranch(pullRequest.getSourceProject(), pullRequest.getSourceBranch())) {
                throw new UnauthorizedException();
            }
            
            errorMessage = pullRequest.checkDeleteSourceBranchCondition();
            if (errorMessage != null)
                throw new NotAcceptableException(errorMessage); 		
            
            pullRequestService.deleteSourceBranch(user, pullRequest, comment);

            return "Deleted source branch of pull request " + pullRequestReference;
        case "restoreSourceBranch":
            if (!SecurityUtils.canModifyPullRequest(pullRequest) || 
                    !SecurityUtils.canWriteCode(pullRequest.getSourceProject())) {
                throw new UnauthorizedException();
            }
            
            errorMessage = pullRequest.checkRestoreSourceBranchCondition();
            if (errorMessage != null)
                throw new NotAcceptableException(errorMessage);
            
            pullRequestService.restoreSourceBranch(user, pullRequest, comment);

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
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var pullRequest = getPullRequest(currentProject, pullRequestReference);
        var comment = new PullRequestComment();
        comment.setRequest(pullRequest);
        comment.setContent(commentContent);
        comment.setUser(SecurityUtils.getUser());
        comment.setDate(new Date());
        pullRequestCommentService.create(comment);

        return "Commented on pull request " + pullRequestReference;
    }

    private Map<String, Object> getBuildMap(Project currentProject, Build build) {
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var buildMap = objectMapper.convertValue(build, typeReference);
        buildMap.remove("id");
        buildMap.remove("uuid");
        buildMap.remove("numberScopeId");
        buildMap.remove("workspacePath");
        buildMap.remove("checkoutPaths");
        buildMap.remove("submitSequence");
        buildMap.remove("finishTimeGroups");        
        buildMap.put("reference", build.getReference().toString(currentProject));
        buildMap.remove("submitterId");
        buildMap.put("submitter", build.getSubmitter().getName());
        buildMap.remove("cancellerId");
        if (build.getCanceller() != null)
            buildMap.put("canceller", build.getCanceller().getName());
        buildMap.remove("requestId");
        if (build.getRequest() != null)
            buildMap.put("pullRequest", build.getRequest().getReference().toString(currentProject));
        buildMap.remove("issueId");
        if (build.getIssue() != null)
            buildMap.put("issue", build.getIssue().getReference().toString(currentProject));
        buildMap.remove("agentId");
        if (build.getAgent() != null)
            buildMap.put("agent", build.getAgent().getName());
        
        buildMap.put("project", build.getProject().getPath());
        return buildMap;
    }

    @SuppressWarnings("unchecked")
    @Path("/run-job")
    @POST
    public Map<String, Object> runJob(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @NotNull @Valid Map<String, Serializable> data) {
        var subject = SecurityUtils.getSubject();
        var user = SecurityUtils.getUser(subject);

        if (user == null)
            throw new UnauthenticatedException();

        var project = getProject(currentProjectPath);

        var jobName = StringUtils.trimToNull((String)data.get("jobName"));
        if (jobName == null)
            throw new NotAcceptableException("Job name is required");

        if (!SecurityUtils.canRunJob(subject, project, jobName))		
            throw new UnauthorizedException();

        String refName;
        var branch = StringUtils.trimToNull((String)data.get("branch"));
        var tag = StringUtils.trimToNull((String)data.get("tag"));
        var commitHash = StringUtils.trimToNull((String)data.get("commitHash"));
        if (commitHash != null) {
            refName = StringUtils.trimToNull((String)data.get("refName"));
            if (refName == null) {
                throw new NotAcceptableException("Ref name is required when commit hash is specified");
            }
        } else if (branch != null) {            
            refName = GitUtils.branch2ref(branch);
        } else if (tag != null) {
            refName = GitUtils.tag2ref(tag);
        } else {
            throw new NotAcceptableException("Either commit hash, branch or tag should be specified");
        }
        if (commitHash == null)
            commitHash = project.getRevCommit(refName, true).name();
            
        Map<String, List<String>> params;
        var paramData = data.get("params");
        if (paramData instanceof List) {
            params = new HashMap<String, List<String>>();
            List<String> paramPairs = (List<String>) paramData;
            if (paramPairs != null) {
                for (var paramPair: paramPairs) {
                    var paramName = StringUtils.trimToNull(StringUtils.substringBefore(paramPair, "="));
                    var paramValue = StringUtils.trimToNull(StringUtils.substringAfter(paramPair, "="));
                    if (paramName != null && paramValue != null)
                        params.computeIfAbsent(paramName, k -> new ArrayList<>()).add(paramValue);
                }
            }
        } else if (paramData instanceof Map) {
            params = (Map<String, List<String>>) paramData;
        } else {
            params = new HashMap<String, List<String>>();
        }

        var reason = StringUtils.trimToNull((String)data.get("reason"));
        if (reason == null)
            throw new NotAcceptableException("Reason is required");
            
        var build = jobService.submit(user, project, ObjectId.fromString(commitHash), jobName, 
            params, refName, null, null, reason);
        if (build.isFinished())
            jobService.resubmit(user, build, reason);

        var buildMap = getBuildMap(project, build);
        buildMap.put("id", build.getId());
        return buildMap;
    }

    @Path("/get-clone-roots")
    @GET
    public Map<String, String> getCloneUrl() {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var cloneRoots = new HashMap<String, String>();
        var systemSetting = settingService.getSystemSetting();
        var serverUrl = systemSetting.getServerUrl();        
        cloneRoots.put("http", serverUrl);
        if (serverConfig.getSshPort() != 0) {
            var sshRootUrl = systemSetting.getSshRootUrl();
            if (sshRootUrl == null)
                sshRootUrl = SystemSetting.deriveSshRootUrl(serverUrl);
            cloneRoots.put("ssh", sshRootUrl);    
        }
        return cloneRoots;
    }

    @Path("/check-build-spec")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkBuildSpec(
                @QueryParam("project") @NotNull String projectPath, 
                @NotNull String buildSpecString) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var project = getProject(projectPath);
		Project.push(project);
        String schemaNotice = "\n\n**NOTE**: AI assistant may call the getBuildSpecSchema tool to know exact syntax of build spec";
		try {
            var buildSpec = BuildSpec.parse(buildSpecString.getBytes(StandardCharsets.UTF_8));
            List<String> validationErrors = new ArrayList<>();
            for (var violation : validator.validate(buildSpec)) {
                String message = String.format("Error validating build spec (project: %s, location: %s, message: %s)",
                        project.getPath(), violation.getPropertyPath(), violation.getMessage());
                validationErrors.add(message);
            }
            if (validationErrors.isEmpty()) {
                return Response.ok(VersionedYamlDoc.fromBean(buildSpec).toYaml()).build();
            } else {
                return Response.status(NOT_ACCEPTABLE).entity(Joiner.on("\n").join(validationErrors) + schemaNotice).build();
            }
        } catch (Exception e) {
            return Response.status(NOT_ACCEPTABLE).entity(Throwables.getStackTraceAsString(e) + schemaNotice).build();
		} finally {
			Project.pop();
		}
    }

    private Build getBuild(Project currentProject, String referenceString) {
        var buildReference = BuildReference.of(referenceString, currentProject);
        var build = buildService.find(buildReference.getProject(), buildReference.getNumber());
        if (build != null) {
            if (!SecurityUtils.canAccessBuild(build))
                throw new UnauthorizedException("No permission to access build: " + referenceString);
            return build;
        } else {
            throw new NotFoundException("Build not found: " + referenceString);
        }
    }
    
    @Path("/query-packs")
    @GET
    public List<Map<String, Object>> queryPacks(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("project") String projectPath, 
                @QueryParam("query") String query, 
                @QueryParam("offset") int offset, 
                @QueryParam("count") int count) {
        var subject = SecurityUtils.getSubject();
        if (SecurityUtils.getUser(subject) == null)
            throw new UnauthenticatedException();

        var projectInfo = getProjectInfo(projectPath, currentProjectPath);

        if (count > RestConstants.MAX_PAGE_SIZE)
            throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

        EntityQuery<Pack> parsedQuery;
        if (query != null) {
            parsedQuery = PackQuery.parse(projectInfo.project, query, true);
        } else {
            parsedQuery = new PackQuery();
        }

        var packs = new ArrayList<Map<String, Object>>();
        for (var pack : packService.query(subject, projectInfo.project, parsedQuery, false, offset, count)) {
            var packMap = getPackMap(projectInfo.currentProject, pack);
            packMap.put("link", urlService.urlFor(pack, true));
            packs.add(packMap);
        }
        return packs;
    }

    private Map<String, Object> getPackMap(Project currentProject, Pack pack) {
        var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        var packMap = objectMapper.convertValue(pack, typeReference);
        packMap.remove("id");
        packMap.remove("userId");
        packMap.put("user", pack.getUser().getName());
        packMap.remove("buildId");
        if (pack.getBuild() != null)
            packMap.put("build", pack.getBuild().getReference().toString(currentProject));
        packMap.put("project", pack.getProject().getPath());
        
        return packMap;
    }

    private void normalizePullRequestData(Map<String, Serializable> data) {
        for (var entry : data.entrySet()) {
            if (entry.getValue() instanceof String)
                entry.setValue(StringUtils.trimToNull((String) entry.getValue()));
        }
    }    

    private static class ProjectInfo {
        
        Project project;

        Project currentProject;
    }

 }