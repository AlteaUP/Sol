package com.solgroup.core.login;

import de.hybris.platform.core.model.user.UserModel;

public interface SolGroupLoginService {
	
	boolean prepareUserSession(UserModel currentUser, boolean fullMode);

}
