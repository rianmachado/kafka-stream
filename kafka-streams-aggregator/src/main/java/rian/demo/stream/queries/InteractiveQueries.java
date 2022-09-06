package rian.demo.stream.queries;

import static org.apache.kafka.streams.StoreQueryParameters.fromNameAndType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.jboss.logging.Logger;

import rian.demo.constants.Global;
import rian.demo.model.MoviePlayCountData;
import rian.demo.pojo.MoviePlayCount;

@ApplicationScoped
public class InteractiveQueries {

	@Inject
	Logger logger;

	@Inject
	KafkaStreams streams;

	public Optional<MoviePlayCountData> getMoviePlayCountData(int id) {
		MoviePlayCount moviePlayCount = getMoviesPlayCount().get(id);
		if (moviePlayCount != null) {
			// Wrap the result into MoviePlayCountData
			return Optional.of(new MoviePlayCountData(moviePlayCount.name, moviePlayCount.count));
		} else {
			return Optional.empty();
		}
	}

	public List<MoviePlayCount> getMoviePlayCountData() {
		List<MoviePlayCount> allMoviePlayCount = new ArrayList<>();
		KeyValueIterator<Integer, MoviePlayCount> list = getMoviesPlayCount().all();
		if (list != null) {
			while (list.hasNext()) {
				allMoviePlayCount.add(list.next().value);
			}
		}
		return allMoviePlayCount;
	}

	private ReadOnlyKeyValueStore<Integer, MoviePlayCount> getMoviesPlayCount() {
		while (true) {
			try {
				return streams.store(fromNameAndType(Global.COUNT_MOVIE_STORE, QueryableStoreTypes.keyValueStore()));
			} catch (InvalidStateStoreException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
	}

}