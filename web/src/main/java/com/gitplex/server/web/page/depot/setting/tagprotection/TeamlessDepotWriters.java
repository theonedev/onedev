package com.gitplex.server.web.page.depot.setting.tagprotection;

import com.gitplex.server.model.support.tagcreator.DepotWriters;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(order=100, name="All Users Able to Write to the Repository")
public class TeamlessDepotWriters extends DepotWriters implements TeamlessTagCreator {

	private static final long serialVersionUID = 1L;

}
