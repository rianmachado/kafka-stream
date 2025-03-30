package rian.demo.pojo;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public class Movie {

	private int id;
	private String name;
	private String director;
	private String genre;

	public Movie(int id, String name, String director, String genre) {
		this.id = id;
		this.name = name;
		this.director = director;
		this.genre = genre;
	}
	
	public Movie() {
	}

	@Override
	public String toString() {
		return "Movie [id=" + id + ", name=" + name + ", director=" + director + ", genre=" + genre + "]";
	}

}