package io.onedev.server.web.component.issue.operation;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class IssueOperationsPanel extends Panel {

	private static final String ACTION_OPTIONS_ID = "actionOptions";
	
	public IssueOperationsPanel(String id) {
		super(id);
	}

	private void newEmptyActionOptions(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer actionOptions = new WebMarkupContainer(ACTION_OPTIONS_ID);
		actionOptions.setOutputMarkupPlaceholderTag(true);
		actionOptions.setVisible(false);
		if (target != null) {
			replace(actionOptions);
			target.add(actionOptions);
		} else {
			addOrReplace(actionOptions);
		}
	}
	
	@Override
	protected void onBeforeRender() {
		WebMarkupContainer stateContainer = new WebMarkupContainer("state");
		addOrReplace(stateContainer);
		stateContainer.add(new IssueStateBadge("state", new AbstractReadOnlyModel<Issue>() {

			@Override
			public Issue getObject() {
				return getIssue();
			}
			
		}));
		
		RepeatingView transitionsView = new RepeatingView("transitions");

		List<TransitionSpec> transitions = OneDev.getInstance(SettingManager.class).getIssueSetting().getTransitionSpecs();
		
		AtomicReference<Component> activeTransitionLinkRef = new AtomicReference<>(null);  
		for (TransitionSpec transition: transitions) {
			if (transition.canTransitManually(getIssue(), null)) {
				PressButtonTrigger trigger = (PressButtonTrigger) transition.getTrigger();
				AjaxLink<Void> link = new AjaxLink<Void>(transitionsView.newChildId()) {

					private String comment;
					
					@Override
					protected void onInitialize() {
						super.onInitialize();
						Component thisLink = this;
						add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return activeTransitionLinkRef.get() == thisLink?"active":""; 
							}
							
						}));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						activeTransitionLinkRef.set(this);
						Fragment fragment = new Fragment(ACTION_OPTIONS_ID, "transitionFrag", IssueOperationsPanel.this);
						Class<?> fieldBeanClass = IssueUtils.defineFieldBeanClass(getIssue().getProject());
						Serializable fieldBean = getIssue().getFieldBean(fieldBeanClass, true);

						Form<?> form = new Form<Void>("form") {

							@Override
							protected void onError() {
								super.onError();
								RequestCycle.get().find(AjaxRequestTarget.class).add(this);
							}
							
						};
						
						Collection<String> propertyNames = IssueUtils.getPropertyNames(getIssue().getProject(), 
								fieldBeanClass, trigger.getPromptFields());
						BeanEditor editor = BeanContext.edit("fields", fieldBean, propertyNames, false); 
						form.add(editor);
						
						form.add(new CommentInput("comment", new PropertyModel<String>(this, "comment"), false) {

							@Override
							protected AttachmentSupport getAttachmentSupport() {
								return new ProjectAttachmentSupport(getProject(), getIssue().getUUID(), 
										SecurityUtils.canManageIssues(getProject()));
							}

							@Override
							protected Project getProject() {
								return getIssue().getProject();
							}
							
							@Override
							protected List<AttributeModifier> getInputModifiers() {
								return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a comment"));
							}
							
							@Override
							protected List<User> getMentionables() {
								return OneDev.getInstance(UserManager.class).queryAndSort(getIssue().getParticipants());
							}
							
						});

						form.add(new AjaxButton("save") {

							@Override
							protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
								super.onSubmit(target, form);

								Map<String, Object> fieldValues = IssueUtils.getFieldValues(
										editor.newComponentContext(), fieldBean, trigger.getPromptFields());
								IssueChangeManager manager = OneDev.getInstance(IssueChangeManager.class);
								manager.changeState(getIssue(), transition.getToState(), fieldValues, 
										transition.getRemoveFields(), comment);
								target.add(IssueOperationsPanel.this);
							}
							
						});
						form.add(new AjaxLink<Void>("cancel") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								newEmptyActionOptions(target);
								activeTransitionLinkRef.set(null);
								for (Component each: transitionsView)
									target.add(each);
							}
							
							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
							}
							
						});
						fragment.add(form);
						
						fragment.setOutputMarkupId(true);
						IssueOperationsPanel.this.replace(fragment);
						target.add(fragment);
						
						for (Component each: transitionsView)
							target.add(each);
					}

				};
				link.add(new Label("label", trigger.getButtonLabel()));
				transitionsView.add(link);
			}
		}
		
		addOrReplace(transitionsView);

		stateContainer.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (transitionsView.size() != 0)
					return "with-separator";
				else
					return "";
			}
			
		}));
		
		addOrReplace(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, 
				NewIssuePage.paramsOf(getIssue().getProject())));
		
		newEmptyActionOptions(null);
		
		super.onBeforeRender();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(IssueOperationsPanel.this);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Lists.newArrayList(Issue.getWebSocketObservable(getIssue().getId()));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueOperationsCssResourceReference()));
	}

	protected abstract Issue getIssue();
	
}
