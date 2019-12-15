package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.issue.BoardSpec;
import io.onedev.server.issue.TransitionSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.Usage;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution.FixType;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public class ProjectIssueSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<TransitionSpec> transitionSpecs;
	
	private Collection<String> promptFieldsUponIssueOpen;
	
	private List<String> listFields;
	
	private List<BoardSpec> boardSpecs;

	private List<NamedIssueQuery> namedQueries;
	
	private transient GlobalIssueSetting setting;
	
	private GlobalIssueSetting getGlobalSetting() {
		if (setting == null)
			setting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return setting;
	}

	@Nullable
	public List<TransitionSpec> getTransitionSpecs(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && transitionSpecs == null)
			return new ArrayList<>(getGlobalSetting().getDefaultTransitionSpecs());
		else
			return transitionSpecs;
	}

	public void setTransitionSpecs(List<TransitionSpec> stateTransitions) {
		this.transitionSpecs = stateTransitions;
	}

	@Nullable
	public Collection<String> getPromptFieldsUponIssueOpen(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && promptFieldsUponIssueOpen == null)
			return new HashSet<>(getGlobalSetting().getDefaultPromptFieldsUponIssueOpen());
		else
			return promptFieldsUponIssueOpen;
	}

	public void setPromptFieldsUponIssueOpen(@Nullable Collection<String> promptFieldsUponIssueOpen) {
		this.promptFieldsUponIssueOpen = promptFieldsUponIssueOpen;
	}

	@Nullable
	public List<String> getListFields(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && listFields == null)
			return new ArrayList<>(getGlobalSetting().getListFields());
		else
			return listFields;
	}
	
	public void setListFields(@Nullable List<String> listFields) {
		this.listFields = listFields;
	}

	@Nullable
	public List<BoardSpec> getBoardSpecs(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && boardSpecs == null)
			return new ArrayList<>(getGlobalSetting().getDefaultBoardSpecs());
		else
			return boardSpecs;
	}

	public void setBoardSpecs(@Nullable List<BoardSpec> boardSpecs) {
		this.boardSpecs = boardSpecs;
	}

	@Nullable
	public List<NamedIssueQuery> getNamedQueries(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && namedQueries == null)
			return new ArrayList<>(getGlobalSetting().getNamedQueries());
		else
			return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedIssueQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}

	@Nullable
	public NamedIssueQuery getNamedQuery(String name) {
		for (NamedIssueQuery namedQuery: getNamedQueries(true)) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}

	public void onRenameUser(String oldName, String newName) {
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				board.onRenameUser(getGlobalSetting(), oldName, newName);
		}
	}

	public Usage onDeleteBranch(String branchName) {
		Usage usage = new Usage();
		if (transitionSpecs != null) {
			for (TransitionSpec transitionSpec: transitionSpecs)
				usage.add(transitionSpec.onDeleteBranch(branchName));
		}
		return usage.prefix("issue setting");
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (boardSpecs != null) {
			for (Iterator<BoardSpec> it = boardSpecs.iterator(); it.hasNext();) { 
				Usage usageInBoard = it.next().onDeleteUser(getGlobalSetting(), userName);
				if (usageInBoard != null)
					usage.add(usageInBoard);
				else
					it.remove();
			}
		}
		return usage.prefix("issue setting");
	}

	public void onRenameRole(String oldName, String newName) {
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs)
				transition.onRenameRole(oldName, newName);
		}
	}

	public Usage onDeleteRole(String roleName) {
		Usage usage = new Usage();
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs) 
				usage.add(transition.onDeleteRole(roleName));
		}
		return usage.prefix("issue setting");
	}

	public Collection<String> getUndefinedFields(Project project) {
		Set<String> undefinedFields = new HashSet<>();
		if (listFields != null) {
			for (String fieldName: listFields) {
				if (getGlobalSetting().getFieldSpec(fieldName) == null)
					undefinedFields.add(fieldName);
			}
		}
		if (promptFieldsUponIssueOpen != null) {
			for (String fieldName: promptFieldsUponIssueOpen) {
				if (getGlobalSetting().getFieldSpec(fieldName) == null)
					undefinedFields.add(fieldName);
			}
		}
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs)
				undefinedFields.addAll(transition.getUndefinedFields(project));
		}
		if (namedQueries != null) {
			for (NamedIssueQuery namedQuery: namedQueries) {
				try {
					undefinedFields.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false, true, true).getUndefinedFields());
				} catch (Exception e) {
				}
			}
		}
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs) 
				undefinedFields.addAll(board.getUndefinedFields(project));
		}
		return undefinedFields;
	}

	public void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (listFields != null) {
				listFields.remove(entry.getKey());
				if (entry.getValue().getFixType() == FixType.CHANGE_TO_ANOTHER_FIELD)
					listFields.add(entry.getValue().getNewField());
			}
			if (promptFieldsUponIssueOpen != null) {
				promptFieldsUponIssueOpen.remove(entry.getKey());
				if (entry.getValue().getFixType() == FixType.CHANGE_TO_ANOTHER_FIELD)
					promptFieldsUponIssueOpen.add(entry.getValue().getNewField());
			}
		}
		if (namedQueries != null) {
			for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
				NamedIssueQuery namedQuery = it.next();
				try {
					IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true);
					boolean remove = false;
					for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
						UndefinedFieldResolution resolution = entry.getValue();
						if (resolution.getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
							query.onRenameField(entry.getKey(), resolution.getNewField());
						} else if (query.onDeleteField(entry.getKey())) {
							remove = true;
							break;
						}
					}				
					if (remove)
						it.remove();
					else
						namedQuery.setQuery(query.toString());
				} catch (Exception e) {
				}
			}
		}
		
		if (transitionSpecs != null) {
			for (Iterator<TransitionSpec> it = transitionSpecs.iterator(); it.hasNext();) {
				if (it.next().fixUndefinedFields(resolutions))
					it.remove();
			}		
		}
		
		if (boardSpecs != null) {
			for (Iterator<BoardSpec> it = boardSpecs.iterator(); it.hasNext();) {
				if (it.next().fixUndefinedFields(resolutions))
					it.remove();
			}		
		}
	}	
	
	public Collection<String> getUndefinedStates(Project project) {
		Set<String> undefinedStates = new HashSet<>();

		if (namedQueries != null) {
			for (NamedIssueQuery namedQuery: namedQueries) {
				try {
					undefinedStates.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false, true, true).getUndefinedStates());
				} catch (Exception e) {
				}
			}
		}

		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				undefinedStates.addAll(board.getUndefinedStates(project));
		}
		
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs) 
				undefinedStates.addAll(transition.getUndefinedStates());
		}

		return undefinedStates;
	}

	public void fixUndefinedStates(Project project, Map<String, UndefinedStateResolution> resolutions) {
		if (namedQueries != null) {
			for (NamedIssueQuery namedQuery: namedQueries) {
				try {
					IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false, true, true);
					for (Map.Entry<String, UndefinedStateResolution> resolutionEntry: resolutions.entrySet())
						query.onRenameState(resolutionEntry.getKey(), resolutionEntry.getValue().getNewState());
					namedQuery.setQuery(query.toString());
				} catch (Exception e) {
				}
			}
		}
		
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				board.fixUndefinedStates(project, resolutions);
		}
		
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs)
				transition.fixUndefinedStates(resolutions);
		}
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		if (namedQueries != null) {
			for (NamedIssueQuery namedQuery: namedQueries) {
				try {
					undefinedFieldValues.addAll(IssueQuery.parse(null, namedQuery.getQuery(), false, true, true).getUndefinedFieldValues());
				} catch (Exception e) {
				}
			}
		}
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				undefinedFieldValues.addAll(board.getUndefinedFieldValues());
		}
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs)
				undefinedFieldValues.addAll(transition.getUndefinedFieldValues());
		}
		return undefinedFieldValues;
	}
	
	public void fixUndefinedFieldValues(Map<String, ValueSetEdit> valueSetEdits) {
		if (namedQueries != null) {
			for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
				NamedIssueQuery namedQuery = it.next();
				try {
					boolean remove = false;
					IssueQuery query = IssueQuery.parse(null, namedQuery.getQuery(), false, true, true);
					for (Map.Entry<String, ValueSetEdit> entry: valueSetEdits.entrySet()) {
						if (query.onEditFieldValues(entry.getKey(), entry.getValue())) {
							remove = true;
							break;
						}
					}
					if (remove) 
						it.remove();
					else
						namedQuery.setQuery(query.toString());
				} catch (Exception e) {
				}
			}
		}
		if (boardSpecs != null) {
			for (Iterator<BoardSpec> it = boardSpecs.iterator(); it.hasNext();) {
				if (it.next().fixUndefinedFieldValues(valueSetEdits)) {
					it.remove();
					break;
				}
			}
		}
		if (transitionSpecs != null) {
			for (Iterator<TransitionSpec> it = transitionSpecs.iterator(); it.hasNext();) {
				if (it.next().fixUndefinedFieldValues(valueSetEdits)) {
					it.remove();
					break;
				}
			}
		}
	}
	
}
