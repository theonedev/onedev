package io.onedev.server.ci.job.cache;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.exception.OneException;
import io.onedev.server.util.validation.annotation.Path;
import io.onedev.server.util.validation.annotation.PathSegment;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class JobCache implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String LOCK_FILE = "$OneDev-Cache-Lock$";

	private String key;
	
	private String path;

	@Editable(order=100, description="Specify key of the cache. Caches with same key can be shared")
	@PathSegment
	@NotEmpty
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Editable(order=200, description="Specify path to cache. Non-absolute path is considered to be relative to job workspace. "
			+ "Specify \".\" (without quote) to cache workspace itself")
	@Path
	@NotEmpty
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public CacheAllocation allocate(File cacheHome) {
		File keyDir = new File(cacheHome, getKey());
		if (!keyDir.exists())
			FileUtils.createDir(keyDir);
		return LockUtils.call(keyDir.getAbsolutePath(), new Callable<CacheAllocation>() {

			@Override
			public CacheAllocation call() throws Exception {
				List<File> cacheInstances = Lists.newArrayList(keyDir.listFiles());
				cacheInstances.sort(Comparator.comparing(File::lastModified).reversed());
				for (File cacheInstance: cacheInstances) {
					if (new File(cacheInstance, LOCK_FILE).createNewFile())
						return new CacheAllocation(cacheInstance, path);
				}
				File cacheInstance = new File(keyDir, UUID.randomUUID().toString());
				FileUtils.createDir(cacheInstance);
				File lockFile = new File(cacheInstance, LOCK_FILE);
				if (!lockFile.createNewFile())
					throw new OneException("Unable to create file: " + lockFile.getAbsolutePath());
				return new CacheAllocation(cacheInstance, path);
			}
			
		});
	}
	
}
