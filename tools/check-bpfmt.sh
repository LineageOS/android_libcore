#!/bin/bash

# Copyright (C) 2019 The Android Open Source Project
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

SHA=$1

FIX=
for file in $(git show --name-only --pretty=format: $SHA | grep -E "\.bp$"); do
  if [[ -n "$(bpfmt -d <(git show $SHA:$file))" ]]; then
    FIX="$FIX $file"
  fi
done

if [[ -n "$FIX" ]]; then
  # Remove leading space.
  FIX=$(echo $FIX)
  echo -e "\e[1m\e[31mSome .bp files are incorrectly formatted, run the following commands to fix them:\e[0m"
  echo -e "\e[1m\e[31m    bpfmt -w $FIX\e[0m"
  exit 1
fi
