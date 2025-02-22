package io.onedev.server.web.page.project.builds.detail;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.server.web.component.tabbable.PageTab;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

@ExtensionPoint
public class BuildTab extends PageTab {


	public BuildTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, List<Class<? extends Page>> additionalPageClasses) {
		super(titleModel, iconModel, mainPageClass, mainPageParams, additionalPageClasses);
	}

	public BuildTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams) {
		super(titleModel, iconModel, mainPageClass, mainPageParams);
	}

	public BuildTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, Class<? extends Page> additionalPageClass) {
		super(titleModel, iconModel, mainPageClass, mainPageParams, additionalPageClass);
	}

	public BuildTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, Class<? extends Page> additionalPageClass1, Class<? extends Page> additionalPageClass2) {
		super(titleModel, iconModel, mainPageClass, mainPageParams, additionalPageClass1, additionalPageClass2);
	}

	public BuildTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, Class<? extends Page> additionalPageClass1, Class<? extends Page> additionalPageClass2, Class<? extends Page> additionalPageClass3) {
		super(titleModel, iconModel, mainPageClass, mainPageParams, additionalPageClass1, additionalPageClass2, additionalPageClass3);
	}

	public BuildTab(IModel<String> titleModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, List<Class<? extends Page>> additionalPageClasses) {
		super(titleModel, mainPageClass, mainPageParams, additionalPageClasses);
	}

	public BuildTab(IModel<String> titleModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams) {
		super(titleModel, mainPageClass, mainPageParams);
	}

	public BuildTab(IModel<String> titleModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, Class<? extends Page> additionalPageClass) {
		super(titleModel, mainPageClass, mainPageParams, additionalPageClass);
	}

	public BuildTab(IModel<String> titleModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, Class<? extends Page> additionalPageClass1, Class<? extends Page> additionalPageClass2) {
		super(titleModel, mainPageClass, mainPageParams, additionalPageClass1, additionalPageClass2);
	}

	public BuildTab(IModel<String> titleModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams, Class<? extends Page> additionalPageClass1, Class<? extends Page> additionalPageClass2, Class<? extends Page> additionalPageClass3) {
		super(titleModel, mainPageClass, mainPageParams, additionalPageClass1, additionalPageClass2, additionalPageClass3);
	}
	
}