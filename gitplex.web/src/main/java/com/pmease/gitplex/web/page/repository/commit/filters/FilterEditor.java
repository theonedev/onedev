package com.pmease.gitplex.web.page.repository.commit.filters;

import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class FilterEditor extends FormComponentPanel<List<String>> {

	private boolean focus;
	
	public FilterEditor(String id, final CommitFilter filter, boolean focus) {
		super(id, new IModel<List<String>>() {

			@Override
			public void detach() {
			}

			@Override
			public List<String> getObject() {
				return filter.getValues();
			}

			@Override
			public void setObject(List<String> object) {
				filter.setValues(object);
			}
			
		});
		
		this.focus = focus;
	}

	protected abstract String getFocusScript();
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		if (focus) {
			response.render(OnDomReadyHeaderItem.forScript(getFocusScript()));
			focus = false;
		}
	}
	
}
