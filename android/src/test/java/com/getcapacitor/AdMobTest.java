package com.getcapacitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.AdOptionsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class AdMobTest {
  @Mock
  PluginCall pluginCallMock;

  @Test
  @DisplayName(
    "adId is replaced by the Tester Banner ID when the user request a test ad"
  )
  public void ad_Id_for_testing() {
    when(pluginCallMock.getBoolean(eq("isTesting"), anyBoolean()))
      .thenReturn(true);

    final AdOptions adOptions = new AdOptionsTest.AdOptionsTesterClass(
      pluginCallMock
    );

    assertEquals(AdOptionsTest.AdOptionsTesterClass.TESTING_ID, adOptions.adId);
  }
}
