package com.pmease.gitplex.web.editable.path;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.editable.PathChoice;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.pathselector.PathSelector;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class PathSingleChoiceEditor extends PropertyEditor<String> {

	private TextField<String> input;
	
	public PathSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
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
					PathChoice pathChoice = getPropertyDescriptor().getPropertyGetter().getAnnotation(PathChoice.class);
					return new PathSelector(id, new AbstractReadOnlyModel<Repository>() {

						@Override
						public Repository getObject() {
							return ((RepositoryPage) getPage()).getRepository();
						}
						
					}, GitUtils.branch2ref(defaultBranch), pathChoice.value()) {
	
						@Override
						protected void onSelect(AjaxRequestTarget target, BlobIdent blobIdent) {
							String path = StringEscapeUtils.escapeEcmaScript(blobIdent.path);
							String script = String.format("gitplex.selectDirectory('%s', '%s', '%s', %s);", 
									input.getMarkupId(), getMarkupId(), path, false);
							target.appendJavaScript(script);
						}
						
					};
				} else {
					return new Fragment(id, "noDefaultBranchFrag", PathSingleChoiceEditor.this);
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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(PathChoiceResourceReference.INSTANCE));
	}
	
	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}
	
}