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

package com.android.settings.applications.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionServiceInfo;

import com.android.internal.app.AssistUtils;
import com.android.settings.applications.defaultapps.DefaultAppInfo;
import com.android.settings.applications.defaultapps.DefaultAppPreferenceController;

import java.util.List;

public class DefaultAssistPreferenceController extends DefaultAppPreferenceController {

    private static final String KEY_DEFAULT_ASSIST = "default_assist";

    private AssistUtils mAssistUtils;

    public DefaultAssistPreferenceController(Context context) {
        super(context);
        mAssistUtils = new AssistUtils(context);
    }

    @Override
    protected Intent getSettingIntent(DefaultAppInfo info) {
        final ComponentName cn = mAssistUtils.getAssistComponentForUser(mUserId);
        if (cn == null) {
            return null;
        }
        final Intent probe = new Intent(VoiceInteractionService.SERVICE_INTERFACE)
                .setPackage(cn.getPackageName());

        final PackageManager pm = mPackageManager.getPackageManager();
        final List<ResolveInfo> services = pm.queryIntentServices(probe, PackageManager
                .GET_META_DATA);
        if (services == null || services.isEmpty()) {
            return null;
        }
        final ResolveInfo resolveInfo = services.get(0);
        final VoiceInteractionServiceInfo voiceInfo =
                new VoiceInteractionServiceInfo(pm, resolveInfo.serviceInfo);
        if (!voiceInfo.getSupportsAssist()) {
            return null;
        }
        final String activity = voiceInfo.getSettingsActivity();
        return new Intent(Intent.ACTION_MAIN)
                .setComponent(new ComponentName(cn.getPackageName(), activity));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_DEFAULT_ASSIST;
    }

    @Override
    protected DefaultAppInfo getDefaultAppInfo() {
        final ComponentName cn = mAssistUtils.getAssistComponentForUser(mUserId);
        if (cn == null) {
            return null;
        }
        return new DefaultAppInfo(mPackageManager, mUserId, cn);
    }
}