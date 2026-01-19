package io.onedev.server.web.component.diff.blob.image;

import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.util.Provider;
import io.onedev.server.web.resource.RawBlobResource;
import io.onedev.server.web.resource.RawBlobResourceReference;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.ResourceReference;

public class BlobImageDiffPanel extends Panel {

	private final BlobChange change;
	
	public BlobImageDiffPanel(String id, BlobChange change) {
		super(id);
	
		this.change = change;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Image oldImage = newImage("old", change.getOldBlobIdent(), new Provider<Blob>() {

			@Override
			public Blob get() {
				return change.getOldBlob();
			}
			
		});
		add(oldImage);
		
		Label oldPlaceholder = new Label("oldPlaceholder", _T("Not available"));
		oldPlaceholder.setVisible(!oldImage.isVisible());
		add(oldPlaceholder);
		
		Image newImage = newImage("new", change.getNewBlobIdent(), new Provider<Blob>() {

			@Override
			public Blob get() {
				return change.getNewBlob();
			}
			
		});
		add(newImage);
		
		Label newPlaceholder = new Label("newPlaceholder", _T("Not available"));
		newPlaceholder.setVisible(!newImage.isVisible());
		add(newPlaceholder);
		
		add(AttributeAppender.append("class", "border border-top-0 rounded-bottom blob-image-diff d-flex"));
	}
	
	private Image newImage(String id, BlobIdent blobIdent, Provider<Blob> blobProvider) {
		Image image;
		if (blobIdent.path != null) {
			image = new Image(id, new RawBlobResourceReference(), 
					RawBlobResource.paramsOf(change.getProject(), blobIdent));
			image.setVisible(true);
		} else {
			image = new Image(id, (ResourceReference) null);
			image.setVisible(false);
		}
		return image;
	}

}
