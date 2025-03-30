package rian.demo.stream.topology;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.jboss.logging.Logger;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import rian.demo.constants.Global;
import rian.demo.pojo.Movie;
import rian.demo.pojo.MoviePlayCount;
import rian.demo.pojo.MoviePlayed;

@ApplicationScoped
public class TopologyAggregator {

	@Inject
	Logger logger;

	@Produces
	public Topology getTopChartsJoinFilter() {
		
		KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(Global.COUNT_MOVIE_STORE);
		StreamsBuilder builder = new StreamsBuilder();
		
		ObjectMapperSerde<Movie> movieSerder = new ObjectMapperSerde<>(Movie.class);
		ObjectMapperSerde<MoviePlayed> moviePlayedSerder = new ObjectMapperSerde<>(MoviePlayed.class);
		ObjectMapperSerde<MoviePlayCount> moviePlayCountSerder = new ObjectMapperSerde<>(MoviePlayCount.class);
		
		GlobalKTable<Integer, Movie> moviesTable = builder.globalTable(Global.MOVIES_TOPIC,Consumed.with(Serdes.Integer(), movieSerder));
		KStream<String, MoviePlayed> playEvents = builder.stream(Global.PLAY_MOVIES_TOPIC,Consumed.with(Serdes.String(), moviePlayedSerder));

		playEvents
		.filter((region, event) -> event.getDuration() >= 100) 
		.map((key, value) -> KeyValue.pair(value.getId(), value))
		.join(moviesTable, (movieId, moviePlayedId) -> movieId, (moviePlayed, movie) -> movie)
		.groupByKey(Grouped.with(Serdes.Integer(), movieSerder))
		.aggregate(MoviePlayCount::new,(movieId, movie, moviePlayCounter) -> moviePlayCounter.aggregate(movie.getName()),
				   Materialized.<Integer, MoviePlayCount> as(storeSupplier)
	               	.withKeySerde(Serdes.Integer())
	               	.withValueSerde(moviePlayCountSerder))
		.toStream()
//		.to(Global.COUNT_MOVIE_TOPIC,Produced.with(Serdes.Integer(), moviePlayCountSerder));
		.print(Printed.toSysOut());
		
		return builder.build();
    }
}
