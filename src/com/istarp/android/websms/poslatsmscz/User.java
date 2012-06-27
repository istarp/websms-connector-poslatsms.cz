package com.istarp.android.websms.poslatsmscz;

import com.google.gson.annotations.SerializedName;

public class User {
	
	@SerializedName("info")
	public UserInfo userInfo;
	
	@SerializedName("account")
	public String accountType;
				
	
	public String toString() {		
		return ("accountType: "  + accountType + ", number:" + userInfo.phoneNumber + ", email:" + userInfo.email + ", creditCZK:" + userInfo.creditCZK + ", creditSMS:" + userInfo.creditSMS + ", creditFree:"+ userInfo.creditFree);
		
	}
	
}

class UserInfo {
	@SerializedName("account")
	public String accountType;

	@SerializedName("profile_number")
    public String phoneNumber;
	
	@SerializedName("email")
    public String email;
	
	@SerializedName("credit_czk")
    public String creditCZK;
	
	@SerializedName("credit_sms")
    public String creditSMS;
	
	@SerializedName("credit_free")
    public String creditFree;
}
