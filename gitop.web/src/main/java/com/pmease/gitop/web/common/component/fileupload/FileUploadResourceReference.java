package com.pmease.gitop.web.common.component.fileupload;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.upload.FileItem;

import com.pmease.gitop.web.SitePaths;

/**
 * A resource reference provides default implementation of
 * AbstractFileUploadResource. The implementation generates JSON response as
 * expected by the demo at <a href="http://blueimp.github.com/jQuery-File-Upload/">
 * http://blueimp.github.com/jQuery-File-Upload</a>
 */
@SuppressWarnings("serial")
public class FileUploadResourceReference extends ResourceReference {
	
	private final FileManager fileManager;

	public FileUploadResourceReference(String baseFolder) {
		super(FileUploadResourceReference.class, "file-upload");

		this.fileManager = new FileManager(baseFolder);
	}

	@Override
	public IResource getResource() {
		return new AbstractFileUploadResource(fileManager) {
			@Override
			protected String generateJsonResponse(
					ResourceResponse resourceResponse,
					ServletWebRequest webRequest, List<FileItem> files) {
				JSONArray json = new JSONArray();

				for (FileItem fileItem : files) {
					JSONObject fileJson = new JSONObject();

					try {
						fileJson.put("name", fileItem.getName());
						fileJson.put("url", getViewUrl(fileItem));
						fileJson.put("thumbnail_url", getViewUrl(fileItem));
						fileJson.put("size", fileItem.getSize());
						fileJson.put("delete_type", "POST");
						fileJson.put("delete_url", getDeleteUrl(fileItem));
					} catch (JSONException e) {
						try {
							fileJson.put("error", e.getMessage());
						} catch (JSONException e1) {
							e1.printStackTrace();
						}
					}

					json.put(fileJson);
				}

				return json.toString();
			}

			@Override
			protected String generateHtmlResponse(
					ResourceResponse resourceResponse,
					ServletWebRequest webRequest, List<FileItem> files) {
				String jsonResponse = generateJsonResponse(resourceResponse,
						webRequest, files);
				String escapedJson = StringEscapeUtils.escapeHtml4(jsonResponse);
				return escapedJson;
			}
		};
	}

	private CharSequence getViewUrl(FileItem fileItem) {
		PageParameters params = new PageParameters();
		params.set("filename", fileItem.getName());
		CharSequence url = RequestCycle.get().urlFor(
				new FileManagerResourceReference(SitePaths.get().uploadsDir().getAbsolutePath()),
				params);
		return url;
	}

	private CharSequence getDeleteUrl(FileItem fileItem) {
		PageParameters params = new PageParameters();
		params.set("filename", fileItem.getName());
		params.set("delete", true);
		CharSequence url = RequestCycle.get().urlFor(
				new FileManagerResourceReference(SitePaths.get().uploadsDir().getAbsolutePath()),
				params);
		return url;
	}

}