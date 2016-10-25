
# Docker Proxy install script
# Outline:
# 1. source the "functions.sh" file:  mtwilson-linux-util-3.0-SNAPSHOT.sh
# 2. load existing environment configuration
# 3. look for ~/docker-proxy.env and source it if it's there
# 4. prompt for installation variables if they are not provided
# 5. determine if we are installing as root or non-root user; set paths
# 6. detect java
# 7. if java not installed, and we have it bundled, install it
# 8. unzip docker-proxy archive docker-proxy-zip-0.1-SNAPSHOT.zip into /opt/docker-proxy, overwrite if any files already exist
# 9. link /usr/local/bin/docker-proxy -> /opt/docker-proxy/bin/docker-proxy, if not already there
# 10. add docker-proxy to startup services
# 11. look for DOCKER_PROXY_PASSWORD environment variable; if not present print help message and exit:
#     Docker Proxy requires a master password
#     losing the master password will result in data loss
# 12. docker-proxy setup
# 13. docker-proxy start

#####

# default settings
# note the layout setting is used only by this script
# and it is not saved or used by the app script
export DOCKER_PROXY_HOME=${DOCKER_PROXY_HOME:-/opt/docker-proxy}
DOCKER_PROXY_LAYOUT=${DOCKER_PROXY_LAYOUT:-home}
DOCKER_CONFIG_FILE=${DOCKER_CONFIG_FILE:-/etc/default/docker}
DOCKER_INSTALATION_DIR=${DOCKER_INSTALATION_DIR:-/var/lib/docker}
DOCKER_PLUGINS_DIR=${DOCKER_PLUGINS_DIR:-/etc/docker/plugins}
DOCKER_PROXY_PLUGIN_PORT=${DOCKER_PROXY_PLUGIN_PORT:-22080}
VRTM_ENV=${VRTM_ENV:-/opt/vrtm/env}

POLICY_AGENT_PATH=${POLICYAGENT_BIN}
if [ "$POLICY_AGENT_PATH" == "" ]
then
	POLICY_AGENT_PATH=`which policyagent`
	if [ "$POLICY_AGENT_PATH" == "" ]
	then
		POLICY_AGENT_PATH="/opt/policyagent/bin/policyagent.py"
	fi
fi
# the env directory is not configurable; it is defined as DOCKER_PROXY_HOME/env and
# the administrator may use a symlink if necessary to place it anywhere else
export DOCKER_PROXY_ENV=$DOCKER_PROXY_HOME/env

# load application environment variables if already defined

EXTENSIONS_CACHE_FILE=$DOCKER_PROXY_HOME/configuration/extensions.cache
echo "EXTENSIONS_CACHE_FILE:: $EXTENSIONS_CACHE_FILE"
if [ -f $EXTENSIONS_CACHE_FILE ] ; then
echo "removing existing extension cache file"
    rm -rf $EXTENSIONS_CACHE_FILE
fi

