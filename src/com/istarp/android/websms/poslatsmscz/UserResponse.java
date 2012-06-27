package com.istarp.android.websms.poslatsmscz;

import com.google.gson.annotations.SerializedName;


public class UserResponse {
	
	@SerializedName("ok")
	public User user;
		
	@SerializedName("state")
	public String state;
}
