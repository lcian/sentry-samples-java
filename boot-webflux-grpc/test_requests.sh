#!/bin/bash
random_string() { LC_ALL=C tr -dc 'a-z0-9' < /dev/urandom | fold -w 8 | head -n 1; }
make_request() {
    device_id="device-$(random_string)"
    file_name="file-$(random_string).txt"
    curl -s -X POST -H "Content-Type: application/json" \
        -d "{\"deviceId\": \"$device_id\", \"fileName\": \"$file_name\"}" \
        http://localhost:8080/device/get_upload_url
}
export -f random_string make_request
parallel -j 50 --progress make_request ::: {1..1000}
