package io.onedev.server.web.page.admin.issuesetting.transitionspec;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.support.issue.transitionspec.TransitionSpec;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.component.issue.transitionspec.StateTransitionListPanel;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

public class StateTransitionListPage extends IssueSettingPage {

	public StateTransitionListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new StateTransitionListPanel("transitions") {
			
			@Override
			protected List<TransitionSpec> getTransitions() {
				return getSetting().getTransitionSpecs();
			}
			
			@Override
			protected void onSave(AjaxRequestTarget target, int transitionIndex, TransitionSpec transition) {
				String oldAuditContent = null;
				if (transitionIndex != -1) {
					var oldTransition = getSetting().getTransitionSpecs().set(transitionIndex, transition);
					oldAuditContent = VersionedXmlDoc.fromBean(oldTransition).toXML();
				} else {
					getSetting().getTransitionSpecs().add(transition);
				}
				var newAuditContent = VersionedXmlDoc.fromBean(transition).toXML();
				OneDev.getInstance(SettingService.class).saveIssueSetting(getSetting());
				var verb = transitionIndex != -1 ? "changed" : "added";
				auditService.audit(null, verb + " issue transition", oldAuditContent, newAuditContent);
			}
			
			@Override
			protected void onDelete(AjaxRequestTarget target, int transitionIndex) {
				var transition = getSetting().getTransitionSpecs().remove(transitionIndex);
				var oldAuditContent = VersionedXmlDoc.fromBean(transition).toXML();
				OneDev.getInstance(SettingService.class).saveIssueSetting(getSetting());
				auditService.audit(null, "deleted issue transition", oldAuditContent, null);
			}
			
			@Override
			protected void onReorder(AjaxRequestTarget target, int fromIndex, int toIndex) {
				var oldAuditContent = VersionedXmlDoc.fromBean(getSetting().getTransitionSpecs()).toXML();
				CollectionUtils.move(getSetting().getTransitionSpecs(), fromIndex, toIndex);
				var newAuditContent = VersionedXmlDoc.fromBean(getSetting().getTransitionSpecs()).toXML();
				OneDev.getInstance(SettingService.class).saveIssueSetting(getSetting());
				auditService.audit(null, "changed order of issue transitions", oldAuditContent, newAuditContent);
			}
			
		});
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>" + _T("Issue State Transitions") + "</span>").setEscapeModelStrings(false);
	}
	
}
