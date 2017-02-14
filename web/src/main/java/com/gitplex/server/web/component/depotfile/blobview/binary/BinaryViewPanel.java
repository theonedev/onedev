package com.gitplex.server.web.component.depotfile.blobview.binary;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import com.gitplex.server.web.component.depotfile.blobview.BlobViewContext;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewPanel;

@SuppressWarnings("serial")
public class BinaryViewPanel extends BlobViewPanel {

	public BinaryViewPanel(String id, BlobViewContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new BinaryViewResourceReference()));
	}

}
