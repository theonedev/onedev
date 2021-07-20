package io.onedev.server.imports;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.SimpleLogger;

public abstract class Importer<Where extends Serializable,  What extends Serializable, How extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Class<Where> whereClass;
	
	private final Class<What> whatClass;
	
	private final Class<How> howClass;

	@SuppressWarnings("unchecked")
	public Importer() {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(Importer.class, getClass());
		whereClass = (Class<Where>) typeArguments.get(0);
		whatClass = (Class<What>) typeArguments.get(1);
		howClass = (Class<How>) typeArguments.get(2);
	}
	
	public Class<Where> getWhereClass() {
		return whereClass;
	}
	
	public Class<What> getWhatClass() {
		return whatClass;
	}
	
	public Class<How> getHowClass() {
		return howClass;
	}
	
	public abstract String getName();

	public abstract What getWhat(Where where, SimpleLogger logger);
	
	public abstract How getHow(Where where, What what, SimpleLogger logger);
	
}
