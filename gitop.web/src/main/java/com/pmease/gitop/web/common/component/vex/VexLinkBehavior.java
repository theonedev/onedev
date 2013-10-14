package com.pmease.gitop.web.common.component.vex;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.value.IValueMap;

@SuppressWarnings("serial")
public class VexLinkBehavior extends Behavior {

	public static enum VexIcon {
		QUESTION, INFO, SUCCESS, WARNING, ERROR
	}

	private static final String ATTR_MESSAGE = "data-confirm-message";
	private static final String ATTR_YES_LABEL = "data-confirm-yes-label";
	private static final String ATTR_NO_LABEL = "data-confirm-no-label";
	private static final String ATTR_CSS_CLASS_NAME = "data-confirm-css-class";
	private static final String ATTR_CONFIRM_ICON = "data-confirm-icon";

	private final IModel<String> textModel;
	private final IModel<VexIcon> iconModel;
	private final IModel<String> yesLabelModel;
	private final IModel<String> noLabelModel;
	private final IModel<String> confirmCssClassModel;

	public VexLinkBehavior(IModel<String> textModel, IModel<VexIcon> iconModel,
			IModel<String> yesLabelModel, IModel<String> noLabelModel,
			IModel<String> confirmCssClassModel) {

		this.textModel = textModel;
		this.iconModel = iconModel;
		this.yesLabelModel = yesLabelModel;
		this.noLabelModel = noLabelModel;
		this.confirmCssClassModel = confirmCssClassModel;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(VexConfirmJavaScriptResourceReference.get()));
		response.render(JavaScriptHeaderItem.forScript("vex.defaultOptions.className = 'vex-theme-wireframe'", "vex-theme-options"));
		
		String markupId = component.getMarkupId(true);
		response.render(OnDomReadyHeaderItem.forScript(String.format(
				"$('#%s').on('click', function(e){e.preventDefault(); $(this).confirm(); });", 
				markupId)));
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag) {
		super.onComponentTag(component, tag);
		IValueMap attributes = tag.getAttributes();
		addAttribute(component, attributes, ATTR_MESSAGE, textModel);
		addAttribute(component, attributes, ATTR_YES_LABEL, yesLabelModel);
		addAttribute(component, attributes, ATTR_NO_LABEL, noLabelModel);
		addAttribute(component, attributes, ATTR_CSS_CLASS_NAME, confirmCssClassModel);
		
		if (iconModel != null) {
			VexIcon icon = iconModel.getObject();
			if (icon != null) {
				addAttribute(component, attributes, ATTR_CONFIRM_ICON, 
						Model.of(icon.name().toLowerCase()));
			}
		}
	}

	@Override
	public void detach(Component component) {
		super.detach(component);

		if (textModel != null) {
			textModel.detach();
		}
		if (yesLabelModel != null) {
			yesLabelModel.detach();
		}
		if (noLabelModel != null) {
			noLabelModel.detach();
		}
		if (confirmCssClassModel != null) {
			confirmCssClassModel.detach();
		}
		if (iconModel != null) {
			iconModel.detach();
		}
	}

	private void addAttribute(Component component, IValueMap attributes,
			String attributeName, IModel<String> model) {
		String label = getLabel(component, model);
		if (label != null) {
			attributes.put(attributeName, label);
		}
	}

	private String getLabel(Component component, IModel<String> labelModel) {
		if (labelModel != null) {
			IModel<String> model = labelModel;
			if (model instanceof IComponentAssignedModel) {
				model = ((IComponentAssignedModel<String>) model)
						.wrapOnAssignment(component);
			}
			return model.getObject();
		} else {
			return null;
		}
	}
}
