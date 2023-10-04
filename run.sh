#!/bin/sh
for s in item-service user-service stockmovement-service order-service order-stockmovement-service
do
    echo "creating temp common-service for $s"
    [ -d "$s/temp" ] && rm -rf $s/temp
    mkdir $s/temp
    cp -r common-service/src common-service/pom.xml $s/temp   
done
docker-compose up &
echo "Servers should be up in a min. Thank you!"
