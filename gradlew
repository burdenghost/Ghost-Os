#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="9.8.2"
DIST_NAME="gradle-${GRADLE_VERSION}-bin"
CACHE_ROOT="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/${DIST_NAME}/ghostos"
GRADLE_HOME="${CACHE_ROOT}/gradle-${GRADLE_VERSION}"
GRADLE_BIN="${GRADLE_HOME}/bin/gradle"
ZIP_PATH="${CACHE_ROOT}/${DIST_NAME}.zip"

if [ ! -x "${GRADLE_BIN}" ]; then
  mkdir -p "${CACHE_ROOT}"
  if [ ! -f "${ZIP_PATH}" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fL --retry 3 "https://downloads.gradle.org/distributions/${DIST_NAME}.zip" -o "${ZIP_PATH}"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "${ZIP_PATH}" "https://downloads.gradle.org/distributions/${DIST_NAME}.zip"
    else
      echo "Error: curl or wget is required to download Gradle ${GRADLE_VERSION}." >&2
      exit 1
    fi
  fi
  if ! command -v unzip >/dev/null 2>&1; then
    echo "Error: unzip is required to extract Gradle ${GRADLE_VERSION}." >&2
    exit 1
  fi
  unzip -q -o "${ZIP_PATH}" -d "${CACHE_ROOT}"
fi

exec "${GRADLE_BIN}" "$@"
