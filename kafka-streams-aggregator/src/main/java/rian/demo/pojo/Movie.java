package rian.demo.pojo;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Movie {

	public int id;
	public String name;
	public String director;
	public String genre;

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