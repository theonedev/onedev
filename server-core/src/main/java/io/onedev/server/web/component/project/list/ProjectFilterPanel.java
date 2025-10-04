package io.onedev.server.web.component.project.list;

import static io.onedev.server.search.entity.project.ProjectQueryLexer.Is;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.IsSince;
import static io.onedev.server.search.entity.project.ProjectQueryLexer.IsUntil;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.project.ChildrenOfCriteria;
import io.onedev.server.search.entity.project.ForksOfCriteria;
import io.onedev.server.search.entity.project.LabelCriteria;
import io.onedev.server.search.entity.project.LastActivityDateCriteria;
import io.onedev.server.search.entity.project.LeafsCriteria;
import io.onedev.server.search.entity.project.OwnedByCriteria;
import io.onedev.server.search.entity.project.OwnedByUserCriteria;
import io.onedev.server.search.entity.project.RootsCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.datepicker.DatePicker;
import io.onedev.server.web.component.filteredit.FilterEditPanel;
import io.onedev.server.web.component.project.choice.ProjectMultiChoice;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.user.choice.UserMultiChoice;

class ProjectFilterPanel extends FilterEditPanel<Project> {
	
	public ProjectFilterPanel(String id, IModel<EntityQuery<Project>> queryModel) {
		super(id, queryModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var rootsCheck = new CheckBox("roots", new IModel<Boolean>() {

			@Override
			public Boolean getObject() {
				return !getMatchingCriterias(getModelObject().getCriteria(), RootsCriteria.class, null).isEmpty();
			}

			@Override
			public void setObject(Boolean object) {
				var criteria = object? new RootsCriteria() : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), RootsCriteria.class, criteria, null));
				getModel().setObject(query);
			}

			@Override
			public void detach() {
			}

		});
		rootsCheck.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(rootsCheck);

		var leafsCheck = new CheckBox("leafs", new IModel<Boolean>() {

			@Override
			public Boolean getObject() {
				return !getMatchingCriterias(getModelObject().getCriteria(), LeafsCriteria.class, null).isEmpty();
			}

			@Override
			public void setObject(Boolean object) {
				var criteria = object? new LeafsCriteria() : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), LeafsCriteria.class, criteria, null));
				getModel().setObject(query);
			}

			@Override
			public void detach() {
			}

		});
		leafsCheck.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(leafsCheck);

        var childrenChoice = new ProjectMultiChoice("children", new IModel<Collection<Project>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<Project> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), ChildrenOfCriteria.class, null);
				return criterias.stream().map(it->getProjectService().findByPath(it.getParentPath())).filter(Objects::nonNull).collect(toList());
			}

			@Override
			public void setObject(Collection<Project> object) {	
				var criterias = Criteria.orCriterias(object.stream().map(it->new ChildrenOfCriteria(it.getPath())).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), ChildrenOfCriteria.class, criterias, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				var projects = new ArrayList<>(SecurityUtils.getAuthorizedProjects(new AccessProject()));
				var cache = getProjectService().cloneCache();
				projects.sort(cache.comparingPath());
				return projects;
			}
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder("");
			}

		};

		childrenChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(childrenChoice);		

        var forksChoice = new ProjectMultiChoice("forks", new IModel<Collection<Project>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<Project> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), ForksOfCriteria.class, null);
				return criterias.stream().map(it->getProjectService().findByPath(it.getForkedFromPath())).filter(Objects::nonNull).collect(toList());
			}

			@Override
			public void setObject(Collection<Project> object) {	
				var criterias = Criteria.orCriterias(object.stream().map(it->new ForksOfCriteria(it.getPath())).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), ForksOfCriteria.class, criterias, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<Project>>() {

			@Override
			protected List<Project> load() {
				var projects = new ArrayList<>(SecurityUtils.getAuthorizedProjects(new AccessProject()));
				var cache = getProjectService().cloneCache();
				projects.sort(cache.comparingPath());
				return projects;
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder("");
			}

		};

		forksChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(forksChoice);		

		var ownedByChoice = new UserMultiChoice("ownedBy", new IModel<Collection<User>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<User> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), OwnedByCriteria.class, null);
				return criterias.stream().map(it->it.getUser()).filter(Objects::nonNull).collect(toList());
			}

			@Override
			public void setObject(Collection<User> object) {	
				var criteria = Criteria.orCriterias(object.stream().map(it->new OwnedByUserCriteria(it)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), OwnedByUserCriteria.class, criteria, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {			
				var users = getUserService().query().stream().filter(it -> !it.isDisabled()).collect(toList());
				var cache = getUserService().cloneCache();
				users.sort(cache.comparingDisplayName(new ArrayList<>()));
				return users;
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder("");
			}

		};
		ownedByChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});			
		add(ownedByChoice);

        var labelChoice = new StringMultiChoice("label", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), LabelCriteria.class, null);
				return criterias.stream()
						.map(it->it.getLabelSpec().getName())
						.collect(toList());
			}

			@Override
			public void setObject(Collection<String> object) {	
				var criterias = Criteria.orCriterias(object.stream().map(it->new LabelCriteria(getLabelSpecService().find(it), Is)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), LabelCriteria.class, criterias, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				var names = getLabelSpecService().query().stream().map(it->it.getName()).collect(toList());
				Collections.sort(names);
				return names;
			}

		}, false);
		labelChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(labelChoice);		

		var activeSincePicker = new DatePicker("activeSince", new IModel<Date>() {

			@Override
			public void detach() {
			}

			private Predicate<LastActivityDateCriteria> getPredicate() {
				return t -> t.getOperator() == IsSince;
			}

			@Override
			public Date getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), LastActivityDateCriteria.class, getPredicate());
				if (criterias.isEmpty())
					return null;
				else
					return criterias.get(0).getDate();
			}

			@Override
			public void setObject(Date object) {
				var criteria = object!=null? new LastActivityDateCriteria(DateUtils.formatDate(object), IsSince) : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), LastActivityDateCriteria.class, criteria, getPredicate()));
				getModel().setObject(query);
			}

		}, false);
		activeSincePicker.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(activeSincePicker);

		var notActiveSincePicker = new DatePicker("notActiveSince", new IModel<Date>() {

			@Override
			public void detach() {
			}

			private Predicate<LastActivityDateCriteria> getPredicate() {
				return t -> t.getOperator() == IsUntil;
			}

			@Override
			public Date getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), LastActivityDateCriteria.class, getPredicate());
				if (criterias.isEmpty())
					return null;
				else
					return criterias.get(0).getDate();
			}

			@Override
			public void setObject(Date object) {
				var criteria = object!=null? new LastActivityDateCriteria(DateUtils.formatDate(object), IsUntil) : null;
				var query = getModelObject();	
				query.setCriteria(setMatchingCriteria(query.getCriteria(), LastActivityDateCriteria.class, criteria, getPredicate()));
				getModel().setObject(query);
			}

		}, false);
		notActiveSincePicker.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(notActiveSincePicker);
	}

	private LabelSpecService getLabelSpecService() {
		return OneDev.getInstance(LabelSpecService.class);
	}	

	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}

	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

}
