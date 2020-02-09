package de.frauas.group13.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the parent class of all classes that implement the graph data
 * structure given in this package. This ensures the shared behaviors of all
 * graph implementation and provides a central class for the implementation of
 * shared functionality.
 * <p>
 * By default, the edges in the graph are assumed to be directed
 *
 * @param <V> data type of vertices
 * @param <E> data type of edges
 * @author Tung Vo Le
 * @see de.frauas.group13.graph.Graph
 */
public abstract class AbstractGraph<V, E> implements Graph<V, E> {

	public static final EdgeType DEFAULT_EDGE_TYPE = EdgeType.DIRECTED;

	protected EdgeType edgeType;

	private static final Logger LOGGER = LogManager.getRootLogger();

	public AbstractGraph() {
		edgeType = AbstractGraph.DEFAULT_EDGE_TYPE;
	}

	public AbstractGraph(EdgeType edgeType) {
		if (edgeType == null) {
			throw LOGGER.throwing(new IllegalArgumentException("edge type should not be null"));
		}

		this.edgeType = edgeType;
	}

	/**
	 * Get the default edge type of the graph.
	 *
	 * @return edge type
	 * @see app.graph.EdgeType
	 */
	@Override
	public EdgeType getEdgeType() {
		return edgeType;
	}

}
