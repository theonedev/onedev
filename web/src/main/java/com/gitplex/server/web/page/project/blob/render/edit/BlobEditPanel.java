package com.gitplex.server.web.page.project.blob.render.edit;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.gitplex.server.web.component.link.ViewStateAwareAjaxLink;
import com.gitplex.server.web.page.project.blob.navigator.BlobNameChanging;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext.Mode;
import com.gitplex.server.web.page.project.blob.render.commitoption.CommitOptionPanel;
import com.gitplex.server.web.util.ajaxlistener.ConfirmLeaveListener;
import com.gitplex.utils.Provider;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public abstract class BlobEditPanel extends Panel {

	protected final BlobRenderContext context;
	
	private FormComponentPanel<byte[]> contentEditor;
	
	private CommitOptionPanel commitOption;
	
	private AbstractDefaultAjaxBehavior recreateBehavior;
		
	public BlobEditPanel(String id, BlobRenderContext context) {
		super(id);
		this.context = context;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("saveTab")
				.setVisible(context.getMode() != Mode.ADD || context.getNewPath() != null));
		
		add(new ViewStateAwareAjaxLink<Void>("cancelLink", true) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				context.onModeChange(target, Mode.VIEW);
			}
			
		});
		
		add(newEditOptions("editOptions"));
		
		add(new Form<Void>("content") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				byte[] content;
				if (context.getMode() == Mode.EDIT)
					content = context.getProject().getBlob(context.getBlobIdent()).getBytes();
				else
					content = new byte[0];
				add(contentEditor = newContentEditor("editor", content));
				add(newContentSubmitLink("submit"));
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();

				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				Preconditions.checkNotNull(target);
				
				commitOption.onContentChange(target);
				
				String script = String.format(
						"gitplex.server.blobEdit.selectTab($('#%s>.blob-edit>.head>.save'));", 
						BlobEditPanel.this.getMarkupId(true));
				target.appendJavaScript(script);
			}

		});
		
		add(commitOption = new CommitOptionPanel("commitOptions", context, new Provider<byte[]>() {

			@Override
			public byte[] get() {
				return contentEditor.getModelObject();
			}
			
		}));
		
		add(recreateBehavior = new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				context.onModeChange(target, Mode.ADD);
			}
			
		});
		
		setOutputMarkupId(true);
	}

	protected Component newContentSubmitLink(String componentId) {
		return new AjaxSubmitLink(componentId) {
		};
	}
	
	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		if ((event.getPayload() instanceof BlobNameChanging) && context.getMode() == Mode.ADD) {
			/*
			 * Blob name is changing and current editor might be inappropriate for current  
			 * blob name, so we need to re-create the editor if the form does not have 
			 * any change yet
			 */
			BlobNameChanging payload = (BlobNameChanging) event.getPayload();
			String script = String.format("gitplex.server.blobEdit.checkClean('%s', %s);", 
					getMarkupId(), recreateBehavior.getCallbackFunction());
			payload.getHandler().appendJavaScript(script);
		}
	}

	protected WebMarkupContainer newEditOptions(String componentId) {
		WebMarkupContainer options = new WebMarkupContainer(componentId);
		options.setVisible(false);
		return options;
	}
	
	protected abstract FormComponentPanel<byte[]> newContentEditor(String componentId, byte[] initialContent);
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new BlobEditResourceReference()));
		
		String script = String.format("gitplex.server.blobEdit.onDomReady('%s');", getMarkupId()); 
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
