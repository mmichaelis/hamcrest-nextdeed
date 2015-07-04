#!/usr/bin/env bash

set -o nounset
set -o errexit
### Enable debugging with -o instead of +o
set +o xtrace

declare -r MY_CMD="$(readlink -f "${0}")"
declare -r MY_DIR="$(dirname "${MY_CMD}")"
declare -r MY_NAME="$(basename "${MY_CMD}")"

declare -r GRADLE_WRAPPER="${MY_DIR}/gradlew"

"${GRADLE_WRAPPER}" release
"${GRADLE_WRAPPER}" uploadArchives publishGhPages
