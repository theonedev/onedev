package io.onedev.server.web.page.project.issues.imports;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.SimpleLogger;

public abstract class IssueImporter<T extends Serializable, S extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Class<T> sourceClass;
	
	private final Class<S> optionClass;

	@SuppressWarnings("unchecked")
	public IssueImporter() {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(IssueImporter.class, getClass());
		sourceClass = (Class<T>) typeArguments.get(0);
		optionClass = (Class<S>) typeArguments.get(1);
	}
	
	public Class<T> getSourceClass() {
		return sourceClass;
	}
	
	public Class<S> getOptionClass() {
		return optionClass;
	}
	
	public abstract String getName();

	public abstract S getImportOption(T importSource, SimpleLogger logger);
	
	@Nullable
	public abstract String doImport(Project project, T importSource, S importOption, boolean dryRun, SimpleLogger logger);
	
}
