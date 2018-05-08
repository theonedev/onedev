package io.onedev.server.web.page.project.issues.issuedetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueRelationManager;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueRelation;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.model.support.issue.workflow.StateTransition;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.PromptedField;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.issuechoice.SelectToAddIssue;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issueedit.IssueEditPage;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;

@SuppressWarnings("serial")
public class IssueDetailPage extends ProjectPage implements InputContext {

	public static final String PARAM_ISSUE = "issue";
	
	private static final String ACTION_OPTIONS_ID = "actionOptions";
	
	private final IModel<Issue> issueModel;
	
	public IssueDetailPage(PageParameters params) {
		super(params);
		
		issueModel = new LoadableDetachableModel<Issue>() {

			@Override
			protected Issue load() {
				Long issueNumber = params.get(PARAM_ISSUE).toLong();
				Issue issue = OneDev.getInstance(IssueManager.class).find(getProject(), issueNumber);
				if (issue == null)
					throw new EntityNotFoundException("Unable to find issue #" + issueNumber + " in project " + getProject());
				return issue;
			}

		};
	}
	
	private Issue getIssue() {
		return issueModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new IssueStateLabel("state", issueModel));
		add(new Label("number", "#" + getIssue().getNumber()));
		add(new Label("title", getIssue().getTitle()));
		
		add(new UserLink("submitter", User.getForDisplay(getIssue().getSubmitter(), getIssue().getSubmitterName())));
		add(new Label("submitDate", DateUtils.formatAge(getIssue().getSubmitDate())));
		
		RepeatingView transitionsView = new RepeatingView("transitions");

