package io.onedev.server.web.mapper;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

/** Mounts the existing project editors in the administration area. */
public class ProjectDefaultsMapper extends BasePageMapper {

	public ProjectDefaultsMapper(String path, Class<? extends ProjectSettingPage> pageClass) {
		super("~administration/project-defaults/" + path, pageClass);
	}

	@Override
	protected PageParameters extractPageParameters(Request request, Url url) {
		var parameters = super.extractPageParameters(request, url);
		if (parameters == null)
			parameters = new PageParameters();
		return parameters.set(ProjectMapperUtils.PARAM_PROJECT, Project.DEFAULT_NAME);
	}

	@Override
	protected boolean setPlaceholders(PageParameters parameters, Url url) {
		if (!Project.DEFAULT_NAME.equals(parameters.get(ProjectMapperUtils.PARAM_PROJECT).toOptionalString()))
			return false;
		parameters.remove(ProjectMapperUtils.PARAM_PROJECT);
		return super.setPlaceholders(parameters, url);
	}
	
}
