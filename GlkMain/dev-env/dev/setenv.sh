# configures tomcat and its jvm
#
# needs to be copied to /opt/share/tomcat/bin/ with the correct privileges
# so that tomcat loads it during startup
#

CATALINA_OPTS="-Xms20g -Xmx20g -Dspring.profiles.active=dev"

# set rmi host name for jmx
CATALINA_OPTS="$CATALINA_OPTS -Djava.rmi.server.hostname=vs850.mymanaged.host"
# enable remote JMX access
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9090"
# disable any security for JMX
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"