if [ -d $DOCKER_PROXY_ENV ]; then
  DOCKER_PROXY_ENV_FILES=$(ls -1 $DOCKER_PROXY_ENV/*)
  for env_file in $DOCKER_PROXY_ENV_FILES; do
    . $env_file
    env_file_exports=$(cat $env_file | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
    if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
  done
fi

# functions script (mtwilson-linux-util-3.0-SNAPSHOT.sh) is required
# we use the following functions:
# java_detect java_ready_report 
# echo_failure echo_warning
# register_startup_script
UTIL_SCRIPT_FILE=$(ls -1 mtwilson-linux-util-*.sh | head -n 1)
if [ -n "$UTIL_SCRIPT_FILE" ] && [ -f "$UTIL_SCRIPT_FILE" ]; then
  . $UTIL_SCRIPT_FILE
fi

DOCKER_PROXY_UTIL_SCRIPT_FILE=$(ls -1 docker-proxy-functions.sh | head -n 1)
if [ -n "$DOCKER_PROXY_UTIL_SCRIPT_FILE" ] && [ -f "$DOCKER_PROXY_UTIL_SCRIPT_FILE" ]; then
  . $DOCKER_PROXY_UTIL_SCRIPT_FILE
fi

# load installer environment file, if present
if [ -f ~/docker-proxy.env ]; then
  echo "Loading environment variables from $(cd ~ && pwd)/docker-proxy.env"
  . ~/docker-proxy.env
  env_file_exports=$(cat ~/docker-proxy.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
else
  echo "No environment file"
fi

# determine if we are installing as root or non-root
if [ "$(whoami)" == "root" ]; then
  # create a docker-proxy user if there isn't already one created
  DOCKER_PROXY_USERNAME=${DOCKER_PROXY_USERNAME:-docker-proxy}
  if ! getent passwd $DOCKER_PROXY_USERNAME 2>&1 >/dev/null; then
    useradd --comment "Mt Wilson Trust Director" --home $DOCKER_PROXY_HOME --system --shell /bin/false $DOCKER_PROXY_USERNAME
    usermod --lock $DOCKER_PROXY_USERNAME
    # note: to assign a shell and allow login you can run "usermod --shell /bin/bash --unlock $DOCKER_PROXY_USERNAME"
  fi
else
  # already running as docker-proxy user
  DOCKER_PROXY_USERNAME=$(whoami)
  echo_warning "Running as $DOCKER_PROXY_USERNAME; if installation fails try again as root"
  if [ ! -w "$DOCKER_PROXY_HOME" ] && [ ! -w $(dirname $DOCKER_PROXY_HOME) ]; then
    export DOCKER_PROXY_HOME=$(cd ~ && pwd)
  fi
fi

# if an existing docker-proxy is already running, stop it while we install
if which docker-proxy; then
  docker-proxy stop
fi

# define application docker-proxy layout
if [ "$DOCKER_PROXY_LAYOUT" == "linux" ]; then
  export DOCKER_PROXY_CONFIGURATION=${DOCKER_PROXY_CONFIGURATION:-/etc/docker-proxy}
  export DOCKER_PROXY_REPOSITORY=${DOCKER_PROXY_REPOSITORY:-/var/opt/docker-proxy}
  export DOCKER_PROXY_LOGS=${DOCKER_PROXY_LOGS:-/var/log/docker-proxy}
elif [ "$DOCKER_PROXY_LAYOUT" == "home" ]; then
  export DOCKER_PROXY_CONFIGURATION=${DOCKER_PROXY_CONFIGURATION:-$DOCKER_PROXY_HOME/configuration}
  export DOCKER_PROXY_REPOSITORY=${DOCKER_PROXY_REPOSITORY:-$DOCKER_PROXY_HOME/repository}
  export DOCKER_PROXY_LOGS=${DOCKER_PROXY_LOGS:-$DOCKER_PROXY_HOME/logs}
fi

export DOCKER_PROXY_BIN=$DOCKER_PROXY_HOME/bin
export DOCKER_PROXY_JAVA=$DOCKER_PROXY_HOME/java

# note that the env dir is not configurable; it is defined as "env" under home
export DOCKER_PROXY_ENV=$DOCKER_PROXY_HOME/env

docker-proxy_backup_configuration() {
  if [ -n "$DOCKER_PROXY_CONFIGURATION" ] && [ -d "$DOCKER_PROXY_CONFIGURATION" ]; then
    datestr=`date +%Y%m%d.%H%M`
	mkdir -p /var/backup/
    backupdir=/var/backup/docker-proxy.configuration.$datestr
    cp -r $DOCKER_PROXY_CONFIGURATION $backupdir
  fi
}

docker-proxy_backup_repository() {
  if [ -n "$DOCKER_PROXY_REPOSITORY" ] && [ -d "$DOCKER_PROXY_REPOSITORY" ]; then
	mkdir -p /var/backup/
    datestr=`date +%Y%m%d.%H%M`
    backupdir=/var/backup/docker-proxy.repository.$datestr
    cp -r $DOCKER_PROXY_REPOSITORY $backupdir
  fi
}

# backup current configuration and data, if they exist
docker-proxy_backup_configuration
docker-proxy_backup_repository

if [ -d $DOCKER_PROXY_CONFIGURATION ]; then
  backup_conf_dir=$DOCKER_PROXY_REPOSITORY/backup/configuration.$(date +"%Y%m%d.%H%M")
  mkdir -p $backup_conf_dir
  cp -R $DOCKER_PROXY_CONFIGURATION/* $backup_conf_dir
fi

# create application directories (chown will be repeated near end of this script, after setup)
for directory in $DOCKER_PROXY_HOME $DOCKER_PROXY_CONFIGURATION $DOCKER_PROXY_ENV $DOCKER_PROXY_REPOSITORY $DOCKER_PROXY_LOGS $DOCKER_ENGINE_LOGS; do
  mkdir -p $directory
  chown -R $DOCKER_PROXY_USERNAME:$DOCKER_PROXY_USERNAME $directory
  chmod 700 $directory
done

# store directory layout in env file
echo "# $(date)" > $DOCKER_PROXY_ENV/docker-proxy-layout
echo "export DOCKER_PROXY_HOME=$DOCKER_PROXY_HOME" >> $DOCKER_PROXY_ENV/docker-proxy-layout
echo "export DOCKER_PROXY_CONFIGURATION=$DOCKER_PROXY_CONFIGURATION" >> $DOCKER_PROXY_ENV/docker-proxy-layout
echo "export DOCKER_PROXY_REPOSITORY=$DOCKER_PROXY_REPOSITORY" >> $DOCKER_PROXY_ENV/docker-proxy-layout
echo "export DOCKER_PROXY_JAVA=$DOCKER_PROXY_JAVA" >> $DOCKER_PROXY_ENV/docker-proxy-layout
echo "export DOCKER_PROXY_BIN=$DOCKER_PROXY_BIN" >> $DOCKER_PROXY_ENV/docker-proxy-layout
echo "export DOCKER_PROXY_LOGS=$DOCKER_PROXY_LOGS" >> $DOCKER_PROXY_ENV/docker-proxy-layout

# store docker-proxy username in env file
echo "# $(date)" > $DOCKER_PROXY_ENV/docker-proxy-username
echo "export DOCKER_PROXY_USERNAME=$DOCKER_PROXY_USERNAME" >> $DOCKER_PROXY_ENV/docker-proxy-username

# store the auto-exported environment variables in env file
# to make them available after the script uses sudo to switch users;
# we delete that file later
echo "# $(date)" > $DOCKER_PROXY_ENV/docker-proxy-setup
for env_file_var_name in $env_file_exports
do
  eval env_file_var_value="\$$env_file_var_name"
  echo "export $env_file_var_name=$env_file_var_value" >> $DOCKER_PROXY_ENV/docker-proxy-setup
done

DOCKER_PROXY_PROPERTIES_FILE=${DOCKER_PROXY_PROPERTIES_FILE:-"$DOCKER_PROXY_CONFIGURATION/docker-proxy.properties"}
touch "$DOCKER_PROXY_PROPERTIES_FILE"
chown "$DOCKER_PROXY_USERNAME":"$DOCKER_PROXY_USERNAME" "$DOCKER_PROXY_PROPERTIES_FILE"
chmod 600 "$DOCKER_PROXY_PROPERTIES_FILE"


DOCKER_PROXY_INSTALL_LOG_FILE=${DOCKER_PROXY_INSTALL_LOG_FILE:-"$DOCKER_PROXY_LOGS/docker-proxy_install.log"}
export INSTALL_LOG_FILE="$DOCKER_PROXY_INSTALL_LOG_FILE"
touch "$DOCKER_PROXY_INSTALL_LOG_FILE"
chown "$DOCKER_PROXY_USERNAME":"$DOCKER_PROXY_USERNAME" "$DOCKER_PROXY_INSTALL_LOG_FILE"
chmod 600 "$DOCKER_PROXY_INSTALL_LOG_FILE"

echo "install log file is" $INSTALL_LOG_FILE




# docker-proxy requires java 1.7 or later
# detect or install java (jdk-1.7.0_51-linux-x64.tar.gz)
echo "Installing Java..."
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
JAVA_PACKAGE=`ls -1 jdk-* jre-* java-*.bin 2>/dev/null | tail -n 1`
# check if java is readable to the non-root user
if [ -z "$JAVA_HOME" ]; then
  java_detect > /dev/null
fi
if [ -n "$JAVA_HOME" ]; then
  if [ $(whoami) == "root" ]; then
    JAVA_USER_READABLE=$(sudo -u $DOCKER_PROXY_USERNAME /bin/bash -c "if [ -r $JAVA_HOME ]; then echo 'yes'; fi")
  else
    JAVA_USER_READABLE=$(/bin/bash -c "if [ -r $JAVA_HOME ]; then echo 'yes'; fi")
  fi
fi
if [ -z "$JAVA_HOME" ] || [ -z "$JAVA_USER_READABLE" ]; then
  JAVA_HOME=$DOCKER_PROXY_HOME/share/jdk1.7.0_79
fi
mkdir -p $JAVA_HOME
java_install_in_home $JAVA_PACKAGE
echo "# $(date)" > $DOCKER_PROXY_ENV/docker-proxy-java
echo "export JAVA_HOME=$JAVA_HOME" >> $DOCKER_PROXY_ENV/docker-proxy-java
echo "export JAVA_CMD=$JAVA_HOME/bin/java" >> $DOCKER_PROXY_ENV/docker-proxy-java
echo "export JAVA_REQUIRED_VERSION=$JAVA_REQUIRED_VERSION" >> $DOCKER_PROXY_ENV/docker-proxy-java

# libguestfs packages has a custom prompt about installing supermin which ignores the â€œ-yâ€? option we provide to apt-get. Following code will help to avoid that prompt 
#export DEBIAN_FRONTEND=noninteractive
#echo libguestfs-tools libguestfs/update-appliance boolean true | debconf-set-selections

# make sure unzip and authbind are installed
# added jq for JSON parsing
DOCKER_PROXY_YUM_PACKAGES="zip unzip authbind jq"
DOCKER_PROXY_APT_PACKAGES="zip  unzip authbind jq"
DOCKER_PROXY_YAST_PACKAGES="zip unzip authbind jq"
DOCKER_PROXY_ZYPPER_PACKAGES="zip  unzip authbind jq"
auto_install "Installer requirements" "DOCKER_PROXY"
if [ $? -ne 0 ]; then echo_failure "Failed to install prerequisites through package installer"; exit -1; fi

function getFlavour() {
  flavour=""
  grep -c -i ubuntu /etc/*-release > /dev/null
  if [ $? -eq 0 ] ; then
    flavour="ubuntu"
  fi
  grep -c -i "red hat" /etc/*-release > /dev/null
  if [ $? -eq 0 ] ; then
    flavour="rhel"
  fi
  # grep -c -i fedora /etc/*-release > /dev/null
  # if [ $? -eq 0 ] ; then
    # flavour="fedora"
  # fi
  # grep -c -i suse /etc/*-release > /dev/null
  # if [ $? -eq 0 ] ; then
    # flavour="suse"
  # fi
  if [ "$flavour" == "" ] ; then
    echo_failure "Unsupported linux flavor, Supported versions are ubuntu, rhel, fedora"
    exit -1
  else
    echo $flavour
  fi
}


DOCKER_PROXY_PORT_HTTP=${DOCKER_PROXY_PORT_HTTP:-${JETTY_PORT:-80}}
DOCKER_PROXY_PORT_HTTPS=${DOCKER_PROXY_PORT_HTTPS:-${JETTY_SECURE_PORT:-443}}
# setup authbind to allow non-root docker-proxy to listen on ports 80 and 443
if [ -n "$DOCKER_PROXY_USERNAME" ] && [ "$DOCKER_PROXY_USERNAME" != "root" ] && [ -d /etc/authbind/byport ] && [ "$DOCKER_PROXY_PORT_HTTP" -lt "1024" ]; then
  touch /etc/authbind/byport/$DOCKER_PROXY_PORT_HTTP
  chmod 500 /etc/authbind/byport/$DOCKER_PROXY_PORT_HTTP
  chown $DOCKER_PROXY_USERNAME /etc/authbind/byport/$DOCKER_PROXY_PORT_HTTP
fi
if [ -n "$DOCKER_PROXY_USERNAME" ] && [ "$DOCKER_PROXY_USERNAME" != "root" ] && [ -d /etc/authbind/byport ] && [ "$DOCKER_PROXY_PORT_HTTPS" -lt "1024" ]; then
  touch /etc/authbind/byport/$DOCKER_PROXY_PORT_HTTPS
  chmod 500 /etc/authbind/byport/$DOCKER_PROXY_PORT_HTTPS
  chown $DOCKER_PROXY_USERNAME /etc/authbind/byport/$DOCKER_PROXY_PORT_HTTPS
fi

# delete existing java files, to prevent a situation where the installer copies
# a newer file but the older file is also there
if [ -d $DOCKER_PROXY_HOME/java ]; then
  rm $DOCKER_PROXY_HOME/java/*.jar
fi

# extract docker-proxy  (docker-proxy-zip-0.1-SNAPSHOT.zip)
echo "Extracting application..."
DOCKER_PROXY_ZIPFILE=`ls -1 docker-proxy-*.zip 2>/dev/null | head -n 1`
unzip -oq $DOCKER_PROXY_ZIPFILE -d $DOCKER_PROXY_HOME

# copy utilities script file to application folder
cp $UTIL_SCRIPT_FILE $DOCKER_PROXY_HOME/bin/functions.sh

# set permissions
chown -R $DOCKER_PROXY_USERNAME:$DOCKER_PROXY_USERNAME $DOCKER_PROXY_HOME
chmod 755 $DOCKER_PROXY_HOME/bin/*

# link /usr/local/bin/docker-proxy -> /opt/docker-proxy/bin/docker-proxy
EXISTING_DOCKER_PROXY_COMMAND=`which docker-proxy`
if [ -z "$EXISTING_DOCKER_PROXY_COMMAND" ]; then
  ln -s $DOCKER_PROXY_HOME/bin/docker-proxy.sh /usr/local/bin/docker-proxy
fi


# register linux startup script
register_startup_script $DOCKER_PROXY_HOME/bin/docker-proxy.sh docker-proxy 15 85

disable_tcp_timestamps


## Installing Docker
## already installed needs to be checked not implemented in code
# version_gt() { 
	# test "$(echo "$@" | tr " " "\n" | sort -V | head -n 1)" != "$1"; 
# }

# MINIMUM_KERNEL_VERSION_REQUIRED="3.10"
# CURRENT_KERNEL_VERSION=`uname -r | awk -F. '{print $1 FS $2}'`
# echo "CURRENT_KERNEL_VERSION=$CURRENT_KERNEL_VERSION"
# echo "MINIMUM_KERNEL_VERSION_REQUIRED=$MINIMUM_KERNEL_VERSION_REQUIRED"

# if ! version_gt $CURRENT_KERNEL_VERSION $MINIMUM_KERNEL_VERSION_REQUIRED; then
	# echo "Sorry Your kernel doesn't support this version of docker..!!"
	# exit 1
# fi

# CODENAME=`lsb_release -c | awk --field-separator=: '{print $2}'`
# CODENAME=`echo $CODENAME | tr " " "\n"`

# LIST="precise trusty vivid wily"

# if echo "$LIST" | grep -q "$CODENAME"; then
  # echo "Valid ubuntu version";
# else
  # echo "Sorry Your Ubuntu Version is not supported";
  # exit 1;
# fi

# REPO_ADDRESS=`echo "deb https://apt.dockerproject.org/repo ubuntu-$CODENAME main"`

# apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D


# echo $REPO_ADDRESS 
# if grep -Fxq "$REPO_ADDRESS" /etc/apt/sources.list.d/docker.list
# then
	# echo "repo already set"
# else
	# echo "updating repo..."
	# echo $REPO_ADDRESS > /etc/apt/sources.list.d/docker.list
	# apt-get update
# fi

# echo "Installing docker....!!!!!"
# apt-get -y install docker-engine=1.9.1-0~$CODENAME --force-yes
## Docker

#Creating docker_proxy plugin file in docker plugins directory
mkdir -p "$DOCKER_PLUGINS_DIR"
PLUGIN_NAME=docker_proxy
DOCKER_PROXY_PLUGIN_FILE=${DOCKER_PROXY_PLUGIN_FILE:-"$DOCKER_PLUGINS_DIR/$PLUGIN_NAME.spec"}
touch "$DOCKER_PROXY_PLUGIN_FILE"
chmod 644 "$DOCKER_PROXY_PLUGIN_FILE"

#Modify the docker_proxy plugin file to include the host and port
echo tcp://localhost:$DOCKER_PROXY_PLUGIN_PORT > "$DOCKER_PROXY_PLUGIN_FILE"

#Modify the docker config file to include docker_proxy
FLAVOUR=$(getFlavour)
is_ubuntu_16_or_rhel=""
if [ "$FLAVOUR" == "ubuntu" ]; then
  is_ubuntu_16=`lsb_release -a | grep "^Release" | grep 16.04`
  if [ -n "$is_ubuntu_16" ]; then
    is_ubuntu_16_or_rhel="true"
  fi
elif [ "$FLAVOUR" == "rhel" ]; then
  is_ubuntu_16_or_rhel="true"
else
  echo_failure "Docker Proxy Unsupported, Supported versions are ubuntu, rhel"
  exit -1
fi

if [ -n "$is_ubuntu_16_or_rhel" ]
then
  DOCKER_CONFIG_DIR=/etc/systemd/system/docker.service.d
  DOCKER_CONFIG_FILE="$DOCKER_CONFIG_DIR/docker.conf"
  mkdir -p $DOCKER_CONFIG_DIR
  echo "[Service]
ExecStart=
ExecStart=/usr/bin/dockerd --authorization-plugin=$PLUGIN_NAME" > "$DOCKER_CONFIG_FILE"
  systemctl daemon-reload
else
  is_docker_opts_present=""
  . "$DOCKER_CONFIG_FILE"
  if [ -z "$DOCKER_OPTS" ]
  then
    is_docker_opts_present="false"
    DOCKER_OPTS="--storage-driver=aufs"
  fi
  if test "${DOCKER_OPTS#*$PLUGIN_NAME}" == "$DOCKER_OPTS"
  then
	DOCKER_OPTS="$DOCKER_OPTS --authorization-plugin=$PLUGIN_NAME"
  fi
  if [ -n "$is_docker_opts_present" ]
  then
    echo "DOCKER_OPTS=\"$DOCKER_OPTS\"" >> "$DOCKER_CONFIG_FILE"
  else
    sed -i "s/^DOCKER_OPTS.*/DOCKER_OPTS=\"$DOCKER_OPTS\"/g" "$DOCKER_CONFIG_FILE"
  fi
fi

#Populate docker-proxy.properties file
echo "docker.conf.file.path=${DOCKER_CONFIG_FILE}" > "$DOCKER_PROXY_PROPERTIES_FILE"
echo "docker.installation.path=${DOCKER_INSTALATION_DIR}" >> "$DOCKER_PROXY_PROPERTIES_FILE"
echo "docker.plugins.dir.path=${DOCKER_PLUGINS_DIR}" >> "$DOCKER_PROXY_PROPERTIES_FILE"
echo "policy.agent.path=${POLICY_AGENT_PATH}" >> "$DOCKER_PROXY_PROPERTIES_FILE"

#load vRTM configuration too, as socket on which vRTM listens is required and add it to docker-proxy.properties file
if [ -f "$VRTM_ENV/vrtm-layout" ]
then
	. "$VRTM_ENV/vrtm-layout"
	if [ -f  "$VRTM_CONFIGURATION/vRTM.cfg" ]
	then
		. "$VRTM_CONFIGURATION/vRTM.cfg"
		if [ -z "$rpcore_port" ]
		then
			echo_warning "vRTM layout file at location ${VRTM_CONFIGURATION} is not found"
			echo_warning "installer will not write port, on which vRTM listens for api calls, in docker-proxy properties file"
			echo_warning "please specify the port explicitly in docker-properties file"
		else
			echo "vrtm.port=${rpcore_port}" >> "$DOCKER_PROXY_PROPERTIES_FILE"
		fi
	else
		echo_warning "vRTM layout file at location ${VRTM_CONFIGURATION} is not found"
		echo_warning "installer will not write port, on which vRTM listens for api calls, in docker-proxy properties file"
		echo_warning "please specify the port explicitly in docker-properties file"
	fi
else
	echo_warning "vRTM layout file at location ${VRTM_ENV}/vrtm-layout is not found"
	echo_warning "installer will not write port, on which vRTM listens for api calls, in docker-proxy properties file"
	echo_warning "please specify the port explicitly in docker-properties file"
fi

#add the present configuration to docker-proxy-layout file too
echo "export DOCKER_CONF_FILE_PATH=${DOCKER_CONFIG_FILE}" >> $DOCKER_PROXY_ENV/docker-proxy-layout
echo "export DOCKER_INSTALLATION_PATH=${DOCKER_INSTALATION_DIR}" >> $DOCKER_PROXY_ENV/docker-proxy-layout
echo "export POLICY_AGENT_PATH=${POLICY_AGENT_PATH}" >> $DOCKER_PROXY_ENV/docker-proxy-layout


# setup the docker-proxy, unless the NOSETUP variable is defined
if [ -z "$DOCKER_PROXY_NOSETUP" ]; then
  # the master password is required
  if [ -z "$DOCKER_PROXY_PASSWORD" ] && [ ! -f $DOCKER_PROXY_CONFIGURATION/.docker-proxy_password ]; then
    docker-proxy generate-password > $DOCKER_PROXY_CONFIGURATION/.docker-proxy_password
  fi
  
  docker-proxy config mtwilson.extensions.fileIncludeFilter.contains "${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-mtwilson,docker-proxy}" >/dev/null
  docker-proxy config mtwilson.extensions.packageIncludeFilter.startsWith "${MTWILSON_EXTENSIONS_PACKAGEINCLUDEFILTER_STARTSWITH:-com.intel,org.glassfish.jersey.media.multipart}" >/dev/null


  docker-proxy config jetty.port $DOCKER_PROXY_PLUGIN_PORT >/dev/null
  docker-proxy config jetty.secure.port $DOCKER_PROXY_PORT_HTTPS >/dev/null

  docker-proxy setup
fi

# delete the temporary setup environment variables file
rm -f $DOCKER_PROXY_ENV/docker-proxy-setup

# ensure the docker-proxy owns all the content created during setup
for directory in $DOCKER_PROXY_HOME $DOCKER_PROXY_CONFIGURATION $DOCKER_PROXY_JAVA $DOCKER_PROXY_BIN $DOCKER_PROXY_ENV $DOCKER_PROXY_REPOSITORY $DOCKER_PROXY_LOGS $DOCKER_ENGINE_LOGS; do
  chown -R $DOCKER_PROXY_USERNAME:$DOCKER_PROXY_USERNAME $directory
done

# start the server, unless the NOSETUP variable is defined
if [ -z "$DOCKER_PROXY_NOSETUP" ]; then docker-proxy start; fi
echo_success "Installation complete"
