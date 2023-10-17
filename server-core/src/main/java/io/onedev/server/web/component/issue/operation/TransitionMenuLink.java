package io.onedev.server.web.component.issue.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.onedev.server.web.page.base.BasePage;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.model.support.issue.transitiontrigger.PressButtonTrigger;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.transitionoption.TransitionOptionPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public abstract class TransitionMenuLink extends MenuLink {

	private final IModel<List<TransitionSpec>> manualTransitionsModel = 
			new LoadableDetachableModel<List<TransitionSpec>>() {

		@Override
		protected List<TransitionSpec> load() {
			return OneDev.getInstance(SettingManager.class).getIssueSetting().getTransitionSpecs()
					.stream()
					.filter(it->it.canTransitManually(getIssue(), null))
					.collect(Collectors.toList());
		}
		
	};
	
	public TransitionMenuLink(String id) {
		super(id);
	}

	@Override
	protected void onDetach() {
		manualTransitionsModel.detach();
		super.onDetach();
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		if (!manualTransitionsModel.getObject().isEmpty()) {
			return super.newContent(id, dropdown);
		} else {
			return new Label(id, "<div class='px-3 py-2'><i>No applicable transitions or no permission to transit</i></div>")
					.setEscapeModelStrings(false);
		}
	}

	@Override
	protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
		List<MenuItem> menuItems = new ArrayList<>();
		
		for (TransitionSpec transition: manualTransitionsModel.getObject()) {
			PressButtonTrigger trigger = (PressButtonTrigger) transition.getTrigger();
			menuItems.add(new MenuItem() {

				@Override
				public String getLabel() {
					return trigger.getButtonLabel();
				}

				@Override
				public WebMarkupContainer newLink(String id) {
					return new ModalLink(id) {
						
						@Override
						public void onClick(AjaxRequestTarget target) {
							super.onClick(target);
							dropdown.close();
						}

						@Override
						protected Component newContent(String id, ModalPanel modal) {
							return new TransitionOptionPanel(id) {

								@Override
								protected Issue getIssue() {
									return TransitionMenuLink.this.getIssue();
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
									((BasePage)getPage()).notifyObservablesChange(target, getIssue().getChangeObservables(true));
									modal.close();
								}

								@Override
								protected void onCancel(AjaxRequestTarget target) {
									modal.close();
								}
								
							};
						}
						
					};
				}
				
			});
		}

		return menuItems;		
	}

	protected abstract Issue getIssue();
	
}
