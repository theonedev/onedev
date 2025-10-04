package io.onedev.server.web.component.commit.list;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.search.commit.AfterCriteria;
import io.onedev.server.search.commit.AuthorCriteria;
import io.onedev.server.search.commit.BeforeCriteria;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.search.commit.CommitterCriteria;
import io.onedev.server.search.commit.PathCriteria;
import io.onedev.server.search.commit.Revision;
import io.onedev.server.search.commit.RevisionCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.build.choice.BuildChoiceProvider;
import io.onedev.server.web.component.build.choice.BuildSingleChoice;
import io.onedev.server.web.component.datepicker.DatePicker;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.xodus.CommitInfoService;

abstract class CommitFilterPanel extends GenericPanel<CommitQuery> {
	
	public CommitFilterPanel(String id, IModel<CommitQuery> queryModel) {
		super(id, queryModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();	

		addBranchOrTagChoice("branch", true);
		addBranchOrTagChoice("tag", false);

		var buildChoice = new BuildSingleChoice("build", new IModel<Build>() {

			@Override
			public void detach() {
			}

			@Override
			public Build getObject() {
				var query = getModelObject();
				for (var criteria: query.getCriterias()) {
					if (criteria instanceof RevisionCriteria) {
						var revisionCriteria = (RevisionCriteria) criteria;
						for (var revision: revisionCriteria.getRevisions()) {
							if (!revision.isSince() && revision.getType() == Revision.Type.BUILD) {
								return revision.getValueAsBuild(getProject());
							}
						}
					}
				}
				return null;
			}

			@Override
			public void setObject(Build object) {	
				var query = getModelObject();
				var found = false;
				for (var criteria: query.getCriterias()) {
					if (criteria instanceof RevisionCriteria) {
						var revisionCriteria = (RevisionCriteria) criteria;
						for (var i=0; i<revisionCriteria.getRevisions().size(); i++) {
							var revision = revisionCriteria.getRevisions().get(i);
							if (!revision.isSince() && revision.getType() == Revision.Type.BUILD) {
								if (object != null) {
									revisionCriteria.getRevisions().set(i, new Revision(Revision.Type.BUILD, object.getReference().toString(getProject()), false));
								} else {
									revisionCriteria.getRevisions().remove(i);
								}
								found = true;
								break;
							}
						}
						if (revisionCriteria.getRevisions().isEmpty()) {
							query.getCriterias().remove(revisionCriteria);
						}
						if (found)
							break;
					}
				}
				if (!found && object != null) {
					query.getCriterias().add(new RevisionCriteria(Lists.newArrayList(new Revision(Revision.Type.BUILD, object.getReference().toString(getProject()), false))));
				}
				getModel().setObject(query);
			}

		}, new BuildChoiceProvider() {

			@Override
			protected Project getProject() {
				return CommitFilterPanel.this.getProject();
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder("");
			}

		};
		buildChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(buildChoice);	

        var fileChoice = new StringSingleChoice("file", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				var query = getModelObject();
				for (var criteria: query.getCriterias()) {
					if (criteria instanceof PathCriteria) {
						var pathCriteria = (PathCriteria) criteria;
						if (!pathCriteria.getValues().isEmpty()) 
							return pathCriteria.getValues().get(0);
					}
				}
				return null;
			}

			@Override
			public void setObject(String object) {	
				var query = getModelObject();
				var criterias = query.getCriterias();
				var found = false;
				for (var i=0; i<criterias.size(); i++) {
					var criteria = criterias.get(i);
					if (criteria instanceof PathCriteria) {
						if (object != null) 
							criterias.set(i, new PathCriteria(Lists.newArrayList(object)));
						else
							criterias.remove(i);
						found = true;
						break;
					}
				}
				if (!found && object != null) {
					query.getCriterias().add(new PathCriteria(Lists.newArrayList(object)));
				}
				getModel().setObject(query);			
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return OneDev.getInstance(CommitInfoService.class).getFiles(getProject().getId());
			}
		}, true);
		fileChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(fileChoice);		

		addAuthorOrCommitterChoice("authoredBy", true);
		addAuthorOrCommitterChoice("committedBy", false);

		addCommitBeforeOrAfterChoice("committedAfter", false);
		addCommitBeforeOrAfterChoice("committedBefore", true);
	}

