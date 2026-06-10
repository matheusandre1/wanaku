#!/bin/bash
SDK_DIR="$1"
PROTO_DIR="$2"

for f in "$SDK_DIR"/*.proto; do
  filename=$(basename "$f")
  if [ ! -f "$PROTO_DIR/$filename" ]; then
    cp "$f" "$PROTO_DIR/$filename"
  fi
done
