package io.onedev.server.web.component.build.list;

import static io.onedev.server.search.entity.build.BuildQueryLexer.IsSince;
import static io.onedev.server.search.entity.build.BuildQueryLexer.IsUntil;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.entitymanager.UserManager;
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

		}, new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				var map = new LinkedHashMap<String, String>();
				for (Build.Status status: Build.Status.values()) {
					map.put(status.name(), status.name());
				}
				return map;
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

		}, new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				var map = new LinkedHashMap<String, String>();
				for (String jobName: getBuildManager().getAccessibleJobNames(getProject())) {
					map.put(jobName, jobName);
				}
				return map;
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

		}, new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				var map = new LinkedHashMap<String, String>();
				for (Agent agent: getAgentManager().query()) {
					map.put(agent.getName(), agent.getName());
				}
				return map;
			}
		}, false);

		ranAgentChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(ranAgentChoice);

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
				return getUserManager().query();
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
				var criterias = Criteria.orCriterias(object.stream().map(it->new LabelCriteria(getLabelSpecManager().find(it), BuildQueryLexer.Is)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), LabelCriteria.class, criterias, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				var names = getLabelSpecManager().query().stream().map(it->it.getName()).collect(toList());
				Collections.sort(names);
				var map = new LinkedHashMap<String, String>();
				for (String name: names) {
					map.put(name, name);
				}
				return map;
			}

		}, false);
		labelChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(labelChoice);		

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

	private BuildManager getBuildManager() {
		return OneDev.getInstance(BuildManager.class);
	}

	private LabelSpecManager getLabelSpecManager() {
		return OneDev.getInstance(LabelSpecManager.class);
	}	

	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}

	private AgentManager getAgentManager() {
		return OneDev.getInstance(AgentManager.class);
	}

}
