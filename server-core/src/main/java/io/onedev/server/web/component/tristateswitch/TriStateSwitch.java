package io.onedev.server.web.component.tristateswitch;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

/**
 * A three-position switch: OFF ({@code false}), unspecified ({@code null}), and
 * ON ({@code true}). Bind to {@code <input wicket:id="switch">} and supply an
 * accessible name via a label or {@code aria-label}. Supports normal form submission
 * and {@code AjaxFormComponentUpdatingBehavior("change")}.
 */
public class TriStateSwitch extends FormComponent<Boolean> {

	private static final long serialVersionUID = 1L;

	public TriStateSwitch(String id) {
		this(id, null);
	}

	public TriStateSwitch(String id, IModel<Boolean> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	@Override
	protected String getModelValue() {
		var value = getModelObject();
		return value == null ? "1" : value ? "2" : "0";
	}

	@Override
	protected Boolean convertValue(String[] values) throws ConversionException {
		if (values == null || values.length == 0)
			return null;
		if (values.length == 1) {
			if ("0".equals(values[0]))
				return false;
			if ("1".equals(values[0]))
				return null;
			if ("2".equals(values[0]))
				return true;
		}
		throw new ConversionException("Invalid switch value").setTargetType(Boolean.class);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		checkComponentTag(tag, "input");
		super.onComponentTag(tag);
		tag.put("type", "range");
		tag.put("min", "0");
		tag.put("max", "2");
		tag.put("step", "1");
		tag.put("value", getValue());
		tag.append("class", "tri-state-switch-input", " ");
		tag.put("data-off", _T("OFF"));
		tag.put("data-unspecified", _T("Unspecified"));
		tag.put("data-on", _T("ON"));
		if (getLabel() != null && tag.getAttribute("aria-label") == null)
			tag.put("aria-label", getLabel().getObject());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TriStateSwitchResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(
				"onedev.server.triStateSwitch.onDomReady('" + getMarkupId() + "');"));
	}
}
