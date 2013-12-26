package com.pmease.gitop.web.editable.directory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.git.TreeNode;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.web.component.directory.DirectoryChooser;
import com.pmease.gitop.web.page.project.AbstractProjectPage;

@SuppressWarnings("serial")
public class DirectorySingleChoiceEditor extends Panel {
	
	private final DirectorySingleChoiceEditContext editContext;

	public DirectorySingleChoiceEditor(String id, DirectorySingleChoiceEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final TextField<String> directory;
		
		add(directory = new TextField<String>("directory", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return (String) editContext.getPropertyValue();
			}

			@Override
			public void setObject(String object) {
				editContext.setPropertyValue(object);
			}
			
		}));
		directory.setOutputMarkupId(true);
		
		DropdownPanel chooser = new DropdownPanel("chooser") {

			@Override
			protected Component newContent(String id) {
				return new DirectoryChooser(id, new LoadableDetachableModel<Branch>() {

					@Override
					protected Branch load() {
						AbstractProjectPage page = (AbstractProjectPage) getPage();
						return Gitop.getInstance(BranchManager.class).findBy(page.getProject(), "master");
					}
					
				}) {

					@Override
					protected MarkupContainer newLinkComponent(String id, IModel<TreeNode> node) {
						WebMarkupContainer link = new WebMarkupContainer(id);
						String path = StringEscapeUtils.escapeEcmaScript(node.getObject().getPath());
						String script = String.format("document.getElementById('%s').value = '%s';pmease.commons.dropdown.hide($(this).closest('.dropdown-panel')[0].id);", 
								directory.getMarkupId(), path);
						link.add(AttributeAppender.append("onclick", script));
						link.add(AttributeAppender.append("href", "javascript:void(0);"));
						return link;
					}
					
				};
			}
			
		};
		add(chooser);
		add(new WebMarkupContainer("chooserTrigger").add(new DropdownBehavior(chooser)));
	}

}
