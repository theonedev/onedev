package io.onedev.server.model.support.administration;

import com.google.common.collect.Lists;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.Choice;
import io.onedev.server.buildspecmodel.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.buildspecmodel.inputspec.showcondition.ShowCondition;
import io.onedev.server.buildspecmodel.inputspec.showcondition.ValueIsOneOf;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.*;
import io.onedev.server.model.support.issue.field.spec.BuildChoiceField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.choicefield.defaultvalueprovider.DefaultValue;
import io.onedev.server.model.support.issue.field.spec.choicefield.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.model.support.issue.transitiontrigger.BranchUpdateTrigger;
import io.onedev.server.model.support.issue.transitiontrigger.BuildSuccessfulTrigger;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.model.support.issue.transitiontrigger.StateTransitionTrigger;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.*;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Editable
public class GlobalIssueSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
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

		Choice buildFailure = new Choice();
		buildFailure.setValue("Build Failure");
		buildFailure.setColor("#F64E60");
		choices.add(buildFailure);
		
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
		
		BuildChoiceField failedBuild = new BuildChoiceField();
		failedBuild.setName("Failed Build");
		failedBuild.setAllowEmpty(true);
		failedBuild.setNameOfEmptyValue("Not specified");
		
		fieldSpecs.add(failedBuild);
		
		ShowCondition showCondition = new ShowCondition();
		showCondition.setInputName("Type");
		ValueIsOneOf valueIsOneOf = new ValueIsOneOf();
		valueIsOneOf.setValues(Lists.newArrayList("Build Failure"));
		showCondition.setValueMatcher(valueIsOneOf);
		failedBuild.setShowCondition(showCondition);
		
		StateSpec open = new StateSpec();
		open.setName("Open");
		open.setColor("#FFA800");
		
		stateSpecs.add(open);
		
		StateSpec closed = new StateSpec();
		closed.setColor("#1BC5BD");
		closed.setName("Closed");
		
		stateSpecs.add(closed);
		
		TransitionSpec transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Closed");
		PressButtonTrigger pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Close");
		pressButton.setAuthorizedRoles(Lists.newArrayList("Code Writer", PressButtonTrigger.ROLE_SUBMITTER, "{Assignees}"));
		pressButton.setIssueQuery("not(any \"Blocked By\" matching(\"State\" is \"Open\")) and not(any \"Child Issue\" matching(\"State\" is \"Open\"))");
		transition.setTrigger(pressButton);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Closed");
		BranchUpdateTrigger branchUpdate = new BranchUpdateTrigger();
		branchUpdate.setBranches("main");
		transition.setTrigger(branchUpdate);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Closed");
		BuildSuccessfulTrigger buildSuccessful = new BuildSuccessfulTrigger();
		buildSuccessful.setBranches("main");
		buildSuccessful.setIssueQuery("\"Type\" is \"Build Failure\" and (\"Failed Build\" is current or \"Failed Build\" is previous)");
		transition.setTrigger(buildSuccessful);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Open"));
		transition.setToState("Closed");
		StateTransitionTrigger stateTransition = new StateTransitionTrigger();
		stateTransition.setStates(Lists.newArrayList("Closed"));
		stateTransition.setIssueQuery("any \"Child Issue\" matching(current issue) and all \"Child Issue\" matching(\"State\" is \"Closed\")");
		transition.setTrigger(stateTransition);
		
		transitionSpecs.add(transition);
		
		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Open");
		pressButton = new PressButtonTrigger();
		pressButton.setButtonLabel("Reopen");
		pressButton.setAuthorizedRoles(Lists.newArrayList("Code Writer", "Code Reader", "Issue Reporter"));
		transition.setTrigger(pressButton);
		
		transitionSpecs.add(transition);

		transition = new TransitionSpec();
		transition.setFromStates(Lists.newArrayList("Closed"));
		transition.setToState("Open");
		stateTransition = new StateTransitionTrigger();
		stateTransition.setStates(Lists.newArrayList("Open"));
		stateTransition.setIssueQuery("any \"Child Issue\" matching(current issue) or any \"Blocked By\" matching(current issue)");
		transition.setTrigger(stateTransition);
		
		transitionSpecs.add(transition);
		
		BoardSpec board = new BoardSpec();
		board.setName(Issue.NAME_STATE);
		board.setIdentifyField(Issue.NAME_STATE);
		board.setColumns(Lists.newArrayList("Open", "Closed"));
		board.setDisplayFields(Lists.newArrayList(Issue.NAME_STATE, "Type", "Priority", "Assignees", IssueSchedule.NAME_MILESTONE));
		board.setDisplayLinks(Lists.newArrayList("Child Issue", "Blocked By"));
		boardSpecs.add(board);
		
		listFields.add(Issue.NAME_STATE);
		listFields.add("Type");
		listFields.add("Priority");
		listFields.add("Assignees");
		listFields.add(IssueSchedule.NAME_MILESTONE);
		
		listLinks.add("Child Issue");
		listLinks.add("Blocked By");
		
		namedQueries.add(new NamedIssueQuery("Open", "\"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Assigned to me & Open", "\"Assignees\" is me and \"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Submitted by me & Open", "submitted by me and \"State\" is \"Open\""));
		namedQueries.add(new NamedIssueQuery("Assigned to me", "\"Assignees\" is me"));
		namedQueries.add(new NamedIssueQuery("Submitted by me", "submitted by me"));
		namedQueries.add(new NamedIssueQuery("Submitted recently", "\"Submit Date\" is since \"last week\""));
		namedQueries.add(new NamedIssueQuery("Mentioned me", "mentioned me"));
		namedQueries.add(new NamedIssueQuery("Blocked Issues", "any \"Blocked By\" matching(\"State\" is \"Open\") or any \"Child Issue\" matching(\"State\" is \"Open\")"));
		namedQueries.add(new NamedIssueQuery("Has activity recently", "\"Last Activity Date\" is since \"last week\""));
		namedQueries.add(new NamedIssueQuery("Open & Critical", "\"State\" is \"Open\" and \"Priority\" is \"Critical\""));
		namedQueries.add(new NamedIssueQuery("Open & Unassigned", "\"State\" is \"Open\" and \"Assignees\" is empty"));
		namedQueries.add(new NamedIssueQuery("Open & Unscheduled", "\"State\" is \"Open\" and \"Milestone\" is empty"));
		namedQueries.add(new NamedIssueQuery("Closed", "\"State\" is \"Closed\""));
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
		
		timeTrackingSetting.setAggregationLink("Child Issue");
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
	
	public List<StateSpec> getStateSpecs() {
		return stateSpecs;
	}

	public void setStateSpecs(List<StateSpec> stateSpecs) {
		this.stateSpecs = stateSpecs;
	}

	public List<TransitionSpec> getTransitionSpecs() {
		return transitionSpecs;
	}

	public void setTransitionSpecs(List<TransitionSpec> transitionSpecs) {
		this.transitionSpecs = transitionSpecs;
	}

	@Editable
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
					&& !fieldName.equals(IssueSchedule.NAME_MILESTONE) 
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
	
	public List<BoardSpec> getBoardSpecs() {
		return boardSpecs;
	}

	public void setBoardSpecs(List<BoardSpec> boardSpecs) {
		this.boardSpecs = boardSpecs;
	}

	public TimeTrackingSetting getTimeTrackingSetting() {
		return timeTrackingSetting;
	}

	public void setTimeTrackingSetting(TimeTrackingSetting timeTrackingSetting) {
		this.timeTrackingSetting = timeTrackingSetting;
	}

	public List<String> getListFields() {
		return listFields;
	}

	public void setListFields(List<String> listFields) {
		this.listFields = listFields;
	}
	
	public List<String> getListLinks() {
		return listLinks;
	}

	public void setListLinks(List<String> listLinks) {
		this.listLinks = listLinks;
	}

	public List<NamedIssueQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedIssueQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	public List<IssueTemplate> getIssueTemplates() {
		return issueTemplates;
	}

	public void setIssueTemplates(List<IssueTemplate> issueTemplates) {
		this.issueTemplates = issueTemplates;
	}

	public CommitMessageFixPatterns getCommitMessageFixPatterns() {
		return commitMessageFixPatterns;
	}

	public void setCommitMessageFixPatterns(CommitMessageFixPatterns commitMessageFixPatterns) {
		this.commitMessageFixPatterns = commitMessageFixPatterns;
	}

	@Nullable
	public NamedIssueQuery getNamedQuery(String name) {
		for (NamedIssueQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
	public Collection<String> getPromptFieldsUponIssueOpen(Project project) {
		Matcher matcher = new PathMatcher();
		return getFieldSpecs().stream()
				.filter(it->it.isPromptUponIssueOpen() && (it.getApplicableProjects() == null || PatternSet.parse(it.getApplicableProjects()).matches(matcher, project.getPath())))
				.map(it->it.getName())
				.collect(Collectors.toList());
	}
	
	public int getStateOrdinal(String state) {
		return getStateSpecs().indexOf(getStateSpec(state));
	}
	
}