		List<StateTransition> transitions = getProject().getIssueWorkflow().getStateTransitions();
		Collections.sort(transitions, new Comparator<StateTransition>() {

			@Override
			public int compare(StateTransition o1, StateTransition o2) {
				IssueWorkflow workflow = getProject().getIssueWorkflow();
				return workflow.getStateIndex(o1.getToState()) - workflow.getStateIndex(o2.getToState());
			}
			
		});
		for (StateTransition transition: transitions) {
			if (transition.getFromStates().contains(getIssue().getState()) 
					&& transition.getOnAction().getButton() != null 
					&& getLoginUser() != null
					&& transition.getOnAction().getButton().getAuthorized().matches(getProject(), getLoginUser())) {
				boolean applicable = false;
				if (transition.getPrerequisite() == null) {
					applicable = true;
				} else {
					PromptedField field = getIssue().getPromptedFields().get(transition.getPrerequisite().getFieldName());
					List<String> fieldValues;
					if (field != null)
						fieldValues = field.getValues();
					else
						fieldValues = new ArrayList<>();
					if (transition.getPrerequisite().getValueSpecification().matches(fieldValues))
						applicable = true;
				}
				if (applicable) {
					AjaxLink<Void> link = new AjaxLink<Void>(transitionsView.newChildId()) {

						private String comment;
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							Fragment fragment = new Fragment(ACTION_OPTIONS_ID, "transitionFrag", IssueDetailPage.this);
							Serializable fieldBean = getIssueFieldManager().readFields(getIssue());
							Set<String> excludedFields = getIssueFieldManager().getExcludedFields(getIssue().getProject(), transition.getToState());

							Form<?> form = new Form<Void>("form") {

								@Override
								protected void onError() {
									super.onError();
									RequestCycle.get().find(AjaxRequestTarget.class).add(this);
								}
								
							};
							
							form.add(BeanContext.editBean("fields", fieldBean, excludedFields));
							
							form.add(new CommentInput("comment", new PropertyModel<String>(this, "comment"), false) {

								@Override
								protected Project getProject() {
									return getIssue().getProject();
								}
								
								@Override
								protected List<AttributeModifier> getInputModifiers() {
									return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
								}
								
							});
							
							form.add(new AjaxButton("save") {

								@Override
								protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
									super.onSubmit(target, form);
									getIssue().setState(transition.getToState());
									StateSpec stateSpec = Preconditions.checkNotNull(getProject().getIssueWorkflow().getState(getIssue().getState()));
									Set<String> promptedFields = new HashSet<>(getIssue().getPromptedFields().keySet());
									promptedFields.addAll(stateSpec.getFields());
									getIssueManager().save(getIssue(), fieldBean, promptedFields);
									setResponsePage(IssueDetailPage.class, IssueDetailPage.paramsOf(getIssue()));
								}
								
							});
							
							form.add(new AjaxLink<Void>("cancel") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									newEmptyActionOptions(target);
								}
								
							});
							fragment.add(form);
							
							fragment.setOutputMarkupId(true);
							IssueDetailPage.this.replace(fragment);
							target.add(fragment);
						}
						
					};
					link.add(new Label("label", transition.getOnAction().getButton().getName()));
					transitionsView.add(link);
				}
			}
		}
		
		add(transitionsView);
		
		add(new Link<Void>("edit") {

			@Override
			public void onClick() {
				setResponsePage(IssueEditPage.class, IssueEditPage.paramsOf(getIssue()));
			}
			
		}.setVisible(SecurityUtils.canModify(getIssue())));
		
		Link<Void> deleteLink = new Link<Void>("delete") {

			@Override
			public void onClick() {
				getIssueManager().delete(getIssue());
				setResponsePage(IssueListPage.class, IssueListPage.paramsOf(getProject()));
			}
			
		};
		deleteLink.add(new ConfirmOnClick("Do you really want to delete this issue?"));
		deleteLink.setVisible(SecurityUtils.canModify(getIssue()));
		add(deleteLink);
		
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canRead(getProject()));
			}
			
		});
		
		newEmptyActionOptions(null);
		
		add(new MarkdownViewer("description", Model.of(getIssue().getDescription()), null));

		add(new ListView<PromptedField>("fields", new LoadableDetachableModel<List<PromptedField>>() {

			@Override
			protected List<PromptedField> load() {
				return new ArrayList<>(getIssue().getPromptedFields().values());
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getFields().isEmpty());
			}

			@Override
			protected void populateItem(ListItem<PromptedField> item) {
				PromptedField field = item.getModelObject();
				item.add(new Label("name", field.getName()));
				item.add(new FieldValuesPanel("values", item.getModel()));
			}
			
		});

		WebMarkupContainer relations = new WebMarkupContainer("relations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getRelationsByCurrent().isEmpty() || SecurityUtils.canWrite(getProject()));
			}
			
		};
		
		relations.setOutputMarkupId(true);
		add(relations);
		
		IModel<List<Issue>> relationsModel = new LoadableDetachableModel<List<Issue>>() {

			@Override
			protected List<Issue> load() {
				Set<Issue> set = new HashSet<>();
				for (IssueRelation relation: getIssue().getRelationsByCurrent())
					set.add(relation.getOther());
				for (IssueRelation relation: getIssue().getRelationsByOther())
					set.add(relation.getCurrent());
				List<Issue> list = new ArrayList<>(set);
				Collections.sort(list, new Comparator<Issue>() {

					@Override
					public int compare(Issue o1, Issue o2) {
						return (int) (o2.getNumber() - o1.getNumber());
					}
					
				});
				return list;
			}
			
		};
		
		relations.add(new ListView<Issue>("relations", relationsModel) {

			@Override
			protected void populateItem(ListItem<Issue> item) {
				Issue issue = item.getModelObject();
				Link<Void> link = new BookmarkablePageLink<Void>("link", IssueDetailPage.class, IssueDetailPage.paramsOf(issue));
				link.add(new Label("label", "#" + issue.getNumber()));
				item.add(link);
				
				item.add(new IssueStateLabel("state", item.getModel()));
				
				item.add(new AjaxLink<Void>("remove") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener("Really want to remove this relation?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						IssueRelationManager issueRelationManager = OneDev.getInstance(IssueRelationManager.class);
						IssueRelation relation = issueRelationManager.find(getIssue(), item.getModelObject());
						if (relation != null) {
							issueRelationManager.delete(relation);
							getIssue().getRelationsByCurrent().remove(relation);
						}
						
						relation = issueRelationManager.find(item.getModelObject(), getIssue());
						if (relation != null) {
							issueRelationManager.delete(relation);
							getIssue().getRelationsByOther().remove(relation);
						}
						
						relationsModel.detach();
						
						target.add(relations);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canWrite(getProject()));
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
		
		relations.add(new NotificationPanel("feedback", relations));
		
		add(new SelectToAddIssue("addRelation", new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getProject();
			}
			
		}) {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setPlaceholder("Add related issue...");
			}

			@Override
			protected void onSelect(AjaxRequestTarget target, Issue selection) {
				if (selection.equals(getIssue())) {
					relations.error("Can not add self as related issue");
				} else {
					IssueRelationManager issueRelationManager = OneDev.getInstance(IssueRelationManager.class);
					IssueRelation relationByCurrent = issueRelationManager.find(getIssue(), selection);
					IssueRelation relationByOther = issueRelationManager.find(selection, getIssue());
					if (relationByCurrent == null && relationByOther == null) {
						relationByCurrent = new IssueRelation();
						relationByCurrent.setCurrent(getIssue());
						relationByCurrent.setOther(selection);
						getIssue().getRelationsByCurrent().add(relationByCurrent);
						issueRelationManager.save(relationByCurrent);
					}
				}
				target.add(relations);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canWrite(getProject()));
			}
			
		});
	}
	
	private void newEmptyActionOptions(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer actionOptions = new WebMarkupContainer(ACTION_OPTIONS_ID);
		actionOptions.setOutputMarkupPlaceholderTag(true);
		actionOptions.setVisible(false);
		if (target != null) {
			replace(actionOptions);
			target.add(actionOptions);
		} else {
			add(actionOptions);
		}
	}

	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}

	private IssueFieldManager getIssueFieldManager() {
		return OneDev.getInstance(IssueFieldManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueDetailResourceReference()));
	}

	@Override
	protected void onDetach() {
		issueModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Issue issue) {
		PageParameters params = ProjectPage.paramsOf(issue.getProject());
		params.set(PARAM_ISSUE, issue.getNumber());
		return params;
	}

	@Override
	public List<String> getInputNames() {
		return getProject().getIssueWorkflow().getInputNames();
	}

	@Override
	public InputSpec getInput(String inputName) {
		return getProject().getIssueWorkflow().getInput(inputName);
	}
	
	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
}
