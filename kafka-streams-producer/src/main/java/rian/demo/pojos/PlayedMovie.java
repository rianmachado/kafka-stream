package rian.demo.pojos;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public class PlayedMovie {

	private int id;
	private long duration;

	public PlayedMovie(int id, long duration) {
		this.id = id;
		this.duration = duration;
	}
}
