package io.onedev.server.web.component.iteration.actions;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.IterationService;
import io.onedev.server.model.Iteration;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.page.project.issues.iteration.IterationEditPage;

public abstract class IterationActionsPanel extends GenericPanel<Iteration> {

	public IterationActionsPanel(String id, IModel<Iteration> model) {
		super(id, model);
	}

	private Iteration getIteration() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("reopen") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				getIteration().setClosed(false);
				getIterationService().createOrUpdate(getIteration());
				getAuditService().audit(getIteration().getProject(), "reopened iteration \"" + getIteration().getName() + "\"", null, null);
				target.add(IterationActionsPanel.this);
				onUpdated(target);
				getSession().success(MessageFormat.format(_T("Iteration \"{0}\" reopened"), getIteration().getName()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getIteration().isClosed());
			}
			
		});
		
		add(new AjaxLink<Void>("close") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getIteration().isClosed());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getIteration().setClosed(true);
				getIterationService().createOrUpdate(getIteration());
				getAuditService().audit(getIteration().getProject(), "closed iteration \"" + getIteration().getName() + "\"", null, null);
				target.add(IterationActionsPanel.this);
				onUpdated(target);
				getSession().success(MessageFormat.format(_T("Iteration \"{0}\" closed"), getIteration().getName()));
			}

		});
		
		add(new BookmarkablePageLink<Void>("edit", IterationEditPage.class, 
				IterationEditPage.paramsOf(getIteration())));

		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(
						MessageFormat.format(_T("Do you really want to delete iteration \"{0}\"?"), getIteration().getName())));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				getIterationService().delete(getIteration());
				var oldAuditContent = VersionedXmlDoc.fromBean(getIteration()).toXML();
				getAuditService().audit(getIteration().getProject(), "deleted iteration \"" + getIteration().getName() + "\"", oldAuditContent, null);
				target.add(IterationActionsPanel.this);
				onDeleted(target);
				getSession().success(MessageFormat.format(_T("Iteration \"{0}\" deleted"), getIteration().getName()));
			}

		});		
		
		setOutputMarkupId(true);
	}
	
	private IterationService getIterationService() {
		return OneDev.getInstance(IterationService.class);
	}

	private AuditService getAuditService() {
		return OneDev.getInstance(AuditService.class);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	protected abstract void onUpdated(AjaxRequestTarget target);
	
}
