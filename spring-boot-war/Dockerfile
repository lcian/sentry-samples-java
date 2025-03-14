FROM tomcat:10.1-jdk17

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Create webapps directory if it doesn't exist
RUN mkdir -p /usr/local/tomcat/webapps

# Copy the WAR file into the container
COPY build/libs/spring-boot-war-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
COPY sentry-opentelemetry-agent-8.3.0.jar /usr/local/tomcat/sentry-opentelemetry-agent-8.3.0.jar

# Set environment variables
ENV CATALINA_OPTS="-Xmx512m"
ENV JAVA_TOOL_OPTIONS="-javaagent:/usr/local/tomcat/sentry-opentelemetry-agent-8.3.0.jar"

# Expose the default Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"] 
