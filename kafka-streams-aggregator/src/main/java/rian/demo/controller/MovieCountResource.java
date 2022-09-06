package rian.demo.controller;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.smallrye.mutiny.Uni;
import rian.demo.model.MoviePlayCountData;
import rian.demo.pojo.MoviePlayCount;
import rian.demo.stream.queries.InteractiveQueries;

@Path("/movie")
public class MovieCountResource {

	@Inject
	InteractiveQueries interactiveQueries;

	@GET
	@Path("/data/{id}")
	public Response movieCountData(@PathParam("id") int id) {
		Optional<MoviePlayCountData> moviePlayCountData = interactiveQueries.getMoviePlayCountData(id);

		if (moviePlayCountData.isPresent()) {
			return Response.ok(moviePlayCountData.get()).build();
		} else {
			return Response.status(Status.NOT_FOUND.getStatusCode(), "No data found for movie " + id).build();
		}

	}

	@GET
	@Path("/data")
	public Uni<List<MoviePlayCount>> movieCountData() {
		List<MoviePlayCount> allMoviePlayCount = interactiveQueries.getMoviePlayCountData();
		return Uni.createFrom().item(allMoviePlayCount);
	}

}