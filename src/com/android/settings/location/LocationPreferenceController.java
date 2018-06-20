/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.provider.Settings.Secure;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

public class LocationPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {

    private static final String KEY_LOCATION = "location";
    private Context mContext;
    private Preference mPreference;

    @VisibleForTesting
    BroadcastReceiver mLocationProvidersChangedReceiver;

    public LocationPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        mContext = context;
        mLocationProvidersChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                    updateSummary();
                }
            }
        };
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(KEY_LOCATION);
    }

    @Override
    public void onResume() {
        if (mLocationProvidersChangedReceiver != null) {
            mContext.registerReceiver(mLocationProvidersChangedReceiver, new IntentFilter(
                    LocationManager.PROVIDERS_CHANGED_ACTION));
        }
    }

    @Override
    public void onPause() {
        if (mLocationProvidersChangedReceiver != null) {
            mContext.unregisterReceiver(mLocationProvidersChangedReceiver);
        }
    }

    @Override
    public void updateState(Preference preference) {
        preference.setSummary(getLocationSummary(mContext));
    }

    @Override
    public String getPreferenceKey() {
        return KEY_LOCATION;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public void updateSummary() {
        updateState(mPreference);
    }

    public static String getLocationSummary(Context context) {
        int mode = Secure.getInt(context.getContentResolver(),
                Secure.LOCATION_MODE, Secure.LOCATION_MODE_OFF);
        if (mode != Secure.LOCATION_MODE_OFF) {
            return context.getString(R.string.location_on_summary);
        }
        return context.getString(R.string.location_off_summary);
    }
}
