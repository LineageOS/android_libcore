#!/bin/bash
#
# Copyright (C) 2022 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Declare a map of relative paths from the libcore directory to checkstyle
# configuration files to apply.
declare -A PATH_TO_CONFIG=(
    [dalvik]=tools/checkstyle/aosp-copyright.xml
    [dom]=tools/checkstyle/w3c-copyright.xml
    [json]=tools/checkstyle/aosp-copyright.xml
    [libart]=tools/checkstyle/aosp-copyright.xml
    [luni/src/main/java]=tools/checkstyle/not-gpl.xml
    [ojluni/src/main]=tools/checkstyle/ojluni-src-main-header.xml
    [ojluni/src/test]=tools/checkstyle/ojluni-src-test-header.xml
    [xml/src/main/java]=tools/checkstyle/not-gpl.xml
)

function warn() {
    echo "${@}" >&2
}

# Function to apply path specific checkstyle configurations to a list of specified paths.
# Usage: checkstyle_of_paths <path1> [... <pathN>]
function checkstyle_of_paths() {
    local matching_files prefix requested_path
    declare -A visited
    for prefix in "${!PATH_TO_CONFIG[@]}"; do
        matching_files=()
        for requested_path in $* ; do
            if [[ "${requested_path}" = "${prefix}" || "${requested_path}" = "${prefix}"/* ]] ; then
                matching_files+=("${requested_path}")
                visited[${requested_path}]="yes"
            fi
        done

        if [ "${matching_files[*]}" = "" ]; then
            continue
        fi

        echo "Applying ${PATH_TO_CONFIG[${prefix}]} to ${matching_files[@]}"
        ${CHECKSTYLE} --config_xml "${PATH_TO_CONFIG[${prefix}]}" --file "${matching_files[@]}" \
            || exit 1
    done

    for requested_path in "${@}" ; do
        if [ -z "${visited[${requested_path}]}" ] ; then
            warn "WARNING: No checkstyle configuration covers ${requested_path}"
        fi
    done
}

# Main function that applies checkstyle to the files provided as arguments or to the all
# the paths in libcore that have checkstyle configurations (if no arguments are provided).
# Usage: main [<path1> ... <pathN>]
function main() {
    if [ -n "$REPO_ROOT" ] ; then
        ROOT=${REPO_ROOT}
    elif [ -n "$ANDROID_BUILD_TOP" ]; then
        ROOT=${ANDROID_BUILD_TOP}
    else
        echo "This script requires REPO_ROOT or ANDROID_BUILD_TOP to be defined."
        echo "Run `lunch` and try again."
        exit 1
    fi

    CHECKSTYLE=${ROOT}/prebuilts/checkstyle/checkstyle.py
    if [ ! -x ${CHECKSTYLE} ]; then
        echo Checkstyle is not present or is not executable: ${CHECKSTYLE}
        exit 1
    fi

    cd "${ROOT}/libcore"
    if [ $# = 0 ] ; then
        checkstyle_of_paths "${!PATH_TO_CONFIG[@]}"
    else
        checkstyle_of_paths "${@}"
    fi
}

main "${@}"