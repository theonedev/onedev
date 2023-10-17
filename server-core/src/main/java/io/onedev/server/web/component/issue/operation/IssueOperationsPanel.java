package io.onedev.server.web.component.issue.operation;

import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.transitionoption.TransitionOptionPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
		stateContainer.add(new IssueStateBadge("state", new LoadableDetachableModel<>() {
			@Override
			protected Issue load() {
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
						
						TransitionOptionPanel transitionOption = new TransitionOptionPanel(ACTION_OPTIONS_ID) {

							@Override
							protected Issue getIssue() {
								return IssueOperationsPanel.this.getIssue();
							}

							@Override
							protected PressButtonTrigger getTrigger() {
								return trigger;
							}

							@Override
							protected void onTransit(AjaxRequestTarget target, Map<String, Object> fieldValues,
									String comment) {
								IssueChangeManager manager = OneDev.getInstance(IssueChangeManager.class);
								manager.changeState(getIssue(), transition.getToState(), fieldValues, 
										transition.getRemoveFields(), comment);
								target.add(IssueOperationsPanel.this);
								((BasePage)getPage()).notifyObservablesChange(target, getIssue().getChangeObservables(true));
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								newEmptyActionOptions(target);
								activeTransitionLinkRef.set(null);
								for (Component each: transitionsView)
									target.add(each);
							}
							
						};
						
						IssueOperationsPanel.this.replace(transitionOption);
						target.add(transitionOption);
						
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
		
		add(new ChangeObserver() {
			
			@Override
			public Collection<String> findObservables() {
				return Lists.newArrayList(Issue.getDetailChangeObservable(getIssue().getId()));
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
