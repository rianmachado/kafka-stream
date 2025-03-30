package rian.demo.pojo;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public class MoviePlayed {

	private int id;
	private long duration;

	public MoviePlayed(int id, long duration) {
		this.id = id;
		this.duration = duration;
	}

	public MoviePlayed() {
	}

	@Override
	public String toString() {
		return "MoviePlayed [id=" + id + ", duration=" + duration + "]";
	}

}