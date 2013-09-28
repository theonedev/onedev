package com.pmease.gitop.web.common.component.fileuploader;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.template.PackageTextTemplate;

import com.pmease.gitop.web.GitopWebApp;
import com.pmease.gitop.web.assets.AssetLocator;

@SuppressWarnings("serial")
public class FileUploadResourceBehavior extends Behavior {

	private static final ResourceReference FILEUPLOAD_JS = 
			new JavaScriptResourceReference(
					AssetLocator.class, 
					"js/vendor/jquery-file-upload/jquery.file.upload.js");
	
	private static final ResourceReference IFRAME_TRANSPORT_JS =
			new JavaScriptResourceReference(
					AssetLocator.class,
					"js/vendor/jquery-file-upload/jquery.iframe-transport.js");
	
	private static final ResourceReference FILEUPLOAD_UI_JS =
			new JavaScriptResourceReference(
					AssetLocator.class,
					"js/vendor/jquery-file-upload/jquery.fileupload-ui.js");
	
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
		
		response.render(JavaScriptHeaderItem.forReference(AssetLocator.JQUERY_UI_WIDGET_JS));
		response.render(JavaScriptHeaderItem.forReference(IFRAME_TRANSPORT_JS));
		response.render(JavaScriptHeaderItem.forReference(FILEUPLOAD_JS));
		response.render(JavaScriptHeaderItem.forReference(FILEUPLOAD_UI_JS));
		
		PackageTextTemplate jsTmpl = new PackageTextTemplate(AssetLocator.class, "js/vendor/jquery-file-upload/main.js");
		try {
	        Map<String, Object> variables = new HashMap<String, Object>();
	
	        variables.put("componentMarkupId", component.getMarkupId());
	        variables.put("url", component.urlFor(new FileUploadResourceReference(GitopWebApp.get().getUploadsDir().getAbsolutePath()), null));
	        variables.put("paramName", PARAM_NAME);
	
	        String s = jsTmpl.asString(variables);
	        response.render(JavaScriptHeaderItem.forScript(s, "fileupload"));
		} finally {
			IOUtils.closeQuietly(jsTmpl);
		}
	}
}
