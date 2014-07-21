package com.pmease.gitplex.web.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.pmease.gitplex.web.common.quantity.Data;
import com.pmease.gitplex.web.service.FileTypes;
import com.pmease.gitplex.web.util.MediaTypeUtils;

@Singleton
public class TikaFileTypes implements FileTypes {
	
	private final Tika tika;
	
	@Inject
	TikaFileTypes(MimeTypes mimeTypes) {
		this.tika = new Tika();
		tika.setMaxStringLength(2 * (int) Data.ONE_MB);
	}
	
	@Override
	public MediaType getMediaType(final String path, final byte[] content) {
		String type = tika.detect(content, path);
		
		if (MimeType.isValid(type)) {
			return MediaType.parse(type);
		}
		
		return MediaType.OCTET_STREAM;
	}
	
	@Override
	public MediaType getMediaType(final String path, final InputStream in) {
		try {
			String type = tika.detect(in, path);
			if (MimeType.isValid(type)) {
				return MediaType.parse(type);
			}
			
			return MediaType.OCTET_STREAM;
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
	
	
	static final Set<MediaType> EXTRA_SAFE_TYPES = ImmutableSet.<MediaType>builder()
			.addAll(MediaTypeUtils.EXTRA_TEXT_TYPES)
			.add(MediaType.image("png"))
			.add(MediaType.image("jpeg"))
			.add(MediaType.image("gif"))
			.add(MediaType.image("ico"))
			.build();
    

	@Override
    public boolean isSafeInline(final MediaType type) {
        return MediaTypeUtils.isTextType(type)
                || EXTRA_SAFE_TYPES.contains(type);
    }
}
