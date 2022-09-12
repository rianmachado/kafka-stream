Kafka Streams in Native Image
========================

Este projeto mostra como você pode criar aplicativos Apache Kafka Streams usando o Quarkus.

## Estrutura 

Esta aplicação de exemplo é composta pelas seguintes partes::

* Apache Kafka and ZooKeeper
* _producer_,  aplicativo Quarkus que utiliza <b>SmallRye Reactive Messaging</b> para enviar dados a dois tópicos Kafka: `movies` e `playtimemovies`. Em `Movies` encontraremos informações de Filmes e `playtimemovies` amarzenará ocorrências de quanto tempo o filme foi reproduzido.

* _aggregator_, aplicativo Quarkus que processa os tópicos, `movies` e `playtimemovies` usando a API Kafka Streams. O _aggregator_ é parte interessante neste aplicativo de exemplo. Isso porque executa um pipeline KStreams, que une os dois tópicos agrupando valores pelo `ID` do Filme. A pipeline filtra os Filmes que foram reproduzidos por mais de 100 minutos e também armazena a quantidade de vezes que um determinado Filme foi reproduzido por mais de 100 minutos. Um endpoint HTTP será utilizado psra consultas interativas do Kafka Streams.


## Pré requisitos

* JDK 11+ instalado com JAVA_HOME configurado adequadamente
* Apache Maven 3.8.1+
* Docker e Docker Compose ou Podman e Docker Compose
* Opcionalmente, a CLI do Quarkus, se você quiser usá-la
* Opcionalmente, Mandrel ou GraalVM instalado e configurado adequadamente se você deseja compilar um executável nativo (ou Docker, se usar uma compilação de contêiner nativo)


## Building Native Image

Para construir os aplicativos execute o comando abaixo na raiz principal do projeto(kafka-stream).
```bash
mvn package -f kafka-streams-producer/pom.xml -Pnative -Dquarkus.native.container-build=true
mvn package -f kafka-streams-aggregator/pom.xml -Pnative -Dquarkus.native.container-build=true
```

## Running Broker Kafka 

```bash
docker-compose -f docker-compose-kafka.yaml up
```


## Criando imagens Docker e iniciando os aplicativos de exemplo

No meu Docker hub(rianmachado) está disponível duas imagens Docker empacotando os executáveis producer e aggregator, mas você poderá criar suas proprias imagens. Os comandos abaixo utilizam o `docker compose ` para criar a imagem a partir do Dockerfile e também iniciar o aplicativo. Execute cada comando em consoles diferentes, assim poderá acompanhar melhor os Logs do <b>Producer e Aggregator</b>.  

```bash
docker-compose -f docker-compose-producer.yaml up
docker-compose -f docker-compose-aggregator.yaml up
```



## Criando tópicos para pipeline de streaming

```bash

docker exec -it kafka-stream_kafka_1 kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic playtimemovies
docker exec -it kafka-stream_kafka_1 kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic movies
docker exec -it kafka-stream_kafka_1 kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kstream-aggregator-countmoviestore-changelog
docker exec -it kafka-stream_kafka_1 kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kstream-aggregator-countmoviestore-repartition
```

## Running

O arquivo docker-compose.yaml é fornecido para executar os aplicativos _aggregator_ e _producer_ .
Inicie todos os contêineres executando:

```bash
docker-compose up
```

## Scalando

Os pipelines do Kafka Streams podem ser dimensionados, ou seja, a carga pode ser distribuída entre várias instâncias de aplicativos que executam o mesmo pipeline.

Dimensione o serviço _aggregator_ para três nós:

```bash
docker-compose up --scale aggregator=3
```

Isso ativará mais duas instâncias desse serviço.
O armazenamento de estado que materializa o estado atual do pipeline de streaming, é agora distribuído entre os três nós através do balanceador de carga do Docker Compose que distribuirá solicitações para o serviço _aggregator_ de forma round-robin. 

Para executar as consultas interativas do Kafka Streams execute:
(your actual host names will differ):

```bash
http://localhost:8080/movie/data/1
```



Now start Docker Compose as described above.

## Running locally

For development purposes it can be handy to run the _producer_ and _aggregator_ applications
directly on your local machine instead of via Docker.
For that purpose, a separate Docker Compose file is provided which just starts Apache Kafka and ZooKeeper, _docker-compose-local.yaml_
configured to be accessible from your host system.
Open this file an editor and change the value of the `KAFKA_ADVERTISED_LISTENERS` variable so it contains your host machine's name or ip address.
Then run:

```bash
docker-compose -f docker-compose-local.yaml up

mvn quarkus:dev -f producer/pom.xml

mvn quarkus:dev -Dquarkus.http.port=8081 -f aggregator/pom.xml
```

Any changes done to the _aggregator_ application will be picked up instantly,
and a reload of the stream processing application will be triggered upon the next Kafka message to be processed.
