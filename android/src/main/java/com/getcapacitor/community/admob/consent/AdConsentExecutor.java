package com.getcapacitor.community.admob.consent;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.List;

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

    public boolean canShowPersonalizedAds() {
        Context context = contextSupplier.get();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String purposeConsent = prefs.getString("IABTCF_PurposeConsents", "");
        String vendorConsent = prefs.getString("IABTCF_VendorConsents", "");
        String vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "");
        String purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "");

        int googleId = 755;
        boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, googleId);
        boolean hasGoogleVendorLI = hasAttribute(vendorLI, googleId);

        List<Integer> indexes = new ArrayList<>();
        indexes.add(1);
        indexes.add(3);
        indexes.add(4);

        List<Integer> indexesLI = new ArrayList<>();
        indexesLI.add(2);
        indexesLI.add(7);
        indexesLI.add(9);
        indexesLI.add(10);

        return (
            hasConsentFor(indexes, purposeConsent, hasGoogleVendorConsent) &&
            hasConsentOrLegitimateInterestFor(indexesLI, purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI)
        );
    }

    public boolean isConsentOutdated() {
        Context context = contextSupplier.get();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String tcString = prefs.getString("IABTCF_TCString", "");

        if (tcString == null || tcString.isEmpty()) {
            return false;
        }

        // base64 alphabet used to store data in IABTCF string
        String base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

        // date is stored in digits 1..7 of the IABTCF string
        String dateSubstring = tcString.substring(1, 7);

        // interpret date substring as base64-encoded integer value
        long timestamp = 0L;

        for (char c : dateSubstring.toCharArray()) {
            int value = base64.indexOf(c);
            timestamp = timestamp * 64 + value;
        }

        // timestamp is given in deci-seconds, convert to milliseconds
        timestamp *= 100;

        // compare with current timestamp to get age in days
        long daysAgo = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24);

        // delete TC string if age is over a year
        if (daysAgo > 365) {
            prefs.edit().remove("IABTCF_TCString").apply();
            return true;
        }

        return false;
    }

    public boolean canShowAds() {
        Context context = contextSupplier.get();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String purposeConsent = prefs.getString("IABTCF_PurposeConsents", "");
        String vendorConsent = prefs.getString("IABTCF_VendorConsents", "");
        String vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "");
        String purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "");

        int googleId = 755;
        boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, googleId);
        boolean hasGoogleVendorLI = hasAttribute(vendorLI, googleId);

        List<Integer> indexes = new ArrayList<>();
        indexes.add(1);

        List<Integer> indexesLI = new ArrayList<>();
        indexesLI.add(2);
        indexesLI.add(7);
        indexesLI.add(9);
        indexesLI.add(10);

        return (
            hasConsentFor(indexes, purposeConsent, hasGoogleVendorConsent) &&
            hasConsentOrLegitimateInterestFor(indexesLI, purposeConsent, purposeLI, hasGoogleVendorConsent, hasGoogleVendorLI)
        );
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
                    consentInfo.put("canShowAds", canShowAds());
                    consentInfo.put("canShowPersonalizedAds", canShowPersonalizedAds());
                    consentInfo.put("isConsentOutdated", isConsentOutdated());
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
                .runOnUiThread(() ->
                    UserMessagingPlatform.loadConsentForm(
                        contextSupplier.get(),
                        consentForm ->
                            consentForm.show(activitySupplier.get(), formError -> {
                                if (formError != null) {
                                    call.reject("Error when show consent form", formError.getMessage());
                                } else {
                                    JSObject consentFormInfo = new JSObject();
                                    consentFormInfo.put("status", getConsentStatusString(consentInformation.getConsentStatus()));
                                    consentFormInfo.put("canShowAds", canShowAds());
                                    consentFormInfo.put("canShowPersonalizedAds", canShowPersonalizedAds());
                                    consentFormInfo.put("isConsentOutdated", isConsentOutdated());
                                    call.resolve(consentFormInfo);
                                }
                            }),
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

    private boolean hasAttribute(String input, int index) {
        if (input == null) return false;
        return input.length() >= index && input.charAt(index - 1) == '1';
    }

    private boolean hasConsentFor(List<Integer> indexes, String purposeConsent, boolean hasVendorConsent) {
        for (Integer p : indexes) {
            if (!hasAttribute(purposeConsent, p)) {
                return false;
            }
        }
        return hasVendorConsent;
    }

    private boolean hasConsentOrLegitimateInterestFor(
        List<Integer> indexes,
        String purposeConsent,
        String purposeLI,
        boolean hasVendorConsent,
        boolean hasVendorLI
    ) {
        for (Integer p : indexes) {
            boolean purposeAndVendorLI = hasAttribute(purposeLI, p) && hasVendorLI;
            boolean purposeConsentAndVendorConsent = hasAttribute(purposeConsent, p) && hasVendorConsent;
            boolean isOk = purposeAndVendorLI || purposeConsentAndVendorConsent;
            if (!isOk) {
                return false;
            }
        }
        return true;
    }
}
