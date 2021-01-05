package io.onedev.server.web.component.chart.pie;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;

@SuppressWarnings("serial")
public abstract class PieChartPanel extends GenericPanel<List<PieSlice>> {

	private AbstractPostAjaxBehavior selectionBehavior;
	
	public PieChartPanel(String id, IModel<List<PieSlice>> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(selectionBehavior = new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				String sliceName = RequestCycle.get().getRequest()
						.getPostParameters().getParameterValue("sliceName").toString();
				onSelectionChange(target, sliceName);
			}
			
		});
	}

	@Nullable
	private List<PieSlice> getSlices() {
		return getModelObject();
	}
	
	protected abstract void onSelectionChange(AjaxRequestTarget target, String sliceName);

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new PieChartResourceReference()));
		
		String jsonOfSlices;
		try {
			jsonOfSlices = OneDev.getInstance(ObjectMapper.class).writeValueAsString(getSlices());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		
		CallbackParameter param = CallbackParameter.explicit("sliceName");
		String callback = selectionBehavior.getCallbackFunction(param).toString();
		String script = String.format("onedev.server.pieChart.onDomReady('%s', %s, %s);", 
				getMarkupId(true), jsonOfSlices, callback);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
