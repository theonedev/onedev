package com.pmease.commons.security.extensionpoint;

import java.util.Collection;

import com.pmease.commons.security.AbstractUser;
import com.pmease.commons.security.AbstractRealm;

public interface RealmContribution {
	Collection<? extends AbstractRealm<? extends AbstractUser>> getRealms();
}
