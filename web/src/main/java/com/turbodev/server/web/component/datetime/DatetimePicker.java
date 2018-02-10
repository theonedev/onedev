package com.turbodev.server.web.component.datetime;

import java.util.Date;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerConfig;

public class DatetimePicker extends de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePicker {

	private static final long serialVersionUID = 1L;

    public DatetimePicker(String id, String datePattern) {
        super(id, null, datePattern);
    }

    public DatetimePicker(String id, IModel<Date> model, String datePattern) {
    	super(id, model, datePattern);
    }

    public DatetimePicker(String id, DatetimePickerConfig config) {
    	super(id, config);
    }

    public DatetimePicker(String id, IModel<Date> model, DatetimePickerConfig config) {
    	super(id, model, config);
    }

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		String script = String.format(""
				+ "$('#%s').keyup(function() {"
				+ "  if (e.keyCode == 13) {"
				+ "    $(this).closest('form').find(\"input[type='submit'],button[type='submit']\").trigger('click');"
				+ "  }"
				+ "});", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
