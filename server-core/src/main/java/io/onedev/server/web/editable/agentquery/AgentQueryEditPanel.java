package io.onedev.server.web.editable.agentquery;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.behavior.AgentQueryBehavior;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentListPage;

public class AgentQueryEditPanel extends PropertyEditor<String> {

	private final boolean forExecutor;
	
	private FormComponent<String> input;
	
	public AgentQueryEditPanel(String id, PropertyDescriptor descriptor, 
			IModel<String> propertyModel, boolean forExecutor) {
		super(id, descriptor, propertyModel);
		this.forExecutor = forExecutor;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new TextField<String>("input", Model.of(getModelObject())));
		input.setType(getDescriptor().getPropertyClass());
		input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));		
		
		input.add(new AgentQueryBehavior(forExecutor));
		
		input.add(AttributeAppender.append("spellcheck", "false"));
		input.add(AttributeAppender.append("autocomplete", "off"));
		if (!getDescriptor().isPropertyRequired())
			input.add(AttributeAppender.append("class", "no-autofocus"));
		
		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
				Link<?> link = newShowSelectedAgentsLink();
				replace(link);
				target.add(link);
			}
			
		});
		
		input.add(newPlaceholderModifier());

		add(newShowSelectedAgentsLink());
	}
	
	private Link<?> newShowSelectedAgentsLink() {
		Link<?> link = new BookmarkablePageLink<Void>("showSelectedAgents", AgentListPage.class, 
				AgentListPage.paramsOf(input.getConvertedInput(), 0));
		link.setOutputMarkupId(true);
		return link;
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
