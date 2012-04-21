/*
 * Copyright (C) 2010 Felix Bechstein
 * 
 * This file is part of WebSMS.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package com.istarp.android.websms.poslatsmscz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;


import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import de.ub0r.android.websms.connector.common.Connector;
import de.ub0r.android.websms.connector.common.ConnectorCommand;
import de.ub0r.android.websms.connector.common.ConnectorSpec;
import de.ub0r.android.websms.connector.common.Utils;
import de.ub0r.android.websms.connector.common.WebSMSException;
import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
import de.ub0r.android.websms.connector.common.Utils.HttpOptions;

/**
 * Receives commands coming as broadcast from WebSMS.
 * 
 * @author istarp
 */
public class ConnectorPoslatSMSCZ extends Connector {

	InputStream isResponse = null;

	private static Context currentContext;
	private static int maxSMSLenght = 160;

	private static User user;

	private final static String SMS_PAGE_URL = "http://j.poslatsms.cz/Send";
	private static final String SMS_USER_INFO_URL = "http://j.poslatsms.cz/GetUserInfo";
	private final static String ENCODING = "UTF-8";
	private static final String HASH_TYPE = "MD5";
	private final static String REFERER_URL = "https://www.poslatsms.cz";
	private final static String GATE_TYPE = "1";
	private final static String API_ID = "2c2a4c9f166417aee26c3eed2556df1ffd5d9fa9";
	private final static String USER_AGENT = "";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ConnectorSpec initSpec(final Context context) {
		setCurrentContext(context);
		return createConnectorSpec();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ConnectorSpec updateSpec(final Context context,
			final ConnectorSpec connectorSpec) {
		setCurrentContext(context);
		return setConnectorStatus(connectorSpec);
	}

	@Override
	protected final void doSend(final Context context, final Intent intent) {
		setCurrentContext(context);

		// user must be correct logged
		if (!isOnline())
			throw new WebSMSException(
					getStringResource(R.string.error_connection));
		else {
			setUser();
			if (getUserName().length() > 0 && getUserPassword().length() > 0
					&& user == null)
				throw new WebSMSException(
						getStringResource(R.string.error_user_not_find));
			else {
				// throw new WebSMSException(user.toString());
			}

		}

		final String phoneNumber = getPhonenumber(intent);
		final String text = getMessageText(intent);

		sendSMS(text, phoneNumber);
	}

	/**
	 * {@inheritDoc}
	 */

	private final void setCurrentContext(final Context context) {
		currentContext = context;
	}

	private final String getStringResource(final int resourceStringID) {
		return currentContext.getString(resourceStringID);
	}

	private final String getStringResource(final int resourceStringID, int i) {
		return currentContext.getString(resourceStringID, i);
	}

	private final ConnectorSpec createConnectorSpec() {
		final String name = getStringResource(R.string.connector_poslatsmscz_name);
		ConnectorSpec c = new ConnectorSpec(name);
		c.setAuthor(getStringResource(R.string.connector_poslatsmscz_author));

		setUser();

		if (user != null)
			c.setBalance(user.userInfo.creditCZK);
		else
			c.setBalance(null);

		c.setCapabilities(ConnectorSpec.CAPABILITIES_BOOTSTRAP
				| ConnectorSpec.CAPABILITIES_UPDATE
				| ConnectorSpec.CAPABILITIES_SEND
				| ConnectorSpec.CAPABILITIES_PREFS);
		c.addSubConnector("poslatsms.cz", name, SubConnectorSpec.FEATURE_NONE);

		return c;
	}

	private final ConnectorSpec setConnectorStatus(
			final ConnectorSpec connectorSpec) {
		final SharedPreferences p = getDefaultSharedPreferences();
		setUser();

		if (user != null)
			connectorSpec.setBalance(user.userInfo.creditCZK);
		else
			connectorSpec.setBalance(null);

		if (p.getBoolean(Preferences.PREFS_ENABLED, false)) {
			if (getUserName().length() > 0 && getUserPassword().length() > 0) {

				if (user != null)
					connectorSpec.setReady();
				else
					connectorSpec.setReady(); // priprava pro kontrolu
												// prihlaseni
			} else
				connectorSpec.setStatus(ConnectorSpec.STATUS_ENABLED);
		} else
			connectorSpec.setStatus(ConnectorSpec.STATUS_INACTIVE);
		return connectorSpec;
	}

	private final SharedPreferences getDefaultSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(currentContext);
	}

	private final String getUserName() {
		final SharedPreferences p = getDefaultSharedPreferences();
		String s = p.getString(Preferences.PREFS_USER, null);
		if (s == null)
			s = "";
		return s;
	}

	private final void setUser() {
		user = loadUserInfo();
	}

	private final String getUserPassword() {
		final SharedPreferences p = getDefaultSharedPreferences();
		String s = p.getString(Preferences.PREFS_PASSWORD, null);
		if (s == null)
			s = "";
		return s;

	}

	private final String getPhonenumber(final Intent intent) {
		final ConnectorCommand cc = new ConnectorCommand(intent);
		return Utils.getRecipientsNumber(cc.getRecipients()[0]);
	}

