package com.gitplex.server.web.page.depot.setting.tagprotection;

import com.gitplex.server.model.support.tagcreator.SpecifiedUser;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(order=400, name="Specified User")
public class TeamlessSpecifiedUser extends SpecifiedUser implements TeamlessTagCreator {

	private static final long serialVersionUID = 1L;

}
