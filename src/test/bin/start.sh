#!/bin/sh
#

mvn compile
mvn dependency:copy-dependencies
java -server -cp "target/classes:target/dependency/*" -Xms128m -Xmx3g -Dlogback.configurationFile=src/test/resources/logback-test.xml com.handwin.server.Startup performance


