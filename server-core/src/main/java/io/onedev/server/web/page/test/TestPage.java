package io.onedev.server.web.page.test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.server.web.component.chart.line.LineChartPanel;
import io.onedev.server.web.component.chart.line.LineSeries;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	public TestPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Map<String, List<Integer>> lineValues = new LinkedHashMap<>();
		lineValues.put("Mon", Lists.newArrayList(0, 100));
		lineValues.put("Tue", Lists.newArrayList(25, 75));
		lineValues.put("Wed", Lists.newArrayList(50, 50));
		lineValues.put("Thu", Lists.newArrayList(75, 25));
		lineValues.put("Fri", Lists.newArrayList(100, 0));
		
		LineSeries lineSeries = new LineSeries("Success Rate", Lists.newArrayList("Test Suite", "Test Case"), 
				lineValues, null, 20, 100, Lists.newArrayList("red", "blue"));
		add(new LineChartPanel("chart", Model.of(lineSeries)));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TestResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.test.onDomReady();"));
	}		

}
