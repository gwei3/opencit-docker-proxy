#!/bin/bash

# chkconfig: 2345 80 30
# description: Intel Docker Proxy

### BEGIN INIT INFO
# Provides:          docker-proxy
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Should-Start:      $portmap
# Should-Stop:       $portmap
# X-Start-Before:    nis
# X-Stop-After:      nis
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# X-Interactive:     true
# Short-Description: docker-proxy
# Description:       Main script to run docker-proxy commands
### END INIT INFO
DESC="DOCKER_PROXY"
NAME=docker-proxy

# the home docker-proxy must be defined before we load any environment or
# configuration files; it is explicitly passed through the sudo command
export DOCKER_PROXY_HOME=${DOCKER_PROXY_HOME:-/opt/docker-proxy}

# the env docker-proxy is not configurable; it is defined as DOCKER_PROXY_HOME/env and
# the administrator may use a symlink if necessary to place it anywhere else
export DOCKER_PROXY_ENV=$DOCKER_PROXY_HOME/env
docker-proxy_load_env() {
  local env_files="$@"
  local env_file_exports
  for env_file in $env_files; do
    if [ -n "$env_file" ] && [ -f "$env_file" ]; then
      . $env_file
      env_file_exports=$(cat $env_file | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
      if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
    fi
  done  
}

if [ -z "$DOCKER_PROXY_USERNAME" ]; then
  docker-proxy_load_env $DOCKER_PROXY_HOME/env/docker-proxy-username
fi

###################################################################################################

### THIS NEEDS TO BE UPDATED LATER, MUST NOT REQUIRE USER TO RUN APPLICATION AS ROOT -rksavino

## if non-root execution is specified, and we are currently root, start over; the DOCKER_PROXY_SUDO variable limits this to one attempt
## we make an exception for the uninstall command, which may require root access to delete users and certain directories
#if [ -n "$DOCKER_PROXY_USERNAME" ] && [ "$DOCKER_PROXY_USERNAME" != "root" ] && [ $(whoami) == "root" ] && [ -z "$DOCKER_PROXY_SUDO" ] && [ "$1" != "uninstall" ]; then
#  sudo -u $DOCKER_PROXY_USERNAME DOCKER_PROXY_USERNAME=$DOCKER_PROXY_USERNAME DOCKER_PROXY_HOME=$DOCKER_PROXY_HOME DOCKER_PROXY_PASSWORD=$DOCKER_PROXY_PASSWORD DOCKER_PROXY_SUDO=true docker-proxy $*
#  exit $?
#fi

###################################################################################################

# load environment variables; these may override the defaults set above and 
# also note that docker-proxy-username file is loaded twice, once before sudo and
# once here after sudo.
if [ -d $DOCKER_PROXY_ENV ]; then
  docker-proxy_load_env $(ls -1 $DOCKER_PROXY_ENV/*)
fi

# default docker-proxy layout follows the 'home' style
export DOCKER_PROXY_CONFIGURATION=${DOCKER_PROXY_CONFIGURATION:-${DOCKER_PROXY_CONF:-$DOCKER_PROXY_HOME/configuration}}
export DOCKER_PROXY_JAVA=${DOCKER_PROXY_JAVA:-$DOCKER_PROXY_HOME/java}
export DOCKER_PROXY_BIN=${DOCKER_PROXY_BIN:-$DOCKER_PROXY_HOME/bin}
export DOCKER_PROXY_REPOSITORY=${DOCKER_PROXY_REPOSITORY:-$DOCKER_PROXY_HOME/repository}
export DOCKER_PROXY_LOGS=${DOCKER_PROXY_LOGS:-$DOCKER_PROXY_HOME/logs}
export DOCKER_PROXY_PROPERTIES_FILE=${DOCKER_PROXY_PROPERTIES_FILE:-"$DOCKER_PROXY_CONFIGURATION/docker-proxy.properties"}

# needed for if certain methods are called from docker-proxy.sh like java_detect, etc.
DOCKER_PROXY_INSTALL_LOG_FILE=${DOCKER_PROXY_INSTALL_LOG_FILE:-"$DOCKER_PROXY_LOGS/docker-proxy_install.log"}
export INSTALL_LOG_FILE="$DOCKER_PROXY_INSTALL_LOG_FILE"

###################################################################################################

# load linux utility
if [ -f "$DOCKER_PROXY_HOME/bin/functions.sh" ]; then
  . $DOCKER_PROXY_HOME/bin/functions.sh
fi

###################################################################################################

# stored master password
if [ -z "$DOCKER_PROXY_PASSWORD" ] && [ -f $DOCKER_PROXY_CONFIGURATION/.docker-proxy_password ]; then
  export DOCKER_PROXY_PASSWORD=$(cat $DOCKER_PROXY_CONFIGURATION/.docker-proxy_password)
fi

# all other variables with defaults
DOCKER_PROXY_APPLICATION_LOG_FILE=${DOCKER_PROXY_APPLICATION_LOG_FILE:-$DOCKER_PROXY_LOGS/docker-proxy.log}
touch "$DOCKER_PROXY_APPLICATION_LOG_FILE"
chown "$DOCKER_PROXY_USERNAME":"$DOCKER_PROXY_USERNAME" "$DOCKER_PROXY_APPLICATION_LOG_FILE"
chmod 600 "$DOCKER_PROXY_APPLICATION_LOG_FILE"
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
JAVA_OPTS=${JAVA_OPTS:-"-Dlogback.configurationFile=$DOCKER_PROXY_CONFIGURATION/logback.xml"}

DOCKER_PROXY_SETUP_FIRST_TASKS=${DOCKER_PROXY_SETUP_FIRST_TASKS:-"update-extensions-cache-file"}
DOCKER_PROXY_SETUP_TASKS=${DOCKER_PROXY_SETUP_TASKS:-"password-vault jetty-tls-keystore shiro-ssl-port"}

# the standard PID file location /var/run is typically owned by root;
# if we are running as non-root and the standard location isn't writable 
# then we need a different place
DOCKER_PROXY_PID_FILE=${DOCKER_PROXY_PID_FILE:-/var/run/docker-proxy.pid}
DOCKER_ENGINE_PID_FILE=${DOCKER_ENGINE_PID_FILE:-/var/run/dockerengine.pid}
if [ ! -w "$DOCKER_PROXY_PID_FILE" ] && [ ! -w $(dirname "$DOCKER_PROXY_PID_FILE") ]; then
  DOCKER_PROXY_PID_FILE=$DOCKER_PROXY_REPOSITORY/docker-proxy.pid
fi
if [ ! -w "$DOCKER_ENGINE_PID_FILE" ] && [ ! -w $(dirname "$DOCKER_ENGINE_PID_FILE") ]; then
  DOCKER_ENGINE_PID_FILE=$DOCKER_PROXY_REPOSITORY/dockerengine.pid
fi

###################################################################################################

# java command
if [ -z "$JAVA_CMD" ]; then
  if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD=$JAVA_HOME/bin/java
  else
    JAVA_CMD=`which java`
  fi
fi

# generated variables
JARS=$(ls -1 $DOCKER_PROXY_JAVA/*.jar)
CLASSPATH=$(echo $JARS | tr ' ' ':')

if [ -z "$JAVA_HOME" ]; then java_detect; fi
CLASSPATH=$CLASSPATH:$(find "$JAVA_HOME" -name jfxrt*.jar | head -n 1)

# the classpath is long and if we use the java -cp option we will not be
# able to see the full command line in ps because the output is normally
# truncated at 4096 characters. so we export the classpath to the environment
export CLASSPATH
###################################################################################################

# run a docker-proxy command
docker_proxy_run() {
  local args="$*"
  $JAVA_CMD $JAVA_OPTS com.intel.mtwilson.launcher.console.Main $args
  return $?
}

# run default set of setup tasks and check if admin user needs to be created
docker_proxy_complete_setup() {
  # run all setup tasks, don't use the force option to avoid clobbering existing
  # useful configuration files
  docker_proxy_run setup $DOCKER_PROXY_SETUP_FIRST_TASKS
  docker_proxy_run setup $DOCKER_PROXY_SETUP_TASKS
  
}

# arguments are optional, if provided they are the names of the tasks to run, in order
docker_proxy_setup() {
  local args="$*"
  $JAVA_CMD $JAVA_OPTS com.intel.mtwilson.launcher.console.Main setup $args
  return $?
}

docker_proxy_start() {
    if [ -z "$DOCKER_PROXY_PASSWORD" ]; then
      echo_failure "Master password is required; export DOCKER_PROXY_PASSWORD"
      return 1
    fi

    # check if we're already running - don't start a second instance
    if docker_proxy_is_running; then
      echo "Docker Proxy is running"
      return 0
    fi
     
    # check if we need to use authbind or if we can start java directly
    prog="$JAVA_CMD"
    if [ -n "$DOCKER_PROXY_USERNAME" ] && [ "$DOCKER_PROXY_USERNAME" != "root" ] && [ $(whoami) != "root" ] && [ -n $(which authbind) ]; then
      prog="authbind $JAVA_CMD"
      JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"
    fi

    # the subshell allows the java process to have a reasonable current working
    # docker-proxy without affecting the user's working docker-proxy. 
    # the last background process pid $! must be stored from the subshell.
    (
      cd $DOCKER_PROXY_HOME
      $prog $JAVA_OPTS com.intel.mtwilson.launcher.console.Main jetty-start >>$DOCKER_PROXY_APPLICATION_LOG_FILE 2>&1 &      
      echo $! > $DOCKER_PROXY_PID_FILE    )
    if docker_proxy_is_running; then
      echo_success "Started Docker Proxy"
    else
      echo_failure "Failed to start Docker Proxy"
      exit 1
    fi	
}

# returns 0 if Docker Proxy is running, 1 if not running
# side effects: sets DOCKER_PROXY_PID if Docker Proxy is running, or to empty otherwise
docker_proxy_is_running() {
  DOCKER_PROXY_PID=
  if [ -f $DOCKER_PROXY_PID_FILE ]; then
    DOCKER_PROXY_PID=$(cat $DOCKER_PROXY_PID_FILE)
    local is_running=`ps -A -o pid | grep "^\s*${DOCKER_PROXY_PID}$"`
    if [ -z "$is_running" ]; then
      # stale PID file
      DOCKER_PROXY_PID=
    fi
  fi
  if [ -z "$DOCKER_PROXY_PID" ]; then
    # check the process list just in case the pid file is stale
    DOCKER_PROXY_PID=$(ps -A ww | grep -v grep | grep java | grep "com.intel.mtwilson.launcher.console.Main jetty-start" | grep "$DOCKER_PROXY_CONFIGURATION" | awk '{ print $1 }')
  fi
  if [ -z "$DOCKER_PROXY_PID" ]; then
    # Docker Proxy is not running
    return 1
  fi
  # Docker Proxy is running and DOCKER_PROXY_PID is set
  return 0
}

docker_engine_is_running() {
	DOCKER_ENGINE_PID=""
	if [ -f $DOCKER_ENGINE_PID_FILE ]; then
		DOCKER_ENGINE_PID=$(cat $DOCKER_ENGINE_PID_FILE)
		local is_running=`ps -A -o pid | grep "^\s*${DOCKER_ENGINE_PID}$"`
		if [ -z "$is_running" ]; then
		      # stale PID file
			DOCKER_ENGINE_PID=""
		fi
	fi
	if [ -z "$DOCKER_ENGINE_PID" ]; then
		# check the process list just in case the pid file is stale
		DOCKER_ENGINE_PID=$(ps -A ww | grep -v grep | grep "docker daemon" | awk '{ print $1}')
	fi
	if [ -z "$DOCKER_ENGINE_PID" ]; then
		# Docker Engine is not running
		return 1
	fi
	# Docker Engine is running and DOCKER_ENGINE_PID is set
	return 0
}

docker_proxy_stop() {
  if docker_proxy_is_running; then
    kill -9 $DOCKER_PROXY_PID
    if [ $? ]; then
      echo "Stopped Docker Proxy"
      # truncate pid file instead of erasing,
      # because we may not have permission to create it
      # if we're running as a non-root user
      echo > $DOCKER_PROXY_PID_FILE
    else
      echo_failure "Failed to stop docker proxy"
      exit 1
    fi
  fi
}

# removes Docker Proxy home docker-proxy (including configuration and data if they are there).
# if you need to keep those, back them up before calling uninstall,
# or if the configuration and data are outside the home docker-proxy
# they will not be removed, so you could configure DOCKER_PROXY_CONFIGURATION=/etc/docker-proxy
# and DOCKER_PROXY_REPOSITORY=/var/opt/docker-proxy and then they would not be deleted by this.
docker_proxy_uninstall() {
	service docker stop
	docker_proxy_stop
	#remove the startup script
	remove_startup_script docker-proxy

	#unmount any images if any
	m_arr=($(mount | grep "/mnt/docker-proxy/" | awk '{print $3}' ))
	for i in "${m_arr[@]}"
	do
		umount $i
	done
	rm -rf /mnt/docker-proxy

	DOCKER_PROXY_PROPERTIES_FILE=${DOCKER_PROXY_PROPERTIES_FILE:-"/opt/docker-proxy/configuration/docker-proxy.properties"}
	DOCKER_CONFIG_FILE=`cat ${DOCKER_PROXY_PROPERTIES_FILE} | grep 'docker.conf.file.path' | cut -d'=' -f2`
	
	#Reverting the docker config file to exclude docker_proxy
	. "$DOCKER_CONFIG_FILE"
	DOCKER_OPTS=`expr "$DOCKER_OPTS" : '\(.*\) .*'`
	sed -i -e "/DOCKER_OPTS/s/^#*/#/" "$DOCKER_CONFIG_FILE"
	echo DOCKER_OPTS=\"$DOCKER_OPTS\" >> "$DOCKER_CONFIG_FILE"
	
	if [ "$2" = "--purge" ]; then

		DOCKER_PLUGINS_DIR=`cat ${DOCKER_PROXY_PROPERTIES_FILE} | grep 'docker.plugins.dir.path' | cut -d'=' -f2`
		rm -f "$DOCKER_PLUGINS_DIR/docker_proxy.spec"

	fi

	rm -f /usr/local/bin/docker-proxy
	rm -rf /opt/docker-proxy
	groupdel docker-proxy > /dev/null 2>&1
	userdel docker-proxy > /dev/null 2>&1
	service docker start	
}

print_help() {
    echo "Usage: $0 start|stop|uninstall|uninstall --purge|version"
    echo "Usage: $0 setup [--force|--noexec] [task1 task2 ...]"
    echo "Available setup tasks:"
    echo $DOCKER_PROXY_SETUP_TASKS | tr ' ' '\n'
}

###################################################################################################

# here we look for specific commands first that we will handle in the
# script, and anything else we send to the java application

case "$1" in
  help)
    print_help
    ;;
  start)
	service docker stop
    docker_proxy_start
    service docker start
    ;;
  stop)
    service docker stop
    docker_proxy_stop  
    ;;
  restart)
    service docker stop
	docker_proxy_stop
    docker_proxy_start
    service docker start
	;;
  status)
    if docker_proxy_is_running; then
      echo "Docker Proxy is running"
    else
      echo "Docker Proxy is not running"
    fi
    if docker_engine_is_running; then
      echo "Docker Engine is running"
    else
      echo "Docker Engine is not running"
    fi
    ;;
  setup)
    shift
    if [ -n "$1" ]; then
      docker_proxy_setup $*
    else
      docker_proxy_complete_setup
	fi
    ;;
  uninstall)
    docker_proxy_stop
    docker_proxy_uninstall $*
    ;;
  *)
    if [ -z "$*" ]; then
      print_help
    else
      #echo "args: $*"
      $JAVA_CMD $JAVA_OPTS com.intel.mtwilson.launcher.console.Main $*
    fi
    ;;
esac


exit $?