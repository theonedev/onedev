package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Map;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.*;

@SuppressWarnings("serial")
public class AlertTemplatePage extends AbstractTemplatePage {

	public AlertTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_ALERT;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_ALERT;
	}

	@Override
	protected String getHelpText() {
		return "A " + GROOVY_TEMPLATE_LINK + " used as body of system alert email";
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap(
				"alert", "<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Alert.java'>alert</a> to display",
				"serverUrl", "root url of OneDev server");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "System Alert Template");
	}

}