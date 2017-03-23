#!/usr/bin/env python
#
# Copyright (C) 2017 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Helps compare openjdk_java_files contents against upstream file contents.

Outputs a tab-separated table comparing each openjdk_java_files entry
against OpenJDK upstreams. This can help verify updates to later upstreams
or focus attention towards files that may have been missed in a previous
update (http://b/36461944) or are otherwise surprising (http://b/36429512).

 - Identifies each file as identical to, different from or missing from
   each upstream; diffs are not produced.
 - Optionally, copies all openjdk_java_files from the default upstream
   (eg. OpenJDK8u121-b13) to a new directory, for easy directory comparison
   using e.g. kdiff3, which allows inspecting detailed diffs.
 - The ANDROID_BUILD_TOP environment variable must be set to point to the
   AOSP root directory (parent of libcore).
 - Run with -h command line argument to get usage instructions.

To check out upstreams OpenJDK 7u40, 8u60 and 8u121-b13, run:

mkdir openjdk
cd openjdk
hg clone http://hg.openjdk.java.net/jdk7u/jdk7u40/ 7u40
(cd !$ ; sh get_source.sh)
hg clone http://hg.openjdk.java.net/jdk8u/jdk8u 8u121-b13
(cd !$ ; hg update -r jdk8u121-b13 && sh get_source.sh)
hg clone http://hg.openjdk.java.net/jdk8u/jdk8u60/ 8u60
(cd !$ ; sh get_source.sh)

The newly created openjdk directory is then a suitable argument for the
--upstream_root parameter.
"""

import argparse
import filecmp
import os
import re
import shutil

def rel_paths_from_makefile(build_top):
    """Returns the list of relative paths to .java files parsed from openjdk_java_files.mk"""
    list_file = os.path.join(build_top, "libcore", "openjdk_java_files.mk")

    result = []
    with open(list_file, "r") as f:
        for line in f:
            match = re.match("\s+ojluni/src/main/java/(.+\.java)\s*\\\s*", line)
            if match:
                path = match.group(1)
                # convert / to the appropriate separator (e.g. \ on Windows), just in case
                path = os.path.normpath(path)
                result.append(path)
    return result

def ojluni_path(build_top, rel_path):
    """The full path of the file at the given rel_path in ojluni"""
    return os.path.join(build_top, "libcore", "ojluni", "src", "main", "java", rel_path)

def upstream_path(upstream_root, upstream, rel_path):
    """The full path of the file at the given rel_path in the given upstream"""
    source_dirs = [
        "jdk/src/share/classes",
        "jdk/src/solaris/classes"
    ]
    for source_dir in source_dirs:
        source_dir = os.path.normpath(source_dir)
        result = os.path.join(upstream_root, upstream, source_dir, rel_path)
        if os.path.exists(result):
            return result
    return None

def compare_to_upstreams(build_top, upstream_root, upstreams, rel_paths):
    """
    Returns a dict from rel_path to lists of length len(upstreams)
    Each list entry specifies whether the file at a particular
    rel_path is missing from, identical to, or different from
    a particular upstream.
    """
    result = {}
    for rel_path in rel_paths:
        ojluni_file = ojluni_path(build_top, rel_path)
        status = []
        for upstream in upstreams:
            upstream_file = upstream_path(upstream_root, upstream, rel_path)
            if upstream_file is None:
                upstream_status = "missing"
            elif filecmp.cmp(upstream_file, ojluni_file, shallow=False):
                upstream_status = "identical"
            else:
                upstream_status = "different"
            status.append(upstream_status)
        result[rel_path] = status
    return result

def copy_files(rel_paths, upstream_root, upstream, output_dir):
    """Copies files at the given rel_paths from upstream to output_dir"""
    for rel_path in rel_paths:
        upstream_file = upstream_path(upstream_root, upstream, rel_path)
        if upstream_file is not None:
            out_file = os.path.join(output_dir, rel_path)
            out_dir = os.path.dirname(out_file)
            if not os.path.exists(out_dir):
                os.makedirs(out_dir)
            shutil.copyfile(upstream_file, out_file)

def main():
    parser = argparse.ArgumentParser(
    description="Check openjdk_java_files contents against upstream file contents.")
    parser.add_argument("--upstream_root",
        help="Path below where upstream sources are checked out. This should be a "
            "directory with one child directory for each upstream (select the "
            "upstreams to compare against via --upstreams).",
        required=True,)
    parser.add_argument("--upstreams", 
        default="8u121-b13,8u60,7u40",
        help="Comma separated list of subdirectory names of --upstream_root that "
            "each hold one upstream.")
    parser.add_argument("--output_dir",
        help="(optional) path where default upstream sources should be copied to; "
            "this path must not yet exist and will be created. "
            "The default upstream is the one that occurs first in --upstreams.")
    parser.add_argument("--build_top",
        default=os.environ.get('ANDROID_BUILD_TOP'),
        help="Path where Android sources are checked out (defaults to $ANDROID_BUILD_TOP).")
    args = parser.parse_args()
    if args.output_dir is not None and os.path.exists(args.output_dir):
        raise Exception("Output dir already exists: " + args.output_dir)

    upstreams = [upstream.strip() for upstream in args.upstreams.split(',')]
    default_upstream = upstreams[0]
    for upstream in upstreams:
        upstream_path = os.path.join(args.upstream_root, upstream)
        if not os.path.exists(upstream_path):
            raise Exception("Upstream not found: " + upstream_path)

    rel_paths = rel_paths_from_makefile(args.build_top)
    upstream_infos = compare_to_upstreams(args.build_top, args.upstream_root, upstreams, rel_paths)

    if args.output_dir is not None:
        copy_files(rel_paths, args.upstream_root, default_upstream, args.output_dir)

    for rel_path in rel_paths:
        print(rel_path + "\t" +  "\t".join(upstream_infos[rel_path]))

if __name__ == '__main__':
    main()
