package io.onedev.server.web.component.pullrequest.list;

import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.Is;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.IsSince;
import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.IsUntil;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.service.LabelSpecService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.pullrequest.AssignedToCriteria;
import io.onedev.server.search.entity.pullrequest.AssignedToUserCriteria;
import io.onedev.server.search.entity.pullrequest.DiscardedCriteria;
import io.onedev.server.search.entity.pullrequest.LabelCriteria;
import io.onedev.server.search.entity.pullrequest.LastActivityDateCriteria;
import io.onedev.server.search.entity.pullrequest.MergedCriteria;
import io.onedev.server.search.entity.pullrequest.OpenCriteria;
import io.onedev.server.search.entity.pullrequest.StatusCriteria;
import io.onedev.server.search.entity.pullrequest.SubmittedByCriteria;
import io.onedev.server.search.entity.pullrequest.SubmittedByUserCriteria;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.datepicker.DatePicker;
import io.onedev.server.web.component.filteredit.FilterEditPanel;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.user.choice.UserMultiChoice;

class PullRequestFilterPanel extends FilterEditPanel<PullRequest> {
	
	public PullRequestFilterPanel(String id, IModel<EntityQuery<PullRequest>> queryModel) {
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
				var criterias = new ArrayList<Criteria<PullRequest>>();
				for (String status: object) {
					if (status.equals(PullRequest.Status.OPEN.name())) {
						criterias.add(new OpenCriteria());
					} else if (status.equals(PullRequest.Status.DISCARDED.name())) {
						criterias.add(new DiscardedCriteria());
					} else {
						criterias.add(new MergedCriteria());
					}
				}
				Criteria<PullRequest> rootCriteria = Criteria.orCriterias(criterias);
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), StatusCriteria.class, rootCriteria, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return Arrays.stream(PullRequest.Status.values()).map(it->it.name()).collect(toList());
			}
		}, false);
		statusChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(statusChoice);

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

		var assignedToChoice = new UserMultiChoice("assignedTo", new IModel<Collection<User>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<User> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), AssignedToCriteria.class, null);
				return criterias.stream().map(it->it.getUser()).filter(Objects::nonNull).collect(toList());
			}

			@Override
			public void setObject(Collection<User> object) {	
				var criteria = Criteria.orCriterias(object.stream().map(it->new AssignedToUserCriteria(it)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), AssignedToUserCriteria.class, criteria, null));
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
		assignedToChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});			
		add(assignedToChoice);

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

		}, false) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder("");
			}
		};
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

}
