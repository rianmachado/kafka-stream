package rian.demo.producer;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import rian.demo.pojos.Movie;
import rian.demo.pojos.PlayedMovie;

@ApplicationScoped
public class MovieProducer {

	@Inject
	Logger logger;

	private Random random = new Random();
	private long count = 0;

	private List<Movie> movies = List.of(new Movie(1, "Cidade de Deus", "Peter Jackson", "Drama"),
			new Movie(2, "Dois Filhos de Francisco", "Jonathan Frakes", "Drama"),
			new Movie(3, "Tropa de Elite", "Jose Padilha", "Acao"),
			new Movie(4, "Auto da Compadecida", "Guel Arraes", "Comedia"),
			new Movie(5, "Escolinha do Barulho 2", "Garth Jennings", "Jukebox Musical Comedy"),
			new Movie(1, "Cidade de Deus", "Rian Vasconcelos", "Drama"));

	@Outgoing("movies")
	public Multi<Record<Integer, Movie>> movies() {
		return Multi.createFrom()
					.items(movies.stream().map(m -> Record.of(m.getId(), m)));
	}

	@Outgoing("play-time-movies")
	public Multi<Record<String, PlayedMovie>> generate() {
		return Multi.createFrom().ticks().every(Duration.ofMillis(100))
					.onOverflow().drop().map(tick -> {
						Movie movie = movies.get(random.nextInt(movies.size()));
						int time = random.nextInt(300);
						logger.info("FILME " + movie.getName() + " PERIODO DE EXECUCAO:  " + time + " MINUTES");
						count += 1;
						logger.info("TOTAL EXECUCOES................... " + count);
						return Record.of("eu", new PlayedMovie(movie.getId(), time));
					});
	}

}
