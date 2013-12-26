package com.pmease.gitop.web.editable.user;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.choice.UserSingleChoice;

@SuppressWarnings("serial")
public class UserSingleChoiceEditor extends Panel {
	
	private final UserSingleChoiceEditContext editContext;

	public UserSingleChoiceEditor(String id, UserSingleChoiceEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	IModel<User> model = new IModel<User>() {

			@Override
			public void detach() {
			}

			@Override
			public User getObject() {
				Long userId = (Long) editContext.getPropertyValue();
				if (userId != null)
					return Gitop.getInstance(UserManager.class).load(userId); 
				else
					return null;
			}

			@Override
			public void setObject(User object) {
				if (object != null)
					editContext.setPropertyValue(object.getId());
				else
					editContext.setPropertyValue(null);
			}
    		
    	};
    	
    	UserSingleChoice chooser = new UserSingleChoice("chooser", model);
        chooser.setConvertEmptyInputStringToNull(true);
        
        add(chooser);
	}

}
