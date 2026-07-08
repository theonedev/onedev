package io.onedev.server.ai;

import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ServerConfig;
import io.onedev.server.SubscriptionService;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblemContribution;
import io.onedev.server.codequality.ContainerTarget;
import io.onedev.server.codequality.GeneralTarget;
import io.onedev.server.data.migration.VersionedYamlDoc;
import io.onedev.server.entityreference.BuildReference;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.entityreference.PullRequestReference;
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
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.model.support.code.ConventionalCommitChecker;
import io.onedev.server.model.support.code.RegexpCommitChecker;
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
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.resource.support.RestConstants;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.service.IssueLinkService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueWorkService;
import io.onedev.server.service.IterationService;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestAssignmentService;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.PullRequestLabelService;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UrlService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectScope;

@Api(internal = true)
@Path("/tod")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class TodResource {

    private static final Logger logger = LoggerFactory.getLogger(TodResource.class);

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
    private CodeCommentService codeCommentService;

    @Inject
    private BuildService buildService;

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

	@Api(description = "Get tod version range compatible with this server")
	@Path("/check-version")
	@GET
	public VersionInfo checkVersion() {
        var versionInfo = new VersionInfo();
        versionInfo.serverVersion = OneDev.getInstance().getVersion();
        versionInfo.minRequiredTodVersion = "4.1.0";
        return versionInfo;
	}

    private String getParamName(String fieldName) {
        return fieldName.replace(" ", "-");
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

    private ProjectContext getProjectContext(String projectPath, String currentProjectPath) {
        if (projectPath == null) 
            projectPath = currentProjectPath;

        var projectContext = new ProjectContext();
        projectContext.project = getProject(projectPath);
        projectContext.currentProject = getProject(currentProjectPath);
        
        return projectContext;
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

    @Api(description = "Get commit message requirement")
    @Path("/get-commit-message-requirement")
    @GET
    @Nullable
    public String getCommitMessageRequirement(
                @QueryParam("project") @NotNull String projectPath, 
                @QueryParam("branch") @NotNull String branch) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var project = getProject(projectPath);
        if (!SecurityUtils.canWriteCode(project))
            throw new UnauthorizedException();
            
        return getCommitMessageRequirement(user, project, branch);
    }

    @Nullable
    private String getCommitMessageRequirement(User user, Project project, String branch) {            
        var requirementBuilder = new StringBuilder();
        var branchProtection = project.getBranchProtection(branch, user);
        if (branchProtection.getCommitMessageChecker() instanceof ConventionalCommitChecker checker) {
            requirementBuilder.append("Commit messages should use Conventional Commits format: ")
                    .append("<type>[optional (scope)][!]: <description>. Git revert messages are also allowed.");
            if (!checker.getCommitTypes().isEmpty()) {
                requirementBuilder.append("\nAllowed commit types: ")
                        .append(String.join(", ", checker.getCommitTypes()));
            }
            if (!checker.getCommitScopes().isEmpty()) {
                requirementBuilder.append("\nAllowed commit scopes: ")
                        .append(String.join(", ", checker.getCommitScopes()));
            }
            requirementBuilder.append("\nUse exactly one blank line between subject and body/footer.");
            if (checker.isCheckCommitMessageFooter()) {
                requirementBuilder.append("\nA footer is required");
                if (!checker.getCommitTypesForFooterCheck().isEmpty()) {
                    requirementBuilder.append(" for commit types: ")
                            .append(String.join(", ", checker.getCommitTypesForFooterCheck()));
                }
                requirementBuilder.append(". The footer should be the last line, preceded by exactly one blank line, ")
                        .append("and match Java regex: ")
                        .append(checker.getCommitMessageFooterPattern());
            }
        } else if (branchProtection.getCommitMessageChecker() instanceof RegexpCommitChecker checker) {
            requirementBuilder.append("Commit messages should match Java regex: ")
                    .append(checker.getPattern());
            if (checker.getExplanation() != null)
                requirementBuilder.append("\nExplanation: ").append(checker.getExplanation());
        }

        if (branchProtection.getMaxCommitMessageLineLength() != null) {
            if (requirementBuilder.length() != 0)
                requirementBuilder.append('\n');
            requirementBuilder.append("Each line must not exceed ")
                    .append(branchProtection.getMaxCommitMessageLineLength())
                    .append(" characters");
        }

        var fixSuggestion = settingService.getIssueSetting().getCommitMessageFixSetting().getFixSuggestion();
        if (requirementBuilder.length() != 0)
            requirementBuilder.append("\n\n");
        requirementBuilder.append(fixSuggestion);

        if (requirementBuilder.length() != 0)
            return requirementBuilder.toString();
        else
            return null;
    }

    @Path("/get-issue-query-description")
    @GET
    public String getIssueQueryDescription() {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();
        return escapeHtml5(QueryDescriptions.getIssueQueryDescription());
    }

    @Path("/get-build-query-description")
    @GET
    public String getBuildQueryDescription() {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();
        return escapeHtml5(QueryDescriptions.getBuildQueryDescription());
    }

    @Path("/get-pull-request-query-description")
    @GET
    public String getPullRequestQueryDescription() {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();
        return escapeHtml5(QueryDescriptions.getPullRequestQueryDescription());
    }

    @Path("/get-valid-issue-fields")
    @GET
    public Map<String, Object> getValidIssueFields() {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();
        var issueFields = new HashMap<String, Object>();
        for (var field: settingService.getIssueSetting().getFieldSpecs()) {
            var paramName = getParamName(field.getName());
            var fieldProperties = getFieldProperties(field);
            issueFields.put(paramName, fieldProperties);
        }
        return issueFields;
    }

    @Path("/get-valid-issue-links")
    @GET
    public List<String> getValidIssueLinks() {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();
        var linkNames = new ArrayList<String>();
        for (var linkSpec: linkSpecService.query()) {
            linkNames.add(linkSpec.getName());
            if (linkSpec.getOpposite() != null)
                linkNames.add(linkSpec.getOpposite().getName());
        }
        return linkNames;
    }

    @Path("/get-valid-labels")
    @GET
    public List<String> getValidLabels() {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();
        var labelNames = new ArrayList<String>();
        for (var labelSpec: labelSpecService.query()) 
            labelNames.add(labelSpec.getName());
        return labelNames;
    }

    @Path("/get-login-name")
    @GET
    public String getLoginName(@QueryParam("userName") String userName) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        User user;                
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

        var projectContext = getProjectContext(projectPath, currentProjectPath);

        if (count > RestConstants.MAX_PAGE_SIZE)
            throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

        EntityQuery<Issue> parsedQuery;
        if (query != null) {
            var option = new IssueQueryParseOption();
            option.withCurrentUserCriteria(true);
            try {
                parsedQuery = IssueQuery.parse(projectContext.project, query, option, true);
            } catch (ParseCancellationException e) {
                logger.error("Error parsing query", e);
                throw new NotAcceptableException("Invalid issue query, check server log for details");
            }
        } else {
            parsedQuery = new IssueQuery(null, new ArrayList<>());
        }

        var summaries = new ArrayList<Map<String, Object>>();
        for (var issue : issueService.query(subject, new ProjectScope(projectContext.project, true, false), parsedQuery, true, offset, count)) {
            var summary = IssueHelper.getSummary(projectContext.currentProject, issue);
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
    public Map<String, Object> getIssueDetail(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference, 
                @QueryParam("forWrite") Boolean forWrite) {
        var subject = SecurityUtils.getSubject();
        if (SecurityUtils.getUser(subject) == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var issue = getIssue(currentProject, issueReference);                

        if (forWrite != null && forWrite &&!SecurityUtils.canWriteCode(issue.getProject()))
            throw new UnauthorizedException("No permission to write code in issue project");

        return IssueHelper.getDetail(subject, currentProject, issue);
    }

    @Path("/get-project")
    @GET
    @Nullable
    public Map<String, Object> getProjectDetail(@QueryParam("project") @NotNull String projectPath) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        Map<String, Object> projectDetail;
        var project = getProject(projectPath);
        if (SecurityUtils.canManageProject(project)) {
            var typeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
            projectDetail = objectMapper.convertValue(project, typeReference);
            projectDetail.remove("lastActivityDateId");
            projectDetail.remove("pathLen");
        } else {
            projectDetail = new HashMap<String, Object>();
            projectDetail.put("id", project.getId());
            projectDetail.put("key", project.getKey());
            projectDetail.put("name", project.getName());
            projectDetail.put("path", project.getPath());
            projectDetail.put("description", project.getDescription());
            projectDetail.put("codeManagement", project.isCodeManagement());
            projectDetail.put("packManagement", project.isPackManagement());
            projectDetail.put("issueManagement", project.isIssueManagement());
            projectDetail.put("timeTracking", project.isTimeTracking());
        }
        projectDetail.put("effectiveDefaultPullRequestMergeStrategy", project.findDefaultPullRequestMergeStrategy());
        projectDetail.put("defaultBranch", project.getDefaultBranch());
        projectDetail.put("link", urlService.urlFor(project, true));
        return projectDetail;
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
    public Map<String, Object> addIssueComment(
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

        var commentMap = new HashMap<String, Object>();
        commentMap.put("content", comment.getContent());
        commentMap.put("user", comment.getUser().getName());
        commentMap.put("date", comment.getDate());
        return commentMap;
    }

    @Path("/create-issue")
    @POST
    public Map<String, Object> createIssue(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("project") String projectPath, 
                @NotNull @Valid Map<String, Serializable> data) {
        var subject = SecurityUtils.getSubject();
        if (SecurityUtils.getUser(subject) == null)
            throw new UnauthenticatedException();

        var projectContext = getProjectContext(projectPath, currentProjectPath);

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
            if (!projectContext.project.isTimeTracking())
                throw new NotAcceptableException("Time tracking needs to be enabled for the project");
            if (!SecurityUtils.canScheduleIssues(projectContext.project))
                throw new UnauthorizedException("Issue schedule permission required to set own estimated time");
            issue.setOwnEstimatedTime(ownEstimatedTime*60);
        }

        @SuppressWarnings("unchecked")
        List<String> iterationNames = (List<String>) data.remove("iterations");
        if (iterationNames != null) {
            if (!SecurityUtils.canScheduleIssues(projectContext.project))
                throw new UnauthorizedException("Issue schedule permission required to set iterations");
            for (var iterationName : iterationNames) {
                var iteration = iterationService.findInHierarchy(projectContext.project, iterationName);
                if (iteration == null)
                    throw new NotFoundException("Iteration '" + iterationName + "' not found");
                IssueSchedule schedule = new IssueSchedule();
                schedule.setIssue(issue);
                schedule.setIteration(iteration);
                issue.getSchedules().add(schedule);
            }
        }

        issue.setProject(projectContext.project);
        issue.setSubmitDate(new Date());
        issue.setSubmitter(SecurityUtils.getUser());
        issue.setState(issueSetting.getInitialStateSpec().getName());

        issue.setFieldValues(FieldUtils.getFieldValues(subject, issue.getProject(), data));

        issueService.open(issue);

        return IssueHelper.getDetail(subject, projectContext.currentProject, issue);
    }

    @Path("/edit-issue")
    @POST
    public Map<String, Object> editIssue(
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

        var title = (String) data.remove("title");
        if (title != null) { 
            if (!SecurityUtils.canModifyIssue(subject, issue))
                throw new UnauthorizedException("No permission to update issue title");
            issueChangeService.changeTitle(user, issue, title);
        }

        if (data.containsKey("description")) {
            if (!SecurityUtils.canModifyIssue(subject, issue))
                throw new UnauthorizedException("No permission to update issue description");
            issueChangeService.changeDescription(user, issue, (String) data.remove("description"));
        }

        var confidential = (Boolean) data.remove("confidential");
        if (confidential != null) {
            if (!SecurityUtils.canModifyIssue(subject, issue))
                throw new UnauthorizedException("No permission to update issue confidential");
            issueChangeService.changeConfidential(user, issue, confidential);
        }

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
            if (!SecurityUtils.canEditIssueFields(subject, issue)) 
                throw new UnauthorizedException("No permission to update issue fields");

            issueChangeService.changeFields(user, issue, FieldUtils.getFieldValues(subject, issue.getProject(), data));
        }

        return IssueHelper.getDetail(subject, currentProject, issue);
    }

    @Path("/change-issue-state")
    @POST
    public Map<String, Object> changeIssueState(
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
        ManualSpec transition = issue.getProject().getManualSpec(subject, issue, state);
        if (transition == null) {
            var message = MessageFormat.format(
                "No applicable manual transition spec found for current user (issue: {0}, from state: {1}, to state: {2})",
                issue.getReference().toString(), issue.getState(), state);
            throw new NotAcceptableException(message);
        }

        var fieldValues = FieldUtils.getFieldValues(subject, issue.getProject(), data);
        issueChangeService.changeState(user, issue, state, fieldValues, transition.getPromptFields(),
                transition.getRemoveFields(), comment);
        return IssueHelper.getDetail(subject, currentProject, issue);
    }

    @Path("/ensure-issue-branch") 
    @POST
    public String ensureIssueBranch(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String issueReference) {
        var subject = SecurityUtils.getSubject();

        var currentProject = getProject(currentProjectPath);
        var issue = getIssue(currentProject, issueReference);

        if (!issue.getProject().equals(currentProject))
            throw new NotAcceptableException("Issue " + issueReference + " is not in current project");

        return issueService.ensureBranch(subject, issue);
    }

    @Path("/link-issues")
    @GET
    public Map<String, Object> linkIssues(
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

        var linkMap = new HashMap<String, Object>();
        linkMap.put("source", link.getSource().getReference().toString(currentProject));
        linkMap.put("target", link.getTarget().getReference().toString(currentProject));
        linkMap.put("linkName", link.getSpec().getName());

        return linkMap;
    }

    @Path("/log-work")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> logWork(
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
        work.setNote(trimToNull(comment));
        issueWorkService.createOrUpdate(work);

        var workMap = new HashMap<String, Object>();
        workMap.put("minutes", spentHours * 60);
        workMap.put("note", comment);
        workMap.put("user", work.getUser().getName());
        workMap.put("date", work.getDate());
        return workMap;
    }

    private void normalizeIssueData(Map<String, Serializable> data) {
        for (var entry: data.entrySet()) {
            if (entry.getValue() instanceof String) 
                entry.setValue(trimToNull((String) entry.getValue()));
        }
        for (var field: settingService.getIssueSetting().getFieldSpecs()) {
            var paramName = getParamName(field.getName());
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

        var projectContext = getProjectContext(projectPath, currentProjectPath);

        if (!SecurityUtils.canReadCode(projectContext.project))
            throw new UnauthorizedException("Code read permission required to query pull requests");

        if (count > RestConstants.MAX_PAGE_SIZE)
            throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

        EntityQuery<PullRequest> parsedQuery;
        if (query != null) {
            try {
                parsedQuery = PullRequestQuery.parse(projectContext.project, query, true);
            } catch (ParseCancellationException e) {
                logger.error("Error parsing query", e);
                throw new NotAcceptableException("Invalid pull request query, check server log for details");
            }
        } else {
            parsedQuery = new PullRequestQuery();
        }

        var summaries = new ArrayList<Map<String, Object>>();
        for (var pullRequest : pullRequestService.query(subject, projectContext.project, parsedQuery, false, offset, count)) {
            var summary = PullRequestHelper.getSummary(projectContext.currentProject, pullRequest, false);
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

        var projectContext = getProjectContext(projectPath, currentProjectPath);

        if (count > RestConstants.MAX_PAGE_SIZE)
            throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

        EntityQuery<Build> parsedQuery;
        if (query != null) {
            try {
                parsedQuery = BuildQuery.parse(projectContext.project, query, true, true);
            } catch (ParseCancellationException e) {
                logger.error("Error parsing query", e);
                throw new NotAcceptableException("Invalid build query, check server log for details");
            }
        } else {
            parsedQuery = new BuildQuery();
        }

        var summaries = new ArrayList<Map<String, Object>>();
        for (var build : buildService.query(subject, projectContext.project, parsedQuery, false, offset, count)) {
            var summary = BuildHelper.getSummary(projectContext.currentProject, build);
            summary.put("link", urlService.urlFor(build, true));
            summaries.add(summary);
        }
        return summaries;
    }

    @Path("/get-build")
    @GET
    public Map<String, Object> getBuildDetail(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String buildReference) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var build = getBuild(currentProject, buildReference);
        return BuildHelper.getDetail(currentProject, build);
    }

    @Path("/get-build-code-problems")
    @GET
    public List<Map<String, Object>> getBuildCodeProblems(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String buildReference, 
                @QueryParam("reportName") @NotNull String reportName, 
                @QueryParam("severityLevel") @NotNull CodeProblem.Severity severityLevel) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var build = getBuild(currentProject, buildReference);

        if (!SecurityUtils.canAccessReport(build, reportName))
            throw new UnauthorizedException("No permission to access report: " + reportName);

        var problems = new ArrayList<Map<String, Object>>();
        for (var contribution: OneDev.getExtensions(CodeProblemContribution.class)) {
            for (var problem: contribution.getCodeProblems(build, null, reportName)) {
                if (problem.getSeverity().ordinal() <= severityLevel.ordinal()) {
                    var problemMap = new HashMap<String, Object>();
                    problemMap.put("severity", problem.getSeverity().name());
                    problemMap.put("message", problem.getMessage());
                    if (problem.getTarget() instanceof BlobTarget blobTarget) {
                        problemMap.put("file", blobTarget.getGroupKey().getName());
                        if (blobTarget.getLocation() != null) {
                            problemMap.put("beginLine", blobTarget.getLocation().getFromRow() + 1);
                            problemMap.put("endLine", blobTarget.getLocation().getToRow() + 1);
                        }
                    } else if (problem.getTarget() instanceof ContainerTarget containerTarget) {
                        problemMap.put("target", containerTarget.getGroupKey().getName());
                        problemMap.put("platform", ((ContainerTarget.GroupKey) containerTarget.getGroupKey()).getPlatform());
                    } else if (problem.getTarget() instanceof GeneralTarget generalTarget) {
                        problemMap.put("target", generalTarget.getGroupKey().getName());
                    } else {
                        throw new ExplicitException("Unknown problem target type: " + problem.getTarget().getClass().getName());
                    }
                    problems.add(problemMap);
                }               
            }
        }
        return problems;
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
        if (foundBuild != null) 
            return BuildHelper.getDetail(currentProject, foundBuild);
        else 
            throw new NotFoundException("Previous successful similar build not found");
    }

    @Path("/get-pull-request")
    @GET
    public Map<String, Object> getPullRequestDetail(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference, 
                @QueryParam("forWrite") Boolean forWrite) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        if (forWrite != null && forWrite) {
            if (pullRequest.getSourceProject() == null)
                throw new NotAcceptableException("Pull request source project no longer exists");

            if (!SecurityUtils.canWriteCode(pullRequest.getSourceProject()))            
                throw new UnauthorizedException("No permission to write code in pull request source project");
        }

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

        return PullRequestHelper.getCodeComments(pullRequest);
    }

    @Path("/get-pull-request-builds")
    @GET
    public List<Map<String, Object>> getPullRequestBuilds(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference) {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);
        return PullRequestHelper.getBuilds(currentProject, pullRequest);
    }

    @Path("/get-pull-request-patch-info")
    @GET
    public Map<String, String> getPullRequestPatchInfo(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);

        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        var oldCommitId = ObjectId.fromString(pullRequest.getBaseCommitHash());
        var newCommitId = ObjectId.fromString(pullRequest.getLatestUpdate().getHeadCommitHash());
        var comparisonBase = pullRequestService.getComparisonBase(pullRequest, oldCommitId, newCommitId);

        var patchInfo = new HashMap<String, String>();
        patchInfo.put("projectId", pullRequest.getProject().getId().toString());
        patchInfo.put("oldCommitHash", comparisonBase.name());
        patchInfo.put("newCommitHash", newCommitId.name());        
        return patchInfo;
    }

    private MergeStrategy getMergeStrategy(Project project, @Nullable String mergeStrategyName) {
        if (mergeStrategyName != null) {
            try {
                return MergeStrategy.valueOf(mergeStrategyName);
            } catch (IllegalArgumentException e) {
                throw new NotAcceptableException("Invalid merge strategy: " + mergeStrategyName);
            }
        } else {
            return project.findDefaultPullRequestMergeStrategy();
        }
    }

    @Path("/create-pull-request")
    @POST
    public Map<String, Object> createPullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("targetProject") String targetProjectPath,
                @QueryParam("sourceProject") String sourceProjectPath,
                @NotNull Map<String, Serializable> data) {
        normalizePullRequestData(data);

        var targetBranch = (String) data.remove("targetBranch");

        var sourceBranch = (String) data.remove("sourceBranch");
        if (sourceBranch == null)
            throw new NotAcceptableException("Source branch is required");

        var createPullRequestEssentialInfo = getCreatePullRequestEssentialInfo(
            currentProjectPath, targetProjectPath, sourceProjectPath, 
            targetBranch, sourceBranch);

        var currentProject = createPullRequestEssentialInfo.currentProject;
        var target = createPullRequestEssentialInfo.target;
        var source = createPullRequestEssentialInfo.source;
        var submitter = createPullRequestEssentialInfo.submitter;

        var mergeStrategyName = (String) data.remove("mergeStrategy");            
        var mergeStrategy = getMergeStrategy(target.getProject(), mergeStrategyName);

        var request = pullRequestService.findOpen(target, source);
        if (request != null)
            return PullRequestHelper.getDetail(currentProject, request);        

        request = new PullRequest();
        request.setTarget(target);
        request.setSource(source);
        request.setSubmitter(submitter);
        request.setMergeStrategy(mergeStrategy);

        // Pre-populate baseCommitHash + initial update so title/description can
        // be generated from commits below; openNew(...) will keep them as-is.
        ObjectId baseCommitId = gitService.getMergeBase(
                target.getProject(), target.getObjectId(),
                source.getProject(), source.getObjectId());
        if (baseCommitId == null)
            throw new NotAcceptableException("No common base for source and target branches");
        request.setBaseCommitHash(baseCommitId.name());

        PullRequestUpdate update = new PullRequestUpdate();
        update.setRequest(request);
        update.setHeadCommitHash(source.getObjectName());
        update.setTargetHeadCommitHash(target.getObjectName());
        request.getUpdates().add(update);

        var title = (String) data.remove("title");
        if (title == null)
            throw new NotAcceptableException("Title is required");

        request.setTitle(title);

        var description = (String) data.remove("description");
        if (description != null)
            request.setDescription(description);

        @SuppressWarnings("unchecked")
        var reviewerNames = (List<String>) data.remove("reviewers");
        if (reviewerNames != null) {
            for (var reviewerName : reviewerNames) {
                User reviewer = userService.findByName(reviewerName);
                if (reviewer == null)
                    throw new NotFoundException("Reviewer not found: " + reviewerName);
                if (reviewer.equals(request.getSubmitter()))
                    throw new NotAcceptableException("Pull request submitter cannot be reviewer");

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
        }

        pullRequestService.open(request);

        return PullRequestHelper.getDetail(currentProject, request);        
    }

    @Api(description = "Get pull request title and description requirement")
    @Path("/get-pull-request-title-and-description-requirement")
    @GET
    @Nullable
    public String getPullRequestTitleAndDescriptionRequirement(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("targetProject") String targetProjectPath,
                @QueryParam("sourceProject") String sourceProjectPath,
                @QueryParam("targetBranch") String targetBranch,
                @QueryParam("sourceBranch") @NotNull String sourceBranch,
                @QueryParam("mergeStrategy") String mergeStrategyName) {
        var createPullRequestEssentialInfo = getCreatePullRequestEssentialInfo(
            currentProjectPath, targetProjectPath, sourceProjectPath, 
            targetBranch, sourceBranch);
        var target = createPullRequestEssentialInfo.target;
        var submitter = createPullRequestEssentialInfo.submitter;
        var mergeStrategy = getMergeStrategy(target.getProject(), mergeStrategyName);

        if (mergeStrategy == MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS) {
            var commitMessageRequirement = getCommitMessageRequirement(
                    submitter, target.getProject(), target.getBranch());
            if (commitMessageRequirement != null) {
                return String.format("""
                        This pull request will squash source branch commits into a single commit. \
                        And the single commit message will be constructed as:

                        <pull request title>
                        <blank line>
                        <pull request description, if any>

                        Write the title and description so the full commit message conforms to the \
                        commit message requirement below:

                        %s
                        """, commitMessageRequirement);
            }
        }
        return null;
    }

    @Api(description = "Get pull request commit message requirement")
    @Path("/get-pull-request-commit-message-requirement")
    @GET
    @Nullable
    public String getPullRequestCommitMessageRequirement(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("targetProject") String targetProjectPath,
                @QueryParam("sourceProject") String sourceProjectPath,
                @QueryParam("targetBranch") String targetBranch,
                @QueryParam("sourceBranch") @NotNull String sourceBranch) {
        var createPullRequestEssentialInfo = getCreatePullRequestEssentialInfo(
            currentProjectPath, targetProjectPath, sourceProjectPath, 
            targetBranch, sourceBranch);
        var target = createPullRequestEssentialInfo.target;
        var submitter = createPullRequestEssentialInfo.submitter;

        return getCommitMessageRequirement(submitter, target.getProject(), target.getBranch());
    }

    private CreatePullRequestEssentialInfo getCreatePullRequestEssentialInfo(
                String currentProjectPath, 
                @Nullable String targetProjectPath, 
                @Nullable String sourceProjectPath,
                @Nullable String targetBranch, 
                String sourceBranch) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();
        
        var currentProject = getProject(currentProjectPath);

        Project sourceProject;
        if (sourceProjectPath == null)
            sourceProject = currentProject;
        else
            sourceProject = getProject(sourceProjectPath);

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

        if (targetBranch == null)
            targetBranch = targetProject.getDefaultBranch();
        if (targetBranch == null)
            throw new NotAcceptableException("No code in target project: " + targetProject.getPath());

        var target = new ProjectAndBranch(targetProject, targetBranch);
        var source = new ProjectAndBranch(sourceProject, sourceBranch);

        var info = new CreatePullRequestEssentialInfo();
        info.currentProject = currentProject;
        info.target = target;
        info.source = source;
        info.submitter = user;

        return info;
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
    public Map<String, Object> editPullRequest(
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
            autoMerge.setCommitMessage(trimToNull((String) data.remove("autoMergeCommitMessage")));
            var errorMessage = request.checkMergeCommitMessage(user, autoMerge.getCommitMessage());
            if (errorMessage != null) 
                throw new NotAcceptableException("Error validating param auto merge commit message: " + errorMessage);

            pullRequestChangeService.changeAutoMerge(user, request, autoMerge);
        }
                    
        return PullRequestHelper.getDetail(currentProject, request);        
    }    

    @Path("/approve-pull-request")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> approvePullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath,
                @QueryParam("reference") @NotNull String pullRequestReference,
                String comment) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        pullRequestReviewService.review(user, pullRequest, true, trimToNull(comment));
        return PullRequestHelper.getDetail(currentProject, pullRequest);        
    }

    @Path("/request-changes-on-pull-request")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> requestChangesOnPullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath,
                @QueryParam("reference") @NotNull String pullRequestReference,
                String comment) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        pullRequestReviewService.review(user, pullRequest, false, trimToNull(comment));
        return PullRequestHelper.getDetail(currentProject, pullRequest);        
    }

    @Path("/merge-pull-request")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> mergePullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath,
                @QueryParam("reference") @NotNull String pullRequestReference,
                String commitMessage) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        if (!SecurityUtils.canWriteCode(user.asSubject(), pullRequest.getProject()))
            throw new UnauthorizedException();

        commitMessage = trimToNull(commitMessage);

        pullRequestService.merge(user, pullRequest, commitMessage);

        return PullRequestHelper.getDetail(currentProject, pullRequest);        
    }

    @Path("/discard-pull-request")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> discardPullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath,
                @QueryParam("reference") @NotNull String pullRequestReference,
                String comment) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        if (!SecurityUtils.canModifyPullRequest(pullRequest))
            throw new UnauthorizedException();

        pullRequestService.discard(user, pullRequest, trimToNull(comment));
        return PullRequestHelper.getDetail(currentProject, pullRequest);        
    }

    @Path("/reopen-pull-request")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> reopenPullRequest(
                @QueryParam("currentProject") @NotNull String currentProjectPath,
                @QueryParam("reference") @NotNull String pullRequestReference,
                String comment) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        if (!SecurityUtils.canModifyPullRequest(pullRequest))
            throw new UnauthorizedException();

        pullRequestService.reopen(user, pullRequest, trimToNull(comment));
        return PullRequestHelper.getDetail(currentProject, pullRequest);        
    }

    @Path("/delete-pull-request-source-branch")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> deletePullRequestSourceBranch(
                @QueryParam("currentProject") @NotNull String currentProjectPath,
                @QueryParam("reference") @NotNull String pullRequestReference,
                String comment) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        if (!SecurityUtils.canModifyPullRequest(pullRequest)
                || !SecurityUtils.canDeleteBranch(pullRequest.getSourceProject(), pullRequest.getSourceBranch())) {
            throw new UnauthorizedException();
        }

        pullRequestService.deleteSourceBranch(user, pullRequest, trimToNull(comment));
        return PullRequestHelper.getDetail(currentProject, pullRequest);        
    }

    @Path("/restore-pull-request-source-branch")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> restorePullRequestSourceBranch(
                @QueryParam("currentProject") @NotNull String currentProjectPath,
                @QueryParam("reference") @NotNull String pullRequestReference,
                String comment) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        if (!SecurityUtils.canModifyPullRequest(pullRequest)
                || !SecurityUtils.canWriteCode(pullRequest.getSourceProject())) {
            throw new UnauthorizedException();
        }

        pullRequestService.restoreSourceBranch(user, pullRequest, trimToNull(comment));
        return PullRequestHelper.getDetail(currentProject, pullRequest);        
    }

    @Path("/add-pull-request-comment")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> addPullRequestComment(
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

        var commentMap = new HashMap<String, Object>();
        commentMap.put("content", comment.getContent());
        commentMap.put("user", comment.getUser().getName());
        commentMap.put("date", comment.getDate());
        return commentMap;
    }

    @Path("/add-pull-request-code-comment")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> addPullRequestCodeComment(
                @QueryParam("currentProject") @NotNull String currentProjectPath, 
                @QueryParam("reference") @NotNull String pullRequestReference, 
                @QueryParam("filePath") @NotNull String filePath,
                @QueryParam("fromLineNumber") int fromLineNumber, // index starts from 1
                @QueryParam("toLineNumber") int toLineNumber, // index starts from 1
                @NotNull String commentContent) {
        var user = SecurityUtils.getUser();
        if (user == null)
            throw new UnauthenticatedException();

        var currentProject = getProject(currentProjectPath);
        var pullRequest = getPullRequest(currentProject, pullRequestReference);

        var comment = PullRequestHelper.addCodeComment(pullRequest, user, filePath, fromLineNumber, toLineNumber, commentContent);
        return CodeCommentHelper.getDetail(comment);
    }

    @Path("/add-code-comment-reply")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> addCodeCommentReply(
                @QueryParam("commentId") @NotNull Long commentId,
                @NotNull String replyContent) {
        var comment = codeCommentService.load(commentId);
        return CodeCommentHelper.addReply(SecurityUtils.getSubject(), comment, replyContent);
    }

    @Path("/resolve-code-comment")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> resolveCodeComment(@QueryParam("commentId") @NotNull Long commentId, String note) {
        var comment = codeCommentService.load(commentId);
        return CodeCommentHelper.changeStatus(SecurityUtils.getSubject(), comment, true, note);
    }

    @Path("/unresolve-code-comment")
    @Consumes(MediaType.TEXT_PLAIN)
    @POST
    public Map<String, Object> unresolveCodeComment(@QueryParam("commentId") @NotNull Long commentId, String note) {
        var comment = codeCommentService.load(commentId);
        return CodeCommentHelper.changeStatus(SecurityUtils.getSubject(), comment, false, note);
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

        var jobName = trimToNull((String)data.get("jobName"));
        if (jobName == null)
            throw new NotAcceptableException("Job name is required");

        if (!SecurityUtils.canRunJob(subject, project, jobName))		
            throw new UnauthorizedException();

        String refName;
        var branch = trimToNull((String)data.get("branch"));
        var tag = trimToNull((String)data.get("tag"));
        var commitHash = trimToNull((String)data.get("commitHash"));
        if (commitHash != null) {
            refName = trimToNull((String)data.get("refName"));
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
                    var paramName = trimToNull(StringUtils.substringBefore(paramPair, "="));
                    var paramValue = trimToNull(StringUtils.substringAfter(paramPair, "="));
                    if (paramName != null && paramValue != null)
                        params.computeIfAbsent(paramName, k -> new ArrayList<>()).add(paramValue);
                }
            }
        } else if (paramData instanceof Map) {
            params = (Map<String, List<String>>) paramData;
        } else {
            params = new HashMap<String, List<String>>();
        }

        var reason = trimToNull((String)data.get("reason"));
        if (reason == null)
            throw new NotAcceptableException("Reason is required");
            
        var build = jobService.submit(user, project, ObjectId.fromString(commitHash), jobName, 
            params, refName, null, null, reason);
        if (build.isFinished())
            jobService.resubmit(user, build, reason);

        var summary = BuildHelper.getSummary(project, build);
        summary.put("id", build.getId());
        return summary;
    }

    @Path("/get-clone-roots")
    @GET
    public Map<String, String> getCloneRoots() {
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
    
    private void normalizePullRequestData(Map<String, Serializable> data) {
        for (var entry : data.entrySet()) {
            if (entry.getValue() instanceof String)
                entry.setValue(trimToNull((String) entry.getValue()));
        }
    }    

    private static class ProjectContext {
        
        Project project;

        Project currentProject;
    }

    private static class VersionInfo {

        @SuppressWarnings("unused")
        String serverVersion;

        @SuppressWarnings("unused")
        String minRequiredTodVersion;

    }

    private static class CreatePullRequestEssentialInfo {

        Project currentProject;

        ProjectAndBranch target;

        ProjectAndBranch source;

        User submitter;

    }

}