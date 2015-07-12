package com.pmease.gitplex.web.editable.directory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.web.component.directorychooser.DirectoryChooser;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class DirectorySingleChoiceEditor extends PropertyEditor<String> {

	private TextField<String> input;
	
	public DirectorySingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(input = new TextField<String>("input", Model.of(getModelObject())));
		input.setOutputMarkupId(true);
		
		DropdownPanel chooser = new DropdownPanel("chooser") {

			@Override
			protected Component newContent(String id) {
				RepositoryPage page = (RepositoryPage) getPage();
				String defaultBranch = page.getRepository().getDefaultBranch();
				
				if (defaultBranch != null) {
					return new DirectoryChooser(id, Model.of(new RepoAndBranch(page.getRepository(), defaultBranch))) {
	
						@Override
						protected MarkupContainer newLinkComponent(String id, IModel<TreeNode> node) {
							final String path = StringEscapeUtils.escapeEcmaScript(node.getObject().getPath());
							WebMarkupContainer link = new WebMarkupContainer(id) {

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									String script = String.format("gitplex.selectDirectory('%s', '%s', '%s', %s);", 
											input.getMarkupId(), getMarkupId(), path, false);
									tag.put("onclick", script);
									tag.put("href", "javascript:void(0);");
								}
								
							};
							link.setOutputMarkupId(true);
							return link;
						}
						
					};
				} else {
					return new Fragment(id, "noDefaultBranchFrag", DirectorySingleChoiceEditor.this);
				}					
			}
		};
		add(chooser);
		add(new WebMarkupContainer("chooserTrigger").add(new DropdownBehavior(chooser)));
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}
	
}