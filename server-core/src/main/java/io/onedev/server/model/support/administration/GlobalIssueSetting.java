package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.shiro.subject.Subject;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.model.support.issue.CommitMessageFixPatterns;
import io.onedev.server.model.support.issue.ExternalIssueTransformers;
import io.onedev.server.model.support.issue.IssueTemplate;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.model.support.issue.TimeTrackingSetting;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.choicefield.defaultvalueprovider.DefaultValue;
import io.onedev.server.model.support.issue.field.spec.choicefield.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.model.support.issue.transitionspec.BranchUpdatedSpec;
import io.onedev.server.model.support.issue.transitionspec.IssueStateTransitedSpec;
import io.onedev.server.model.support.issue.transitionspec.ManualSpec;
import io.onedev.server.model.support.issue.transitionspec.PullRequestOpenedSpec;
import io.onedev.server.model.support.issue.transitionspec.TransitionSpec;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

@Editable
public class GlobalIssueSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String PROP_EXTERNAL_ISSUE_PATTERN = "externalIssuePattern";
	
	private List<StateSpec> stateSpecs = new ArrayList<>();
	
	private List<TransitionSpec> transitionSpecs = new ArrayList<>();
	
	private List<FieldSpec> fieldSpecs = new ArrayList<>();
	
	private List<BoardSpec> boardSpecs = new ArrayList<>();
	
	private TimeTrackingSetting timeTrackingSetting = new TimeTrackingSetting();
	
	private List<String> listFields = new ArrayList<>();
	
	private List<String> listLinks = new ArrayList<>();
	
	private List<NamedIssueQuery> namedQueries = new ArrayList<>();
	
	private List<IssueTemplate> issueTemplates = new ArrayList<>();
	
	private CommitMessageFixPatterns commitMessageFixPatterns;

	private ExternalIssueTransformers externalIssueTransformers;
	
	private boolean reconciled = true;
	
	public GlobalIssueSetting() {
		ChoiceField type = new ChoiceField();
		type.setName("Type");
		SpecifiedChoices specifiedChoices = new SpecifiedChoices();

		List<Choice> choices = new ArrayList<>(); 
		Choice newFeature = new Choice();
		newFeature.setValue("New Feature");
		newFeature.setColor("#1bc5bd");
		choices.add(newFeature);
		
		Choice improvement = new Choice();
		improvement.setValue("Improvement");
		improvement.setColor("#1bc5bd");
		choices.add(improvement);

		Choice bug = new Choice();
		bug.setValue("Bug");
		bug.setColor("#F64E60");
		choices.add(bug);
		
		Choice task = new Choice();
		task.setValue("Task");
		task.setColor("#8950FC");
		choices.add(task);

		Choice supportRequest = new Choice();
		supportRequest.setValue("Support Request");
		supportRequest.setColor("#8950FC");
		choices.add(supportRequest);
		
		specifiedChoices.setChoices(choices);
		type.setChoiceProvider(specifiedChoices);
		
		SpecifiedDefaultValue specifiedDefaultValue = new SpecifiedDefaultValue();
		DefaultValue defaultValue = new DefaultValue();
		defaultValue.setValue("New Feature");
		specifiedDefaultValue.getDefaultValues().add(defaultValue);
		
		type.setDefaultValueProvider(specifiedDefaultValue);
		
		fieldSpecs.add(type);
		
		ChoiceField priority = new ChoiceField();
		priority.setName("Priority");
		specifiedChoices = new SpecifiedChoices();

		choices = new ArrayList<>(); 
		
		Choice minor = new Choice();
		minor.setValue("Minor");
		minor.setColor("#E4E6EF");
		choices.add(minor);

		Choice normal = new Choice();
		normal.setValue("Normal");
		normal.setColor("#3699FF");
		choices.add(normal);

		Choice major = new Choice();
		major.setValue("Major");
		major.setColor("#FFA800");
		choices.add(major);
		
		Choice critical = new Choice();
		critical.setValue("Critical");
		critical.setColor("#F64E60");
		choices.add(critical);
		
		specifiedChoices.setChoices(choices);
		priority.setChoiceProvider(specifiedChoices);
		
		specifiedDefaultValue = new SpecifiedDefaultValue();
		defaultValue = new DefaultValue();
		defaultValue.setValue("Normal");
		specifiedDefaultValue.getDefaultValues().add(defaultValue);
		priority.setDefaultValueProvider(specifiedDefaultValue);
		
		fieldSpecs.add(priority);

		UserChoiceField assignees = new UserChoiceField();
		assignees.setAllowMultiple(true);
		assignees.setAllowEmpty(true);
		assignees.setNameOfEmptyValue("Not assigned");
		assignees.setName("Assignees");
		fieldSpecs.add(assignees);
		
		StateSpec open = new StateSpec();
		open.setName("Open");
		open.setColor("#2095F2");		
		stateSpecs.add(open);

		StateSpec inProgress = new StateSpec();
		inProgress.setName("In Progress");
		inProgress.setColor("#FFA700");		
		inProgress.setDescription(
			"The AI assistant should switch the working directory to the branch corresponding to the issue by running below command:\n\n" + 
			"git fetch <onedev remote> main && git checkout -b <branch-name> <onedev remote>/main && git push -u <onedev remote> <branch-name>\n\n" +
			"Here <branch name> should be derived from user instruction or issue title, and <onedev remote> should be retrieved by calling tool getCurrentRemote"
		);
		stateSpecs.add(inProgress);

		StateSpec inReview = new StateSpec();
		inReview.setName("In Review");
		inReview.setColor("#9C26B0");		
		inReview.setDescription(
			"AI assistant should do the following:\n\n" +
			"1. Add files to git index, and create conventional commit for current work if there are uncommitted changes. Subject line of the commit message should include the issue number at the end in form of (<issue reference>)\n" +
			"2. Push the branch to remote by running \"git push <onedev remote>\"\n" +
			"3. Create a pull request for current branch, with other params derived from current issue, and user instruction\n\n" +
			"Here <issue reference> should be replaced with reference of the issue whose state is being changed, and <onedev remote> should be retrieved via tool getCurrentRemote"
		);
		stateSpecs.add(inReview);

		StateSpec closed = new StateSpec();
		closed.setColor("#1BC5BD");
		closed.setName("Closed");		
		stateSpecs.add(closed);
		
		var branchUpdatedSpec = new BranchUpdatedSpec();
		branchUpdatedSpec.setToState("Closed");
		branchUpdatedSpec.setBranches("main");
		branchUpdatedSpec.setIssueQuery("fixed in current commit");		
		transitionSpecs.add(branchUpdatedSpec);
		
		var pullRequestOpenedSpec = new PullRequestOpenedSpec();
		pullRequestOpenedSpec.setToState("In Review");
		pullRequestOpenedSpec.setBranches("main");
		pullRequestOpenedSpec.setIssueQuery("fixed in current pull request");		
		transitionSpecs.add(pullRequestOpenedSpec);
		
		var issueStateTransitedSpec = new IssueStateTransitedSpec();
		issueStateTransitedSpec.setToState("Open");
		issueStateTransitedSpec.setIssueQuery("any \"Sub Issues\" matching(current issue) and any \"Sub Issues\" matching(\"State\" is \"Open\")");		
		transitionSpecs.add(issueStateTransitedSpec);
		
		issueStateTransitedSpec = new IssueStateTransitedSpec();
		issueStateTransitedSpec.setToState("In Progress");
		issueStateTransitedSpec.setIssueQuery("any \"Sub Issues\" matching(current issue) and any \"Sub Issues\" matching(\"State\" is \"In Progress\") and all \"Sub Issues\" matching(\"State\" is after \"Open\")");		
		transitionSpecs.add(issueStateTransitedSpec);

		issueStateTransitedSpec = new IssueStateTransitedSpec();
		issueStateTransitedSpec.setToState("In Review");
		issueStateTransitedSpec.setIssueQuery("any \"Sub Issues\" matching(current issue) and any \"Sub Issues\" matching(\"State\" is \"In Review\") and all \"Sub Issues\" matching(\"State\" is after \"In Progress\")");		
		transitionSpecs.add(issueStateTransitedSpec);

		issueStateTransitedSpec = new IssueStateTransitedSpec();
		issueStateTransitedSpec.setToState("Closed");
		issueStateTransitedSpec.setIssueQuery("any \"Sub Issues\" matching(current issue) and all \"Sub Issues\" matching(\"State\" is \"Closed\")");		
		transitionSpecs.add(issueStateTransitedSpec);

		var manualSpec = new ManualSpec();
		manualSpec.setAuthorizedRoles(Lists.newArrayList("Code Writer", "<Issue Submitter>", "{Assignees}"));
		transitionSpecs.add(manualSpec);

		BoardSpec board = new BoardSpec();
		board.setName(Issue.NAME_STATE);
		board.setIdentifyField(Issue.NAME_STATE);
		board.setColumns(Lists.newArrayList("Open", "In Progress", "In Review", "Closed"));
		board.setDisplayFields(Lists.newArrayList(Issue.NAME_STATE, "Type", "Priority", "Assignees", IssueSchedule.NAME_ITERATION));
		board.setDisplayLinks(Lists.newArrayList("Sub Issues", "Parent Issue", "Related"));
		boardSpecs.add(board);
		
		listFields.add(Issue.NAME_STATE);
		listFields.add("Type");
		listFields.add("Priority");
		listFields.add("Assignees");
		listFields.add(IssueSchedule.NAME_ITERATION);
		
		listLinks.add("Sub Issues");
		listLinks.add("Parent Issue");
		listLinks.add("Related");
		
		namedQueries.add(new NamedIssueQuery("Open", "\"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("In Progress", "\"State\" is \"In Progress\""));
		namedQueries.add(new NamedIssueQuery("In Review", "\"State\" is \"In Review\""));
		namedQueries.add(new NamedIssueQuery("Closed", "\"State\" is \"Closed\""));
		namedQueries.add(new NamedIssueQuery("Assigned to me & open", "\"Assignees\" is me and \"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Submitted by me & open", "submitted by me and \"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Assigned to me", "\"Assignees\" is me"));
		namedQueries.add(new NamedIssueQuery("Submitted by me", "submitted by me"));
		namedQueries.add(new NamedIssueQuery("Submitted recently", "\"Submit Date\" is since \"last week\""));
		namedQueries.add(new NamedIssueQuery("Mentioned me", "mentioned me"));
		namedQueries.add(new NamedIssueQuery("Has activity recently", "\"Last Activity Date\" is since \"last week\""));
		namedQueries.add(new NamedIssueQuery("Open & Critical", "\"State\" is \"Open\" and \"Priority\" is \"Critical\""));
		namedQueries.add(new NamedIssueQuery("Open & Unassigned", "\"State\" is \"Open\" and \"Assignees\" is empty"));
		namedQueries.add(new NamedIssueQuery("Open & Unscheduled", "\"State\" is \"Open\" and \"Iteration\" is empty"));
		namedQueries.add(new NamedIssueQuery("All", null));
		
		commitMessageFixPatterns = new CommitMessageFixPatterns();
		var entry = new CommitMessageFixPatterns.Entry();
		entry.setPrefix("(^|\\W)(fix|fixed|fixes|fixing|resolve|resolved|resolves|resolving|close|closed|closes|closing)[\\s:]+");
		entry.setSuffix("(?=$|\\W)");
		commitMessageFixPatterns.getEntries().add(entry);
		entry = new CommitMessageFixPatterns.Entry();
		entry.setPrefix("\\(\\s*");
		entry.setSuffix("\\s*\\)\\s*$");
		commitMessageFixPatterns.getEntries().add(entry);
		
		timeTrackingSetting.setAggregationLink("Sub Issues");
		
		externalIssueTransformers = new ExternalIssueTransformers();
	}
	
	public List<String> sortFieldNames(Collection<String> fieldNames) {
		List<String> sorted = new ArrayList<>(fieldNames);
		List<String> allFieldNames = getFieldNames();
		Collections.sort(sorted, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return allFieldNames.indexOf(o1) - allFieldNames.indexOf(o2);
			}
			
		});
		return sorted;
	}
	
	@NotNull
	@Valid
	public List<StateSpec> getStateSpecs() {
		return stateSpecs;
	}

	public void setStateSpecs(List<StateSpec> stateSpecs) {
		this.stateSpecs = stateSpecs;
	}

	@NotNull
	@Valid
	public List<TransitionSpec> getTransitionSpecs() {
		return transitionSpecs;
	}

	public void setTransitionSpecs(List<TransitionSpec> transitionSpecs) {
		this.transitionSpecs = transitionSpecs;
	}

	@Editable
	@NotNull
	@Valid
	public List<FieldSpec> getFieldSpecs() {
		return fieldSpecs;
	}

	public void setFieldSpecs(List<FieldSpec> fieldSpecs) {
		this.fieldSpecs = fieldSpecs;
	}

	public boolean isReconciled() {
		return reconciled;
	}

	public void setReconciled(boolean reconciled) {
		this.reconciled = reconciled;
	}

	public Map<String, FieldSpec> getFieldSpecMap(@Nullable Collection<String> fieldNames) {
		Map<String, FieldSpec> fieldSpecMap = new LinkedHashMap<>();
		for (FieldSpec fieldSpec: getFieldSpecs()) {
			if (fieldNames == null || fieldNames.contains(fieldSpec.getName()))
				fieldSpecMap.put(fieldSpec.getName(), fieldSpec);
		}
		return fieldSpecMap;
	}
	
	public Map<String, StateSpec> getStateSpecMap() {
		Map<String, StateSpec> stateSpecMap = new LinkedHashMap<>();
		for (StateSpec state: getStateSpecs())
			stateSpecMap.put(state.getName(), state);
		return stateSpecMap;
	}
	
	public List<String> getFieldNames() {
		return new ArrayList<>(getFieldSpecMap(null).keySet());
	}
	
	@Nullable
	public StateSpec getStateSpec(String stateName) {
		return getStateSpecMap().get(stateName);
	}

	@Nullable
	public FieldSpec getFieldSpec(String fieldName) {
		return getFieldSpecMap(null).get(fieldName);
	}
	
	public Collection<String> getUndefinedStates() {
		Collection<String> undefinedStates = new HashSet<>();
		for (TransitionSpec transition: getTransitionSpecs())
			undefinedStates.addAll(transition.getUndefinedStates());
		for (BoardSpec board: getBoardSpecs())
			undefinedStates.addAll(board.getUndefinedStates());
		for (IssueTemplate template: getIssueTemplates())
			undefinedStates.addAll(template.getQueryUpdater().getUndefinedStates());
		
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), option, false);
				undefinedStates.addAll(query.getUndefinedStates());
			} catch (Exception e) {
			}
		}
		return undefinedStates;
	}
	
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = new HashSet<>();
		for (String fieldName: getListFields()) {
			if (!fieldName.equals(Issue.NAME_STATE) 
					&& !fieldName.equals(IssueSchedule.NAME_ITERATION) 
					&& getFieldSpec(fieldName) == null) {
				undefinedFields.add(fieldName);
			}
		}
		
		for (TransitionSpec transition: getTransitionSpecs())
			undefinedFields.addAll(transition.getUndefinedFields());
		for (FieldSpec field: getFieldSpecs())
			undefinedFields.addAll(field.getUndefinedFields());
		for (BoardSpec board: getBoardSpecs())
			undefinedFields.addAll(board.getUndefinedFields());
		for (IssueTemplate template: getIssueTemplates())
			undefinedFields.addAll(template.getQueryUpdater().getUndefinedFields());
		
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), option, false);
				undefinedFields.addAll(query.getUndefinedFields());
			} catch (Exception e) {
			}
		}
		return undefinedFields;
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		for (TransitionSpec transition: getTransitionSpecs())
			undefinedFieldValues.addAll(transition.getUndefinedFieldValues());
		for (FieldSpec field: getFieldSpecs())
			undefinedFieldValues.addAll(field.getUndefinedFieldValues());
		for (BoardSpec board: getBoardSpecs())
			undefinedFieldValues.addAll(board.getUndefinedFieldValues());
		for (IssueTemplate template: getIssueTemplates())
			undefinedFieldValues.addAll(template.getQueryUpdater().getUndefinedFieldValues());

		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), option, false);
				undefinedFieldValues.addAll(query.getUndefinedFieldValues());
			} catch (Exception e) {
			}
		}
		return undefinedFieldValues;
	}
	
	public void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedStates(resolutions))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getBoardSpecs().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedStates(resolutions))
				it.remove();
		}
		for (Iterator<IssueTemplate> it = getIssueTemplates().iterator(); it.hasNext();) {
			if (!it.next().getQueryUpdater().fixUndefinedStates(resolutions))
				it.remove();
		}
		
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (Iterator<NamedIssueQuery> it = getNamedQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), option, false);
				if (query.fixUndefinedStates(resolutions))
					namedQuery.setQuery(query.toString());
				else
					it.remove();
			} catch (Exception e) {
			}
		}
	}

	public Collection<String> fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD)  
				ReconcileUtils.renameItem(listFields, entry.getKey(), entry.getValue().getNewField());
			else 
				listFields.remove(entry.getKey());
		}
		
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFields(resolutions))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getBoardSpecs().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFields(resolutions))
				it.remove();
		}
		for (Iterator<IssueTemplate> it = getIssueTemplates().iterator(); it.hasNext();) {
			if (!it.next().getQueryUpdater().fixUndefinedFields(resolutions))
				it.remove();
		}
		
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (Iterator<NamedIssueQuery> it = getNamedQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), option, false);
				if (query.fixUndefinedFields(resolutions))
					namedQuery.setQuery(query.toString());
				else
					it.remove();
			} catch (Exception e) {
			}
		}
		
		Collection<String> derivedDeletions = new HashSet<>();
		for (Iterator<FieldSpec> it = getFieldSpecs().iterator(); it.hasNext();) {
			FieldSpec field = it.next();
			if (!field.fixUndefinedFields(resolutions)) {
				it.remove();
				derivedDeletions.add(field.getName());
			}
		}

		return derivedDeletions;
	}
	
	public Collection<String> fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (Iterator<TransitionSpec> it = getTransitionSpecs().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFieldValues(resolutions))
				it.remove();
		}
		for (Iterator<BoardSpec> it = getBoardSpecs().iterator(); it.hasNext();) {
			if (!it.next().fixUndefinedFieldValues(resolutions))
				it.remove();
		}
		for (Iterator<IssueTemplate> it = getIssueTemplates().iterator(); it.hasNext();) {
			if (!it.next().getQueryUpdater().fixUndefinedFieldValues(resolutions))
				it.remove();
		}
		
		IssueQueryParseOption option = new IssueQueryParseOption().enableAll(true);
		for (Iterator<NamedIssueQuery> it = getNamedQueries().iterator(); it.hasNext();) {
			NamedIssueQuery namedQuery = it.next();
			try {
				IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), option, false);
				if (query.fixUndefinedFieldValues(resolutions))
					namedQuery.setQuery(query.toString());
				else
					it.remove();
			} catch (Exception e) {
			}
		}
		
		Collection<String> derivedDeletions = new HashSet<>();
		for (Iterator<FieldSpec> it = getFieldSpecs().iterator(); it.hasNext();) {
			FieldSpec field = it.next();
			if (!field.fixUndefinedFieldValues(resolutions)) {
				it.remove();
				derivedDeletions.add(field.getName());
			}
		}

		return derivedDeletions;
	}
	
	public void onRenameUser(String oldName, String newName) {
		for (FieldSpec field: getFieldSpecs())
			field.onRenameUser(oldName, newName);
		for (BoardSpec board: getBoardSpecs())
			board.onRenameUser(this, oldName, newName);
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		for (Iterator<BoardSpec> it = getBoardSpecs().iterator(); it.hasNext();) { 
			if (it.next().onDeleteUser(this, userName))
				it.remove();
		}
		for (FieldSpec field: getFieldSpecs())
			usage.add(field.onDeleteUser(userName));
		
		return usage.prefix("issue settings");
	}
	
	public void onRenameGroup(String oldName, String newName) {
		for (FieldSpec field: getFieldSpecs())
			field.onRenameGroup(oldName, newName);
	}

	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		
		for (FieldSpec field: getFieldSpecs())
			usage.add(field.onDeleteGroup(groupName));
		
		return usage.prefix("issue settings");
	}
	
	public void onMoveProject(String oldPath, String newPath) {
		for (FieldSpec field: getFieldSpecs()) 
			field.onMoveProject(oldPath, newPath);
		
		for (TransitionSpec transition: getTransitionSpecs())
			transition.onMoveProject(oldPath, newPath);
		for (BoardSpec board: getBoardSpecs()) {
			board.getBaseQueryUpdater().onMoveProject(oldPath, newPath);
			board.getBacklogBaseQueryUpdater().onMoveProject(oldPath, newPath);
		}
		
		for (IssueTemplate template: getIssueTemplates())
			template.getQueryUpdater().onMoveProject(oldPath, newPath);
	}
	
	public Usage onDeleteProject(String projectPath) {
		Usage usage = new Usage();
		
		for (FieldSpec field: getFieldSpecs()) 
			usage.add(field.onDeleteProject(projectPath));
		
		int index = 1;
		for (TransitionSpec transition: getTransitionSpecs()) 
			usage.add(transition.onDeleteProject(projectPath).prefix("state transition #" + index++));
		
		index = 1;
		for (BoardSpec board: getBoardSpecs()) { 
			usage.add(board.getBaseQueryUpdater().onDeleteProject(projectPath).prefix("default board #" + index));
			usage.add(board.getBacklogBaseQueryUpdater().onDeleteProject(projectPath).prefix("default board #" + index));
			index++;
		}
		
		index = 1;
		for (IssueTemplate template: getIssueTemplates()) 
			usage.add(template.getQueryUpdater().onDeleteProject(projectPath).prefix("description template #" + index++));
		
		return usage.prefix("issue settings");		
	}
	
	public void onRenameRole(String oldName, String newName) {
		for (TransitionSpec transition: getTransitionSpecs())
			transition.onRenameRole(oldName, newName);
	}
	
	public Usage onDeleteRole(String roleName) {
		Usage usage = new Usage();
		
		int index = 1;
		for (TransitionSpec transition: getTransitionSpecs())
			usage.add(transition.onDeleteRole(roleName).prefix("state transition #" + index++));
		
		return usage.prefix("issue settings");
	}
	
	public void onRenameLink(String oldName, String newName) {
		for (TransitionSpec transition: getTransitionSpecs())
			transition.onRenameLink(oldName, newName);
		for (BoardSpec board: getBoardSpecs()) {
			board.getBaseQueryUpdater().onRenameLink(oldName, newName);
			board.getBacklogBaseQueryUpdater().onRenameLink(oldName, newName);
			ReconcileUtils.renameItem(board.getDisplayLinks(), oldName, newName);
		}
		
		for (IssueTemplate template: getIssueTemplates())
			template.getQueryUpdater().onRenameLink(oldName, newName);
		
		ReconcileUtils.renameItem(listLinks, oldName, newName);
		
		timeTrackingSetting.onRenameLink(oldName, newName);
	}

	public Usage onDeleteLink(String linkName) {
		Usage usage = new Usage();
		
		int index = 1;
		for (TransitionSpec transition: getTransitionSpecs()) 
			usage.add(transition.onDeleteLink(linkName).prefix("state transition #" + index++));
		
		index = 1;
		for (BoardSpec board: getBoardSpecs()) { 
			usage.add(board.getBaseQueryUpdater().onDeleteLink(linkName).prefix("default board #" + index));
			usage.add(board.getBacklogBaseQueryUpdater().onDeleteLink(linkName).prefix("default board #" + index));
			if (board.getDisplayLinks().contains(linkName))
				usage.add(new Usage().add("display links").prefix("default board #" + index));
			index++;
		}
		
		index = 1;
		for (IssueTemplate template: getIssueTemplates()) 
			usage.add(template.getQueryUpdater().onDeleteLink(linkName).prefix("description template #" + index++));
		
		if (listLinks.contains(linkName))
			usage.add(new Usage().add("fields & links").prefix("-> issues"));
		
		usage.add(timeTrackingSetting.onDeleteLink(linkName).prefix("time tracking"));
		
		return usage.prefix("issue settings");
	}
	
	public StateSpec getInitialStateSpec() {
		if (!getStateSpecs().isEmpty())
			return getStateSpecs().iterator().next();
		else
			throw new ExplicitException("No any issue state is defined");
	}
	
	@NotNull
	@Valid
	public List<BoardSpec> getBoardSpecs() {
		return boardSpecs;
	}

	public void setBoardSpecs(List<BoardSpec> boardSpecs) {
		this.boardSpecs = boardSpecs;
	}

	@NotNull
	@Valid
	public TimeTrackingSetting getTimeTrackingSetting() {
		return timeTrackingSetting;
	}

	public void setTimeTrackingSetting(TimeTrackingSetting timeTrackingSetting) {
		this.timeTrackingSetting = timeTrackingSetting;
	}

	@NotNull
	public List<String> getListFields() {
		return listFields;
	}

	public void setListFields(List<String> listFields) {
		this.listFields = listFields;
	}

	@NotNull
	public List<String> getListLinks() {
		return listLinks;
	}

	public void setListLinks(List<String> listLinks) {
		this.listLinks = listLinks;
	}

	@NotNull
	@Valid
	public List<NamedIssueQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedIssueQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	@NotNull
	@Valid
	public List<IssueTemplate> getIssueTemplates() {
		return issueTemplates;
	}

	public void setIssueTemplates(List<IssueTemplate> issueTemplates) {
		this.issueTemplates = issueTemplates;
	}

	@NotNull
	@Valid
	public CommitMessageFixPatterns getCommitMessageFixPatterns() {
		return commitMessageFixPatterns;
	}

	public void setCommitMessageFixPatterns(CommitMessageFixPatterns commitMessageFixPatterns) {
		this.commitMessageFixPatterns = commitMessageFixPatterns;
	}

	@Valid
	@NotNull
	public ExternalIssueTransformers getExternalIssueTransformers() {
		return externalIssueTransformers;
	}

	public void setExternalIssueTransformers(ExternalIssueTransformers externalIssueTransformers) {
		this.externalIssueTransformers = externalIssueTransformers;
	}

	@Nullable
	public NamedIssueQuery getNamedQuery(String name) {
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
		
	public int getStateOrdinal(String state) {
		return getStateSpecs().indexOf(getStateSpec(state));
	}

	@Nullable
	public ManualSpec getManualSpec(Subject subject, Issue issue, String state) {
		for (var transition: getTransitionSpecs()) {
			if (transition instanceof ManualSpec) {
				var manualSpec = (ManualSpec) transition;
				if (manualSpec.canTransit(subject, issue, state) && manualSpec.isAuthorized(subject, issue)) {
					return manualSpec;
				}
			}
		}
		return null;
	}

	public Collection<String> getPromptFieldsUponIssueOpen(Project project) {
		return getFieldSpecs().stream()
				.filter(it -> it.isPromptUponIssueOpen() && it.isApplicable(project))
				.map(it -> it.getName())
				.collect(Collectors.toList());		
	}
	
}
