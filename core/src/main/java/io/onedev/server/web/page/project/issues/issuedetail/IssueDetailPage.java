package io.onedev.server.web.page.project.issues.issuedetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
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
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.VisitManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.PromptedField;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.model.support.issue.workflow.StateTransition;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.util.ConfirmOnClick;

@SuppressWarnings("serial")
public abstract class IssueDetailPage extends ProjectPage implements InputContext {

	public static final String PARAM_ISSUE = "issue";
	
	private static final String ACTION_OPTIONS_ID = "actionOptions";
	
	private static final String TITLE_ID = "title";
	
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
	
	protected Issue getIssue() {
		return issueModel.getObject();
	}
	
	private Fragment newTitleEditor() {
		Fragment titleEditor = new Fragment(TITLE_ID, "titleEditFrag", this);
		Form<?> form = new Form<Void>("form");
		TextField<String> titleInput = new TextField<String>("title", Model.of(getIssue().getTitle()));
		titleInput.setRequired(true);
		titleInput.setLabel(Model.of("Title"));
		
		form.add(titleInput);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				String prevTitle = getIssue().getTitle();
				getIssue().setTitle(titleInput.getModelObject());
				OneDev.getInstance(IssueChangeManager.class).changeTitle(getIssue(), prevTitle);
				
				Fragment titleViewer = newTitleViewer();
				titleEditor.replaceWith(titleViewer);
				target.add(titleViewer);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(titleEditor);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment titleViewer = newTitleViewer();
				titleEditor.replaceWith(titleViewer);
				target.add(titleViewer);
			}
			
		});		
		
		titleEditor.add(form);
		
		titleEditor.add(new NotificationPanel("feedback", form));
		titleEditor.setOutputMarkupId(true);
		
		return titleEditor;
	}
	
	private Fragment newTitleViewer() {
		Fragment titleViewer = new Fragment(TITLE_ID, "titleViewFrag", this);
		titleViewer.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "#" + getIssue().getNumber() + " - " + getIssue().getTitle();
			}
			
		}));
		
		titleViewer.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment titleEditor = newTitleEditor();
				titleViewer.replaceWith(titleEditor);
				target.add(titleEditor);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(SecurityUtils.canModify(getIssue()));
			}
			
		});
		
		titleViewer.setOutputMarkupId(true);
		
		return titleViewer;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newTitleViewer());
	
		add(new IssueStateLabel("state", issueModel));
		
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
							String prevState = getIssue().getState();
							Map<String, PromptedField> prevFields = getIssue().getPromptedFields();
							
							Set<String> excludedFields = getIssueFieldManager().getExcludedFields(getIssue().getProject(), transition.getToState());

							Form<?> form = new Form<Void>("form") {

								@Override
								protected void onError() {
									super.onError();
									RequestCycle.get().find(AjaxRequestTarget.class).add(this);
								}
								
							};
							
							BeanEditor editor = BeanContext.editBean("fields", fieldBean, excludedFields); 
							form.add(editor);
							
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
									Set<String> promptedFields = new HashSet<>(getIssue().getPromptedFields().keySet());
									
									for (PropertyContext<?> propertyContext: editor.getPropertyContexts()) { 
										if (!propertyContext.isExcluded()) {
											if (propertyContext.isPropertyVisible(editor.getOneContext(propertyContext.getPropertyName()), editor.getBeanDescriptor()))
												promptedFields.add(propertyContext.getDisplayName());
											else
												promptedFields.remove(propertyContext.getDisplayName());
										}
									}
									getIssueChangeManager().changeState(getIssue(), fieldBean, comment, prevState, prevFields, promptedFields);
									
									setResponsePage(IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(getIssue()));
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
		
		add(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, NewIssuePage.paramsOf(getProject())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canRead(getProject()));
			}
			
		});
		
		newEmptyActionOptions(null);
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new IssueTab("Activities", IssueActivitiesPage.class));
		
		add(new Tabbable("issueTabs", tabs).setOutputMarkupId(true));
		
		RequestCycle.get().getListeners().add(new IRequestCycleListener() {
			
			@Override
			public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
			}
			
			@Override
			public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
			}
			
			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
			}
			
			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				return null;
			}
			
			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getUser() != null) { 
					OneDev.getInstance(VisitManager.class).visitIssue(SecurityUtils.getUser(), getIssue());
				}
			}
			
			@Override
			public void onDetach(RequestCycle cycle) {
			}
			
			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}
			
		});	
		

		WebMarkupContainer fieldsContainer = new WebMarkupContainer("fields") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIssue().getFields().isEmpty());
			}
			
		};
		fieldsContainer.setOutputMarkupId(true);
		
		fieldsContainer.add(new ListView<PromptedField>("fields", new LoadableDetachableModel<List<PromptedField>>() {

			@Override
			protected List<PromptedField> load() {
				return new ArrayList<>(getIssue().getPromptedFields().values());
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<PromptedField> item) {
				PromptedField field = item.getModelObject();
				item.add(new Label("name", field.getName()));
				item.add(new FieldValuesPanel("values", item.getModel()));
			}
			
		});
		
		fieldsContainer.add(new ModalLink("editFields") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "fieldEditFrag", IssueDetailPage.this);
				Form<?> form = new Form<Void>("form");

				Serializable fieldBean = OneDev.getInstance(IssueFieldManager.class).readFields(getIssue()); 
				Map<String, PromptedField> prevFields = getIssue().getPromptedFields();
				
				Map<String, PropertyDescriptor> propertyDescriptors = 
						new BeanDescriptor(fieldBean.getClass()).getMapOfDisplayNameToPropertyDescriptor();
				
				Set<String> excludedFields = new HashSet<>();
				for (InputSpec fieldSpec: getProject().getIssueWorkflow().getFields()) {
					if (!getIssue().getPromptedFields().containsKey(fieldSpec.getName()))
						excludedFields.add(propertyDescriptors.get(fieldSpec.getName()).getPropertyName());
				}

				form.add(BeanContext.editBean("editor", fieldBean, excludedFields));
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						OneDev.getInstance(IssueChangeManager.class).changeFields(
								getIssue(), fieldBean, prevFields, getIssue().getPromptedFields().keySet());
						modal.close();
						target.add(fieldsContainer);
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}
					
				});
				
				form.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				form.setOutputMarkupId(true);
				fragment.add(form);
				
				return fragment;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModify(getIssue()));
			}
			
		});
		
		add(fieldsContainer);
		
		Link<Void> deleteLink = new Link<Void>("delete") {

			@Override
			public void onClick() {
				OneDev.getInstance(IssueManager.class).delete(getIssue());
				setResponsePage(IssueListPage.class, IssueListPage.paramsOf(getProject()));
			}
			
		};
		deleteLink.add(new ConfirmOnClick("Do you really want to delete this issue?"));
		deleteLink.setVisible(SecurityUtils.canModify(getIssue()));
		add(deleteLink);		
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

	private IssueChangeManager getIssueChangeManager() {
		return OneDev.getInstance(IssueChangeManager.class);
	}

	private IssueFieldManager getIssueFieldManager() {
		return OneDev.getInstance(IssueFieldManager.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueDetailResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.issueDetail.onDomReady();"));
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
	
	private class IssueTab extends PageTab {

		public IssueTab(String title, Class<? extends Page> pageClass) {
			super(Model.of(title), pageClass);
		}
		
		@Override
		public Component render(String componentId) {
			return new PageTabLink(componentId, this) {

				@Override
				protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
					return new ViewStateAwarePageLink<Void>(linkId, pageClass, paramsOf(getIssue()));
				}
				
			};
		}
		
	}
	
}
