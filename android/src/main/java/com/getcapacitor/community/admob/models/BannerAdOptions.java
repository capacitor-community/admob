package com.getcapacitor.community.admob.models;

import com.getcapacitor.PluginCall;

public class BannerAdOptions extends AdOptions {

  public BannerAdOptions(PluginCall call) {
    super(call);
  }

  @Override
  public String getTestingId() {
    return "ca-app-pub-3940256099942544/6300978111";
  }
}
