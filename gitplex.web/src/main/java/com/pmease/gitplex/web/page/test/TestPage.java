package com.pmease.gitplex.web.page.test;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private RepeatingView rows;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		rows = new RepeatingView("rows");
		for (int i=0; i<10000; i++) {
			rows.add(newRow(rows.newChildId()));
		}
		add(rows);
		
		add(new AjaxLink<Void>("expand") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				StringBuilder builder = new StringBuilder();
				for (int i=0; i<10; i++) {
					String time = String.valueOf(System.currentTimeMillis());
					builder.append(String.format(""
							+ "<tr id='%s'>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "<td>%s</td>"
							+ "</tr>", 
							time, time, time, time, time, time, time, time, time, time, time, time, time));
				}
				target.appendJavaScript(String.format("$('table').append(\"%s\");", builder.toString()));
			}
			
		});
	}

	private Component newRow(String id) {
		WebMarkupContainer row = new WebMarkupContainer(id);
		row.add(new Label("column1", System.currentTimeMillis()));
		row.add(new Label("column2", System.currentTimeMillis()));
		row.add(new Label("column3", System.currentTimeMillis()));
		row.add(new Label("column4", System.currentTimeMillis()));
		row.add(new Label("column5", System.currentTimeMillis()));
		row.add(new Label("column6", System.currentTimeMillis()));
		row.add(new Label("column7", System.currentTimeMillis()));
		row.add(new Label("column8", System.currentTimeMillis()));
		row.add(new Label("column9", System.currentTimeMillis()));
		row.add(new Label("column10", System.currentTimeMillis()));
		row.add(new Label("column11", System.currentTimeMillis()));
		row.add(new Label("column12", System.currentTimeMillis()));
		row.setOutputMarkupId(true);
		return row;
	}
}
