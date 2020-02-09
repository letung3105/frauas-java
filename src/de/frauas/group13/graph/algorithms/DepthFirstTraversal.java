package de.frauas.group13.graph.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.Graph;
import de.frauas.group13.graph.exceptions.VertexNotFoundException;

/**
 * An implementation of depth-first traversal on a given graph
 *
 * @param <V> data type of vertices
 * @param <E> data type of edges
 * @author Hieu Minh Truong
 * @author Tung Le Vo
 * @see de.frauas.group13.graph.Graph
 */
public class DepthFirstTraversal<V, E> {

	protected Graph<V, E> G;

	private static final Logger LOGGER = LogManager.getRootLogger();

	public DepthFirstTraversal(Graph<V, E> G) {
		this.G = G;
	}

	/**
	 * Start depth-first traversal from the given vertex
	 *
	 * @param src the vertex to start depth-first search on
	 * @return visited vertices
	 * @throws IllegalArgumentException if <code>src</code> is null or the graph
	 *                                  does not contain <code>src</code>
	 */
	public Set<V> traverse(V src) throws IllegalArgumentException {
		if (src == null) {
			throw LOGGER.throwing(new IllegalArgumentException("source vertex should not be null"));
		}

		if (!G.hasVertex(src)) {
			throw LOGGER.throwing(new IllegalArgumentException(src + "does not exist in the graph"));
		}
		var visited = new HashSet<V>(G.countVertices());
		recursiveHelper(src, visited);
		return visited;
	}

	private void recursiveHelper(V v, Set<V> visited) {
		visited.add(v);

		Collection<V> successors;
		try {
			successors = G.getSuccessors(v);
		} catch (VertexNotFoundException | IllegalArgumentException e) {
			LOGGER.catching(e);
			return;
		}

		// visits vertices recursively
		for (var s : successors) {
			if (!visited.contains(s)) {
				recursiveHelper(s, visited);
			}
		}
	}

}
