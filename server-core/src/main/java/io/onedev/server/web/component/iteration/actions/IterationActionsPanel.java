package io.onedev.server.web.component.iteration.actions;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IterationManager;
import io.onedev.server.model.Iteration;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.page.project.issues.iteration.IterationEditPage;

@SuppressWarnings("serial")
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
				getIterationManager().createOrUpdate(getIteration());
				target.add(IterationActionsPanel.this);
				onUpdated(target);
				getSession().success("Iteratioin '" + getIteration().getName() + "' reopened");
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
				getIterationManager().createOrUpdate(getIteration());
				target.add(IterationActionsPanel.this);
				onUpdated(target);
				getSession().success("Iteration '" + getIteration().getName() + "' closed");
			}

		});
		
		add(new BookmarkablePageLink<Void>("edit", IterationEditPage.class, 
				IterationEditPage.paramsOf(getIteration())));

		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(
						"Do you really want to delete iteration '" + getIteration().getName() + "'?"));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				getIterationManager().delete(getIteration());
				target.add(IterationActionsPanel.this);
				onDeleted(target);
				getSession().success("Iteration '" + getIteration().getName() + "' deleted");
			}

		});		
		
		setOutputMarkupId(true);
	}
	
	private IterationManager getIterationManager() {
		return OneDev.getInstance(IterationManager.class);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	protected abstract void onUpdated(AjaxRequestTarget target);
	
}
