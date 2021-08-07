package io.onedev.server.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.wicket.util.string.Strings;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.web.UploadItemManager;

public class FileUpload implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String uploadId;
	
	private final int uploadIndex;
	
	private transient FileItem uploadItem;
	
	public FileUpload(String uploadId, int uploadIndex) {
		this.uploadId = uploadId;
		this.uploadIndex = uploadIndex;
	}
	
	private FileItem getUploadItem() {
		if (uploadItem == null) {
			List<FileItem> items = OneDev.getInstance(UploadItemManager.class).getUploadItems(uploadId);
			if (uploadIndex >= items.size())
				throw new ExplicitException("Uploaded files timed out and cleaned up");
			uploadItem = items.get(uploadIndex);
		}
		return uploadItem;
	}
	
	public String getFileName() {
		String name = getUploadItem().getName();

		// when uploading from localhost some browsers will specify the entire path, we strip it
		// down to just the file name
		name = Strings.lastPathComponent(name, '/');
		name = Strings.lastPathComponent(name, '\\');

		return name;
	}
	
	public InputStream getInputStream() {
		try {
			return getUploadItem().getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getContentType() {
		return getUploadItem().getContentType();
	}

	public byte[] getBytes() {
		return getUploadItem().get();
	}
	
	public void release() {
		OneDev.getInstance(UploadItemManager.class).setUploadItems(uploadId, new ArrayList<>());
	}
	
}
