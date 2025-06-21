package io.onedev.server.web.component.datepicker;

import static io.onedev.server.web.translation.Translation._T;

import java.time.LocalDate;
import java.util.Locale;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.DateRange;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BasePage;

public class DateRangePicker extends TextField<DateRange> {

	private static final long serialVersionUID = 1L;
		
	private final IConverter<DateRange> converter;

    public DateRangePicker(String id, IModel<DateRange> model) {
    	super(id, model);

		setType(DateRange.class);
		converter = new IConverter<DateRange>() {

			@Override
			public DateRange convertToObject(String value, Locale locale) throws ConversionException {
				if (value == null) {
					return null;
				} else {
					var errorMessage = _T("Invalid date range, expecting \"yyyy-MM-dd to yyyy-MM-dd\"");
					if (value.contains(" ")) {
						try {
							var fromDate = LocalDate.from(DateUtils.DATE_FORMATTER.parse(StringUtils.substringBefore(value, " ")));
							var toDate = LocalDate.from(DateUtils.DATE_FORMATTER.parse(StringUtils.substringAfterLast(value, " ")));
							return new DateRange(fromDate, toDate);
						} catch (Exception e) {
							throw new ConversionException(errorMessage);
						}
					} else {
						try {
							var date = LocalDate.from(DateUtils.DATE_FORMATTER.parse(value));
							return new DateRange(date, date);
						} catch (Exception e) {
							throw new ConversionException(errorMessage);
						}
					}
				}
			}

			@Override
			public String convertToString(DateRange value, Locale locale) {
				if (value == null)
					return null;
				else if (!value.getFrom().equals(value.getTo()))
					return value.getFrom().format(DateUtils.DATE_FORMATTER) + " to " + value.getTo().format(DateUtils.DATE_FORMATTER);
				else
					return value.getFrom().format(DateUtils.DATE_FORMATTER);
			}
			
		};
    }

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new DatePickerResourceReference()));
		
		BasePage page = (BasePage) getPage();
		if (page.isDarkMode()) {
			response.render(CssHeaderItem.forReference(
					new BaseDependentCssResourceReference(DatePickerResourceReference.class, "dark.css")));
		}
		
		String script = String.format("onedev.server.datePicker.onDomReady('%s', false, true)", 
				getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

    @Override
    protected IConverter<?> createConverter(Class<?> type) {
		return converter;
    }

} 