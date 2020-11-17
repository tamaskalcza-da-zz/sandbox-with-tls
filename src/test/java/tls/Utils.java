package tls;

import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

class Utils {
  static boolean ping(URL url) {
    try {
      System.out.printf("Ping: %s%n", url);
      URLConnection connection = url.openConnection();
      connection.connect();
      return true;
    } catch (Throwable ignore) {
      return false;
    }
  }

  static boolean eventually(BooleanSupplier code, Duration timeout) {
    try {
      Instant start = Instant.now();
      boolean succeeded;
      while (!(succeeded = code.getAsBoolean()) && !timedOutSince(start, timeout)) {
        TimeUnit.MILLISECONDS.sleep(200);
      }
      return succeeded;
    } catch (InterruptedException e) {
      return false;
    }
  }

  private static boolean timedOutSince(Instant start, Duration timeout) {
    return 0 < elapsedSince(start).compareTo(timeout);
  }

  private static Duration elapsedSince(Instant from) {
    return Duration.between(from, Instant.now());
  }
}
