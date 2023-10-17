package io.onedev.server.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.*;
import io.onedev.server.model.*;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.rest.support.RestConstants;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.web.page.help.ApiHelpUtils;
import io.onedev.server.web.page.help.ValueInfo;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.*;

@Api(order=2000, description="In most cases, issue resource is operated with issue id, which is different from issue number. "
		+ "To get issue id of a particular issue number, use the <a href='/~help/api/io.onedev.server.rest.IssueResource/queryBasicInfo'>Query Basic Info</a> operation with query for "
		+ "instance <code>&quot;Number&quot; is &quot;projectName#100&quot;</code>")
@Path("/issues")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueResource {

	private final SettingManager settingManager;
	
	private final IssueManager issueManager;
	
	private final IssueChangeManager issueChangeManager;
	
	private final MilestoneManager milestoneManager;
	
	private final ProjectManager projectManager;
	
	private final ObjectMapper objectMapper;
	
	@Inject
	public IssueResource(SettingManager settingManager, IssueManager issueManager, 
						 IssueChangeManager issueChangeManager, MilestoneManager milestoneManager, 
						 ProjectManager projectManager, ObjectMapper objectMapper) {
		this.settingManager = settingManager;
		this.issueManager = issueManager;
		this.issueChangeManager = issueChangeManager;
		this.milestoneManager = milestoneManager;
		this.projectManager = projectManager;
		this.objectMapper = objectMapper;
	}

	@Api(order=100)
	@Path("/{issueId}")
    @GET
    public Issue getBasicInfo(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue;
    }

	@Api(order=200, exampleProvider = "getFieldsExample")
	@Path("/{issueId}/fields")
    @GET
    public Map<String, Serializable> getFields(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
		
		Map<String, Serializable> fields = new HashMap<>();
		for (var field: issue.getFieldInputs().values()) {
			if (field.getValues().isEmpty())
				fields.put(field.getName(), null);
			else if (field.getValues().size() == 1)
				fields.put(field.getName(), field.getValues().iterator().next());
			else
				fields.put(field.getName(), (Serializable) field.getValues());
		}
		return fields;
    }
	
	@Api(order=300)
	@Path("/{issueId}/changes")
    @GET
    public Collection<IssueChange> getChanges(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getChanges();
    }
	
	@Api(order=400)
	@Path("/{issueId}/comments")
    @GET
    public Collection<IssueComment> getComments(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getComments();
    }

	@Api(order=425)
	@Path("/{issueId}/works")
	@GET
	public Collection<IssueWork> getWorks(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
		if (!SecurityUtils.canAccess(issue))
			throw new UnauthorizedException();
		return issue.getWorks();
	}
	
	@Api(order=450)
	@Path("/{issueId}/milestones")
    @GET
    public Collection<Milestone> getMilestones(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getMilestones();
    }
	
	@Api(order=500)
	@Path("/{issueId}/votes")
    @GET
    public Collection<IssueVote> getVotes(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getVotes();
    }
	
	@Api(order=600)
	@Path("/{issueId}/watches")
    @GET
    public Collection<IssueWatch> getWatches(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canAccess(issue)) 
			throw new UnauthorizedException();
    	return issue.getWatches();
    }
	
	@Api(order=700)
	@Path("/{issueId}/pulls")
    @GET
    public Collection<PullRequest> getPullRequests(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canReadCode(issue.getProject())) 
			throw new UnauthorizedException();
    	return issue.getPullRequests();
    }
	
	@Api(order=800)
	@Path("/{issueId}/commits")
    @GET
    public List<FixCommit> getCommits(@PathParam("issueId") Long issueId) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canReadCode(issue.getProject())) 
			throw new UnauthorizedException();
    	
    	List<FixCommit> issueCommits = new ArrayList<>();
    	for (ProjectScopedCommit commit: issue.getCommits()) {
    		FixCommit issueCommit = new FixCommit();
    		issueCommit.setProjectId(commit.getProject().getId());
    		issueCommit.setCommitHash(commit.getCommitId().name());
    		issueCommits.add(issueCommit);
    	}
    	return issueCommits;
    }
	
	@Api(order=900, exampleProvider = "getIssuesExample")
	@GET
    public List<Map<String, Object>> query(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as query box in <a href='/~issues'>issues page</a>", example="\"Number\" is \"projectName#100\"") String query,
			@QueryParam("withFields") @Api(description = "Whether or not to include issue fields. Default to false", example="true") Boolean withFields, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		
    	if (!SecurityUtils.isAdministrator() && count > RestConstants.MAX_PAGE_SIZE)
    		throw new InvalidParamException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

    	IssueQuery parsedQuery;
		try {
			IssueQueryParseOption option = new IssueQueryParseOption().withCurrentUserCriteria(true);
			parsedQuery = IssueQuery.parse(null, query, option, true);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}

		var typeReference = new TypeReference<Map<String, Object>>() {};		
		var issues = new ArrayList<Map<String, Object>>();
		for (var issue: issueManager.query(null, parsedQuery, false, offset, count)) {
			var issueMap = objectMapper.convertValue(issue, typeReference);
			if (withFields != null && withFields)
				issueMap.put("fields", issue.getFields());
			issues.add(issueMap);
		}
		
		return issues;
    }
	
	private static List<Map<String, Object>> getIssuesExample() {
		var issues = new ArrayList<Map<String, Object>>();
		var issue = ApiHelpUtils.getExampleValue(Issue.class, ValueInfo.Origin.RESPONSE_BODY);
		issues.add(OneDev.getInstance(ObjectMapper.class).convertValue(issue, new TypeReference<Map<String, Object>>() {}));
		return issues;
	}
	
	@Api(order=1000)
    @POST
    public Long create(@NotNull @Valid IssueOpenData data) {
    	User user = SecurityUtils.getUser();
    	
    	Project project = projectManager.load(data.getProjectId());
    	if (!SecurityUtils.canAccess(project))
			throw new UnauthorizedException();

		if (!data.getMilestoneIds().isEmpty() && !SecurityUtils.canScheduleIssues(project))
			throw new UnauthorizedException("No permission to schedule issue");

		var issueSetting = settingManager.getIssueSetting();
		
		Issue issue = new Issue();
		issue.setTitle(data.getTitle());
		issue.setDescription(data.getDescription());
		issue.setConfidential(data.isConfidential());
		issue.setProject(project);
		issue.setSubmitDate(new Date());
		issue.setSubmitter(user);
		issue.setState(issueSetting.getInitialStateSpec().getName());
		issue.setOwnEstimatedTime(data.getOwnEstimatedTime());

		for (Long milestoneId : data.getMilestoneIds()) {
			Milestone milestone = milestoneManager.load(milestoneId);
			if (!milestone.getProject().isSelfOrAncestorOf(project))
				throw new BadRequestException("Milestone is not defined in project hierarchy of the issue");
			IssueSchedule schedule = new IssueSchedule();
			schedule.setIssue(issue);
			schedule.setMilestone(milestone);
			issue.getSchedules().add(schedule);
		}

		issue.setFieldValues(getFieldObjs(issue, data.fields));		
		issueManager.open(issue);
		return issue.getId();
    }
	
	@Api(order=1100)
	@Path("/{issueId}/title")
    @POST
    public Response setTitle(@PathParam("issueId") Long issueId, @NotEmpty String title) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canModify(issue))
			throw new UnauthorizedException();
		issueChangeManager.changeTitle(issue, title);
		return Response.ok().build();
    }
	
	@Api(order=1200)
	@Path("/{issueId}/description")
    @POST
    public Response setDescription(@PathParam("issueId") Long issueId, String description) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canModify(issue))
			throw new UnauthorizedException();
		issueChangeManager.changeDescription(issue, description);
		return Response.ok().build();
    }
	
	@Api(order=1250)
	@Path("/{issueId}/confidential")
    @POST
    public Response setConfidential(@PathParam("issueId") Long issueId, boolean confidential) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canModify(issue))
			throw new UnauthorizedException();
		issueChangeManager.changeConfidential(issue, confidential);
		return Response.ok().build();
    }

	@Api(order=1275)
	@Path("/{issueId}/own-estimated-time")
	@POST
	public Response setOwnEstimatedTime(@PathParam("issueId") Long issueId, int hours) {
		Issue issue = issueManager.load(issueId);
		if (!SecurityUtils.canScheduleIssues(issue.getProject()))
			throw new UnauthorizedException();
		issueChangeManager.changeOwnEstimatedTime(issue, hours);
		return Response.ok().build();
	}
	
	@Api(order=1300, description="Schedule issue into specified milestones with list of milestone id")
	@Path("/{issueId}/milestones")
    @POST
    public Response setMilestones(@PathParam("issueId") Long issueId, List<Long> milestoneIds) {
		Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canScheduleIssues(issue.getProject()))
			throw new UnauthorizedException("No permission to schedule issue");
		
    	Collection<Milestone> milestones = new HashSet<>();
    	for (Long milestoneId: milestoneIds) {
    		Milestone milestone = milestoneManager.load(milestoneId);
	    	if (!milestone.getProject().isSelfOrAncestorOf(issue.getProject()))
	    		throw new InvalidParamException("Milestone is not defined in project hierarchy of the issue");
	    	milestones.add(milestone);
    	}
    	
    	issueChangeManager.changeMilestones(issue, milestones);
    	
		return Response.ok().build();
    }
	
	@Api(order=1400)
	@Path("/{issueId}/fields")
    @POST
    public Response setFields(@PathParam("issueId") Long issueId, @NotNull @Api(exampleProvider = "getFieldsExample") Map<String, Serializable> fields) {
		Issue issue = issueManager.load(issueId);
		var issueSetting = settingManager.getIssueSetting();
		String initialState = issueSetting.getInitialStateSpec().getName();
		
    	if (!SecurityUtils.canManageIssues(issue.getProject()) 
				&& !(issue.getSubmitter().equals(SecurityUtils.getUser()) && issue.getState().equals(initialState))) {
			throw new UnauthorizedException();
		}

		issueChangeManager.changeFields(issue, getFieldObjs(issue, fields));
		return Response.ok().build();
    }

	private static Map<String, Serializable> getFieldsExample() {
		var example = new LinkedHashMap<String, Serializable>();
		example.put("field1", "value1");
		example.put("field2", new String[]{"value1", "value2"});
		return example;
	}
	
	@Api(order=1500)
	@Path("/{issueId}/state-transitions")
    @POST
    public Response transitState(@PathParam("issueId") Long issueId, @NotNull @Valid StateTransitionData data) {
		Issue issue = issueManager.load(issueId);
		var applicableTriggers = new ArrayList<PressButtonTrigger>();
		for (var transitionSpec: settingManager.getIssueSetting().getTransitionSpecs()) {
			if (transitionSpec.getFromStates().contains(issue.getState()) 
					&& transitionSpec.getToState().equals(data.getState())
					&& transitionSpec.getTrigger() instanceof PressButtonTrigger) {
				PressButtonTrigger pressButtonTrigger = (PressButtonTrigger) transitionSpec.getTrigger();
				applicableTriggers.add(pressButtonTrigger);
			}
		}
		if (applicableTriggers.isEmpty()) 
			throw new BadRequestException("No applicable transition spec for: " + issue.getState() + "->" + data.getState());
		if (applicableTriggers.stream().noneMatch(it->it.isAuthorized(issue)))
			throw new UnauthorizedException();
    	
		issueChangeManager.changeState(issue, data.getState(), getFieldObjs(issue, data.getFields()), 
				data.getRemoveFields(), data.getComment());
		return Response.ok().build();
    }
	
	@Api(order=1600)
	@Path("/{issueId}")
    @DELETE
    public Response delete(@PathParam("issueId") Long issueId) {
    	Issue issue = issueManager.load(issueId);
    	if (!SecurityUtils.canManageIssues(issue.getProject()))
			throw new UnauthorizedException();
    	issueManager.delete(issue);
    	return Response.ok().build();
    }

	private Map<String, Object> getFieldObjs(Issue issue, Map<String, Serializable> fields) {
		var issueSetting = settingManager.getIssueSetting();
		Map<String, Object> fieldObjs = new HashMap<>();
		for (Map.Entry<String, Serializable> entry: fields.entrySet()) {
			var fieldName = entry.getKey();
			var fieldSpec = issueSetting.getFieldSpec(fieldName);
			if (fieldSpec == null)
				throw new BadRequestException("Undefined field: " + fieldName);
			if (!SecurityUtils.canEditIssueField(issue.getProject(), fieldName))
				throw new UnauthorizedException("No permission to edit field: " + fieldName);

			List<String> values = new ArrayList<>();
			if (entry.getValue() instanceof String) {
				values.add((String) entry.getValue());
			} else if (entry.getValue() instanceof Collection) {
				values.addAll((Collection<String>) entry.getValue());
			}
			fieldObjs.put(entry.getKey(), fieldSpec.convertToObject(values));
		}
		return fieldObjs;
	}
	
	@EntityCreate(Issue.class)
	public static class IssueOpenData implements Serializable {

		private static final long serialVersionUID = 1L;

		@Api(order=100)
		private Long projectId;
		
		@Api(order=200)
		private String title;
		
		@Api(order=300)
		private String description;
		
		@Api(order=400)
		private boolean confidential;
		
		@Api(order=450, description = "Own estimated time in hours")
		private int ownEstimatedTime;
		
		@Api(order=500)
		private List<Long> milestoneIds = new ArrayList<>();
		
		@Api(order=600, exampleProvider = "getFieldsExample")
		private Map<String, Serializable> fields = new HashMap<>();

		@NotNull
		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		@NotNull
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean isConfidential() {
			return confidential;
		}

		public void setConfidential(boolean confidential) {
			this.confidential = confidential;
		}

		public int getOwnEstimatedTime() {
			return ownEstimatedTime;
		}

		public void setOwnEstimatedTime(int ownEstimatedTime) {
			this.ownEstimatedTime = ownEstimatedTime;
		}
		
		public List<Long> getMilestoneIds() {
			return milestoneIds;
		}

		public void setMilestoneIds(List<Long> milestoneIds) {
			this.milestoneIds = milestoneIds;
		}

		@NotNull
		public Map<String, Serializable> getFields() {
			return fields;
		}

		public void setFields(Map<String, Serializable> fields) {
			this.fields = fields;
		}
		
		private static Map<String, Serializable> getFieldsExample() {
			return IssueResource.getFieldsExample();
		}
	}
	
	public static class StateTransitionData implements Serializable {

		private static final long serialVersionUID = 1L;

		@Api(order=100)
		private String state;
		
		@Api(order=200, exampleProvider = "getFieldsExample")
		private Map<String, Serializable> fields = new HashMap<>();
		
		@Api(order=300)
		private Collection<String> removeFields = new HashSet<>();
		
		@Api(order=400)
		private String comment;

		@NotEmpty
		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		@NotNull
		public Map<String, Serializable> getFields() {
			return fields;
		}

		public void setFields(Map<String, Serializable> fields) {
			this.fields = fields;
		}

		@NotNull
		public Collection<String> getRemoveFields() {
			return removeFields;
		}

		public void setRemoveFields(Collection<String> removeFields) {
			this.removeFields = removeFields;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
		
		private static Map<String, Serializable> getFieldsExample() {
			return IssueResource.getFieldsExample();
		}
		
	}

	public static class FixCommit implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private Long projectId;
		
		private String commitHash;

		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		public String getCommitHash() {
			return commitHash;
		}

		public void setCommitHash(String commitHash) {
			this.commitHash = commitHash;
		}
		
	}
	
}
