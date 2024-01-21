package io.onedev.server.web.page.admin.emailtemplates;

import com.google.common.collect.Lists;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.page.admin.AdministrationPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class AbstractTemplatePage extends AdministrationPage {

	protected final String GROOVY_TEMPLATE_LINK = "<a href='https://docs.groovy-lang.org/latest/html/api/groovy/text/SimpleTemplateEngine.html' target='_blank'>Groovy simple template</a>";

	public AbstractTemplatePage(PageParameters params) {
		super(params);
	}

	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		EmailTemplates templates = getSettingManager().getEmailTemplates();
		add(new Label("templateHelp", getTemplateHelp(getHelpText(), getVariableHelp()))
				.setEscapeModelStrings(false));
		
		BeanEditor editor = BeanContext.edit("editor", templates, Lists.newArrayList(getPropertyName()), false);
		
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				getSettingManager().saveEmailTemplates(templates);
				getSession().success("Template saved");
			}
			
		};
		
		AjaxLink<?> useDefaultLink;
		add(useDefaultLink = new AjaxLink<Void>("useDefault") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to use default template?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				setTemplate(templates, getDefaultTemplate());
				getSettingManager().saveEmailTemplates(templates);
				setResponsePage(getPageClass());
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				configure();
				if (!isEnabled())
					tag.put("disabled", "disabled");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(!getTemplate(templates).equals(getDefaultTemplate()));
			}

		});
		
		Form<?> form = new Form<Void>("form");
		form.add(editor);
		form.add(saveButton);
		form.add(useDefaultLink);
		
		add(form);
	}
	
	private String getTemplate(EmailTemplates templates) {
		return (String) new PropertyDescriptor(EmailTemplates.class, getPropertyName()).getPropertyValue(templates);
	}

	private void setTemplate(EmailTemplates templates, String template) {
		new PropertyDescriptor(EmailTemplates.class, getPropertyName()).setPropertyValue(templates, template);
	}

	protected String getTemplateHelp(String helpText, Map<String, String> variableHelp) {
		StringBuilder builder = new StringBuilder(helpText);
		if (!builder.toString().endsWith("."))
			builder.append(".");
		builder.append(" When evaluating this template, below variables will be available:<ul class='mb-0'>");
		
		var currentVariableHelp = new LinkedHashMap<String, String>();
		currentVariableHelp.put("htmlVersion", "true for html version, false for text version");
		currentVariableHelp.putAll(variableHelp);
		
		for (Map.Entry<String, String> entry: currentVariableHelp.entrySet())
			builder.append("<li><code>" + entry.getKey() + ":</code> " + entry.getValue());
		
		return builder.append("</ul>").toString();
	}
	
	protected abstract String getPropertyName();

	protected abstract String getDefaultTemplate();
	
	protected abstract String getHelpText();
	
	protected abstract Map<String, String> getVariableHelp();
	
}