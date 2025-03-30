package rian.demo.pojo;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@RegisterForReflection
@Getter
@Setter
public class MoviePlayCount {
	
	private String name;
	private int count;

	public MoviePlayCount aggregate(String name) {
		this.name = name;
		this.count++;

		return this;
	}

	@Override
	public String toString() {
		return "MoviePlayCount [count=" + count + ", name=" + name + "]";
	}

}