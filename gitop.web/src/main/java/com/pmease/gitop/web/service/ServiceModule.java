package com.pmease.gitop.web.service;

import javax.inject.Singleton;

import org.apache.tika.mime.MimeTypes;

import com.google.inject.AbstractModule;
import com.pmease.gitop.web.service.impl.DefaultFileBlobService;
import com.pmease.gitop.web.service.impl.TikaFileTypes;

public class ServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MimeTypes.class).toInstance(MimeTypes.getDefaultMimeTypes());
		bind(FileTypes.class).to(TikaFileTypes.class);
		bind(FileBlobService.class).to(DefaultFileBlobService.class).in(Singleton.class);
	}
}
