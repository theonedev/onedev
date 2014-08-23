package com.pmease.commons.git.extensionpoint;

import javax.annotation.Nullable;
import org.apache.tika.mime.MediaType;
import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface TextConverterProvider {
	@Nullable TextConverter getTextConverter(MediaType mediaType);
}
