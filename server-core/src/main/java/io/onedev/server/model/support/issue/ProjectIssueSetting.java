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
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public class ProjectIssueSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
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
			return new ArrayList<>(getGlobalSetting().getBoardSpecs());
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

	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (boardSpecs != null) {
			for (Iterator<BoardSpec> it = boardSpecs.iterator(); it.hasNext();) { 
				if (it.next().onDeleteUser(getGlobalSetting(), userName))
					it.remove();
			}
		}
		return usage.prefix("issue setting");
	}

	public Collection<String> getUndefinedStates(Project project) {
		Set<String> undefinedStates = new HashSet<>();

		if (namedQueries != null) {
			for (NamedIssueQuery namedQuery: namedQueries) {
				try {
					undefinedStates.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true).getUndefinedStates());
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

	public Collection<String> getUndefinedFields(Project project) {
		Set<String> undefinedFields = new HashSet<>();
		if (listFields != null) {
			for (String fieldName: listFields) {
				if (!fieldName.equals(Issue.NAME_STATE) && getGlobalSetting().getFieldSpec(fieldName) == null)
					undefinedFields.add(fieldName);
			}
		}
		if (namedQueries != null) {
			for (NamedIssueQuery namedQuery: namedQueries) {
				try {
					undefinedFields.addAll(IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true).getUndefinedFields());
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

	public Collection<UndefinedFieldValue> getUndefinedFieldValues(Project project) {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		if (namedQueries != null) {
			for (NamedIssueQuery namedQuery: namedQueries) {
				try {
					undefinedFieldValues.addAll(IssueQuery.parse(null, namedQuery.getQuery(), false, true, true, true, true).getUndefinedFieldValues());
				} catch (Exception e) {
				}
			}
		}
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				undefinedFieldValues.addAll(board.getUndefinedFieldValues(project));
		}
		return undefinedFieldValues;
	}
	
	public void fixUndefinedStates(Project project, Map<String, UndefinedStateResolution> resolutions) {
		if (namedQueries != null) {
			for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
				NamedIssueQuery namedQuery = it.next();
				try {
					IssueQuery parsedQuery = IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true);
					if (parsedQuery.fixUndefinedStates(resolutions))
						namedQuery.setQuery(parsedQuery.toString());
					else
						it.remove();
				} catch (Exception e) {
				}
			}
		}
		
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				board.fixUndefinedStates(project, resolutions);
		}
	}
	
	public void fixUndefinedFields(Project project, Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (listFields != null) {
				if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) 
					ReconcileUtils.renameItem(listFields, entry.getKey(), entry.getValue().getNewField());
				else 
					listFields.remove(entry.getKey());
			}
		}
		if (namedQueries != null) {
			for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
				NamedIssueQuery namedQuery = it.next();
				try {
					IssueQuery parsedQuery = IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true);
					if (parsedQuery.fixUndefinedFields(resolutions))
						namedQuery.setQuery(parsedQuery.toString());
					else
						it.remove();
				} catch (Exception e) {
				}
			}
		}
		
		if (boardSpecs != null) {
			for (Iterator<BoardSpec> it = boardSpecs.iterator(); it.hasNext();) {
				if (!it.next().fixUndefinedFields(project, resolutions))
					it.remove();
			}		
		}
	}	
	
	public void fixUndefinedFieldValues(Project project, Map<String, UndefinedFieldValuesResolution> resolutions) {
		if (namedQueries != null) {
			for (Iterator<NamedIssueQuery> it = namedQueries.iterator(); it.hasNext();) {
				NamedIssueQuery namedQuery = it.next();
				try {
					IssueQuery parsedQuery = IssueQuery.parse(project, namedQuery.getQuery(), false, true, true, true, true);
					if (parsedQuery.fixUndefinedFieldValues(resolutions))
						namedQuery.setQuery(parsedQuery.toString());
					else
						it.remove();
				} catch (Exception e) {
				}
			}
		}
		if (boardSpecs != null) {
			for (Iterator<BoardSpec> it = boardSpecs.iterator(); it.hasNext();) {
				if (!it.next().fixUndefinedFieldValues(project, resolutions)) {
					it.remove();
					break;
				}
			}
		}
	}
	
}
