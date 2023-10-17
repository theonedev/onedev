package io.onedev.server.web.page.admin.labelmanagement;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.LabelSpecManager;
import io.onedev.server.model.LabelSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Comparator;

@SuppressWarnings("serial")
public class LabelManagementPage extends AdministrationPage {

	public LabelManagementPage(PageParameters params) {
		super(params);
	}

	private LabelSpecManager getLabelManager() {
		return OneDev.getInstance(LabelSpecManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		LabelManagementBean bean = new LabelManagementBean();
		
		var labels = getLabelManager().query();
		labels.sort(Comparator.comparing(LabelSpec::getName));
		bean.getLabels().addAll(labels);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				getLabelManager().sync(bean.getLabels());
				setResponsePage(LabelManagementPage.class);
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(BeanContext.edit("editor", bean));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new org.apache.wicket.markup.html.basic.Label(componentId, "Labels");
	}

}
