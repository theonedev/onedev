package io.onedev.server.ee.xsearch;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.search.code.query.QueryOption;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.LayoutPage;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.onedev.server.util.ReflectionUtils.getTypeArguments;

public abstract class CodeSearchPage<T extends Serializable> extends LayoutPage {

	private static final MetaDataKey<HashMap<Class<?>, QueryOption>> QUERY_OPTIONS =
			new MetaDataKey<>() {};

	private static final MetaDataKey<String> PROJECTS =
			new MetaDataKey<>() {};
	
	private QueryOption option;
	
	private String projects;
	
	public CodeSearchPage(PageParameters params) {
		super(params);
		
		List<Class<?>> typeArguments = getTypeArguments(CodeSearchPage.class, getClass());

		try {
			option = (QueryOption) typeArguments.get(0).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
		
		Map<Class<?>, QueryOption> savedOptions = getSavedOptions();
		if (savedOptions.containsKey(option.getClass()))
			option = savedOptions.get(option.getClass());

		String projects = WebSession.get().getMetaData(PROJECTS);
		if (projects != null)
			this.projects = projects;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var projectsBean = new ProjectsBean();
		projectsBean.setProjects(projects);
		
		var form = new Form<Void>("form");
		
		FormComponent<? extends QueryOption> optionEditor = option.newOptionEditor("option");			
		form.add(optionEditor);
		form.add(BeanContext.edit("projects", projectsBean));
		form.add(new AjaxButton("search") {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				option = optionEditor.getModelObject();
				projects = projectsBean.getProjects();

				var savedOptions = getSavedOptions();
				savedOptions.put(option.getClass(), option);
				WebSession.get().setMetaData(QUERY_OPTIONS, savedOptions);
				WebSession.get().setMetaData(PROJECTS, projects);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		add(form);
	}

	private HashMap<Class<?>, QueryOption> getSavedOptions() {
		HashMap<Class<?>, QueryOption> savedOptions = WebSession.get().getMetaData(QUERY_OPTIONS);
		if (savedOptions == null)
			savedOptions = new HashMap<>();
		return savedOptions;
	}
	
}
