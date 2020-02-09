package de.frauas.group13.graph.utils;

import java.util.Objects;

/**
 * Implementation of pairing a vertex to another vertex
 * 
 * @author Alexander Orquera Barrera
 * @param <T> data type of Vertex
 * @param <U> data type of Vertex or Edge
 * @see app.graph.util.Vertex
 * @see app.graph.util.Edge
 */
public class Pair<T, U> {
	private T first;
	private U second;

	public Pair(T first, U second) {
		if (first == null) {
			throw new IllegalArgumentException("first element should not be null");
		}

		if (second == null) {
			throw new IllegalArgumentException("second argument should not be null");
		}

		this.first = first;
		this.second = second;
	}

	/**
	 * getter for the first element
	 * 
	 * @return the first element
	 */
	public T getFirst() {
		return first;
	}

	/**
	 * getter for the second element
	 * 
	 * @return the second element
	 */
	public U getSecond() {
		return second;
	}

	@Override
	public String toString() {
		return "Pair(" + first + ", " + second + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (obj.getClass() != getClass()) {
			return false;
		}

		var other = (Pair<?, ?>) obj;
		return first.equals(other.first) && second.equals(other.second);
	}
}