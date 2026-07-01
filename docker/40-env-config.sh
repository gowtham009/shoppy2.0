#!/bin/sh
set -e

# Substitute env vars into the template and write env-config.js
envsubst < /usr/share/nginx/html/env-config.template.js > /usr/share/nginx/html/env-config.js

echo "env-config.js generated"
