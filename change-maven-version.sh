#!/bin/bash

# define action usage commands
usage() { echo "Usage: $0 [-v \"version\"]" >&2; exit 1; }

# set option arguments to variables and echo usage on failures
version=
while getopts ":v:" o; do
  case "${o}" in
    v)
      version="${OPTARG}"
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      usage
      ;;
    *)
      usage
      ;;
  esac
done

if [ -z "$version" ]; then
  echo "Version not specified" >&2
  exit 2
fi

changeVersionCommand="mvn versions:set -DnewVersion=${version}"
changeParentVersionCommand="mvn versions:update-parent -DallowSnapshots=true -DparentVersion=${version}"
mvnInstallCommand="mvn clean install"

(cd maven/docker-proxy-maven-root && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven/docker-proxy-maven-root\" folder" >&2; exit 3; fi
(cd maven/docker-proxy-maven-root && $mvnInstallCommand)
if [ $? -ne 0 ]; then echo "Failed to maven install \"maven/docker-proxy-maven-root\"" >&2; exit 3; fi
(cd maven && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"maven\" folder" >&2; exit 3; fi
ant ready
if [ $? -ne 0 ]; then echo "Failed to run \"ant ready\" command" >&2; exit 3; fi
$changeVersionCommand
if [ $? -ne 0 ]; then echo "Failed to change maven version at top level" >&2; exit 3; fi
$changeParentVersionCommand
if [ $? -ne 0 ]; then echo "Failed to change maven parent versions" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/api/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/api/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/vrtmclient/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/vrtmclient/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/workflow/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/workflow/feature.xml\"" >&2; exit 3; fi
sed -i 's/\(<version>\).*\(<\/version>\)/\1'${version}'\2/g' features/webservice/feature.xml
if [ $? -ne 0 ]; then echo "Failed to change version in \"features/webservice/feature.xml\"" >&2; exit 3; fi





(cd packages  && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"packages\" folder" >&2; exit 3; fi
(cd packages  && $changeParentVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven parent versions in \"packages\" folder" >&2; exit 3; fi

sed -i 's/\-[0-9\.]*\(\-SNAPSHOT\|\(\-\|\.zip$\|\.bin$\|\.jar$\)\)/-'${version}'\2/g' build.targets
if [ $? -ne 0 ]; then echo "Failed to change versions in \"build.targets\" file" >&2; exit 3; fi
