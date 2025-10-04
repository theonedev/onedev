package io.onedev.server.web.component.issue.operation;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.transitionspec.ManualSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.transitionoption.TransitionOptionPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.base.BasePage;

public abstract class TransitionMenuLink extends MenuLink {

	private final IModel<List<ManualSpec>> manualTransitionsModel =
			new LoadableDetachableModel<>() {

				@Override
				protected List<ManualSpec> load() {
					var subject = SecurityUtils.getSubject();
					var manualSpecs = new ArrayList<ManualSpec>();
					for (var transitionSpec: OneDev.getInstance(SettingService.class).getIssueSetting().getTransitionSpecs()) {
						if (transitionSpec instanceof ManualSpec) {
							ManualSpec manualSpec = (ManualSpec) transitionSpec;
							if (manualSpec.canTransit(subject, getIssue(), null)) 
								manualSpecs.add(manualSpec);								
						}
					}
					return manualSpecs;
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
			return new Label(id, "<div class='px-3 py-2'><i>" + _T("No applicable transitions or no permission to transit") + "</i></div>")
					.setEscapeModelStrings(false);
		}
	}

	@Override
	protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
		List<MenuItem> menuItems = new ArrayList<>();

		Set<String> encounteredStates = new HashSet<>();
		for (ManualSpec transition: manualTransitionsModel.getObject()) {
			var toStates = transition.getToStates();
			if (toStates.isEmpty())
				toStates = new ArrayList<>(OneDev.getInstance(SettingService.class).getIssueSetting().getStateSpecMap().keySet());
			for (var toState: toStates) {
				if (!toState.equals(getIssue().getState()) && encounteredStates.add(toState)) {
					menuItems.add(new MenuItem() {

						@Override
						public String getLabel() {
							return toState;
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
										protected String getToState() {
											return toState;
										}

										@Override
										protected ManualSpec getTransition() {
											return transition;
										}

										@Override
										protected void onTransit(AjaxRequestTarget target, Map<String, Object> fieldValues, String comment) {
											IssueChangeService service = OneDev.getInstance(IssueChangeService.class);
											var user = SecurityUtils.getUser();
											service.changeState(user, getIssue(), toState, fieldValues, transition.getPromptFields(), transition.getRemoveFields(), comment);
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
			}
		}

		return menuItems;		
	}

	protected abstract Issue getIssue();
	
}
