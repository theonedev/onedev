package com.gitplex.server.web.page.depot.setting.tagprotection;

import com.gitplex.server.model.support.tagcreator.DepotAdministrators;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(order=200, name="Repository Administrators")
public class TeamlessDepotAdministrators extends DepotAdministrators implements TeamlessTagCreator {

	private static final long serialVersionUID = 1L;

}
