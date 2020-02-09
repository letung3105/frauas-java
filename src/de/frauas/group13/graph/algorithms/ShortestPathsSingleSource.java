package de.frauas.group13.graph.algorithms;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.Graph;
import de.frauas.group13.graph.exceptions.PathNotFoundException;
import de.frauas.group13.graph.utils.WeightedEdge;

/**
 * A helper for aggregating the shortest paths from the states computed by
 * <code>Dijkstra</code>
 *
 * @param <V> data type of vertices
 * @param <E> data type of edges
 * @author Hieu Minh Truong
 * @author Tung Le Vo
 * @see de.frauas.group13.graph.algorithms.Dijkstra
 * @see de.frauas.group13.graph.Graph
 * @see de.frauas.group13.graph.util.WeightedEdge
 */
public class ShortestPathsSingleSource<V, E extends WeightedEdge> extends Dijkstra<V, E> {

	private static final Logger LOGGER = LogManager.getRootLogger();

	public ShortestPathsSingleSource(Graph<V, E> G) {
		super(G);
	}

	public ShortestPathsSingleSource(Dijkstra<V, E> other) {
		super(other);
	}

	/**
	 * Return a collection of the vertices on the shortest path between the source
	 * vertex given in <code>computePaths</code> and the destination vertex in the
	 * order of traversal
	 * <p>
	 * If there exists multiple paths with the distance equals to the shortest
	 * distance, an arbitrary path will be chosen from those paths.
	 *
	 * @param dst destination vertex
	 * @return vertices on the shortest path
	 * @throws PathNotFoundException    if there exists no path to <code>dst</code>
	 * @throws IllegalArgumentException if <code>dst</code> is null
	 */
	public List<V> getPathTo(V dst) throws PathNotFoundException, IllegalArgumentException {
		if (dst == null) {
			throw LOGGER.throwing(new IllegalArgumentException("destination is not be null"));
		}

		if (!visited.contains(dst)) {
			throw LOGGER.throwing(new PathNotFoundException("no found path to " + dst));
		}

		var path = new LinkedList<V>();
		while (true) {
			path.addFirst(dst);

			var pred = predecessors.get(dst);
			if (pred.isEmpty()) {
				break;
			}

			dst = pred.get(pred.size() - 1);
		}

		return path;
	}

	/**
	 * Return the distance of the shortest path between the source vertex given in
	 * <code>computePaths</code> and the destination vertex
	 *
	 * @param dst destination vertex
	 * @return shortest path distance
	 * @throws PathNotFoundException    if there exists no path to <code>dst</code>
	 * @throws IllegalArgumentException if <code>dst</code> is null
	 */
	public Double getDistanceTo(V dst) throws PathNotFoundException, IllegalArgumentException {
		if (dst == null) {
			throw LOGGER.throwing(new IllegalArgumentException("destination is not be null"));
		}

		if (!visited.contains(dst)) {
			throw LOGGER.throwing(new PathNotFoundException("no found path to " + dst));
		}

		return distances.get(dst);
	}

}
