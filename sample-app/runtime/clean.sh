#!/usr/bin/env bash
set -eux -o pipefail

rm -rf node_modules &
rm -rf resources-node/node_modules &
rm -rf generated-resources-browser &
rm -rf target &
rm -rf vendor/promesa/target &
wait
