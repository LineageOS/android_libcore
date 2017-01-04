#!/bin/bash

# A script to generate TZ data updates.
#
# Usage: ./createTimeZoneBundle.sh <tzupdate.properties file> <output file>
# See libcore.tzdata.update2.tools.CreateTimeZoneBundle for more information.

TOOLS_DIR=src/main/libcore/tzdata/update2/tools
UPDATE_DIR=../update2/src/main/libcore/tzdata/update2
GEN_DIR=./gen

# Fail if anything below fails
set -e

rm -rf ${GEN_DIR}
mkdir -p ${GEN_DIR}

javac \
    ${TOOLS_DIR}/CreateTimeZoneBundle.java \
    ${TOOLS_DIR}/TimeZoneBundleBuilder.java \
    ${UPDATE_DIR}/BundleException.java \
    ${UPDATE_DIR}/BundleVersion.java \
    ${UPDATE_DIR}/FileUtils.java \
    ${UPDATE_DIR}/TimeZoneBundle.java \
    -d ${GEN_DIR}

java -cp ${GEN_DIR} libcore.tzdata.update2.tools.CreateTimeZoneBundle $@
