package com.pmease.gitop.web.component.comment;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.parboiled.common.Preconditions;

import com.google.common.base.Strings;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.common.wicket.form.BaseForm;
import com.pmease.gitop.web.component.link.UserAvatarLink;
import com.pmease.gitop.web.component.wiki.WikiTextPanel;
import com.pmease.gitop.web.model.UserModel;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public abstract class CommitCommentEditor extends Panel {

	abstract protected Component createSubmitButtons(String id, Form<?> form);
	
	protected Form<?> form;
	
	public CommitCommentEditor(String id) {
		this(id, Model.of(""));
	}
	
	public CommitCommentEditor(String id, IModel<String> textModel) {
		super(id, textModel);
		
		form = new BaseForm<Void>("form");
		add(form);
	}

	private static enum Mode {
		WRITE, PREVIEW
	}
	
	private Mode mode = Mode.WRITE;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		form.add(new Loop("tabli", Mode.values().length) {

			@Override
			protected void populateItem(LoopItem item) {
				final Mode m = Mode.values()[item.getIndex()];
				item.add(AttributeAppender.append("class", m == mode ? "active" : ""));
				AbstractLink link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						mode = m;
						onModeChanged(target);
					}
				};
				
				link.add(new Label("name", WordUtils.capitalizeFully(m.name())));
				item.add(link);
			}
		});
		
		form.add(newContent("body"));
		
		form.add(createSubmitButtons("actions", form));
	}
	
	protected void onModeChanged(AjaxRequestTarget target) {
		Component c = newContent("body");
		form.addOrReplace(c);
		target.add(form);
	}
	
	protected Component newContent(String id) {
		Component c;
		if (mode == Mode.WRITE) {
			c = newEditPanel(id);
		} else {
			// PREVIEW
			c = newPreviewPanel(id);
		}
		
		c.setOutputMarkupId(true);
		return c;
	}
	
	public String getCommentText() {
		return (String) getDefaultModelObject();
	}
	
	@SuppressWarnings("unchecked")
	private Component newEditPanel(String id) {
		Fragment frag = new Fragment(id, "editfrag", this);
		frag.add(new NotificationPanel("feedback", form));
		frag.add(new TextArea<String>("input", (IModel<String>) getDefaultModel()));
		
		return frag;
	}
	
	private Component newPreviewPanel(String id) {
		Fragment frag = new Fragment(id, "previewfrag", this);
		User user = Preconditions.checkNotNull(Gitop.getInstance(UserManager.class).getCurrent());
		frag.add(new UserAvatarLink("author", new UserModel(user)));
		frag.add(new WikiTextPanel("content", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String str = getCommentText();
				if (Strings.isNullOrEmpty(str)) {
					return "Nothing to be shown";
				} else {
					return str;
				}
			}
		}));
		
		return frag;
	}
	
	public Form<?> getForm() {
		return form;
	}
}
