package io.onedev.server.web.component.chart.line;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.web.page.base.BasePage;

public class LineChartPanel extends GenericPanel<LineSeries> {

	public LineChartPanel(String id, IModel<LineSeries> model) {
		super(id, model);
	}

	@Nullable
	private LineSeries getSeries() {
		return getModelObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new LineChartResourceReference()));
		
		try {
			ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
			BasePage page = (BasePage) getPage();
			String script = String.format("onedev.server.lineChart.onDomReady('%s', %s, %s, %b);", 
					getMarkupId(true), mapper.writeValueAsString(getSeries()), 
					getSeries().getYAxisValueFormatter(), page.isDarkMode());
			response.render(OnDomReadyHeaderItem.forScript(script));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
