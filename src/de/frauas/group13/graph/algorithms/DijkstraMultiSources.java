package de.frauas.group13.graph.algorithms;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.Graph;
import de.frauas.group13.graph.utils.WeightedEdge;

/**
 * Provides an interface for performing Dijkstra's algorithm on multiple sources
 * with threads support.
 *
 * @param <V> data type of vertices
 * @param <E> data type of edges
 * @author Hieu Minh Truong
 * @author Tung Le Vo
 * @see de.frauas.group13.graph.algorithms.Dijkstra
 * @see de.frauas.group13.graph.Graph
 * @see de.frauas.group13.graph.util.WeightedEdge
 */
public class DijkstraMultiSources<V, E extends WeightedEdge> {

	protected Graph<V, E> G;
	protected ConcurrentMap<V, Dijkstra<V, E>> states;

	private final Logger LOGGER = LogManager.getRootLogger();

	public DijkstraMultiSources(Graph<V, E> G) {
		if (G == null) {
			throw LOGGER.throwing(new IllegalArgumentException("graph should not be null"));
		}

		this.G = G;
		states = new ConcurrentHashMap<>(G.countVertices());
	}

	public DijkstraMultiSources(DijkstraMultiSources<V, E> other) {
		if (other == null) {
			throw LOGGER.throwing(new IllegalArgumentException("other should not be null"));
		}

		this.G = other.G;
		states = other.getStates();
	}

	/**
	 * Return a mapping of each vertex, given in the <code>compute</code> function,
	 * to the Dijkstra's traversal state with that vertex as the source vertex.
	 *
	 * @return Dijkstra's traversal states mapping
	 */
	public ConcurrentMap<V, Dijkstra<V, E>> getStates() {
		return new ConcurrentHashMap<>(states);
	}

	/**
	 * Perform Dijkstra algorithm sequentially on the given collection of vertices.
	 * 
	 * @param sources collection of vertices
	 */
	public void compute(List<V> sources) throws IllegalArgumentException {
		if (sources == null) {
			throw LOGGER.throwing(new IllegalArgumentException("list of sources should not be null"));
		}

		populateStates(sources);
	}

	/**
	 * Perform Dijkstra algorithm on the given collection of vertices.
	 * <p>
	 * If an <code>ExecutorService</code> and the number of partitions (N > 1) are
	 * provided, the given collection of vertices is split into N equally-sized
	 * partitions and the computation is performed concurrently on N partitions.
	 *
	 * @param executor threads pool manager
	 * @param N        number of partitions
	 * @param sources  collection of vertices
	 * @return collection of <code>Future</code> (states of concurrent workers)
	 * @throws IllegalArgumentException if <code>executor</code> is null or
	 *                                  <code>N</code> is greater than 1 or
	 *                                  <code>sources</code> is null
	 * @see java.util.concurrent.ExecutorService
	 * @see java.util.concurrent.Future
	 */
	public Collection<Future<?>> compute(ExecutorService executor, int N, List<V> sources)
			throws IllegalArgumentException {
		if (executor == null) {
			throw LOGGER.throwing(new IllegalArgumentException("list of sources should not be null"));
		}

		if (N <= 1) {
			throw LOGGER.throwing(new IllegalArgumentException("number of partitions must be greater than 1"));
		}

		if (sources == null) {
			throw LOGGER.throwing(new IllegalArgumentException("list of sources should not be null"));
		}

		var tasks = new LinkedList<Future<?>>();
		var nSources = sources.size();

		int partitionSize = sources.size() / N + 1;
		for (int i = 0; i < nSources; i += partitionSize) {
			LOGGER.debug("Dijkstra partion[" + i + ", " + Math.min(i + partitionSize, nSources) + ")");
			var sourcesPartition = sources.subList(i, Math.min(i + partitionSize, nSources));
			tasks.add(executor.submit(() -> populateStates(sourcesPartition)));
		}

		return tasks;
	}

	// Go through the list of sources, computes the Dijkstra state and adds the
	// states to the mapping
	private void populateStates(Collection<V> sources) {
		for (var src : sources) {
			var dijkstra = new Dijkstra<>(G);
			try {
				dijkstra.compute(src);
			} catch (IllegalArgumentException e) {
				LOGGER.catching(e);
				continue;
			}

			states.put(src, dijkstra);
		}
		LOGGER.debug("Dijkstra finishes on a partition");
	}

}
