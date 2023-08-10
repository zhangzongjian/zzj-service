if [[ $- == *x* ]]; then
    JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8005,server=y,suspend=n"
fi

java $JAVA_OPTS -jar target/zzj-service-1.0.0-SNAPSHOT.war
