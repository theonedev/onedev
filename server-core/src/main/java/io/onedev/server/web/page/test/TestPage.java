package io.onedev.server.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	public TestPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
			}
			
		});

		/*
		List<String> xAxisValues = new ArrayList<>();
		for (int i=1; i<=100; i++) {
			String xAxisValue = String.valueOf(i);
			xAxisValues.add(xAxisValue);
		}
		Map<String, Integer> redValues = new HashMap<>();
		Map<String, Integer> greenValues = new HashMap<>();
		Map<String, Integer> blueValues = new HashMap<>();
		for (int i=1; i<=50; i++) {
			String xAxisValue = String.valueOf(i);
			redValues.put(xAxisValue, RandomUtils.nextInt(10)+20);
			greenValues.put(xAxisValue, RandomUtils.nextInt(10)+40);
			blueValues.put(xAxisValue, RandomUtils.nextInt(10)+60);
		}
		
		List<Line> lines = new ArrayList<>();
		lines.add(new Line("red", redValues, "red", "redgreen"));
		lines.add(new Line("green", greenValues, "green", "redgreen"));
		lines.add(new Line("blue", blueValues, "blue", "redgreen"));
		
		LineSeries series = new LineSeries("chart", xAxisValues, lines, null, null, null);
		add(new LineChartPanel("chart", Model.of(series)));
		*/
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TestResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.test.onDomReady();"));
	}		

}
