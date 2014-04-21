package com.pmease.gitop.web.service;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.service.impl.DefaultAvatarManager;

@ImplementedBy(DefaultAvatarManager.class)
public interface AvatarManager {
	
	String getAvatarUrl(User user);
	
	String getAvatarUrl(String emailAddress);
}

