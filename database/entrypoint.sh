#!/bin/sh
set -eu

: "${H2_USER:?H2_USER is required}"
: "${H2_PASSWORD:?H2_PASSWORD is required}"

mkdir -p /data

# Every pod receives the same immutable seed. No runtime write operations exist in this laboratory.
if [ ! -f /data/authdb.mv.db ]; then
  java -cp /opt/h2/h2.jar org.h2.tools.RunScript \
    -url "jdbc:h2:file:/data/authdb" \
    -user "$H2_USER" \
    -password "$H2_PASSWORD" \
    -script /opt/h2/seed.sql
fi

exec java -cp /opt/h2/h2.jar org.h2.tools.Server \
  -tcp -tcpAllowOthers -baseDir /data
