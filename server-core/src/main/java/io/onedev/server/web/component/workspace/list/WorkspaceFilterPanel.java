package io.onedev.server.web.component.workspace.list;

import static io.onedev.server.search.entity.workspace.WorkspaceQueryLexer.Is;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryLexer.IsSince;
import static io.onedev.server.search.entity.workspace.WorkspaceQueryLexer.IsUntil;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.workspace.ActiveCriteria;
import io.onedev.server.search.entity.workspace.CreateDateCriteria;
import io.onedev.server.search.entity.workspace.CreatedByCriteria;
import io.onedev.server.search.entity.workspace.CreatedByUserCriteria;
import io.onedev.server.search.entity.workspace.ErrorCriteria;
import io.onedev.server.search.entity.workspace.PendingCriteria;
import io.onedev.server.search.entity.workspace.SpecCriteria;
import io.onedev.server.search.entity.workspace.StatusCriteria;
import io.onedev.server.service.UserService;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.datepicker.DatePicker;
import io.onedev.server.web.component.filteredit.FilterEditPanel;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.user.choice.UserMultiChoice;

abstract class WorkspaceFilterPanel extends FilterEditPanel<Workspace> {

	@Inject
	private UserService userService;
		
	public WorkspaceFilterPanel(String id, IModel<EntityQuery<Workspace>> queryModel) {
		super(id, queryModel);
	}

	protected abstract Project getProject();

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var statusChoice = new StringMultiChoice("status", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), StatusCriteria.class, null);
				return criterias.stream().map(it -> it.getStatus().name()).collect(toList());
			}

			@Override
			public void setObject(Collection<String> object) {
				var criterias = new ArrayList<Criteria<Workspace>>();
				for (String status : object) {
					if (status.equals(Workspace.Status.PENDING.name())) {
						criterias.add(new PendingCriteria());
					} else if (status.equals(Workspace.Status.ACTIVE.name())) {
						criterias.add(new ActiveCriteria());
					} else {
						criterias.add(new ErrorCriteria());
					}
				}
				Criteria<Workspace> rootCriteria = Criteria.orCriterias(criterias);
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), StatusCriteria.class, rootCriteria, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return Arrays.stream(Workspace.Status.values()).map(it -> it.name()).collect(toList());
			}

		}, false);
		statusChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}

		});
		add(statusChoice);

		var createdByChoice = new UserMultiChoice("createdBy", new IModel<Collection<User>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<User> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), CreatedByCriteria.class, null);
				return criterias.stream().map(it -> it.getCreator()).filter(Objects::nonNull).collect(toList());
			}

			@Override
			public void setObject(Collection<User> object) {
				var criteria = Criteria.orCriterias(object.stream().map(it -> new CreatedByUserCriteria(it)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), CreatedByUserCriteria.class, criteria, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				var users = userService.query().stream().filter(it -> !it.isDisabled()).collect(toList());
				var cache = userService.cloneCache();
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
		createdByChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}

		});
		add(createdByChoice);

		var specChoice = new StringMultiChoice("spec", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), SpecCriteria.class, null);
				return criterias.stream().map(SpecCriteria::getValue).collect(toList());
			}

			@Override
			public void setObject(Collection<String> object) {
				var criterias = Criteria.orCriterias(object.stream()
						.map(it -> new SpecCriteria(it, Is)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), SpecCriteria.class, criterias, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				var project = getProject();
				if (project == null)
					return new ArrayList<>();
				var names = new ArrayList<>(project.getHierarchyWorkspaceSpecs().stream()
						.map(WorkspaceSpec::getName)
						.collect(toList()));
				Collections.sort(names);
				return names;
			}

		}, false) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject() != null);
			}

		};
		specChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}

		});
		add(specChoice);


		var createdAfterPicker = new DatePicker("createdAfter", new IModel<Date>() {

			@Override
			public void detach() {
			}

			private Predicate<CreateDateCriteria> getPredicate() {
				return t -> t.getOperator() == IsSince;
			}

			@Override
			public Date getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), CreateDateCriteria.class, getPredicate());
				return criterias.isEmpty() ? null : criterias.get(0).getDate();
			}

			@Override
			public void setObject(Date object) {
				var criteria = object != null ? new CreateDateCriteria(DateUtils.formatDate(object), IsSince) : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), CreateDateCriteria.class, criteria, getPredicate()));
				getModel().setObject(query);
			}

		}, false);
		createdAfterPicker.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}

		});
		add(createdAfterPicker);

		var createdBeforePicker = new DatePicker("createdBefore", new IModel<Date>() {

			@Override
			public void detach() {
			}

			private Predicate<CreateDateCriteria> getPredicate() {
				return t -> t.getOperator() == IsUntil;
			}

			@Override
			public Date getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), CreateDateCriteria.class, getPredicate());
				return criterias.isEmpty() ? null : criterias.get(0).getDate();
			}

			@Override
			public void setObject(Date object) {
				var criteria = object != null ? new CreateDateCriteria(DateUtils.formatDate(object), IsUntil) : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), CreateDateCriteria.class, criteria, getPredicate()));
				getModel().setObject(query);
			}

		}, false);
		createdBeforePicker.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}

		});
		add(createdBeforePicker);

		add(AttributeModifier.append("class", "no-autofocus"));
	}

}
