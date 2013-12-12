package com.pmease.gitop.web.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.util.lang.Bytes;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.pmease.gitop.web.service.FileTypeRegistry;
import com.pmease.gitop.web.util.MimeTypeUtils;

@Singleton
public class TikaFileTypeRegistry implements FileTypeRegistry {
	
	public static class Module extends AbstractModule {

		@Override
		protected void configure() {
//			bind(Languages.class).in(Singleton.class);
			bind(FileTypeRegistry.class).to(TikaFileTypeRegistry.class);
		}
	}
	
	private final Tika tika;
	private final MimeTypes mimeTypes;
	
	@Inject
	TikaFileTypeRegistry() {
		this.tika = new Tika();
		tika.setMaxStringLength((int) Bytes.megabytes(10).bytes());
		this.mimeTypes = MimeTypes.getDefaultMimeTypes();
	}
	
	@Override
	public MimeType getMimeType(final String path, final byte[] content) {
		try {
			String type = tika.detect(content, path);
			
			if (MimeType.isValid(type)) {
				return mimeTypes.getRegisteredMimeType(type);
			}
			
			return mimeTypes.getRegisteredMimeType(MimeTypes.OCTET_STREAM);
		} catch (MimeTypeException e) {
			throw Throwables.propagate(e);
		}
	}
	
	@Override
	public MimeType getMimeType(final String path, final InputStream in) {
		try {
			String type = tika.detect(in, path);
			
			if (MimeType.isValid(type)) {
				return mimeTypes.getRegisteredMimeType(type);
			}
			
			return mimeTypes.getRegisteredMimeType(MimeTypes.OCTET_STREAM);
		} catch (MimeTypeException e) {
			throw Throwables.propagate(e);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
	
	
	static final Set<String> DEFAULT_SAFE_TYPES = ImmutableSet.of(
            "application/x-sh",
            "application/javascript",
            "application/x-httpd-jsp",
            
            // images
            "image/bmp",
            "image/png",
            "image/jpeg",
            "image/x-xbitmap", // xbm
            "image/x-xpixmap"  // xpm
            );
    

	@Override
    public boolean isSafeInline(final MimeType type) {
        return MimeTypeUtils.isTextType(type)
        		|| MimeTypeUtils.isXMLType(type)
                || DEFAULT_SAFE_TYPES.contains(type.getName());
    }

//    boolean isSafe(Config cfg, MimeType type, boolean def) {
//        return isTextType(type) ||
//                isXMLType(type) ||
//                cfg.getBoolean(SECTION_MIMETYPE, type.getName(), KEY_SAFE, def);
//    }
}
