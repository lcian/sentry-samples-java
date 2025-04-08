#!/bin/bash
random_string() { LC_ALL=C tr -dc 'a-z0-9' < /dev/urandom | fold -w 8 | head -n 1; }
make_request() {
    starsystem="$(random_string)"
    asteroid="$(random_string)"
    header1="X-Random-Header-1: $(random_string)"
    header2="X-Random-Header-2: $(random_string)"
    curl -H "$header1" -H "$header2" "localhost:8000/config?starsystem=$starsystem&asteroid=$asteroid"
}
export -f random_string make_request
parallel -j 100 --progress make_request ::: {1..1000}
