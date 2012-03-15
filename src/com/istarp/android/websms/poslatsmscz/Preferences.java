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

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Preferences.
 * 
 * @author flx
 */
public final class Preferences extends PreferenceActivity {
	/** Preference key: enabled. */
	static final String PREFS_ENABLED = "enable_poslatsmscz";
	/** Preference's name: user's api key. */
	static final String PREFS_APIKEY = "username_poslatsmscz";
	/** Preference's name: hide api status. */
	static final String PREFS_HIDE_APISTATUS = "hide_apistatus";
		
	static final String PREFS_USER = "connector_user_poslatsmscz";
	static final String PREFS_PASSWORD = "connector_password_poslatsmscz";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.connector_poslatsmscz_prefs);
	}
}
