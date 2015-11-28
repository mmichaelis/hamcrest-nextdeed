#!/usr/bin/env bash
###
### https://docs.gradle.org/current/userguide/sonar_plugin.html
### https://docs.gradle.org/current/userguide/sonar_runner_plugin.html
### https://hub.docker.com/_/sonarqube/
###

set +o verbose
set +o xtrace
## DEBUG SETTINGS Start
#set -o verbose
#set -o xtrace
## DEBUG SETTINGS END
set -o errexit
set -o nounset

declare -r MY_CMD="$(readlink -f "${0}")"
declare -r MY_DIR="$(dirname "${MY_CMD}")"
declare -r MY_NAME="$(basename "${MY_CMD}")"

declare -i SONAR_PORT_INTERNAL=9000
declare -i SONAR_PORT_PUBLISHED=9000

declare -r LABEL="sonarqube"
declare -r DOCKER="docker"

function info() {
  echo "${MY_NAME}: [INFO ] $*" >&1
}

function warn () {
  echo "${MY_NAME}: [WARN ] $*" >&2
}

function error () {
  echo "${MY_NAME}: [ERROR] $*" >&2
}

function doHelp() {
  cat <<HELP
${MY_NAME} starts/stops a local Sonar via Docker.

Available Commands:

  * start - starts Selenium Grid and Nodes
  * stop - stops Selenium Grid and Nodes
  * restart - restarts Selenium Grid and Nodes
  * status - shows the current status of running Docker containers
  * help (default) - shows this help

HELP
}

function getDockerLatestId() {
  echo "$(docker ps -l -q)"
}

function doStartSonar() {
  local id

  id="$(docker run --detach --publish ${SONAR_PORT_PUBLISHED}:${SONAR_PORT_INTERNAL} --publish 9092:9092 --label="${LABEL}" sonarqube)"

  echo "${id}"
}

function doStart() {
  local sonarId

  sonarId="$(doStartSonar)"

  info "Started Sonar with Docker ID ${sonarId}. Available at:"
  info ""
  info "    * http://localhost:${SONAR_PORT_PUBLISHED}/"
  info ""

  info ""
  info "Call '${MY_NAME} stop' to stop."
}

function doStatus() {
  docker ps --filter "label=${LABEL}" --filter "status=running" --format='table {{.ID}}\t{{.Names}}\t{{.Ports}}'
  echo ""
}

function doStop() {
  for nodeId in $(docker ps --filter "label=${LABEL}" --all --format "{{.ID}}"); do
    echo "Stopped $(docker stop "${nodeId}")."
    echo "Removed $(docker rm -f "${nodeId}")."
  done
}

function main() {
  local args="${1:-help}"

  case "${args}" in
    start)
      doStart
      ;;
    stop)
      doStop
      ;;
    restart)
      doStop
      doStart
      ;;
    status)
      doStatus
      ;;
    help)
      doHelp
      ;;
    *)
      echo "Usage: ${MY_NAME} start|stop|restart|status|help"
      echo ""
      doHelp
      exit 1
      ;;
  esac

  info "Done."
}

main "${@}"
