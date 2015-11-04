package com.pmease.gitplex.web.component.pathchoice;

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

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.pathselector.PathSelector;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class PathSingleChoice extends FormComponentPanel<String> {

	private TextField<String> input;
	
	private final int[] pathTypes;
	
	private final String placeholder;
	
	public PathSingleChoice(String id, IModel<String> model, int[] pathTypes, String placeholder) {
		super(id, model);
		
		this.pathTypes = pathTypes;
		this.placeholder = placeholder;
	}
	
	public PathSingleChoice(String id, IModel<String> model, int[] pathTypes) {
		this(id, model, pathTypes, null);
	}	

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(input = new TextField<String>("input", Model.of(getModelObject())));
		if (placeholder != null)
			input.add(AttributeAppender.append("placeholder", placeholder));
		else
			input.add(AttributeAppender.append("placeholder", "Input path"));
		input.setOutputMarkupId(true);
		
		add(new DropdownLink<Void>("chooserTrigger") {

			@Override
			protected Component newContent(String id) {
				RepositoryPage page = (RepositoryPage) getPage();
				String defaultBranch = page.getRepository().getDefaultBranch();
				
				if (defaultBranch != null) {
					return new PathSelector(id, new AbstractReadOnlyModel<Repository>() {

						@Override
						public Repository getObject() {
							return ((RepositoryPage) getPage()).getRepository();
						}
						
					}, GitUtils.branch2ref(defaultBranch), pathTypes) {
	
						@Override
						protected void onSelect(AjaxRequestTarget target, BlobIdent blobIdent) {
							String path = StringEscapeUtils.escapeEcmaScript(blobIdent.path);
							String script = String.format("gitplex.selectPath('%s', '%s', '%s', %s);", 
									input.getMarkupId(), getMarkupId(), path, false);
							target.appendJavaScript(script);
							close(target);
						}
						
					};
				} else {
					return new Fragment(id, "noDefaultBranchFrag", PathSingleChoice.this);
				}					
			}
			
		});
	}

	@Override
	protected void convertInput() {
		setConvertedInput(input.getConvertedInput());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(PathChoiceResourceReference.INSTANCE));
	}
	
}