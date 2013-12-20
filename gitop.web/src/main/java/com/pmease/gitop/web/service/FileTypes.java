package com.pmease.gitop.web.service;

import java.io.InputStream;

import org.apache.tika.mime.MediaType;

public interface FileTypes {
	 /**
     * Get the most specific media type available for a file.
     * 
     * @param path name of the file. The base name (component after the last
     *        '/') may be used to help determine the media type, such as by
     *        examining the extension (portion after the last '.' if present).
     * @param content the complete file content. If non-null the content may be
     *        used to guess the media type by examining the beginning for common
     *        file headers.
     * @return the media type for this content. If the media type is not
     *         recognized or cannot be determined, return
     *         {@code application/octet-stream}.
     */
    MediaType getMediaType(final String path, final byte[] content);
    
    /**
     * Get the most specific media type available for a file.
     * NOTE: this function will never close the inputstream. You need close the
     * inputstream yourself.
     * 
     * @param path name of the file. The base name (component after the last
     *        '/') may be used to help determine the media type, such as by
     *        examining the extension (portion after the last '.' if present).
     * @param in the inputstream.
     * @return the media type for this content. If the media type is not
     *         recognized or cannot be determined, return
     *         {@code application/octet-stream}.
     */
    MediaType getMediaType(final String path, final InputStream in);

    /**
     * Is this content type safe to transmit to a browser directly?
     * 
     * @param type the media type of the file content.
     * @return true if the gitop administrator wants to permit this content to
     *         be served as-is; false if the administrator does not trust this
     *         content type and wants it to be protected (typically by wrapping
     *         the data in a ZIP archive).
     */
    boolean isSafeInline(final MediaType type);

}
