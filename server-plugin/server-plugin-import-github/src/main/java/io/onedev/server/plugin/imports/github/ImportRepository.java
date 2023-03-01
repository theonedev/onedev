package io.onedev.server.plugin.imports.github;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable
public class ImportRepository extends ImportOrganization {

	private static final long serialVersionUID = 1L;
	
	private String repository;

	@Editable(order=200, name="GitHub Repository", description="Select repository to import from")
	@ChoiceProvider("getRepositoryChoices")
	@NotEmpty
	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}
	
	@Override
	public boolean isIncludeForks() {
		return super.isIncludeForks();
	}

	@SuppressWarnings("unused")
	private static List<String> getRepositoryChoices() {
		BeanEditor editor = ComponentContext.get().getComponent().findParent(BeanEditor.class);
		ImportRepository setting = (ImportRepository) editor.getModelObject();
		String organization = (String) EditContext.get().getInputValue("organization");
		return setting.server.listRepositories(organization,  true);
	}
	
}
