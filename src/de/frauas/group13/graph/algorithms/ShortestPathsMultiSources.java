package de.frauas.group13.graph.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.Graph;
import de.frauas.group13.graph.exceptions.PathNotFoundException;
import de.frauas.group13.graph.utils.WeightedEdge;

/**
 * A helper for aggregating the shortest paths from the states computed by
 * <code>DijkstraMultiSources</code>
 *
 * @param <V> data type of vertices
 * @param <E  data type of edges
 * @author Hieu Minh Truong
 * @author Tung Le Vo
 * @see de.frauas.group13.graph.algorithms.DijkstraMultiSources
 * @see de.frauas.group13.graph.Graph
 * @see de.frauas.group13.graph.util.WeightedEdge
 */
public class ShortestPathsMultiSources<V, E extends WeightedEdge> extends DijkstraMultiSources<V, E> {

	private static final Logger LOGGER = LogManager.getRootLogger();
	private ConcurrentMap<V, Map<V, List<V>>> paths;

	public ShortestPathsMultiSources(Graph<V, E> G) {
		super(G);
		paths = new ConcurrentHashMap<>(G.countVertices());
	}

	public ShortestPathsMultiSources(DijkstraMultiSources<V, E> pathsStates) {
		super(pathsStates);
		paths = new ConcurrentHashMap<>(G.countVertices());
	}

	/**
	 * Computes sequentially the shortest paths between every pairs of vertices in
	 * the given graph based on the Dijkstra's traversal state
	 */
	public void computePaths() {
		paths.clear();
		populatePaths(new LinkedList<>(this.states.keySet()));
	}

	/**
	 * Computes the shortest paths between every pairs of vertices in the given
	 * graph based on the Dijkstra's traversal state
	 * <p>
	 * If an <code>ExecutorService</code> and the number of partitions (N > 1) are
	 * provided, the collection of vertices, that already have Dijkstra's algorithm
	 * performed on, is split into N equally-sized partitions and the computation is
	 * performed concurrently on N partitions.
	 * <p>
	 * If there exists multiple paths with the distance equals to the shortest
	 * distance, an arbitrary path will be chosen from those paths.
	 *
	 * @param executor threads pool manager
	 * @param N        number of partitions
	 * @return collection of <code>Future</code>
	 * @throws IllegalArgumentException if <code>executor</code> is null or
	 *                                  <code>N</code> is not greater than 1
	 * @see java.util.concurrent.ExecutorService
	 * @see java.util.concurrent.Future
	 */
	public Collection<Future<?>> computePaths(ExecutorService executor, int N) throws IllegalArgumentException {
		if (executor == null) {
			throw LOGGER.throwing(new IllegalArgumentException("list of sources should not be null"));
		}

		if (N <= 1) {
			throw LOGGER.throwing(new IllegalArgumentException("number of partitions must be greater than 1"));
		}

		paths.clear();

		var tasks = new LinkedList<Future<?>>();
		var sources = new LinkedList<>(this.states.keySet());
		var nVertices = sources.size();
		var partitionSize = nVertices / N + 1;

		for (int i = 0; i < nVertices; i += partitionSize) {
			LOGGER.debug("Shortest paths partion[" + i + ", " + Math.min(i + partitionSize, nVertices) + ")");
			var sourcesPartition = sources.subList(i, Math.min(i + partitionSize, nVertices));
			tasks.add(executor.submit(() -> populatePaths(sourcesPartition)));
		}

		return tasks;
	}

	/**
	 * Return the mapping from the destination vertex to the distance of the
	 * shortest path between the source vertex and the destination vertex
	 *
	 * @param src source vertex
	 * @return all distances from source
	 * @throws PathNotFoundException    if the paths have not been computed for
	 *                                  <code>src</code>
	 * @throws IllegalArgumentException if <code>src</code> is null
	 */
	public Map<V, List<V>> getPathsFrom(V src) throws PathNotFoundException {
		if (src == null) {
			throw LOGGER.throwing(new IllegalArgumentException("source vertex should not be null"));
		}

		if (!paths.containsKey(src)) {
			throw LOGGER.throwing(new PathNotFoundException("shortest paths not computed for " + src));
		}

		return paths.get(src);
	}