	private void addBranchOrTagChoice(String componentId, boolean isBranch) {
		var type = isBranch? Revision.Type.BRANCH: Revision.Type.TAG;
        var choice = new StringSingleChoice(componentId, new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				var query = getModelObject();
				for (var criteria: query.getCriterias()) {
					if (criteria instanceof RevisionCriteria) {
						var revisionCriteria = (RevisionCriteria) criteria;
						for (var revision: revisionCriteria.getRevisions()) {
							if (!revision.isSince() && revision.getType() == type) {
								return revision.getValue() != null? revision.getValue(): getProject().getDefaultBranch();
							}
						}
					}
				}
				return null;
			}

			@Override
			public void setObject(String object) {					
				var query = getModelObject();
				var found = false;
				for (var criteria: query.getCriterias()) {
					if (criteria instanceof RevisionCriteria) {
						var revisionCriteria = (RevisionCriteria) criteria;
						for (var i=0; i<revisionCriteria.getRevisions().size(); i++) {
							var revision = revisionCriteria.getRevisions().get(i);
							if (!revision.isSince() && revision.getType() == type) {
								if (object != null) {
									revisionCriteria.getRevisions().set(i, new Revision(type, object, false));
								} else {
									revisionCriteria.getRevisions().remove(i);
								}
								found = true;
								break;
							}
						}
						if (revisionCriteria.getRevisions().isEmpty()) {
							query.getCriterias().remove(revisionCriteria);
						}
						if (found)
							break;
					}
				}
				if (!found && object != null) {
					query.getCriterias().add(new RevisionCriteria(Lists.newArrayList(new Revision(type, object, false))));
				}
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				if (type == Revision.Type.BRANCH) {	
					return getProject().getBranchRefs().stream().map(it->GitUtils.ref2branch(it.getName())).collect(toList());
				} else {
					return getProject().getTagRefs().stream().map(it->GitUtils.ref2tag(it.getName())).collect(toList());
				}
			}
		}, false);
		choice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(choice);
	}

	private void addAuthorOrCommitterChoice(String componentId, boolean isAuthor) {
        var choice = new StringMultiChoice(componentId, new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				var query = getModelObject();
				for (var criteria: query.getCriterias()) {					
					if (isAuthor && (criteria instanceof AuthorCriteria)) {
						var authorCriteria = (AuthorCriteria) criteria;
						return authorCriteria.getValues().stream().map(it->it!=null?it:SecurityUtils.getUser().getName()).collect(toList());
					} else if (!isAuthor && criteria instanceof CommitterCriteria) {
						var committerCriteria = (CommitterCriteria) criteria;
						return committerCriteria.getValues().stream().map(it->it!=null?it:SecurityUtils.getUser().getName()).collect(toList());
					}
				}
				return Collections.emptyList();
			}

			@Override
			public void setObject(Collection<String> object) {	
				var query = getModelObject();
				var criterias = query.getCriterias();
				var found = false;
				for (var i=0; i<criterias.size(); i++) {
					var criteria = criterias.get(i);
					if (isAuthor && (criteria instanceof AuthorCriteria)) {
						if (!object.isEmpty()) 
							criterias.set(i, new AuthorCriteria(new ArrayList<>(object)));
						else
							criterias.remove(i);
						found = true;
						break;
					} else if (!isAuthor && criteria instanceof CommitterCriteria) {
						if (!object.isEmpty()) 
							criterias.set(i, new CommitterCriteria(new ArrayList<>(object)));
						else
							criterias.remove(i);
						found = true;
						break;
					}
				}
				if (!found && !object.isEmpty()) {
					if (isAuthor)
						query.getCriterias().add(new AuthorCriteria(new ArrayList<>(object)));
					else
						query.getCriterias().add(new CommitterCriteria(new ArrayList<>(object)));
				}
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				var users = OneDev.getInstance(CommitInfoService.class).getUsers(getProject().getId());
				return users.stream().map(it-> {
					String content;
					if (StringUtils.isNotBlank(it.getEmailAddress()))
						content = it.getName() + " <" + it.getEmailAddress() + ">";
					else
						content = it.getName() + " <>";
					return content.trim();
				}).collect(toList());
			}
		}, false);
		choice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(choice);
	}

	private void addCommitBeforeOrAfterChoice(String componentId, boolean isBefore) {
		var picker = new DatePicker(componentId, new IModel<Date>() {

			@Override
			public void detach() {
			}

			@Override
			public Date getObject() {
				var query = getModelObject();
				for (var criteria: query.getCriterias()) {					
					if (isBefore && (criteria instanceof BeforeCriteria)) {
						return ((BeforeCriteria) criteria).getDate();
					} else if (!isBefore && (criteria instanceof AfterCriteria)) {
						return ((AfterCriteria) criteria).getDate();
					}
				}
				return null;
			}

			@Override
			public void setObject(Date object) {
				var query = getModelObject();
				var criterias = query.getCriterias();
				var found = false;
				for (var i=0; i<criterias.size(); i++) {
					var criteria = criterias.get(i);
					if (isBefore && (criteria instanceof BeforeCriteria)) {
						if (object != null) 
							criterias.set(i, new BeforeCriteria(Lists.newArrayList(DateUtils.formatDate(object))));
						else
							criterias.remove(i);
						found = true;
						break;
					} else if (!isBefore && (criteria instanceof AfterCriteria)) {
						if (object != null) 
							criterias.set(i, new AfterCriteria(Lists.newArrayList(DateUtils.formatDate(object))));
						else
							criterias.remove(i);
						found = true;
						break;
					}
				}
				if (!found && object != null) {
					if (isBefore)
						query.getCriterias().add(new BeforeCriteria(Lists.newArrayList(DateUtils.formatDate(object))));
					else
						query.getCriterias().add(new AfterCriteria(Lists.newArrayList(DateUtils.formatDate(object))));
				}
				getModel().setObject(query);
			}

		}, false);
		picker.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(picker);
	}
	
	protected abstract Project getProject();
	
}
