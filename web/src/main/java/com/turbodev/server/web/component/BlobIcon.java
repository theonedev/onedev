package com.turbodev.server.web.component;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.turbodev.server.git.BlobIdent;

@SuppressWarnings("serial")
public class BlobIcon extends WebComponent {

	public BlobIcon(String id, IModel<BlobIdent> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				BlobIdent blobIdent = (BlobIdent) getDefaultModelObject();
				if (blobIdent.isTree())
					return " fa fa-folder-o";
				else if (blobIdent.isGitLink()) 
					return " fa fa-ext fa-folder-submodule-o";
				else if (blobIdent.isSymbolLink()) 
					return " fa fa-ext fa-folder-symbol-link-o";
				else  
					return " fa fa-file-text-o";
			}
			
		}));
	}

}
