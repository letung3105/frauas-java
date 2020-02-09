package de.frauas.group13.graph.utils;

/**
 * This interface indicates an edge type that is used in a graph has an
 * associated weight.
 *
 * @author Alexander Orquera Barrera
 */
public interface WeightedEdge {

	/**
	 * Get the associated weight of the edge.
	 *
	 * @return Associated weight.
	 */
	Number getWeight();

}