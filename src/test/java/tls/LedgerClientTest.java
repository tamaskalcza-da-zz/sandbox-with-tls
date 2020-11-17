package tls;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static tls.Utils.eventually;
import static tls.Utils.ping;

import com.daml.ledger.rxjava.DamlLedgerClient;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LedgerClientTest {
  private final Path certificates = Paths.get("examples", "tls");
  private Process sandbox;

  @BeforeEach
  void setUp() throws IOException {
    startSandboxWithTls();
  }

  private void startSandboxWithTls() throws IOException {
    Files.deleteIfExists(Paths.get("sandbox.log"));
    ProcessBuilder processBuilder =
        new ProcessBuilder()
            .command(
                "daml",
                "sandbox",
                "--pem",
                certificates.resolve("server.key").toString(),
                "--crt",
                certificates.resolve("server.crt").toString(),
                "--cacrt",
                certificates.resolve("ca.crt").toString());
    Map<String, String> environment = processBuilder.environment();
    environment.put("DAML_SDK_VERSION", "1.7.0");
    sandbox = processBuilder.start();
    waitForSandbox();
  }

  private void waitForSandbox() throws MalformedURLException {
    System.out.println("Waiting for sandbox...");
    URL sandbox = new URL("http://localhost:6865");
    if (!eventually(() -> ping(sandbox), Duration.ofSeconds(10))) {
      fail("Could not connect to Sandbox.");
    }
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    sandbox.destroy();
    sandbox.waitFor();
  }

  @Test
  void can_connect_to_sandbox_with_tls() throws SSLException {
    File trustCertCollectionFile = certificates.resolve("ca.crt").toFile();
    File keyCertChainFile = certificates.resolve("client.crt").toFile();
    File keyFile = certificates.resolve("client.key").toFile();

    SslContext sslContext =
        GrpcSslContexts.forClient()
            .trustManager(trustCertCollectionFile)
            .keyManager(keyCertChainFile, keyFile)
            .clientAuth(ClientAuth.REQUIRE)
            .build();

    DamlLedgerClient ledgerClient =
        DamlLedgerClient.newBuilder("localhost", 6865).withSslContext(sslContext).build();

    assertDoesNotThrow(ledgerClient::connect);
  }
}
