package io.onedev.server.web.component.issue.list;

import static io.onedev.server.search.entity.issue.IssueQueryLexer.Is;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.IsSince;
import static io.onedev.server.search.entity.issue.IssueQueryLexer.IsUntil;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.field.spec.BooleanField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.GroupChoiceField;
import io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.issue.BooleanFieldCriteria;
import io.onedev.server.search.entity.issue.ChoiceFieldCriteria;
import io.onedev.server.search.entity.issue.ConfidentialCriteria;
import io.onedev.server.search.entity.issue.FieldCriteria;
import io.onedev.server.search.entity.issue.FieldOperatorCriteria;
import io.onedev.server.search.entity.issue.IterationCriteria;
import io.onedev.server.search.entity.issue.LastActivityDateCriteria;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.search.entity.issue.SubmittedByCriteria;
import io.onedev.server.search.entity.issue.SubmittedByUserCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.datepicker.DatePicker;
import io.onedev.server.web.component.filteredit.FilterEditPanel;
import io.onedev.server.web.component.groupchoice.GroupMultiChoice;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.component.user.choice.UserMultiChoice;

abstract class IssueFilterPanel extends FilterEditPanel<Issue> {
	
	public IssueFilterPanel(String id, IModel<EntityQuery<Issue>> queryModel) {
		super(id, queryModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var issueSetting = OneDev.getInstance(SettingService.class).getIssueSetting();
        var stateChoice = new StringMultiChoice("state", new IModel<Collection<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public Collection<String> getObject() {
				var criterias = getMatchingCriterias(getModelObject().getCriteria(), StateCriteria.class, null);				
				return criterias.stream().map(it->it.getValue()).collect(toList());
			}

			@Override
			public void setObject(Collection<String> object) {	
				var criteria = Criteria.orCriterias(object.stream().map(it -> new StateCriteria(it, Is)).collect(toList()));
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), StateCriteria.class, criteria, null));
				getModel().setObject(query);
			}

		}, new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return issueSetting.getStateSpecs().stream().map(it->it.getName()).collect(toList());
			}

		}, false);
		stateChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(stateChoice);

		var customFieldsView = new RepeatingView("customFields");
		for (var field: issueSetting.getFieldSpecs()) {
			if (field instanceof ChoiceField) {
				var fragment = new Fragment(customFieldsView.newChildId(), "choiceFieldFrag", this);
				fragment.add(new Label("label", field.getName()));

				var choiceField = (ChoiceField) field;
				var choiceValues = new ArrayList<>(choiceField.getChoiceProvider().getChoices(true).keySet());
				var fieldChoice = new StringMultiChoice("choice", new IModel<Collection<String>>() {

					@Override
					public void detach() {
					}
		
					@Override
					public Collection<String> getObject() {
						var criterias = getMatchingCriterias(getModelObject().getCriteria(), ChoiceFieldCriteria.class, getFieldPredicate(field));
						return criterias.stream().map(it->it.getValue()).collect(toList());
					}
		
					@Override
					public void setObject(Collection<String> object) {	
						var criteria = Criteria.orCriterias(object.stream().map(it -> new ChoiceFieldCriteria(field.getName(), it, choiceValues.indexOf(it), Is, field.isAllowMultiple())).collect(toList()));
						var query = getModelObject();
						query.setCriteria(setMatchingCriteria(query.getCriteria(), ChoiceFieldCriteria.class, criteria, getFieldPredicate(field)));
						getModel().setObject(query);
					}
		
				}, new LoadableDetachableModel<List<String>>() {

					@Override
					protected List<String> load() {
						return choiceValues;
					}
		
				}, false);
				fieldChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
		
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
					}
					
				});			
				fragment.add(fieldChoice);				
				customFieldsView.add(fragment);
			} else if (field instanceof UserChoiceField) {
				var fragment = new Fragment(customFieldsView.newChildId(), "choiceFieldFrag", this);
				fragment.add(new Label("label", field.getName()));

				var userChoiceField = (UserChoiceField) field;
				var fieldChoice = new UserMultiChoice("choice", new IModel<Collection<User>>() {

					@Override
					public void detach() {
					}
		
					@Override
					public Collection<User> getObject() {
						var criterias = getMatchingCriterias(getModelObject().getCriteria(), FieldCriteria.class, getFieldPredicate(field));
						var users = new ArrayList<User>();
						for (var criteria: criterias) {
							if (criteria instanceof ChoiceFieldCriteria) {
								var user = getUserService().findByName(((ChoiceFieldCriteria) criteria).getValue());
								if (user != null)
									users.add(user);
							} else if (criteria instanceof FieldOperatorCriteria) {
								var user = SecurityUtils.getUser();
								if (user != null)
									users.add(user);
							}
						}
						return users;
					}
		
					@Override
					public void setObject(Collection<User> object) {	
						var criteria = Criteria.orCriterias(object.stream().map(it -> new ChoiceFieldCriteria(field.getName(), it.getName(), -1, Is, field.isAllowMultiple())).collect(toList()));
						var query = getModelObject();
						query.setCriteria(setMatchingCriteria(query.getCriteria(), FieldCriteria.class, criteria, getFieldPredicate(field)));
						getModel().setObject(query);
					}
		
				}, new LoadableDetachableModel<List<User>>() {
		
					@Override
					protected List<User> load() {
						return userChoiceField.getChoiceProvider().getChoices(true);
					}
		
				}) {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						getSettings().setPlaceholder("");
					}

				};
				fieldChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
		
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
					}
					
				});			
				fragment.add(fieldChoice);				
				customFieldsView.add(fragment);
			} else if (field instanceof GroupChoiceField) {
				var fragment = new Fragment(customFieldsView.newChildId(), "choiceFieldFrag", this);
				fragment.add(new Label("label", field.getName()));

				var groupChoiceField = (GroupChoiceField) field;
				var fieldChoice = new GroupMultiChoice("choice", new IModel<Collection<Group>>() {

					@Override
					public void detach() {
					}
		
					@Override
					public Collection<Group> getObject() {
						var criterias = getMatchingCriterias(getModelObject().getCriteria(), ChoiceFieldCriteria.class, getFieldPredicate(field));
						return criterias.stream().map(it->it.getValue()).map(it->getGroupService().find(it)).filter(it->it!=null).collect(toList());
					}
		
					@Override
					public void setObject(Collection<Group> object) {	
						var criteria = Criteria.orCriterias(object.stream().map(it -> new ChoiceFieldCriteria(field.getName(), it.getName(), -1, Is, field.isAllowMultiple())).collect(toList()));
						var query = getModelObject();
						query.setCriteria(setMatchingCriteria(query.getCriteria(), ChoiceFieldCriteria.class, criteria, getFieldPredicate(field)));
						getModel().setObject(query);
					}
		
				}, new LoadableDetachableModel<Collection<Group>>() {
		
					@Override
					protected List<Group> load() {
						return groupChoiceField.getChoiceProvider().getChoices(true);
					}
		
				}) {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						getSettings().setPlaceholder("");
					}
				};
				fieldChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
		
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
					}
					
				});			
				fragment.add(fieldChoice);				
				customFieldsView.add(fragment);
			} else if (field instanceof BooleanField) {
				var fragment = new Fragment(customFieldsView.newChildId(), "choiceFieldFrag", this);
				fragment.add(new Label("label", field.getName()));

				var fieldChoice = new StringSingleChoice("choice", new IModel<String>() {

					@Override
					public void detach() {
					}
		
					@Override
					public String getObject() {
						var criterias = getMatchingCriterias(getModelObject().getCriteria(), BooleanFieldCriteria.class, getFieldPredicate(field));
						if (criterias.isEmpty())
							return null;
						else
							return String.valueOf(criterias.get(0).getValue());
					}
		
					@Override
					public void setObject(String object) {							
						var criteria = object == null? null : new BooleanFieldCriteria(field.getName(), Boolean.valueOf(object), Is);
						var query = getModelObject();
						query.setCriteria(setMatchingCriteria(query.getCriteria(), BooleanFieldCriteria.class, criteria, getFieldPredicate(field)));
						getModel().setObject(query);
					}
		
				}, new LoadableDetachableModel<List<String>>() {
		
					@Override
					protected List<String> load() {
						return Lists.newArrayList(String.valueOf(true), String.valueOf(false));
					}
		
				}, false);
				fieldChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
		
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
					}
					
				});			
				fragment.add(fieldChoice);				
				customFieldsView.add(fragment);				
			}
		}
		add(customFieldsView);

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
				var criteria = Criteria.orCriterias(object.stream().map(it -> new SubmittedByUserCriteria(it)).collect(toList()));
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

		if (getProject() != null) {
			var iterationChoice = new StringMultiChoice("iteration", new IModel<Collection<String>>() {

				@Override
				public void detach() {
				}

				@Override
				public Collection<String> getObject() {
					var criterias = getMatchingCriterias(getModelObject().getCriteria(), IterationCriteria.class, null);
					return criterias.stream().map(it->it.getIterationName()).collect(toList());
				}

				@Override
				public void setObject(Collection<String> object) {	
					var criteria = Criteria.orCriterias(object.stream().map(it -> new IterationCriteria(it, Is)).collect(toList()));
					var query = getModelObject();
					query.setCriteria(setMatchingCriteria(query.getCriteria(), IterationCriteria.class, criteria, null));
					getModel().setObject(query);
				}

			}, new LoadableDetachableModel<List<String>>() {

				@Override
				protected List<String> load() {
					return getProject().getSortedHierarchyIterations().stream().map(it->it.getName()).collect(toList());
				}

			}, false);
			iterationChoice.add(new AjaxFormComponentUpdatingBehavior("change") {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
				}
				
			});
			add(iterationChoice);		
		} else {
			add(new WebMarkupContainer("iteration").setVisible(false));
		}

		var confidentialCheck = new CheckBox("confidential", new IModel<Boolean>() {

			@Override
			public Boolean getObject() {
				return !getMatchingCriterias(getModelObject().getCriteria(), ConfidentialCriteria.class, null).isEmpty();
			}

			@Override
			public void setObject(Boolean object) {
				var criteria = object? new ConfidentialCriteria() : null;
				var query = getModelObject();
				query.setCriteria(setMatchingCriteria(query.getCriteria(), ConfidentialCriteria.class, criteria, null));
				getModel().setObject(query);
			}

			@Override
			public void detach() {
			}

		});

		confidentialCheck.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
			
		});
		add(confidentialCheck);

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

	@Nullable
	protected abstract Project getProject();

	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}

	private GroupService getGroupService() {
		return OneDev.getInstance(GroupService.class);
	}

	private <T extends FieldCriteria> Predicate<T> getFieldPredicate(FieldSpec field) {
		return it -> it.getFieldName().equals(field.getName());
	}

}
