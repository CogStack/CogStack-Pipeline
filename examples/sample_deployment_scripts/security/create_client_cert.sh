#!/bin/bash

################################################################
# 
# This script creates client keys and certificates that can 
#  be used by client's applications
#

set -e

if [ -z "$1" ]; then
	echo "Usage: $0 <cert_name>"
	exit 1
fi

CA_ROOT_CERT="root-ca.pem"
CA_ROOT_KEY="root-ca.key"

if [ ! -e $CA_ROOT_CERT ]; then
	echo "Root CA certificate and key does not exist: $CA_ROOT_CERT , $CA_ROOT_KEY"
	exit 1
fi

echo "Generating a key for: $1"
openssl genrsa -out "$1-pkcs12.key" 2048 

echo "Converting the key ..."
openssl pkcs8 -v1 "PBE-SHA1-3DES" -in "$1-pkcs12.key" -topk8 -out "$1.key" -nocrypt

echo "Generating the certificate ..."
openssl req -new -key "$1.key" -out "$1.csr"
# ** replace above line if need to use Subject Alternative Names and with 365 days of validity
#openssl req -new -days 365 -key "$1.key" -out "$1.csr" -reqexts SAN -extensions SAN -config <(cat /etc/ssl/openssl.cnf <(printf "[SAN]\nsubjectAltName=DNS:elasticsearch-1,DNS:cogstack-elasticsearch-1,DNS:localhost"))

echo "Signing the certificate ..."
openssl x509 -req -in "$1.csr" -CA $CA_ROOT_CERT -CAkey $CA_ROOT_KEY -CAcreateserial -out "$1.pem" -sha256
# ** replace above line if need to use Subject Alternative Names and with 365 days of validity
#openssl x509 -req -extfile <(printf "subjectAltName=DNS:elasticsearch-1,DNS:cogstack-elasticsearch-1,DNS:localhost") -days 365 -in "$1.csr" -CA $CA_ROOT_CERT -CAkey $CA_ROOT_KEY -CAcreateserial -out "$1.pem"
