package com.pmease.gitplex.web.page.repository.commit.filters;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.gitplex.web.component.pathchoice.PathSingleChoice;

import jersey.repackaged.com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class PathEditor extends FilterEditor {

	private PathSingleChoice input;
	
	public PathEditor(String id, CommitFilter filter, boolean focus) {
		super(id, filter, focus);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String path;
		
		List<String> values = getModelObject();
		if (!values.isEmpty())
			path = values.get(0);
		else
			path = null;
    	add(input = new PathSingleChoice("input", Model.of(path), 
    			new int[]{FileMode.TYPE_FILE, FileMode.TYPE_TREE}, "Filter by path"));
    	input.add(new AjaxFormSubmitBehavior("select") {});
	}

	@Override
	protected void convertInput() {
		String value = input.getConvertedInput();
		if (value != null) 
			setConvertedInput(Lists.newArrayList(value));
		else
			setConvertedInput(new ArrayList<String>());
	}

	@Override
	protected String getFocusScript() {
		return String.format("$('#%s').focus();", input.getMarkupId());
	}
	
}
