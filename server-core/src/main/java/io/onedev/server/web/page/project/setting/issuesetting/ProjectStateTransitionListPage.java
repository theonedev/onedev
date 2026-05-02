package io.onedev.server.web.page.project.setting.issuesetting;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.support.issue.transitionspec.TransitionSpec;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.component.issue.transitionspec.StateTransitionListPanel;

public class ProjectStateTransitionListPage extends ProjectIssueSettingPage {

	public ProjectStateTransitionListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new StateTransitionListPanel("transitions") {
			
			@Override
			protected List<TransitionSpec> getTransitions() {
				return getProject().getIssueSetting().getTransitionSpecs();
			}
			
			@Override
			protected void onSave(AjaxRequestTarget target, int transitionIndex, TransitionSpec transition) {
				String oldAuditContent = null;
				List<TransitionSpec> transitions = getProject().getIssueSetting().getTransitionSpecs();
				if (transitionIndex != -1) {
					var oldTransition = transitions.set(transitionIndex, transition);
					oldAuditContent = VersionedXmlDoc.fromBean(oldTransition).toXML();
				} else {
					transitions.add(transition);
				}
				var newAuditContent = VersionedXmlDoc.fromBean(transition).toXML();
				OneDev.getInstance(ProjectService.class).update(getProject());
				var verb = transitionIndex != -1 ? "changed" : "added";
				auditService.audit(getProject(), verb + " issue transition", oldAuditContent, newAuditContent);
			}
			
			@Override
			protected void onDelete(AjaxRequestTarget target, int transitionIndex) {
				var transition = getProject().getIssueSetting().getTransitionSpecs().remove(transitionIndex);
				var oldAuditContent = VersionedXmlDoc.fromBean(transition).toXML();
				OneDev.getInstance(ProjectService.class).update(getProject());
				auditService.audit(getProject(), "deleted issue transition", oldAuditContent, null);
			}
			
			@Override
			protected void onReorder(AjaxRequestTarget target, int fromIndex, int toIndex) {
				List<TransitionSpec> transitions = getProject().getIssueSetting().getTransitionSpecs();
				var oldAuditContent = VersionedXmlDoc.fromBean(transitions).toXML();
				CollectionUtils.move(transitions, fromIndex, toIndex);
				var newAuditContent = VersionedXmlDoc.fromBean(transitions).toXML();
				OneDev.getInstance(ProjectService.class).update(getProject());
				auditService.audit(getProject(), "changed order of issue transitions", oldAuditContent, newAuditContent);
			}
			
		});
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Issue State Transitions"));
	}
	
}
