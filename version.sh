#!/usr/bin/env bash

set -eu

find '.' -name "version.sbt" |
    head -n1 |
    xargs grep '[ \t]*version :=' |
    head -n1 |
    sed 's/.*"\(.*\)-.*/\1/'

