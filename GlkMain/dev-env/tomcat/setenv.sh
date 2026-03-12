# configures tomcat and its jvm
#
# needs to be copied under debian to /usr/share/tomcat8/bin/
# then tomcat8 loads it during startup
#

CATALINA_OPTS="-server -Xms6g -Xmx6g -Dspring.profiles.active=qa"

# set rmi host name for jmx
CATALINA_OPTS="$CATALINA_OPTS -Djava.rmi.server.hostname=avior.core.binaere-bauten.de"
# enable remote JMX access
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9090"
# disable any security for JMX
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"