package com.gitplex.server.web.component.comment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AttachmentManager;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.web.behavior.markdown.AttachmentSupport;
import com.gitplex.server.web.util.resource.AttachmentResource;
import com.gitplex.server.web.util.resource.AttachmentResourceReference;
import com.google.common.base.Preconditions;

import jersey.repackaged.com.google.common.base.Throwables;

public class DepotAttachmentSupport implements AttachmentSupport {

	private static final long serialVersionUID = 1L;

	private static final int MAX_FILE_SIZE = 50*1024*1024; // mega bytes
	
	private static final int BUFFER_SIZE = 1024*64;
	
	private final Long depotId;
	
	private final String attachmentDirUUID;
	
	public DepotAttachmentSupport(Depot depot, String attachmentDirUUID) {
		depotId = depot.getId();
		this.attachmentDirUUID = attachmentDirUUID;
	}
	
	@Override
	public String getAttachmentUrl(String attachment) {
		PageParameters params = AttachmentResource.paramsOf(getDepot(), attachmentDirUUID, attachment);
		return RequestCycle.get().urlFor(new AttachmentResourceReference(), params).toString();
	}
	
	@Override
	public List<String> getAttachments() {
		List<String> attachments = new ArrayList<>();
		File attachmentDir = GitPlex.getInstance(AttachmentManager.class).getAttachmentDir(getDepot(), attachmentDirUUID);
		if (attachmentDir.exists()) {
			for (File file: attachmentDir.listFiles())
				attachments.add(file.getName());
		}
		return attachments;
	}

	private File getAttachmentDir() {
		return GitPlex.getInstance(AttachmentManager.class).getAttachmentDir(getDepot(), attachmentDirUUID);
	}
	
	@Override
	public void deleteAttachemnt(String attachment) {
		File attachmentDir = GitPlex.getInstance(AttachmentManager.class).getAttachmentDir(getDepot(), attachmentDirUUID);
		FileUtils.deleteFile(new File(attachmentDir, attachment));
	}

	@Override
	public long getAttachmentMaxSize() {
		return MAX_FILE_SIZE;
	}

	private Depot getDepot() {
		return GitPlex.getInstance(DepotManager.class).load(depotId);
	}

	@Override
	public String saveAttachment(String suggestedAttachmentName, InputStream attachmentStream) {
		Preconditions.checkState(SecurityUtils.canRead(getDepot()));
		
		String attachmentName = suggestedAttachmentName;
		File attachmentDir = getAttachmentDir();
		FileUtils.createDir(attachmentDir);
		int index = 2;
		while (new File(attachmentDir, attachmentName).exists()) {
			if (suggestedAttachmentName.contains(".")) {
				String nameBeforeExt = StringUtils.substringBeforeLast(suggestedAttachmentName, ".");
				String ext = StringUtils.substringAfterLast(suggestedAttachmentName, ".");
				attachmentName = nameBeforeExt + "_" + index + "." + ext;
			} else {
				attachmentName = suggestedAttachmentName + "_" + index;
			}
			index++;
		}
		
		Exception ex = null;
		File file = new File(attachmentDir, attachmentName);
		try (OutputStream os = new FileOutputStream(file)) {
			byte[] buffer = new byte[BUFFER_SIZE];
	        long count = 0;
	        int n = 0;
	        while (-1 != (n = attachmentStream.read(buffer))) {
	            count += n;
		        if (count > getAttachmentMaxSize()) {
		        	throw new RuntimeException("Upload must be less than " 
		        			+ FileUtils.byteCountToDisplaySize(getAttachmentMaxSize()));
		        }
	            os.write(buffer, 0, n);
	        }
		} catch (Exception e) {
			ex = e;
		} 
		if (ex != null) {
			if (file.exists())
				FileUtils.deleteFile(file);
			throw Throwables.propagate(ex);
		} else {
			return file.getName();
		}
	}
}
