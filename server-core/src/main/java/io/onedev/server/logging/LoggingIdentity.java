package io.onedev.server.logging;

import java.io.File;
import java.io.Serializable;

public interface LoggingIdentity extends Serializable {

    File getFile();

    String getCacheKey();

    String getLockName();

}