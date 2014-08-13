package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.parboiled.common.Preconditions;

import com.google.common.base.Strings;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.component.wiki.WikiTextPanel;

@SuppressWarnings("serial")
public abstract class OldCommitCommentEditor extends Panel {

	protected Form<?> form;
	
	public OldCommitCommentEditor(String id) {
		this(id, Model.of(""));
	}
	
	public OldCommitCommentEditor(String id, IModel<String> textModel) {
		super(id, textModel);
		
		form = createForm("form");
		add(form);
	}

	protected Form<?> createForm(String id) {
		return new Form<Void>(id);
	}
	
	private WebMarkupContainer previewPanel;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Component editor = newEditPanel("edit");
		previewPanel = (WebMarkupContainer) newPreviewPanel("preview");
		
		form.add(editor);
		form.add(previewPanel);
		
		WebMarkupContainer editLink = new WebMarkupContainer("writelink");
		editLink.add(AttributeModifier.replace("href", "#" + editor.getMarkupId(true)));
		form.add(editLink);
		
		WebMarkupContainer previewLink = new WebMarkupContainer("previewlink");
		previewLink.add(AttributeModifier.replace("href", "#" + previewPanel.getMarkupId(true)));
		previewLink.add(new AjaxEventBehavior("shown.bs.tab") {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				
				attributes.getDynamicExtraParameters()
					.add(String.format("return {'text': $('#%s').val() }", inputField.getMarkupId()));
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
			    response.render(OnLoadHeaderItem.forScript(getCallbackScript().toString()));
			}
			
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				IRequestParameters request = RequestCycle.get().getRequest().getRequestParameters();
				String text = request.getParameterValue("text").toString(null);
				updatePreview(target, text);
			}
			
		});
		
		form.add(previewLink);
		
		form.add(createSubmitBar("actions", form));
	}
	
	protected Component createSubmitBar(String id, Form<?> form) {
		Fragment frag = new Fragment(id, "submitfrag", this);
		
		frag.add(createCancelButton("btnCancel", form));
		frag.add(createSubmitButton("btnSubmit", form));
		
		return frag;
	}
	
	abstract protected void onCancel(AjaxRequestTarget target, Form<?> form);
	abstract protected void onSubmit(AjaxRequestTarget target, Form<?> form);
	
	protected Component createCancelButton(String id, Form<?> form) {
		AjaxLink<Void> cancelBtn = new AjaxLink<Void>("btnCancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target, getForm());
			}
			
		};
		
		cancelBtn.add(new Label("name", getCancelButtonLabel()));
		return cancelBtn;
	}
	
	@SuppressWarnings("unchecked")
	protected void clearInput() {
		IModel<String> model = (IModel<String>) getDefaultModel();
		model.setObject("");
	}
	
	protected Component createSubmitButton(String id, Form<?> form) {
		AjaxButton btn = new AjaxButton(id, form) {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				OldCommitCommentEditor.this.onSubmit(target, form);
			}
		};
		
		btn.add(new Label("name", getSubmitButtonLabel()));
		return btn;
	}
	
	protected IModel<String> getCancelButtonLabel() {
		return Model.of("Close Form");
	}
	
	protected IModel<String> getSubmitButtonLabel() {
		return Model.of("Add a comment");
	}
	
	public String getCommentText() {
		return (String) getDefaultModelObject();
	}
	
	TextArea<String> inputField;
	@SuppressWarnings("unchecked")
	private Component newEditPanel(String id) {
		Fragment frag = new Fragment(id, "editfrag", this);
		frag.add(new FeedbackPanel("feedback", form));
		frag.add(inputField = new TextArea<String>("input", (IModel<String>) getDefaultModel()));
		
		return frag;
	}
	
	@SuppressWarnings("unchecked")
	private Component newPreviewPanel(String id) {
		Fragment frag = new Fragment(id, "previewfrag", this);
		User user = Preconditions.checkNotNull(GitPlex.getInstance(UserManager.class).getCurrent());
		frag.add(new UserLink("author", Model.of(user)));
		frag.add(newPreview((IModel<String>) getDefaultModel()));
		
		return frag;
	}
	
	private Component newPreview(final IModel<String> text) {
		return new WikiTextPanel("content", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String str = text.getObject();
				if (Strings.isNullOrEmpty(str)) {
					return "Nothing to be shown";
				} else {
					return str;
				}
			}
		}).setOutputMarkupId(true);
	}
	
	private void updatePreview(AjaxRequestTarget target, String text) {
		Component c = newPreview(Model.of(text));
		previewPanel.addOrReplace(c);
		target.add(c);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(OnDomReadyHeaderItem.forScript(
				String.format("$('#%s a').click(function(e){e.preventDefault(); $(this).tab('show') })", getMarkupId())));
	}
	
	public Form<?> getForm() {
		return form;
	}
}
