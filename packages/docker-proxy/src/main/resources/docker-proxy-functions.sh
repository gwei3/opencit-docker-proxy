

########## FUNCTIONS LIBRARY ##########
load_docker_proxy_conf() {
  DOCKER_PROXY_PROPERTIES_FILE=${DOCKER_PROXY_PROPERTIES_FILE:-"/opt/docker-proxy/configuration/docker-proxy.properties"}
  if [ -n "$DEFAULT_ENV_LOADED" ]; then return; fi

  # docker-proxy.properties file
  if [ -f "$DOCKER_PROXY_PROPERTIES_FILE" ]; then
    echo -n "Reading properties from file [$DOCKER_PROXY_PROPERTIES_FILE]....."
    export CONF_SAML_TIMEOUT=$(read_property_from_file "saml.timeout" "$DOCKER_PROXY_PROPERTIES_FILE")
    export CONF_REFRESH_INTERVAL=$(read_property_from_file "refresh.interval" "$DOCKER_PROXY_PROPERTIES_FILE")
    export CONF_TENANT_CONF_DIR=$(read_property_from_file "tenant.conf.dir" "$DOCKER_PROXY_PROPERTIES_FILE")
    echo_success "Done"
  fi
  
  export DEFAULT_ENV_LOADED=true
  return 0
}



load_docker_proxy_defaults() {
  export DEFAULT_SAML_TIMEOUT="5400"
  export DEFAULT_REFRESH_INTERVAL="600"
  export DEFAULT_TENANT_CONF_DIR=""
  
  

  export SAML_TIMEOUT=${SAML_TIMEOUT:-${CONF_SAML_TIMEOUT:-$DEFAULT_SAML_TIMEOUT}}
  export REFRESH_INTERVAL=${REFRESH_INTERVAL:-${CONF_REFRESH_INTERVAL:-$DEFAULT_REFRESH_INTERVAL}}
  export TENANT_CONF_DIR=${TENANT_CONF_DIR:-${CONF_TENANT_CONF_DIR:-$DEFAULT_TENANT_CONF_DIR}}
}

docker_proxy_java_install() {
  DOCKER_PROXY_INSTALL_LOG_FILE=${DOCKER_PROXY_INSTALL_LOG_FILE:-"/var/log/docker-proxy/docker-proxy_install.log"}
  java_clear; java_detect 2>&1 >> $DOCKER_PROXY_INSTALL_LOG_FILE
  JAVA_PACKAGE=$(ls -1d jdk*)
  if [[ -z "$JAVA_PACKAGE" || ! -f "$JAVA_PACKAGE" ]]; then
    echo_failure "Missing Java installer: $JAVA_PACKAGE" | tee -a 
    return 1
  fi
  javafile=$JAVA_PACKAGE
  echo "Installing $javafile" >> $DOCKER_PROXY_INSTALL_LOG_FILE
  is_targz=$(echo $javafile | grep -E ".tar.gz$|.tgz$")
  is_gzip=$(echo $javafile | grep ".gz$")
  is_bin=$(echo $javafile | grep ".bin$")
  javaname=$(echo $javafile | awk -F . '{ print $1 }')
  if [ -n "$is_targz" ]; then
    tar xzvf $javafile 2>&1 >> $DOCKER_PROXY_INSTALL_LOG_FILE
  elif [ -n "$is_gzip" ]; then
    gunzip $javafile 2>&1 >/dev/null >> $DOCKER_PROXY_INSTALL_LOG_FILE
    chmod +x $javaname
    ./$javaname | grep -vE "inflating:|creating:|extracting:|linking:|^Creating" 
  elif [ -n "$is_bin" ]; then
    chmod +x $javafile
    ./$javafile | grep -vE "inflating:|creating:|extracting:|linking:|^Creating"  
  fi
  # java gets unpacked in current directory but they cleverly
  # named the folder differently than the archive, so search for it:
  java_unpacked=$(ls -d */ 2>/dev/null)
  for f in $java_unpacked
  do
    if [ -d "/usr/lib/jvm/$f" ]; then
      echo "Java already installed at /usr/lib/jvm/$f"
      export JAVA_HOME="/usr/lib/jvm/$f"
    else
      echo "Installing Java..."
      mkdir -p "/usr/lib/jvm"
      mv "$f" "/usr/lib/jvm"
      export JAVA_HOME="/usr/lib/jvm/$f"
      ##select /usr/lib/jvm/jdk1.7.0_55/bin/java
    fi
  done

  rm "/usr/bin/java" 2>/dev/null
  rm "/usr/bin/keytool" 2>/dev/null
  ln -s "$JAVA_HOME/jre/bin/java" "/usr/bin/java"
  ln -s "$JAVA_HOME/jre/bin/keytool" "/usr/bin/keytool"

  java_detect 2>&1 >> $DOCKER_PROXY_INSTALL_LOG_FILE
  if [[ -z "$JAVA_HOME" || -z "$java" ]]; then
    echo_failure "Unable to auto-install Java" | tee -a $DOCKER_PROXY_INSTALL_LOG_FILE
    echo "  Java download URL:"                >> $DOCKER_PROXY_INSTALL_LOG_FILE
    echo "  http://www.java.com/en/download/"  >> $DOCKER_PROXY_INSTALL_LOG_FILE
  fi
}