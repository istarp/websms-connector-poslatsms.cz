package com.istarp.android.websms.poslatsmscz;

import com.google.gson.annotations.SerializedName;

public class Response {

	@SerializedName("userID")
	public String userID;
	
	@SerializedName("account")
	public String accountType;
	
	@SerializedName("result")
	public Result result;
	
	public String toString() {
		return "userID: " + userID + ",account type: " + accountType + ",phone number: " + result.phoneNumber + ",operator id: " + 
	result.operatorID + ", text: " + result.text + ",gate: " + result.gate + ",from number: " + result.fromNumber;
	}
		
}

class Result {
			
	@SerializedName("userID")
	public String userID;
	
	@SerializedName("phoneNumber")
	public String phoneNumber;
	
	@SerializedName("operator")
	public String operatorID;
	
	@SerializedName("text")
	public String text;
	
	@SerializedName("state")
	public String sendState;
	
	@SerializedName("gate")
	public String gate;
	
	@SerializedName("fromNumber")
	public String fromNumber;

}
