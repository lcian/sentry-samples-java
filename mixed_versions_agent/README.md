This reproduces an issue where we aren't able to detect mixed versions of SDK dependencies.

The gradle plugin adds Sentry with version 8.22.0, but we use the agent with version 8.21.1.

Compile with `./gradlew bootJar` and run with `JAVA_TOOL_OPTIONS="-javaagent:sentry-opentelemetry-agent-8.21.1.jar" java -jar build/libs/mixed_versions_agent-0.0.1-SNAPSHOT.jar`.

