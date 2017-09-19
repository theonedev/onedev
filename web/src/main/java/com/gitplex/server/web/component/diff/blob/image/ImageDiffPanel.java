package com.gitplex.server.web.component.diff.blob.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.gitplex.server.git.Blob;
import com.gitplex.server.git.BlobChange;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.util.Provider;

@SuppressWarnings("serial")
public class ImageDiffPanel extends Panel {

	private final BlobChange change;
	
	public ImageDiffPanel(String id, BlobChange change) {
		super(id);
	
		this.change = change;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(newImage("old", change.getOldBlobIdent(), new Provider<Blob>() {

			@Override
			public Blob get() {
				return change.getOldBlob();
			}
			
		}));
		
		add(newImage("new", change.getNewBlobIdent(), new Provider<Blob>() {

			@Override
			public Blob get() {
				return change.getNewBlob();
			}
			
		}));
		
	}
	
	private Image newImage(String id, BlobIdent blobIdent, Provider<Blob> blobProvider) {
		Image image;
		if (blobIdent.path != null) {
			add(image = new Image(id, new DynamicImageResource() {
				
				@Override
				protected void configureResponse(ResourceResponse response, Attributes attributes) {
					super.configureResponse(response, attributes);
					if (!GitUtils.isHash(blobIdent.revision))
						response.disableCaching();
					response.setContentType(blobProvider.get().getMediaType().toString());
					response.setFileName(blobIdent.getName());
				}

				@Override
				protected byte[] getImageData(Attributes attributes) {
					return blobProvider.get().getBytes();
				}
			}));
			BufferedImage bufferedImage;
			try {
				bufferedImage = ImageIO.read(new ByteArrayInputStream(blobProvider.get().getBytes()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			image.add(AttributeAppender.append("width", bufferedImage.getWidth()));
			image.add(AttributeAppender.append("height", bufferedImage.getHeight()));
		} else {
			add(image = new Image(id, new PackageResourceReference(ImageDiffPanel.class, "blank.png")));
			image.add(AttributeAppender.append("width", "64px"));
			image.add(AttributeAppender.append("height", "64px"));
		}
		return image;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ImageDiffResourceReference()));
		String script = String.format("gitplex.server.imageDiff.onDomReady('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
