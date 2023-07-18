#!/bin/bash

mvn -f /home/dev/Documents/commander/backend/pom.xml clean:clean && \
mvn -f /home/dev/Documents/commander/backend/pom.xml compiler:compile && \
mvn -f /home/dev/Documents/commander/backend/pom.xml war:exploded && \
sudo rm -rf /var/lib/tomcat10/webapps/ROOT && \
sudo cp -r /home/dev/Documents/commander/backend/target/commander-1.0/ /var/lib/tomcat10/webapps/ROOT
