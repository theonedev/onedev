package io.onedev.server.git;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.tika.mime.MediaType;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;

import com.google.common.base.Optional;

import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.web.component.markdown.SuggestionSupport.Selection;

public class Blob implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final int MAX_LOADED_BLOB_SIZE = 5*1024*1024;
	
	private final BlobIdent ident;
	
	private final ObjectId blobId;
	
	private final byte[] bytes;
	
	private final long size;
	
	private transient MediaType mediaType;
	
	private transient Optional<Text> text;
	
	private transient Optional<LfsPointer> lfsPointer;
	
	public Blob(BlobIdent ident, ObjectId blobId, ObjectReader objectReader) {
		this.ident = ident;
		this.blobId = blobId;
		ObjectLoader objectLoader;
		try {
			objectLoader = objectReader.open(blobId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		long blobSize = objectLoader.getSize();
		if (blobSize > MAX_LOADED_BLOB_SIZE) {
			try (InputStream is = objectLoader.openStream()) {
				byte[] bytes = new byte[Blob.MAX_LOADED_BLOB_SIZE];
				is.read(bytes);
				size = blobSize;
				this.bytes = bytes;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			bytes = objectLoader.getCachedBytes();
			size = bytes.length;
		}
	}
	
	public Blob(BlobIdent ident, ObjectId blobId, byte[] bytes) {
		this(ident, blobId, bytes, bytes.length);
	}
	
	public Blob(BlobIdent ident, ObjectId blobId, byte[] bytes, long size) {
		this.ident = ident;
		this.blobId = blobId;
		this.bytes = bytes;
		this.size = size;
	}

	public BlobIdent getIdent() {
		return ident;
	}

	public ObjectId getBlobId() {
		return blobId;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public long getSize() {
		return size;
	}
	
	public boolean isPartial() {
		return bytes.length < size;
	}
	
	public MediaType getMediaType() {
		if (mediaType == null) {
			if (ident.isGitLink() || ident.isSymbolLink() || ident.isTree())
				mediaType = MediaType.TEXT_PLAIN;
			else
				mediaType = ContentDetector.detectMediaType(bytes, ident.path);
		}
		return mediaType;
	}
	
	/**
	 * Get text representation of this blob.
	 * 
	 * @return
	 * 			text representation of this blob, or <tt>null</tt> if this blob 
	 * 			content is binary
	 */
	public @Nullable Text getText() {
 		if (text == null) {
 			if (ident.isGitLink() || ident.isSymbolLink() || ident.isTree()) {
 				Charset charset = StandardCharsets.UTF_8;
 				text = Optional.of(new Text(charset, new String(bytes, charset)));
 			} else if (!ContentDetector.isBinary(bytes, ident.path)) {
				Charset charset = ContentDetector.detectCharset(bytes);
				if (charset == null)
					charset = Charset.defaultCharset();
				text = Optional.of(new Text(charset, new String(bytes, charset)));
			} else {
				text = Optional.absent();
			}
		}
		return text.orNull();
	}
	
	@Nullable
	public LfsPointer getLfsPointer() {
		if (lfsPointer == null) {
			Text text = getText();
			if (text != null) {
				if (!text.getLines().isEmpty() 
						&& text.getLines().get(0).startsWith("version https://git-lfs.github.com/spec/")) {
					String objectId = StringUtils.substringAfter(text.getLines().get(1), ":");
					long objectSize = Long.parseLong(StringUtils.substringAfter(text.getLines().get(2), " ")); 
					lfsPointer = Optional.of(new LfsPointer(objectId, objectSize));
				} else {
					lfsPointer = Optional.absent();
				}
			} else {
				lfsPointer = Optional.absent();
			}
		}
		return lfsPointer.orNull();
	}
	
	public static class Text implements Serializable {

		private static final long serialVersionUID = 1L;

		private final Charset charset;
		
		private final String content;
		
		private transient List<String> lines;
		
		public Text(Charset charset, String content) {
			this.charset = charset;
			this.content = content;
		}

		public Charset getCharset() {
			return charset;
		}

		public String getContent() {
			return content;
		}

		/**
		 * Get lines of the content. In case of using crlf at end of line, the cr character 
		 * will be preserved at end of line
		 * @return
		 */
		public List<String> getLines() {
			if (lines == null) {
				lines = new ArrayList<>();
				StringBuilder builder = new StringBuilder();
				for (int i=0; i<content.length(); i++) {
					char ch = content.charAt(i);
					if (ch == '\n') {
						lines.add(builder.toString());
						builder = new StringBuilder();
					} else {
						builder.append(ch);
					}
				}
				lines.add(builder.toString());
			}
			return lines;
		}
		
		public Selection getSelection(PlanarRange range) {
			List<String> editLines = new ArrayList<>();
			
			for (int i=range.getFromRow(); i<=range.getToRow(); i++) 
				editLines.add(StringUtils.stripEnd(getLines().get(i), "\r"));
			
			int to = 0;
			for (int i=0; i<editLines.size()-1; i++)
				to += editLines.get(i).length() + 1;
			to += range.getToColumn();
			
			return new Selection(editLines, new LinearRange(range.getFromColumn(), to));
		}
		
	}
	
}
