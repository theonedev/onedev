package io.onedev.server.web.component.user.choice;

import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.Similarities;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class UserChoiceProvider extends AbstractUserChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final IModel<List<User>> choicesModel;
	
	public UserChoiceProvider(IModel<List<User>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<User> response) {
		List<User> users = choicesModel.getObject();
		UserManager userManager = OneDev.getInstance(UserManager.class);
		
		UserCache cache = userManager.cloneCache();
		
		List<User> similarities = new Similarities<User>(users) {

			private static final long serialVersionUID = 1L;

			@Override
			public double getSimilarScore(User object) {
				return cache.getSimilarScore(object, term);
			}
			
		};
		
		new ResponseFiller<User>(response).fill(similarities, page, WebConstants.PAGE_SIZE);
	}

}