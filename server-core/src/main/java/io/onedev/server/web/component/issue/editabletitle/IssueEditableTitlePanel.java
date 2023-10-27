package io.onedev.server.web.component.issue.editabletitle;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.component.issue.progress.IssueProgressPanel;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.util.ReferenceTransformer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class IssueEditableTitlePanel extends Panel {

	private static final String CONTENT_ID = "content";
	
	public IssueEditableTitlePanel(String id) {
		super(id);
	}

	private Fragment newTitleEditor() {
		Fragment titleEditor = new Fragment(CONTENT_ID, "titleEditFrag", this);
		
		Form<?> form = new Form<Void>("form");
		form.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (form.hasError())
					return "is-invalid";	
				else
					return "";
			}
			
		}));
		
		TextField<String> titleInput = new TextField<String>("title", Model.of(getIssue().getTitle()));
		titleInput.add(new ReferenceInputBehavior() {

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}
			
		});
		titleInput.setRequired(true);
		titleInput.setLabel(Model.of("Title"));
		
		form.add(titleInput);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				OneDev.getInstance(IssueChangeManager.class).changeTitle(getIssue(), titleInput.getModelObject());
				((BasePage)getPage()).notifyObservablesChange(target, getIssue().getChangeObservables(false));
				
				Fragment titleViewer = newTitleViewer();
				titleEditor.replaceWith(titleViewer);
				target.add(titleViewer);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(titleEditor);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment titleViewer = newTitleViewer();
				titleEditor.replaceWith(titleViewer);
				target.add(titleViewer);
			}
			
		});		
		
		titleEditor.add(form);
		
		titleEditor.setOutputMarkupId(true);
		
		return titleEditor;
	}
	
	private Fragment newTitleViewer() {
		Fragment titleViewer = new Fragment(CONTENT_ID, "titleViewFrag", this);
		titleViewer.add(new Label("title", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				ReferenceTransformer transformer = new ReferenceTransformer(getIssue().getProject(), null);
				String prefix;
				if (getIssue().getProject().equals(getProject()))
					prefix = "";
				else
					prefix = getIssue().getProject().getPath().substring(getProject().getPath().length()+1);
				
				return prefix + "#" + getIssue().getNumber() + "&nbsp;&nbsp;" 
						+ Emojis.getInstance().apply(transformer.apply(getIssue().getTitle()));
			}
			
		}).setEscapeModelStrings(false));
		
		titleViewer.add(new WebMarkupContainer("confidential") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getIssue().isConfidential());
			}
			
		});
		
		titleViewer.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment titleEditor = newTitleEditor();
				titleViewer.replaceWith(titleEditor);
				target.add(titleEditor);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(SecurityUtils.canModify(getIssue()));
			}
			
		});
		titleViewer.add(new CopyToClipboardLink("copy", 
				Model.of(StringUtils.uncapitalize(getIssue().getTitle()) + " (#" + getIssue().getNumber() + ")")));
		
		titleViewer.add(new IssueProgressPanel("progress") {

			@Override
			protected Issue getIssue() {
				return IssueEditableTitlePanel.this.getIssue();
			}
			
		});

		titleViewer.setOutputMarkupId(true);
		
		return titleViewer;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newTitleViewer());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueEditableTitleCssResourceReference()));
	}

	protected abstract Issue getIssue();
	
	protected abstract Project getProject();
	
}
