package io.onedev.server.web.upload;

import io.onedev.server.OneDev;
import org.apache.commons.fileupload.FileItem;
import org.apache.wicket.util.string.Strings;

import java.util.Date;
import java.util.List;

public class FileUpload {
	
	private final String id;
	
	private final Date date = new Date();
	
	private final List<FileItem> items;
	
	public FileUpload(String id, List<FileItem> items) {
		this.id = id;
		this.items = items;
	}

	public String getId() {
		return id;
	}

	public List<FileItem> getItems() {
		return items;
	}

	public Date getDate() {
		return date;
	}
	
	public void clear() {
		OneDev.getInstance(UploadManager.class).clearUpload(id);
	}

	public static String getFileName(FileItem file) {
		String name = file.getName();

		// when uploading from localhost some browsers will specify the entire path, we strip it
		// down to just the file name
		name = Strings.lastPathComponent(name, '/');
		name = Strings.lastPathComponent(name, '\\');

		return name;
	}
	
}
