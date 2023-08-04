#!/bin/bash

mvn -f /home/dev/Documents/pixels/server/pom.xml clean:clean && \
mvn -f /home/dev/Documents/pixels/server/pom.xml compiler:compile && \
mvn -f /home/dev/Documents/pixels/server/pom.xml war:exploded && \
sudo rm -rf /var/lib/tomcat10/webapps/ROOT && \
sudo cp -r /home/dev/Documents/pixels/server/target/pixels-1.0/ /var/lib/tomcat10/webapps/ROOT
