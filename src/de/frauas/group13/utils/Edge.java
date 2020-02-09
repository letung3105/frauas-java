package de.frauas.group13.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.utils.WeightedEdge;

/**
 * Implementation of an edge from an input graphml file
 * 
 * @author Alejandro Guajardo Uribe
 */

public class Edge implements WeightedEdge {
	private static final Logger LOGGER = LogManager.getRootLogger();

	private String id;
	private Double weight;

	public Edge(String id, double weight) {
		if (id == null) {
			throw LOGGER.throwing(new IllegalArgumentException("edge's id should not be null"));
		}
		this.id = id;
		this.weight = weight;
	}

	public Edge(Edge other) {
		id = other.getId();
		weight = other.getWeight();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return "e" + id;
	}

	@Override
	public Double getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return "WeightedEdge(" + id + ", " + weight + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}

		var other = (Edge) obj;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}