package io.onedev.server.web.page.my.aisetting;

import static io.onedev.server.model.User.Type.AI;
import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.my.MyPage;

public abstract class MyAiSettingPage extends MyPage {
		
	public MyAiSettingPage(PageParameters params) {
		super(params);
		if (getUser().isDisabled() || getUser().getType() != AI)
			throw new IllegalStateException();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		
		tabs.add(new PageTab(Model.of(_T("Model")), null, MyModelSettingPage.class, new PageParameters()));		
		tabs.add(new PageTab(Model.of(_T("System Prompt")), null, MySystemPromptPage.class, new PageParameters()));		
		tabs.add(new PageTab(Model.of(_T("Entitlement")), null, MyEntitlementSettingPage.class, new PageParameters()));		

		add(new Tabbable("aiSettingTabs", tabs));
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("My AI Settings"));
	}
	
}
