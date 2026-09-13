#!/usr/bin/env bash

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

PROGRAMPATH="${BASH_SOURCE[0]}"
if [ ! -e "$PROGRAMPATH" ]; then
    PROGRAMPATH="$(
        cd -P "$(dirname "$PROGRAMPATH")" >/dev/null 2>&1
        pwd -P
    )/$(basename "$PROGRAMPATH")
fi

BASEDIR="$(cd -P "$(dirname "$PROGRAMPATH")/.." >/dev/null 2>&1 && pwd -P)"

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-"$BASEDIR/.gradle"}"
export GRADLE_WRAPPER_VERBOSE=false
exec "$BASEDIR/gradlew" "$@"
