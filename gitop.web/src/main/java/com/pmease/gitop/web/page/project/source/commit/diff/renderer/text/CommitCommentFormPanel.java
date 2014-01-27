package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.CaseFormat;
import com.pmease.gitop.web.common.wicket.form.BaseForm;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public abstract class CommitCommentFormPanel extends Panel {

	public CommitCommentFormPanel(String id) {
		this(id, Model.of(""));
	}
	
	public CommitCommentFormPanel(String id, IModel<String> textModel) {
		super(id, textModel);
		setOutputMarkupId(true);
	}
	
	private static enum ViewMode {
		WRITE, PREVIEW
	}
	
	private ViewMode viewMode = ViewMode.WRITE;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final Form<?> form = new BaseForm<Void>("form");
		add(form);
		
		Loop tabLinks = new Loop("li", ViewMode.values().length) {

			@Override
			protected void populateItem(LoopItem item) {
				final ViewMode m = ViewMode.values()[item.getIndex()];
				item.add(AttributeAppender.append("class", m == viewMode ? "active" : ""));
				AjaxButton tabLink = new AjaxButton("link", form) {

					@Override
					public void onSubmit(AjaxRequestTarget target, Form<?> form) {
						if (m == viewMode)
							return;
						
						viewMode = m;
						form.addOrReplace(createContent("tabcontent"));
						target.add(form);
					}
					
				};
				
				item.add(tabLink);
				
				tabLink.add(new Label("name", CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, m.name())));
			}
			
		};
		
		form.add(tabLinks);
		
		form.add(createContent("tabcontent"));
		form.add(newSubmitButtons("actions", form));
	}

	abstract protected Component newSubmitButtons(String id, Form<?> form);
	
	private Component createContent(String id) {
		if (viewMode == ViewMode.WRITE) {
			return createWritePanel(id);
		} else {
			return createPreviewPanel(id);
		}
	}
	
	@SuppressWarnings("unchecked")
	private Component createWritePanel(String id) {
		Fragment frag = new Fragment(id, "writefrag", this);
		frag.setOutputMarkupId(true);
		frag.add(new NotificationPanel("feedback", frag));
		frag.add(new TextArea<String>("input", (IModel<String>) getDefaultModel()));
		return frag;
	}
	
	private Component createPreviewPanel(String id) {
		Fragment frag = new Fragment(id, "previewfrag", this);
		frag.setOutputMarkupId(true);
		return frag;
	}
	
	protected String getComment() {
		return (String) getDefaultModelObject();
	}
	
}
