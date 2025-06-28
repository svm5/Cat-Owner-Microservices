#! /bin/bash

flyway -user=$USER -password=$PASSWORD -url=jdbc:postgresql://$HOST:$PORT/owners migrate