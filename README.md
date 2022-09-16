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

O comando abaixo cria o executável nativo para os aplicativos <b>kafka-streams-producer e kafka-streams-aggregator</b>. Execute o comando abaixo na raiz principal do projeto(kafka-stream).

```bash
mvn package -f kafka-streams-producer/pom.xml -Pnative -Dquarkus.native.container-build=true
mvn package -f kafka-streams-aggregator/pom.xml -Pnative -Dquarkus.native.container-build=true
```

## Running Broker Kafka 

Antes de executar o comando abaixo você precisa informar os diretórios para montar os <b>volumes</b> 
Exemplo: 

    volumes:
      - 'kafka_data:/Users/colaborador/kafka-docker'
      
    volumes:
      - 'zookeeper_data:/Users/colaborador/zookeeper-docker' 
 
```bash
docker-compose -f docker-compose-kafka.yaml up
```

## Criando tópicos para pipeline de streaming

```bash

docker exec -it kafka-stream-kafka-1 kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic playtimemovies
docker exec -it kafka-stream-kafka-1 kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic movies
docker exec -it kafka-stream-kafka-1 kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kstream-aggregator-countmoviestore-changelog
docker exec -it kafka-stream-kafka-1 kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kstream-aggregator-countmoviestore-repartition
```


## Criando imagens Docker e iniciando os aplicativos de exemplo

No Docker hub(rianmachado) estão disponíveis duas imagens Docker quem empacotam os executáveis producer e aggregator, mas você poderá criar suas proprias imagens. Os comandos abaixo utilizam o `docker compose ` para criar a imagem a partir do Dockerfile e também iniciar o aplicativo. 
Para facilitar a demostração do nosso exemplo, execute cada comando em consoles diferentes, assim poderá acompanhar melhor os Logs do <b>Producer e Aggregator</b>.  

```bash
docker pull rianmachado/native-producer-movie:latest
docker-compose -f docker-compose-producer.yaml up

docker pull rianmachado/native-aggregator-movie:latest
docker-compose -f docker-compose-aggregator.yaml up
```
## Monitorando

SmallRye Health permite que os aplicativos forneçam informações sobre seu estado, dessa forma possibilitando o monitoramento por ferramentas externas. Normalmente é útil em ambientes de nuvem onde os processos automatizados devem ser capazes de determinar se o aplicativo deve ser descartado ou reiniciado.

Se as aplicações producer e aggregator foram iniciadas com sucesso teremos respectivamente  

`http://localhost:8081/q/health`
 
```JSON
{
    "status": "UP",
    "checks": [
        {
            "name": "SmallRye Reactive Messaging - liveness check",
            "status": "UP",
            "data": {
                "movies": "[OK]",
                "play-time-movies": "[OK]"
            }
        },
        {
            "name": "Kafka connection health check",
            "status": "UP",
            "data": {
                "nodes": "localhost:9092"
            }
        },
        {
            "name": "SmallRye Reactive Messaging - readiness check",
            "status": "UP",
            "data": {
                "movies": "[OK]",
                "play-time-movies": "[OK]"
            }
        },
        {
            "name": "SmallRye Reactive Messaging - startup check",
            "status": "UP",
            "data": {
                "movies": "[OK]",
                "play-time-movies": "[OK]"
            }
        }
    ]
}
```
`http://localhost:8080/q/health`
 
```JSON

{
    "status": "UP",
    "checks": [
        {
            "name": "Kafka Streams state health check",
            "status": "UP",
            "data": {
                "state": "RUNNING"
            }
        },
        {
            "name": "Kafka Streams topics health check",
            "status": "UP",
            "data": {
                "available_topics": "playtimemovies,movies"
            }
        },
        {
            "name": "Kafka connection health check",
            "status": "UP",
            "data": {
                "nodes": "localhost:55002"
            }
        }
    ]
}
```
## Métricas

Ainda no contexto saúde das aplicações `producer` e `aggregator` podemos extrair métricas incríveis.
Estamos falando do Micrometer(`quarkus-micrometer-registry-prometheu`). Biblioteca que oferece um mecanismo de registro para vários tipos de métricas o que pode ser uma boa idéia para fornecer uma camada de abstração. Podendo ser adaptada para diferentes sistemas de monitoramento de back-end. 

Para coletarmos as métricas vamos utilizar o <b>Prometheu</b>

Antes de executar o comando abaixo informe seu <b>IP</b> alterando os arquivos: 
`kafka-streams-producer/monitoring/prometheus/prometheus.yml` e `kafka-streams-producer/monitoring/prometheus/prometheus.yml`

      - targets:
          - 'xxx.xxx.xxx.xxx'
 
```bash
docker-compose -f kafka-streams-producer/monitoring/docker-compose.yml up

Acesse: http://localhost:9091/targets
```
```bash
docker-compose -f kafka-streams-aggregator/monitoring/docker-compose.yml up

Acesse: http://localhost:9090/targets
```



## Scalando

Os pipelines do Kafka Streams podem ser dimensionados, ou seja, a carga pode ser distribuída entre várias instâncias de aplicativos que executam o mesmo pipeline.

Dimensione o serviço _aggregator_ para três nós:

```bash
docker-compose -f docker-compose-aggregator-no-port.yaml up -d --scale kafka-streams-aggregator-replicas=3
```

Isso ativará mais duas instâncias desse serviço.
O armazenamento de estado que materializa o estado atual do pipeline de streaming, é agora distribuído entre os três nós através do balanceador de carga do Docker Compose que distribuirá solicitações para o serviço _aggregator_ de forma round-robin. 

Para executar as consultas interativas do Kafka Streams execute:

```bash
http://localhost:8080/movie/data/1
```


## Rodando em ambiente de desenvolvimento

Para fins de desenvolvimento, pode ser útil executar os aplicativos _producer_ e _aggregator_
diretamente em sua máquina local em não via Docker. Para esse fim, é fornecido o arquivo _docker-compose-kafka-local.yaml_ que iniciará o Apache Kafka e ZooKeeper.

```bash
docker-compose -f _docker-compose-kafka-local.yaml up
```
Agora podemos executar as aplicações 

```bash
./mvnw quarkus:dev -f kafka-streams-producer/pom.xml
./mvnw quarkus:dev -f kafka-streams-aggregator/pom.xml
```

