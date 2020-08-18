package io.onedev.server.web.component.blob;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class BlobIcon extends WebComponent {

	public BlobIcon(String id, IModel<BlobIdent> model) {
		super(id, model);
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		String icon;
		BlobIdent blobIdent = (BlobIdent) getDefaultModelObject();
		
		if (blobIdent.path.equals(BuildSpec.BLOB_PATH) || blobIdent.path.equals(".onedev-buildspec"))
			icon = "gear";
		else if (blobIdent.isTree())
			icon = "folder";
		else if (blobIdent.isGitLink()) 
			icon = "folder-embed";
		else if (blobIdent.isSymbolLink()) 
			icon = "folder-redo";
		else  
			icon = "file";
		
		String versionedIcon = SpriteImage.getVersionedHref(IconScope.class, icon);
		replaceComponentTagBody(markupStream, openTag, 
				"<use xlink:href='" + versionedIcon + "'></use>");
	}
	
}
