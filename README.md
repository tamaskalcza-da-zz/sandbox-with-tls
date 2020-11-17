# Sandbox with TLS

This repository intents to demonstrate how to use the [`DamlLedgerClient`](https://docs.daml.com/app-dev/bindings-java/javadocs/com/daml/ledger/rxjava/DamlLedgerClient.html) to connect to a Sandbox with TLS.

Use the following command to build the project and run the test which demonstrates the connection:
```shell
gradle test
```

The test will run the [`generate-tls.files.sh`](generate-tls-files.sh) which will create the necessary certificates.
