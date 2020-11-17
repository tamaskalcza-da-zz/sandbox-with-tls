#!/usr/bin/env bash

set -e

bit_length=4096

work_dir="examples/tls"
mkdir -p "$work_dir"

function log() {
  local message=$1
  printf "=> %s\n" "$message"
}

function generate_key_and_signed_certificate() {
  local key=$1
  local request="$work_dir/request.csr"
  local certificate=$2
  local ca_key=$3
  local ca_certificate=$4
  local cn=$5
  openssl req -newkey rsa:$bit_length -nodes -keyout "$key" -out "$request" -subj "/CN=$cn"
  openssl x509 -req -days 365 -in "$request" -CA "$ca_certificate" -CAkey "$ca_key" -set_serial 01 -issuer -out "$certificate" \
    -extfile "openssl-extensions.cnf" \
    -extensions req_ext
  rm $request
}

log "Generating CA key and self-signed certificate..."
ca_key="$work_dir/ca.key"
ca_certificate="$work_dir/ca.crt"
openssl req -newkey rsa:$bit_length -nodes -keyout "$ca_key" -x509 -days 365 -out "$ca_certificate" -subj "/CN=0.0.0.0.ca"

log "Generating server key and certificate signed by CA..."
server_key="$work_dir/server.key"
server_certificate="$work_dir/server.crt"
generate_key_and_signed_certificate $server_key $server_certificate $ca_key $ca_certificate "0.0.0.0.server"

log "Generating client key and certificate signed by CA..."
client_key="$work_dir/client.key"
client_certificate="$work_dir/client.crt"
generate_key_and_signed_certificate $client_key $client_certificate $ca_key $ca_certificate "0.0.0.0.client"

log "Generated values:"
printf -- "- Trust cert collection file: %s\n" $ca_certificate
printf -- "- Cert chain file: %s\n" $server_certificate
printf -- "- Client cert chain file: %s\n" $client_certificate
