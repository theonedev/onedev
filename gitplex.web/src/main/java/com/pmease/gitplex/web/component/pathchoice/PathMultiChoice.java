package com.pmease.gitplex.web.component.pathchoice;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.component.pathselector.PathSelector;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class PathMultiChoice extends FormComponentPanel<List<String>> {

	private TextField<String> input;
	
	private final int[] pathTypes;
	
	private final String placeholder;
	
	public PathMultiChoice(String id, IModel<List<String>> model, int[] pathTypes, String placeholder) {
		super(id, model);
		
		this.pathTypes = pathTypes;
		this.placeholder = placeholder;
	}

	public PathMultiChoice(String id, IModel<List<String>> model, int[] pathTypes) {
		this(id, model, pathTypes, null);
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
		if (placeholder != null)
			input.add(AttributeAppender.append("placeholder", placeholder));
		else
			input.add(AttributeAppender.append("placeholder", "Input paths"));
			
		input.setOutputMarkupId(true);
		
		add(new DropdownLink("chooserTrigger") {

			@Override
			protected Component newContent(String id) {
				RepositoryPage page = (RepositoryPage) getPage();
				String defaultBranch = page.getDepot().getDefaultBranch();
				
				if (defaultBranch != null) {
					return new PathSelector(id, new AbstractReadOnlyModel<Depot>() {

						@Override
						public Depot getObject() {
							return ((RepositoryPage) getPage()).getDepot();
						}
						
					}, GitUtils.branch2ref(defaultBranch), pathTypes) {
	
						@Override
						protected void onSelect(AjaxRequestTarget target, BlobIdent blobIdent) {
							String path = StringEscapeUtils.escapeEcmaScript(blobIdent.path);
							String script = String.format("gitplex.selectPath('%s', '%s', '%s', %s);", 
									input.getMarkupId(), getMarkupId(), path, true);
							target.appendJavaScript(script);
						}
						
					};
				} else {
					return new Fragment(id, "noDefaultBranchFrag", PathMultiChoice.this);
				}					
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(PathChoiceResourceReference.INSTANCE));
	}

	@Override
	protected void convertInput() {
		String directories = input.getConvertedInput();
		if (directories != null)
			setConvertedInput(Lists.newArrayList(StringUtils.splitAndTrim(directories)));
		else
			setConvertedInput(new ArrayList<String>());
	}
	
}