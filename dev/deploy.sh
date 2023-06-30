#!/bin/bash

mvn clean:clean && mvn compiler:compile && mvn war:exploded && sudo rm -rf /var/lib/tomcat10/webapps/ROOT && sudo cp -r /home/dev/Documents/commander/target/commander-1.0/ /var/lib/tomcat10/webapps/ROOT
