package de.frauas.group13.graph.algorithms;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.EdgeType;
import de.frauas.group13.graph.Graph;

/**
 * An implementation of the algorithm for check whether a graph is strongly
 * connected based on depth-first traversal.
 * <p>
 * An undirected graph is strongly connected if the depth-first traversal is
 * able to visit every vertex exists in the given graph
 * <p>
 * No implementation for directed graph is given
 *
 * @param <V> data type of vertices
 * @param <E> data type of edges
 * @author Tung Vo Le
 * @author Hieu Truong Minh
 * @see de.frauas.group13.graph.Graph
 */
public class ConnectivityValidator<V, E> extends DepthFirstTraversal<V, E> {

	private static final Logger LOGGER = LogManager.getRootLogger();

	public ConnectivityValidator(Graph<V, E> G) {
		super(G);
	}

	/**
	 * Check if the given graph is strongly connected.
	 *
	 * @return True if the graph is strongly connected
	 */
	public boolean validate() {
		if (G.countVertices() == 0) {
			return false;
		}

		if (G.getEdgeType() == EdgeType.UNDIRECTED) {
			// since G is an Graph with vertices of type V
			// casting here is correct
			@SuppressWarnings("unchecked")
			var startNode = (V) G.getVertices().toArray()[0];

			Set<V> visited;
			try {
				visited = traverse(startNode);
			} catch (IllegalArgumentException e) {
				LOGGER.catching(e);
				return false;
			}

			if (visited.size() == G.countVertices()) {
				return true;
			}
		}

		return false;
	}

}
