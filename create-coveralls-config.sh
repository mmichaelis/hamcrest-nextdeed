#!/bin/bash

set -o nounset
set -o errexit
### Enable debugging with -o instead of +o
set +o xtrace

declare -r MY_CMD="$(readlink -f "${0}")"
declare -r MY_DIR="$(dirname "${MY_CMD}")"
declare -r MY_NAME="$(basename "${MY_CMD}")"

declare -r COVERALLS_CONFIG_FILE="${MY_DIR}/.coveralls.yml"


echo "repo_token: ${COVERALLS_TOKEN:?Required environment property COVERALLS_TOKEN not set.}" > "${COVERALLS_CONFIG_FILE}"
