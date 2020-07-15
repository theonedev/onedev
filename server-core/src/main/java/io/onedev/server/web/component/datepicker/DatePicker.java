package io.onedev.server.web.component.datepicker;

import java.util.Date;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.Constants;

public class DatePicker extends DateTextField {

	private static final long serialVersionUID = 1L;

    public DatePicker(String id) {
        this(id, null);
    }

    public DatePicker(String id, IModel<Date> model) {
    	super(id, model, Constants.DATE_FORMAT);
    }

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new DatePickerResourceReference()));
		String script = String.format("onedev.server.datePicker.onDomReady('%s')", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
