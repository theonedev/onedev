package io.onedev.server.web.editable.buildspec.step;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.buildspec.step.Step;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyContext;

@SuppressWarnings("serial")
public class StepListViewPanel extends Panel {

	private final List<StepEditBean> beans = new ArrayList<>();
	
	public StepListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
 		for (Serializable each: elements) {
 			StepEditBean bean = new StepEditBean();
 			bean.setStep((Step) each);
			beans.add(bean);
 		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<StepEditBean>("steps", beans) {

			@Override
			protected void populateItem(ListItem<StepEditBean> item) {
				StepEditBean bean = item.getModelObject();
				String typeName = EditableUtils.getDisplayName(bean.getStep().getClass());
				item.add(new Label("title", "#" + (item.getIndex()+1) + " - " + typeName));
				item.add(PropertyContext.view("viewer", bean, "step"));
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new StepResourceReference()));
	}

}
