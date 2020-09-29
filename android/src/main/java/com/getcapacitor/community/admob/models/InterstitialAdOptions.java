package com.getcapacitor.community.admob.models;

import com.getcapacitor.PluginCall;

public class InterstitialAdOptions extends AdOptions {

  public InterstitialAdOptions(PluginCall call) {
    super(call);
  }

  @Override
  public String getTestingId() {
    return "ca-app-pub-3940256099942544/1033173712";
  }
}
