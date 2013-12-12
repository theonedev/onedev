package com.pmease.gitop.web.common.wicket.component.fileupload;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.web.assets.AssetLocator;

@SuppressWarnings("serial")
public class FileUploadResourceBehavior extends Behavior {

	private static final ResourceReference FILEUPLOAD_JS = 
			new JavaScriptResourceReference(
					FileUploadResourceBehavior.class, 
					"res/js/jquery.fileupload.js");
	
	private static final ResourceReference IFRAME_TRANSPORT_JS =
			new JavaScriptResourceReference(
					FileUploadResourceBehavior.class,
					"res/js/jquery.iframe-transport.js");
	
	private static final ResourceReference FILEUPLOAD_UI_JS =
			new JavaScriptResourceReference(
					FileUploadResourceBehavior.class,
					"res/js/jquery.fileupload-ui.js");
	
	/**
     * The name of the request parameter used for the multipart
     * Ajax request
     */
    public static final String PARAM_NAME = "FILE-UPLOAD";

    /**
     * Configures the connected component to render its markup id
     * because it is needed to initialize the JavaScript widget.
     * @param component
     */
    @Override
    public void bind(Component component) {
        super.bind(component);

        component.setOutputMarkupId(true);
    }

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(FileUploadResourceBehavior.class, "res/css/jquery.fileupload-ui.css")));
		
		response.render(JavaScriptHeaderItem.forReference(AssetLocator.JQUERY_UI_WIDGET_JS));
//		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FileUploadResourceBehavior.class, "res/js/tmpl.min.js")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FileUploadResourceBehavior.class, "res/js/load-image.min.js")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FileUploadResourceBehavior.class, "res/js/jquery.blueimp-gallery.min.js")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FileUploadResourceBehavior.class, "res/js/canvas-to-blob.min.js")));
		
		response.render(JavaScriptHeaderItem.forReference(IFRAME_TRANSPORT_JS));
		response.render(JavaScriptHeaderItem.forReference(FILEUPLOAD_JS));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FileUploadResourceBehavior.class, "res/js/jquery.fileupload-process.js")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FileUploadResourceBehavior.class, "res/js/jquery.fileupload-audio.js")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FileUploadResourceBehavior.class, "res/js/jquery.fileupload-video.js")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FileUploadResourceBehavior.class, "res/js/jquery.fileupload-image.js")));
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FileUploadResourceBehavior.class, "res/js/jquery.fileupload-validate.js")));
		response.render(JavaScriptHeaderItem.forReference(FILEUPLOAD_UI_JS));
		
		
//		PackageTextTemplate jsTmpl = new PackageTextTemplate(FileUploadResourceBehavior.class, 
//				"res/js/main.js");
//		try {
//	        Map<String, Object> variables = new HashMap<String, Object>();
//	
//	        variables.put("componentId", component.getMarkupId());
//	        variables.put("url", "/rest/file/upload");
//	        variables.put("paramName", PARAM_NAME);
//	
//	        String s = jsTmpl.asString(variables);
//	        response.render(JavaScriptHeaderItem.forScript(s, "fileupload"));
//		} finally {
//			IOUtils.closeQuietly(jsTmpl);
//		}
	}
}
