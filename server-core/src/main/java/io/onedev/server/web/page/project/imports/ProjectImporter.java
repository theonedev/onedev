package io.onedev.server.web.page.project.imports;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.SimpleLogger;

public abstract class ProjectImporter<T extends Serializable, S extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Class<T> tokenClass;
	
	private final Class<S> optionClass;

	@SuppressWarnings("unchecked")
	public ProjectImporter() {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(ProjectImporter.class, getClass());
		tokenClass = (Class<T>) typeArguments.get(0);
		optionClass = (Class<S>) typeArguments.get(1);
	}
	
	public Class<T> getTokenClass() {
		return tokenClass;
	}
	
	public Class<S> getOptionClass() {
		return optionClass;
	}
	
	public abstract String getName();

	public abstract S getImportOption(T importSource, SimpleLogger logger);
	
	@Nullable
	public abstract String doImport(T importSource, S importOption, boolean dryRun, SimpleLogger logger);
	
}
