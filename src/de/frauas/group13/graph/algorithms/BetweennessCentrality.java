package de.frauas.group13.graph.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.EdgeType;
import de.frauas.group13.graph.Graph;
import de.frauas.group13.graph.utils.WeightedEdge;

/**
 * An implementation of the Brandes' algorithm for finding the betweenness
 * centrality for every vertex in a given graph.
 * <p>
 * The algorithm first solves the single source shortest path problem for every
 * vertex in the given graph, while counting the number of shortest paths pass
 * through every vertex and pushing the visited vertex on each step into a stack
 * on each step of the traversal. After solving the single source shortest paths
 * problem for each vertex, the algorithm iterated through the vertices in the
 * reverse order of the traversal to calculate the betweenness centrality.
 *
 * @param <V> data type of vertices
 * @param <E> data type of edges
 * @author Tung Vo Le
 * @author Hieu Truong Minh
 * @see de.frauas.group13.graph.Graph
 * @see de.frauas.group13.graph.util.WeightedEdge
 */
public class BetweennessCentrality<V, E extends WeightedEdge> extends DijkstraMultiSources<V, E> {

	private static final Logger LOGGER = LogManager.getRootLogger();
	private ConcurrentMap<V, Double> CB;

	public BetweennessCentrality(Graph<V, E> G) {
		super(G);
		CB = new ConcurrentHashMap<>(G.countVertices());
	}

	public BetweennessCentrality(DijkstraMultiSources<V, E> pathsStates) {
		super(pathsStates);
		CB = new ConcurrentHashMap<>(G.countVertices());
	}

	/**
	 * Get the betweenness centrality measure of the given vertex.
	 *
	 * @param v queried vertex.
	 * @return betweenness centrality measure
	 * @throws IllegalArgumentException if <code>v</code> v is null or
	 *                                  <code>v</code>has no associated betweenness
	 *                                  centrality measure.
	 */
	public Double getMeasure(V v) throws IllegalArgumentException {
		if (v == null) {
			throw LOGGER.throwing(new IllegalArgumentException("vertex should not be null"));
		}

		if (!states.containsKey(v)) {
			throw LOGGER.throwing(new IllegalArgumentException("no betweenness centrality measure found for " + v));
		}

		if (G.getEdgeType() == EdgeType.UNDIRECTED) {
			return CB.get(v) / 2.0;
		}

		return CB.get(v);
	}

	/**
	 * Compute sequentially the betweenness centrality for every vertex in the given
	 * graph.
	 */
	public void computeBetweenness() {
		// re-initialize the measures mapping
		CB.clear();

		var vertices = G.getVertices();
		for (var v : vertices) {
			CB.put(v, 0.0);
		}

		populateBetweenness(vertices);
	}

	/**
	 * Compute the betweenness centrality for every vertex in the given graph.
	 * <p>
	 * The function will split the collection of vertices of the graph into N
	 * equally-sized partitions (N > 1) and compute the values concurrently when
	 * given an object that implements the ExecutorService interface.
	 *
	 * @param executor threads pool manager
	 * @param N        number of partitions
	 * @return collection of <code>Future</code> (states of concurrent workers)
	 * @throws IllegalArgumentException if <code>executor</code> is null or
	 *                                  <code>N</code> is greater than 1
	 * @see java.util.concurrent.ExecutorService
	 * @see java.util.concurrent.Future
	 */
	public Collection<Future<?>> computeBetweenness(ExecutorService executor, int N) throws IllegalArgumentException {
		if (executor == null) {
			throw LOGGER.throwing(new IllegalArgumentException("list of sources should not be null"));
		}

		if (N <= 1) {
			throw LOGGER.throwing(new IllegalArgumentException("number of partitions must be greater than 1"));
		}

		// re-initialize the measures mapping
		CB.clear();

		var vertices = new LinkedList<>(G.getVertices());
		var nVertices = vertices.size();
		for (var v : vertices) {
			CB.put(v, 0.0);
		}

		var tasks = new LinkedList<Future<?>>();
		var partitionSize = nVertices / N + 1;

		// partition the collection of vertices into smaller ones
		for (int i = 0; i < nVertices; i += partitionSize) {
			LOGGER.debug("Betweenness partion[" + i + ", " + Math.min(i + partitionSize, nVertices) + ")");
			var partition = vertices.subList(i, Math.min(i + partitionSize, nVertices));
			tasks.add(executor.submit(() -> populateBetweenness(partition)));
		}

		return tasks;

	}

	private void populateBetweenness(Collection<V> partition) {
		var vertices = G.getVertices();
		for (var s : partition) {
			var dependencies = new HashMap<V, Double>(vertices.size());
			for (var v : vertices) {
				dependencies.put(v, 0.0);
			}

			var state = states.get(s);
			var S = state.getVisitOrder();
			var predecessors = state.getPredecessors();
			var pathCounts = state.getPathCounts();

			while (!S.isEmpty()) {
				var w = S.pop();
				var wPaths = pathCounts.get(w).doubleValue();
				var wDep = dependencies.get(w);

				for (var v : predecessors.get(w)) {
					var vPaths = pathCounts.get(v).doubleValue();
					var vDep = dependencies.get(v);
					dependencies.put(v, vDep + vPaths / wPaths * (1.0 + wDep));
				}

				if (!w.equals(s)) {
					CB.compute(w, (k, v) -> v + wDep);
				}
			}
		}
		LOGGER.debug("Betweenness finishes on a partition");
	}

}