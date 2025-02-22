package io.onedev.server.web.editable.buildspec.imports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import io.onedev.server.buildspec.Import;
import io.onedev.server.web.editable.BeanContext;

public class ImportListViewPanel extends Panel {

	private final List<Import> imports = new ArrayList<>();
	
	public ImportListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
 		for (Serializable each: elements) 
			imports.add((Import) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		RepeatingView importsView = new RepeatingView("imports");
		for (Import aImport: imports)
			importsView.add(BeanContext.view(importsView.newChildId(), aImport));
		add(importsView);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ImportCssResourceReference()));
	}

}