	private final String getMessageText(final Intent intent) {
		final ConnectorCommand cc = new ConnectorCommand(intent);
		return cc.getText();
	}

	private final HttpResponse performHttpRequestForStatusLineForResponse(
			final String url, final ArrayList<BasicNameValuePair> postData)
			throws IOException {
		try {
			return performHttpRequestForStatusLineUtils(url, postData);
		} catch (IOException e) {
			// HACK: This fails regulary with
			// "SSL shutdown failed: I/O error during system call, Broken pipe",
			// see
			// https://issues.apache.org/jira/browse/HTTPCLIENT-951?focusedCommentId=12901563&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#action_12901563
			// But in this case the second try usually works ...
			return performHttpRequestForStatusLineUtils(url, postData);
		}
	}

	private final HttpResponse performHttpRequestForStatusLineUtils(
			final String url, final ArrayList<BasicNameValuePair> postData)
			throws IOException {
					
		Utils.HttpOptions o =new Utils.HttpOptions(ENCODING);
		o.url = url;
		o.userAgent = USER_AGENT;
		o.referer = REFERER_URL;
		o.addFormParameter(postData);

		return Utils.getHttpClient(o);
	}

	// get pass hash
	private String getHashPassword(String pass, String type) {
		MessageDigest digest;
		try {
			digest = java.security.MessageDigest.getInstance(type);
			digest.update(pass.getBytes(), 0, pass.length());

			String hash = new BigInteger(1, digest.digest()).toString(16);
			return hash;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}

	}

	// get user from poslatsms.cz
	private final User loadUserInfo() {

		final ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("username", getUserName()));
		nameValuePairs.add(new BasicNameValuePair("password", getHashPassword(
				getUserPassword(), HASH_TYPE)));
		nameValuePairs.add(new BasicNameValuePair("textsms", getHashPassword(
				getUserPassword(), HASH_TYPE)));

		try {

			HttpResponse response = performHttpRequestForStatusLineForResponse(
					SMS_USER_INFO_URL, nameValuePairs);

			HttpEntity entity = response.getEntity();

			Gson gson = new Gson();

			isResponse = entity.getContent();

			Reader reader = new InputStreamReader(isResponse);

			UserResponse userReponse = null;

			userReponse = gson.fromJson(reader, UserResponse.class);

			if (userReponse.user == null) {
				return null;
			}

			else {
				return userReponse.user;
			}

		} catch (Exception e) {
			return null;
			// throw new
			// WebSMSException(getStringResource(R.string.error_service));
		}

	}

	// send sms
	private final void sendSMS(String text, String phoneNumber) {

		phoneNumber = Utils.international2oldformat(phoneNumber);

		if (text.length() > 160)
			throw new WebSMSException(getStringResource(R.string.error_length,
					maxSMSLenght));

		final ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
		
		setUser();
		
		if (user != null) {
			nameValuePairs
					.add(new BasicNameValuePair("username", getUserName()));
			nameValuePairs.add(new BasicNameValuePair("password",
					getHashPassword(getUserPassword(), HASH_TYPE)));
		}
		nameValuePairs.add(new BasicNameValuePair("textsms", text));
		nameValuePairs.add(new BasicNameValuePair("phoneNumber", phoneNumber));
		nameValuePairs.add(new BasicNameValuePair("gate", GATE_TYPE));
		nameValuePairs.add(new BasicNameValuePair("api", API_ID));

		try {

			/*
			 * HttpResponse response =
			 * performHttpRequestForStatusLineForResponse( SMS_PAGE_URL,
			 * nameValuePairs);
			 * 
			 * HttpEntity entity = response.getEntity();
			 * 
			 * isResponse = entity.getContent();
			 * 
			 * BufferedReader reader = new BufferedReader(new InputStreamReader(
			 * isResponse, "iso-8859-1"), 8); StringBuilder sb = new
			 * StringBuilder(); String line = null; while ((line =
			 * reader.readLine()) != null) { sb.append(line + "\n"); }
			 * isResponse.close(); throw new WebSMSException(sb.toString());
			 */

			HttpResponse response = performHttpRequestForStatusLineForResponse(
					SMS_PAGE_URL, nameValuePairs);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new WebSMSException(
						getStringResource(R.string.error_sms_sending_result));
			}

			else {

				HttpEntity entity = response.getEntity();

				Gson gson = new Gson();

				isResponse = entity.getContent();

				Reader reader = new InputStreamReader(isResponse);

				SendResponse sendResponse = null;

				sendResponse = gson.fromJson(reader, SendResponse.class);

				if (sendResponse == null)
					throw new WebSMSException(
							getStringResource(R.string.error_sms_sending_result));
				else {
					if (sendResponse.response == null)
						throw new WebSMSException(
								getStringResource(R.string.error_sms_sending_result));

					if (sendResponse.response.result.sendState == "3+")
						throw new WebSMSException(
								getStringResource(R.string.error_sms_sending_result));
				}

			}

		} catch (IOException e) {
			// return null;
			// throw new
			// WebSMSException(getStringResource(R.string.error_service));
		}

	}

	// check if device is connected to internet
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) currentContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

}
