package com.pmease.gitplex.web.component.blobview.image;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;

@SuppressWarnings("serial")
public class ImageViewPanel extends BlobViewPanel {

	public ImageViewPanel(String id, BlobViewContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ResourceStreamResource resource = new ResourceStreamResource(new AbstractResourceStream() {

			private InputStream is = null;
			
			@Override
			public Bytes length() {
				return Bytes.bytes(context.getBlob().getSize());
			}

			@Override
			public String getContentType() {
				return context.getBlob().getMediaType().toString();
			}

			@Override
			public InputStream getInputStream() throws ResourceStreamNotFoundException {
				is = context.getRepository().getInputStream(context.getBlobIdent());
				return is;
			}

			@Override
			public void close() throws IOException {
				if (is != null) {
					is.close();
					is = null;
				}
			}
			
		}); 
		resource.setFileName(StringUtils.substringAfterLast(context.getBlobIdent().path, "/"));
		add(new Image("img", resource));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(ImageViewPanel.class, "image-view.css")));
	}

}