	/**
	 * Return a mapping of the destination vertices a collection of vertices on the
	 * shortest path between the source vertex and the destination vertex in the
	 * order of traversal
	 *
	 * @param src source vertex
	 * @return all paths from source
	 * @throws PathNotFoundException    if the distances have not been computed for
	 *                                  <code>src</code>
	 * @throws IllegalArgumentException if <code>src</code> is null
	 */
	public Map<V, Double> getDistancesFrom(V src) throws PathNotFoundException, IllegalArgumentException {
		if (src == null) {
			throw LOGGER.throwing(new IllegalArgumentException("source vertex should not be null"));
		}

		if (!states.containsKey(src)) {
			throw LOGGER.throwing(new PathNotFoundException("shortest paths not computed for " + src));
		}

		return states.get(src).getDistances();
	}

	/**
	 * Return a collection of the vertices on the shortest path between the source
	 * vertex and the destination vertex in the order of traversal
	 *
	 * @param src source vertex
	 * @param dst destination vertex
	 * @return vertices on shortest path
	 * @throws PathNotFoundException    if paths have not been compute for
	 *                                  <code>src</code> of there is no path between
	 *                                  <code>src</code> and <code>dst</code>
	 * @throws IllegalArgumentException if <code>src</code> is null or
	 *                                  <code>dst</code> is null
	 */
	public List<V> getPath(V src, V dst) throws PathNotFoundException, IllegalArgumentException {
		if (src == null) {
			throw LOGGER.throwing(new IllegalArgumentException("source vertex should not be null"));
		}

		if (dst == null) {
			throw LOGGER.throwing(new IllegalArgumentException("destination vertex should not be null"));
		}

		if (!paths.containsKey(src)) {
			throw new PathNotFoundException("shortest paths not computed for source " + src);
		}

		if (!paths.get(src).containsKey(dst)) {
			throw new PathNotFoundException("No path found to " + dst);
		}

		return paths.get(src).get(dst);
	}

	/**
	 * Return the distance of the shortest path between the source vertex and the
	 * destination vertex
	 *
	 * @param src source vertex
	 * @param dst destination vertex
	 * @return shortest path distance
	 * @throws PathNotFoundException    if paths have not been compute for
	 *                                  <code>src</code> of there is no path between
	 *                                  <code>src</code> and <code>dst</code>
	 * @throws IllegalArgumentException if <code>src</code> is null or
	 *                                  <code>dst</code> is null
	 */
	public Double getDistance(V src, V dst) throws PathNotFoundException, IllegalArgumentException {
		if (src == null) {
			throw LOGGER.throwing(new IllegalArgumentException("source vertex should not be null"));
		}

		if (dst == null) {
			throw LOGGER.throwing(new IllegalArgumentException("destination vertex should not be null"));
		}

		if (!states.containsKey(src)) {
			throw LOGGER.throwing(new PathNotFoundException("shortest paths not computed for source " + src));
		}

		if (!states.get(src).getVisited().contains(dst)) {
			throw LOGGER.throwing(new PathNotFoundException("no path found to " + dst));
		}

		return states.get(src).getDistances().get(dst);
	}

	private void populatePaths(Collection<V> sourcesPartition) {
		for (var src : sourcesPartition) {
			var pathToDst = new HashMap<V, List<V>>();
			paths.put(src, pathToDst);

			var state = states.get(src);
			for (var dst : state.getVisited()) {
				var predecessors = state.getPredecessors();
				var path = new LinkedList<V>();
				var tmp = dst;

				while (true) {
					path.addFirst(tmp);

					var pred = predecessors.get(tmp);
					if (pred.isEmpty()) {
						break;
					}

					tmp = pred.get(pred.size() - 1);
				}

				pathToDst.put(dst, path);
			}
		}
		LOGGER.debug("Shortest paths finishes on a partition");
	}

}
