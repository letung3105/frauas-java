package de.frauas.group13.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementation of a vertex from an input graphml file
 * 
 * @author Alejandro Guajardo Uribe
 */
public class Vertex {

	private static final Logger LOGGER = LogManager.getRootLogger();

	private String id;

	public Vertex(String id) {
		if (id == null) {
			throw LOGGER.throwing(new IllegalArgumentException("vertex's id should not be null"));
		}
		this.id = id;
	}

	public Vertex(Vertex other) {
		id = other.getId();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return "n" + id;
	}

	@Override
	public String toString() {
		return "Vertex(" + id + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || obj.getClass() != Vertex.class) {
			return false;
		}

		var other = (Vertex) obj;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}