package io.onedev.server.web.component.build.list;

import static io.onedev.server.search.entity.build.BuildQueryLexer.IsSince;
import static io.onedev.server.search.entity.build.BuildQueryLexer.IsUntil;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.build.BuildQueryLexer;
import io.onedev.server.search.entity.build.CancelledCriteria;
import io.onedev.server.search.entity.build.FailedCriteria;
import io.onedev.server.search.entity.build.JobCriteria;
import io.onedev.server.search.entity.build.LabelCriteria;
import io.onedev.server.search.entity.build.PendingCriteria;
import io.onedev.server.search.entity.build.RanOnCriteria;
import io.onedev.server.search.entity.build.RunningCriteria;
import io.onedev.server.search.entity.build.StatusCriteria;
import io.onedev.server.search.entity.build.SubmitDateCriteria;
import io.onedev.server.search.entity.build.SubmittedByCriteria;
import io.onedev.server.search.entity.build.SubmittedByUserCriteria;
import io.onedev.server.search.entity.build.SuccessfulCriteria;
import io.onedev.server.search.entity.build.TimedOutCriteria;
import io.onedev.server.search.entity.build.WaitingCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.datepicker.DatePicker;
import io.onedev.server.web.component.filteredit.FilterEditPanel;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.user.choice.UserMultiChoice;

abstract class BuildFilterPanel extends FilterEditPanel<Build> {
	
	public BuildFilterPanel(String id, IModel<EntityQuery<Build>> queryModel) {
		super(id, queryModel);
	}

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
				return criterias.stream().map(it->it.getStatus().name()).collect(toList());
			}

			@Override
			public void setObject(Collection<String> object) {	
				var criterias = new ArrayList<Criteria<Build>>();
				for (String status: object) {
					if (status.equals(Build.Status.SUCCESSFUL.name())) {
						criterias.add(new SuccessfulCriteria());
					} else if (status.equals(Build.Status.FAILED.name())) {
						criterias.add(new FailedCriteria());
					} else if (status.equals(Build.Status.TIMED_OUT.name())) {
						criterias.add(new TimedOutCriteria());
					} else if (status.equals(Build.Status.CANCELLED.name())) {
						criterias.add(new CancelledCriteria());
					} else if (status.equals(Build.Status.RUNNING.name())) {
						criterias.add(new RunningCriteria());
					} else if (status.equals(Build.Status.PENDING.name())) {
						criterias.add(new PendingCriteria());
					} else if (status.equals(Build.Status.WAITING.name())) {
						criterias.add(new WaitingCriteria());
					}
				}
				Criteria<Build> rootCriteria = Criteria.orCriterias(criterias);
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), StatusCriteria.class, rootCriteria, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return Arrays.stream(Build.Status.values()).map(it->it.name()).collect(toList());
			}

		}, false);
		statusChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(statusChoice);

        var jobChoice = new StringMultiChoice("job", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), JobCriteria.class, null);
				return criterias.stream().map(it->it.getJobName()).collect(toList());
			}

			@Override
			public void setObject(Collection<String> object) {	
				var criterias = Criteria.orCriterias(object.stream().map(it->new JobCriteria(it, BuildQueryLexer.Is)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), JobCriteria.class, criterias, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				var subject = SecurityUtils.getSubject();
				var jobNames = new ArrayList<>(getBuildService().getAccessibleJobNames(subject, getProject()));
				Collections.sort(jobNames);
				return jobNames;
			}
		}, false) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject() != null);
			}
		};

		jobChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(jobChoice);

		var submittedByChoice = new UserMultiChoice("submittedBy", new IModel<Collection<User>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<User> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), SubmittedByCriteria.class, null);
				return criterias.stream().map(it->it.getUser()).filter(Objects::nonNull).collect(toList());
			}

			@Override
			public void setObject(Collection<User> object) {	
				var criteria = Criteria.orCriterias(object.stream().map(it->new SubmittedByUserCriteria(it)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), SubmittedByUserCriteria.class, criteria, null));
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
		submittedByChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});			
		add(submittedByChoice);

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
				var criterias = Criteria.orCriterias(object.stream().map(it->new LabelCriteria(getLabelSpecService().find(it), BuildQueryLexer.Is)).collect(toList()));
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

        var ranAgentChoice = new StringMultiChoice("ranAgent", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), RanOnCriteria.class, null);
				return criterias.stream().map(it->it.getValue()).collect(toList());
			}

			@Override
			public void setObject(Collection<String> object) {	
				var criterias = Criteria.orCriterias(object.stream().map(it->new RanOnCriteria(it)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), RanOnCriteria.class, criterias, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				var agents = new ArrayList<>(getAgentService().query());
				agents.sort(Comparator.comparing(Agent::getName));
				return agents.stream().map(it->it.getName()).collect(toList());
			}
		}, false);

		ranAgentChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(ranAgentChoice);

		var submittedAfterPicker = new DatePicker("submittedAfter", new IModel<Date>() {

			@Override
			public void detach() {
			}

			private Predicate<SubmitDateCriteria> getPredicate() {
				return t -> t.getOperator() == IsSince;
			}

			@Override
			public Date getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), SubmitDateCriteria.class, getPredicate());
				if (criterias.isEmpty())
					return null;
				else
					return criterias.get(0).getDate();
			}

			@Override
			public void setObject(Date object) {
				var criteria = object!=null? new SubmitDateCriteria(DateUtils.formatDate(object), IsSince) : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), SubmitDateCriteria.class, criteria, getPredicate()));
				getModel().setObject(query);
			}

		}, false);
		submittedAfterPicker.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(submittedAfterPicker);

		var submittedBeforePicker = new DatePicker("submittedBefore", new IModel<Date>() {

			@Override
			public void detach() {
			}

			private Predicate<SubmitDateCriteria> getPredicate() {
				return t -> t.getOperator() == IsUntil;
			}

			@Override
			public Date getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), SubmitDateCriteria.class, getPredicate());
				if (criterias.isEmpty())
					return null;
				else
					return criterias.get(0).getDate();
			}

			@Override
			public void setObject(Date object) {
				var criteria = object!=null? new SubmitDateCriteria(DateUtils.formatDate(object), IsUntil) : null;
				var query = getModelObject();	
				query.setCriteria(setMatchingCriteria(query.getCriteria(), SubmitDateCriteria.class, criteria, getPredicate()));
				getModel().setObject(query);
			}

		}, false);
		submittedBeforePicker.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(submittedBeforePicker);			
	}
	
	@Nullable
	protected abstract Project getProject();

	private BuildService getBuildService() {
		return OneDev.getInstance(BuildService.class);
	}

	private LabelSpecService getLabelSpecService() {
		return OneDev.getInstance(LabelSpecService.class);
	}	

	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}

	private AgentService getAgentService() {
		return OneDev.getInstance(AgentService.class);
	}

}
