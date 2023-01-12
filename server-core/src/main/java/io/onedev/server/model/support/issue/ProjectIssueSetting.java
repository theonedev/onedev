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
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectIssueSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> listFields;
	
	private List<String> listLinks;
	
	private List<BoardSpec> boardSpecs;

	private List<NamedIssueQuery> namedQueries;
	
	private transient GlobalIssueSetting setting;
	
	private GlobalIssueSetting getGlobalSetting() {
		if (setting == null)
			setting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return setting;
	}

	@Nullable
	public List<String> getListFields() {
		return listFields;
	}
	
	public void setListFields(@Nullable List<String> listFields) {
		this.listFields = listFields;
	}

	@Nullable
	public List<String> getListLinks() {
		return listLinks;
	}

	public void setListLinks(List<String> listLinks) {
		this.listLinks = listLinks;
	}

	@Nullable
	public List<BoardSpec> getBoardSpecs() {
		return boardSpecs;
	}

	public void setBoardSpecs(@Nullable List<BoardSpec> boardSpecs) {
		this.boardSpecs = boardSpecs;
	}

	@Nullable
	public List<NamedIssueQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(@Nullable List<NamedIssueQuery> namedQueries) {
		this.namedQueries = namedQueries;
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
		return usage.prefix("issue settings");
	}
	
	private Collection<IssueQueryUpdater> getNamedQueryUpdaters() {
		Collection<IssueQueryUpdater> updaters = new ArrayList<>();
		if (namedQueries != null) {
			for (NamedIssueQuery namedQuery: namedQueries) {
				updaters.add(new IssueQueryUpdater() {

					@Override
					protected Usage getUsage() {
						return new Usage().add("saved query '" + namedQuery.getName() + "'");
					}

					@Override
					protected boolean isAllowEmpty() {
						return true;
					}

					@Override
					protected String getIssueQuery() {
						return namedQuery.getQuery();
					}

					@Override
					protected void setIssueQuery(String issueQuery) {
						namedQuery.setQuery(issueQuery);
					}
					
				});
			}
		}
		return updaters;
	}

	public Collection<String> getUndefinedStates() {
		Set<String> undefinedStates = new HashSet<>();

		for (IssueQueryUpdater updater: getNamedQueryUpdaters())
			undefinedStates.addAll(updater.getUndefinedStates());

		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				undefinedStates.addAll(board.getUndefinedStates());
		}
		
		return undefinedStates;
	}

	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		if (listFields != null) {
			for (String fieldName: listFields) {
				if (!fieldName.equals(Issue.NAME_STATE) && getGlobalSetting().getFieldSpec(fieldName) == null)
					undefinedFields.add(fieldName);
			}
		}
		for (IssueQueryUpdater updater: getNamedQueryUpdaters())
			undefinedFields.addAll(updater.getUndefinedFields());
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs) 
				undefinedFields.addAll(board.getUndefinedFields());
		}
		return undefinedFields;
	}

	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		for (IssueQueryUpdater updater: getNamedQueryUpdaters())
			undefinedFieldValues.addAll(updater.getUndefinedFieldValues());
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				undefinedFieldValues.addAll(board.getUndefinedFieldValues());
		}
		return undefinedFieldValues;
	}
	
	public void fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		for (IssueQueryUpdater updater: getNamedQueryUpdaters())
			updater.fixUndefinedStates(resolutions);
		
		if (boardSpecs != null) {
			for (BoardSpec board: boardSpecs)
				board.fixUndefinedStates(resolutions);
		}
	}
	
	public void fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (listFields != null) {
				if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) 
					ReconcileUtils.renameItem(listFields, entry.getKey(), entry.getValue().getNewField());
				else 
					listFields.remove(entry.getKey());
			}
		}
		for (IssueQueryUpdater updater: getNamedQueryUpdaters())
			updater.fixUndefinedFields(resolutions);
		
		if (boardSpecs != null) {
			for (Iterator<BoardSpec> it = boardSpecs.iterator(); it.hasNext();) {
				if (!it.next().fixUndefinedFields(resolutions))
					it.remove();
			}		
		}
	}	
	
	public void fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (IssueQueryUpdater updater: getNamedQueryUpdaters())
			updater.fixUndefinedFieldValues(resolutions);
		
		if (boardSpecs != null) {
			for (Iterator<BoardSpec> it = boardSpecs.iterator(); it.hasNext();) {
				if (!it.next().fixUndefinedFieldValues(resolutions)) {
					it.remove();
					break;
				}
			}
		}
	}
	
}
