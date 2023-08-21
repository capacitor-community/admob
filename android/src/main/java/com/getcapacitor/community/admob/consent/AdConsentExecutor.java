package com.getcapacitor.community.admob.consent;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.core.util.Supplier;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.community.admob.models.Executor;
import com.google.android.gms.common.util.BiConsumer;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

public class AdConsentExecutor extends Executor {

    private ConsentInformation consentInformation;

    public AdConsentExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "AdConsentExecutor");
    }

    @PluginMethod
    public void requestConsentInfo(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        try {
            ensureConsentInfo();

            ConsentRequestParameters.Builder paramsBuilder = new ConsentRequestParameters.Builder();
            ConsentDebugSettings.Builder debugSettingsBuilder = new ConsentDebugSettings.Builder(contextSupplier.get());

            if (call.getData().has("testDeviceIdentifiers")) {
                JSArray devices = call.getArray("testDeviceIdentifiers");

                for (int i = 0; i < devices.length(); i++) {
                    debugSettingsBuilder.addTestDeviceHashedId(devices.getString(i));
                }
            }

            if (call.getData().has("debugGeography")) {
                debugSettingsBuilder.setDebugGeography(call.getInt("debugGeography"));
            }

            paramsBuilder.setConsentDebugSettings(debugSettingsBuilder.build());

            if (call.getData().has("tagForUnderAgeOfConsent")) {
                paramsBuilder.setTagForUnderAgeOfConsent(call.getBoolean("tagForUnderAgeOfConsent"));
            }

            ConsentRequestParameters consentRequestParameters = paramsBuilder.build();

            if (activitySupplier.get() == null) {
                call.reject("Trying to request consent info but the Activity is null");
                return;
            }

            consentInformation.requestConsentInfoUpdate(
                activitySupplier.get(),
                consentRequestParameters,
                () -> {
                    JSObject consentInfo = new JSObject();
                    consentInfo.put("status", getConsentStatusString(consentInformation.getConsentStatus()));
                    consentInfo.put("isConsentFormAvailable", consentInformation.isConsentFormAvailable());
                    call.resolve(consentInfo);
                },
                formError -> call.reject(formError.getMessage())
            );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    @PluginMethod
    public void showConsentForm(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        try {
            if (activitySupplier.get() == null) {
                call.reject("Trying to show the consent form but the Activity is null");
                return;
            }
            ensureConsentInfo();
            activitySupplier
                .get()
                .runOnUiThread(
                    () ->
                        UserMessagingPlatform.loadConsentForm(
                            contextSupplier.get(),
                            consentForm ->
                                consentForm.show(
                                    activitySupplier.get(),
                                    formError -> {
                                        if (formError != null) {
                                            call.reject("Error when show consent form", formError.getMessage());
                                        } else {
                                            JSObject consentFormInfo = new JSObject();
                                            consentFormInfo.put("status", getConsentStatusString(consentInformation.getConsentStatus()));

                                            call.resolve(consentFormInfo);
                                        }
                                    }
                                ),
                            formError -> call.reject("Error when show consent form", formError.getMessage())
                        )
                );
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    @PluginMethod
    public void resetConsentInfo(final PluginCall call, BiConsumer<String, JSObject> notifyListenersFunction) {
        ensureConsentInfo();
        consentInformation.reset();
        call.resolve();
    }

    private String getConsentStatusString(int consentConstant) {
        switch (consentConstant) {
            case ConsentInformation.ConsentStatus.REQUIRED:
                return "REQUIRED";
            case ConsentInformation.ConsentStatus.NOT_REQUIRED:
                return "NOT_REQUIRED";
            case ConsentInformation.ConsentStatus.OBTAINED:
                return "OBTAINED";
            case ConsentInformation.ConsentStatus.UNKNOWN:
            default:
                return "UNKNOWN";
        }
    }

    private void ensureConsentInfo() {
        if (consentInformation == null) {
            consentInformation = UserMessagingPlatform.getConsentInformation(contextSupplier.get());
        }
    }
}
