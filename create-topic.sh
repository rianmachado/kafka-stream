#!/bin/bash

#####################################################################
echo 'Config Topic	 '		    									#	   
								   	 								#
#####################################################################

docker exec -it kafka-streams_kafka_1 bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic countmovie
docker exec -it kafka-streams_kafka_1 bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kstream-aggregator-countmoviestore-changelog
docker exec -it kafka-streams_kafka_1 bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kstream-aggregator-countmoviestore-repartition

echo 'Config Topic SUCESSSSSS.....'	


