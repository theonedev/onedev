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
import io.onedev.server.manager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldResolution.FixType;

public class IssueSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<TransitionSpec> transitionSpecs;
	
	private Set<String> promptFieldsUponIssueOpen;
	
	private Set<String> listFields;
	
	private List<BoardSpec> boardSpecs;

	private List<NamedIssueQuery> savedQueries;
	
	private transient GlobalIssueSetting globalSetting;
	
	private GlobalIssueSetting getGlobalSetting() {
		if (globalSetting == null)
			globalSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return globalSetting;
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
	public Set<String> getPromptFieldsUponIssueOpen(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && promptFieldsUponIssueOpen == null)
			return new HashSet<>(getGlobalSetting().getDefaultPromptFieldsUponIssueOpen());
		else
			return promptFieldsUponIssueOpen;
	}

	public void setPromptFieldsUponIssueOpen(@Nullable Set<String> promptFieldsUponIssueOpen) {
		this.promptFieldsUponIssueOpen = promptFieldsUponIssueOpen;
	}

	@Nullable
	public Set<String> getListFields(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && listFields == null)
			return new HashSet<>(getGlobalSetting().getDefaultListFields());
		else
			return listFields;
	}
	
	public void setListFields(@Nullable Set<String> listFields) {
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
	public List<NamedIssueQuery> getSavedQueries(boolean useDefaultIfNotDefined) {
		if (useDefaultIfNotDefined && savedQueries == null)
			return new ArrayList<>(getGlobalSetting().getDefaultQueries());
		else
			return savedQueries;
	}

	public void setSavedQueries(@Nullable List<NamedIssueQuery> savedQueries) {
		this.savedQueries = savedQueries;
	}

	@Nullable
	public NamedIssueQuery getSavedQuery(String name) {
		for (NamedIssueQuery namedQuery: getSavedQueries(true)) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}

	public void onRenameUser(String oldName, String newName) {
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs)
				transition.onRenameUser(oldName, newName);
		}
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				board.onRenameUser(getGlobalSetting(), oldName, newName);
		}
	}

	public void onDeleteUser(String userName) {
		if (transitionSpecs != null) {
			for (Iterator<TransitionSpec> it = transitionSpecs.iterator(); it.hasNext();) {
				if (it.next().onDeleteUser(userName))
					it.remove();
			}
		}
		if (boardSpecs != null) {
			for (Iterator<BoardSpec> it = boardSpecs.iterator(); it.hasNext();) {
				if (it.next().onDeleteUser(getGlobalSetting(), userName))
					it.remove();
			}
		}
	}

	public void onRenameGroup(String oldName, String newName) {
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs)
				transition.onRenameGroup(oldName, newName);
		}
	}

	public void onDeleteGroup(String groupName) {
		if (transitionSpecs != null) {
			for (Iterator<TransitionSpec> it = transitionSpecs.iterator(); it.hasNext();) {
				if (it.next().onDeleteGroup(groupName))
					it.remove();
			}
		}
	}

	public void onRenameConfiguration(String oldName, String newName) {
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs) 
				transition.onRenameConfiguration(oldName, newName);
		}
	}
	
	public void onDeleteConfiguration(String configurationName) {
		if (transitionSpecs != null) {
			for (Iterator<TransitionSpec> it = transitionSpecs.iterator(); it.hasNext();) {
				if (it.next().onDeleteConfiguration(configurationName))
					it.remove();
			}
		}
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
				undefinedFields.addAll(transition.getUndefinedFields(getGlobalSetting()));
		}
		if (savedQueries != null) {
			for (NamedIssueQuery namedQuery: savedQueries) {
				try {
					undefinedFields.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedFields());
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

	public void fixUndefinedFields(Project project, Map<String, UndefinedFieldResolution> resolutions) {
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
		if (savedQueries != null) {
			for (Iterator<NamedIssueQuery> it = savedQueries.iterator(); it.hasNext();) {
				NamedIssueQuery namedQuery = it.next();
				try {
					IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
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
				if (it.next().fixUndefinedFields(project, resolutions))
					it.remove();
			}		
		}
	}	
	
	public Collection<String> getUndefinedStates(Project project) {
		Set<String> undefinedStates = new HashSet<>();

		if (savedQueries != null) {
			for (NamedIssueQuery namedQuery: savedQueries) {
				try {
					undefinedStates.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedStates());
				} catch (Exception e) {
				}
			}
		}

		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				undefinedStates.addAll(board.getUndefinedStates(project));
		}

		return undefinedStates;
	}

	public void fixUndefinedStates(Project project, Map<String, UndefinedStateResolution> resolutions) {
		if (savedQueries != null) {
			for (NamedIssueQuery namedQuery: savedQueries) {
				try {
					IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
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
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues(Project project) {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		if (savedQueries != null) {
			for (NamedIssueQuery namedQuery: savedQueries) {
				try {
					undefinedFieldValues.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false).getUndefinedFieldValues());
				} catch (Exception e) {
				}
			}
		}
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				undefinedFieldValues.addAll(board.getUndefinedFieldValues(project));
		}
		if (transitionSpecs != null) {
			for (TransitionSpec transition: transitionSpecs)
				undefinedFieldValues.addAll(transition.getUndefinedFieldValues());
		}
		return undefinedFieldValues;
	}
	
	public void fixUndefinedFieldValues(Project project, Map<String, ValueSetEdit> valueSetEdits) {
		if (savedQueries != null) {
			for (Iterator<NamedIssueQuery> it = savedQueries.iterator(); it.hasNext();) {
				NamedIssueQuery namedQuery = it.next();
				try {
					boolean remove = false;
					IssueQuery query = IssueQuery.parse(project, namedQuery.getQuery(), false);
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
				if (it.next().fixUndefinedFieldValues(project, valueSetEdits)) {
					it.remove();
					break;
				}
			}
		}
		if (transitionSpecs != null) {
			for (Iterator<TransitionSpec> it = transitionSpecs.iterator(); it.hasNext();) {
				if (it.next().fixUndefinedFieldValues(project, valueSetEdits)) {
					it.remove();
					break;
				}
			}
		}
	}
	
}
