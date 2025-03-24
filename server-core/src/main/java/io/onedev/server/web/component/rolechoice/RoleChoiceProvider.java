package io.onedev.server.web.component.rolechoice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.Role;
import io.onedev.server.util.Similarities;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class RoleChoiceProvider extends AbstractRoleChoiceProvider {

	private static final long serialVersionUID = 1L;

	private final IModel<Collection<Role>> choicesModel;
	
	public RoleChoiceProvider(IModel<Collection<Role>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<Role> response) {
		List<Role> similarities = new Similarities<Role>(choicesModel.getObject()) {

			private static final long serialVersionUID = 1L;

			@Override
			public double getSimilarScore(Role object) {
				return Similarities.getSimilarScore(object.getName(), term);
			}
			
		};
		
		new ResponseFiller<Role>(response).fill(similarities, page, WebConstants.PAGE_SIZE);
	}

}