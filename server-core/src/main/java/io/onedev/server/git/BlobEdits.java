package io.onedev.server.git;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Joiner;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.git.exception.BlobEditException;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Mark;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.component.markdown.OutdatedSuggestionException;

public class BlobEdits implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Set<String> oldPaths;
	
	private final Map<String, BlobContent> newBlobs;
	
	public BlobEdits() {
		this(new HashSet<>(), new HashMap<>());
	}
	
	public BlobEdits(Set<String> oldPaths, Map<String, BlobContent> newBlobs) {
		this.oldPaths = new HashSet<>();
		for (String oldPath: oldPaths) {
			String normalizedPath = GitUtils.normalizePath(oldPath);
			if (normalizedPath != null)
				this.oldPaths.add(normalizedPath);
			else
				throw new BlobEditException("Invalid old path: " + oldPath);
		}
		this.newBlobs = new HashMap<>();
		for (Map.Entry<String, BlobContent> entry: newBlobs.entrySet()) { 
			String normalizedPath = GitUtils.normalizePath(entry.getKey());
			if (normalizedPath != null)
				this.newBlobs.put(normalizedPath, entry.getValue());
			else
				throw new BlobEditException("Invalid new path: " + entry.getKey());
		}
	}

	public Set<String> getOldPaths() {
		return oldPaths;
	}

	public Map<String, BlobContent> getNewBlobs() {
		return newBlobs;
	}

	public void applySuggestion(Project project, Mark mark, List<String> suggestion, ObjectId commitId) {
		Map<String, BlobContent> newBlobs = getNewBlobs();
		BlobContent blobContent = newBlobs.get(mark.getPath());
		if (blobContent == null) {
			BlobIdent newBlobIdent = new BlobIdent(commitId.name(), mark.getPath());
			Blob newBlob = project.getBlob(newBlobIdent, false);
			if (newBlob == null || newBlob.getText() == null || newBlob.getLfsPointer() != null)
				throw new OutdatedSuggestionException(mark);
			Set<String> oldPaths = getOldPaths();
			oldPaths.add(mark.getPath());
			blobContent = new BlobContent(newBlob.getBytes(), FileMode.REGULAR_FILE.getBits());
			newBlobs.put(mark.getPath(), blobContent);
		}

		BlobIdent oldBlobIdent = new BlobIdent(mark.getCommitHash(), mark.getPath());
		Blob.Text oldBlobText = project.getBlob(oldBlobIdent, true).getText();
		List<String> oldLines = oldBlobText.getLines();
		
		String newTextContent = new String(blobContent.getBytes(), oldBlobText.getCharset());
		List<String> newLines = new Blob.Text(oldBlobText.getCharset(), newTextContent).getLines();
		
		Map<Integer, Integer> lineMapping = DiffUtils.mapLines(oldLines, newLines);
		PlanarRange newRange = DiffUtils.mapRange(lineMapping, mark.getRange());
		if (newRange != null) {
			String eol = newTextContent.contains("\r\n")?"\r\n":"\n";
			List<String> editLines = new ArrayList<>();
			for (int i=0; i<newRange.getFromRow(); i++)
				editLines.add(StringUtils.stripEnd(newLines.get(i), "\r"));
			editLines.addAll(suggestion);
			for (int i=newRange.getToRow()+1; i<newLines.size(); i++)
				editLines.add(StringUtils.stripEnd(newLines.get(i), "\r"));
			byte[] editBytes = Joiner.on(eol).join(editLines).getBytes(oldBlobText.getCharset());
			newBlobs.put(mark.getPath(), new BlobContent(editBytes, FileMode.REGULAR_FILE.getBits()));
		} else {
			throw new OutdatedSuggestionException(mark);
		}
	}
	
}
