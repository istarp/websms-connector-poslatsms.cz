package com.istarp.android.websms.poslatsmscz;

import com.google.gson.annotations.SerializedName;

public class SendResponse {
	
	@SerializedName("userID")
	public String userID;
	
	@SerializedName("account")
	public String accountType;
	
	@SerializedName("phoneNumner")
	public String phoneNumner;
	
	@SerializedName("operatorID")
	public String operatorID;
	
	@SerializedName("state")
	public String sendState;
	
	@SerializedName("fromNumner")
	public String fromNumner;
		
}
