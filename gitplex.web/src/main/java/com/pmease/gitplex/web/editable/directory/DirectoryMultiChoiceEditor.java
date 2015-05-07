package com.pmease.gitplex.web.editable.directory;

import java.util.ArrayList;
import java.util.List;

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

import com.google.common.collect.Lists;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.web.component.directorychooser.DirectoryChooser;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class DirectoryMultiChoiceEditor extends PropertyEditor<List<String>> {

	private TextField<String> input;
	
	public DirectoryMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String directories;
		if (getModelObject() != null)
			directories = StringUtils.join(getModelObject(), ", ");
		else
			directories = null;

		add(input = new TextField<String>("input", Model.of(directories)));
		input.setOutputMarkupId(true);
		
		DropdownPanel chooser = new DropdownPanel("chooser") {

			@Override
			protected Component newContent(String id) {
				RepositoryPage page = (RepositoryPage) getPage();
				Branch defaultBranch = page.getRepository().getDefaultBranch();
				
				if (defaultBranch != null) {
					return new DirectoryChooser(id, Model.of(defaultBranch)) {
	
						@Override
						protected MarkupContainer newLinkComponent(String id, IModel<TreeNode> node) {
							final String path = StringEscapeUtils.escapeEcmaScript(node.getObject().getPath());
							WebMarkupContainer link = new WebMarkupContainer(id) {

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									String script = String.format("gitplex.selectDirectory('%s', '%s', '%s', %s);", 
											input.getMarkupId(), getMarkupId(), path, true);
									tag.put("onclick", script);
									tag.put("href", "javascript:void(0);");
								}
								
							};
							link.setOutputMarkupId(true);
							return link;
						}
						
					};
				} else {
					return new Fragment(id, "noDefaultBranchFrag", DirectoryMultiChoiceEditor.this);
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
	protected List<String> convertInputToValue() throws ConversionException {
		String directories = input.getConvertedInput();
		if (directories != null)
			return Lists.newArrayList(StringUtils.splitAndTrim(directories));
		else
			return new ArrayList<>();
	}
	
}