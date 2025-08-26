package perf;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;

public class BaselineTest {

  private static int    intProp(String k, int d)       { return Integer.getInteger(k, d); }
  private static double doubleProp(String k, double d) { try { return Double.parseDouble(System.getProperty(k, Double.toString(d))); } catch (Exception e) { return d; } }
  private static String strProp(String k, String d)    { return System.getProperty(k, d); }

  @Test
  void personas_and_export_jmx() throws Exception {
    // These two can also be overridden at JMeter runtime with -JBASE / -JRUN_ID
    String BASE   = strProp("BASE",   "${__P(BASE,http://app:8080)}");
    String RUN_ID = strProp("RUN_ID", "${__P(RUN_ID,baseline-2m)}");

    int    VUS           = intProp("VUS", 60);      // total users across personas
    int    DUR           = intProp("DUR", 120);     // seconds (2 minutes)
    double W_LATTE       = doubleProp("W_LATTE", 0.7);
    double W_DEAL        = doubleProp("W_DEAL", 0.3);
    int    THINK_LATTE   = intProp("THINK_LATTE_MS", 1000);
    int    THINK_DEAL    = intProp("THINK_DEAL_MS", 1500);

    int latteVUs = Math.max(1, (int)Math.round(VUS * W_LATTE));
    int dealVUs  = Math.max(1, (int)Math.round(VUS * W_DEAL));

    var latte = threadGroup("tg_latte_lover", latteVUs, DUR,
      httpSampler(BASE + "/menu?loc=sea").header("x-run-id", RUN_ID),
      constantTimer(Duration.ofMillis(THINK_LATTE)),
      httpSampler(BASE + "/orders")
        .post("{\"sku\":\"latte-grande\",\"qty\":1,\"channel\":\"mobile\"}", ContentType.APPLICATION_JSON)
        .header("x-run-id", RUN_ID),
      constantTimer(Duration.ofMillis(THINK_LATTE))
    );

    var deal = threadGroup("tg_deal_hunter", dealVUs, DUR,
      httpSampler(BASE + "/menu?loc=sea&promo=true").header("x-run-id", RUN_ID),
      constantTimer(Duration.ofMillis(THINK_DEAL)),
      httpSampler(BASE + "/orders")
        .post("{\"sku\":\"americano-tall\",\"qty\":1,\"channel\":\"web\",\"coupon\":\"WELCOME10\"}", ContentType.APPLICATION_JSON)
        .header("x-run-id", RUN_ID),
      constantTimer(Duration.ofMillis(THINK_DEAL))
    );

    // Save a VANILLA JMX (no DSL-only listeners) so plain JMeter can run it
    testPlan(latte, deal).saveAsJmx("../jmeter/personas.jmx");
  }
}
