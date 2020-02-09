package de.frauas.group13.graph.algorithms;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.Graph;
import de.frauas.group13.graph.exceptions.EdgeNotFoundException;
import de.frauas.group13.graph.exceptions.VertexNotFoundException;
import de.frauas.group13.graph.utils.Pair;
import de.frauas.group13.graph.utils.WeightedEdge;

/**
 * Implementation of Dijkstra algorithm for finding the distance of all single
 * source shortest paths in the given graph. This class also providing the
 * following information
 * <ul>
 * <li>A set of the visited vertices</li>
 * <li>A stack holding the vertices in the order of the traversal</li>
 * <li>The predecessors of each vertex on the shortest path to it</li>
 * <li>The number of shortest paths can be used to reach a vertex</li>
 * </ul>
 *
 * @param <V>
 * @param <E>
 * @author Hieu Minh Truong
 * @author Tung Le Vo
 * @see de.frauas.group13.graph.Graph
 * @see de.frauas.group13.graph.util.WeightedEdge
 */
public class Dijkstra<V, E extends WeightedEdge> {

	protected Set<V> visited;
	protected Map<V, Double> distances;
	protected Map<V, List<V>> predecessors;

	private static final Logger LOGGER = LogManager.getRootLogger();
	private Graph<V, E> G;
	private Stack<V> visitOrder;
	private Map<V, Integer> pathCounts;

	private class VertexDistanceComparator implements Comparator<Pair<V, Double>> {
		@Override
		public int compare(Pair<V, Double> p1, Pair<V, Double> p2) {
			return p1.getSecond().compareTo(p2.getSecond());
		}
	}

	public Dijkstra(Graph<V, E> G) {
		if (G == null) {
			throw LOGGER.throwing(new IllegalArgumentException("graph should not be null"));
		}

		this.G = G;
		visited = new HashSet<>(G.countVertices());
		visitOrder = new Stack<>();
		predecessors = new HashMap<>(G.countVertices());
		distances = new HashMap<>(G.countVertices());
		pathCounts = new HashMap<>(G.countVertices());
	}

	public Dijkstra(Dijkstra<V, E> other) {
		if (G == null) {
			throw LOGGER.throwing(new IllegalArgumentException("other should not be null"));
		}

		this.G = other.G;
		visited = other.getVisited();
		visitOrder = other.getVisitOrder();
		predecessors = other.getPredecessors();
		distances = other.getDistances();
		pathCounts = other.getPathCounts();
	}

	/**
	 * Return the vertices that can be traversed to from the source vertex given in
	 * <code>compute</code>.
	 *
	 * @return visited vertices
	 */
	public Set<V> getVisited() {
		return new HashSet<>(visited);
	}

	/**
	 * Return a stack holding the vertices that can be traversed to from the source
	 * vertex given in <code>compute</code> in the order of the traversal.
	 *
	 * @return ordered vertices
	 */
	public Stack<V> getVisitOrder() {
		@SuppressWarnings("unchecked")
		var stackClone = (Stack<V>) visitOrder.clone();
		return stackClone;
	}

	/**
	 * Return a map of each vertex to its predecessors on the shortest path starting
	 * from the source vertex given in <code>compute</code>.
	 *
	 * @return predecessors mapping
	 */
	public Map<V, List<V>> getPredecessors() {
		return new HashMap<>(predecessors);
	}

	/**
	 * Return a map of each vertex to the distance of the shortest path starting
	 * from the source vertex given in <code>compute</code>.
	 *
	 * @return distances mapping
	 */
	public Map<V, Double> getDistances() {
		return new HashMap<>(distances);
	}

	/**
	 * Return a map of each vertex to the number of existed shortest paths from the
	 * source vertex given in <code>compute</code>
	 *
	 * @return paths counts mapping
	 * @author Tung Vo Le
	 */
	public Map<V, Integer> getPathCounts() {
		return new HashMap<>(pathCounts);
	}

	/**
	 * Perform Dijkstra algorithm to find the distance of all shortest paths to
	 * every vertex in the graph starting from the given source vertex and populates
	 * the additional states.
	 * <p>
	 * The internal state of this class will be re-initialized for every call to
	 * this function
	 *
	 * @param src the source vertex for starting the algorithm
	 * @throws IllegalArgumentException if <code>src</code> is null or the graph
	 *                                  does not contain <code>src</code>
	 */
	public void compute(V src) throws IllegalArgumentException {
		if (src == null) {
			throw LOGGER.throwing(new IllegalArgumentException("source vertex should not be null"));
		}

		if (!G.hasVertex(src)) {
			throw LOGGER.throwing(new IllegalArgumentException(src + " does not exist in the graph"));
		}

		resetState(src);

		// priority queue for choosing next vertex to be explored,
		// initialized with the source vertex
		var PQ = new PriorityQueue<>(new VertexDistanceComparator());
		PQ.add(new Pair<>(src, 0.0));

		while (!PQ.isEmpty()) {
			// getting vertex with the shortest found distance
			var v = PQ.poll().getFirst();

			// explore unvisited vertex
			if (!visited.contains(v)) {
				visit(PQ, v);
			}
		}
	}

	// Helper method for exploring neighboring vertices
	private void visit(PriorityQueue<Pair<V, Double>> PQ, V v) {
		visited.add(v);
		visitOrder.push(v);
		var distToSrc = distances.get(v);

		Collection<E> outEdges;
		try {
			outEdges = G.getOutEdges(v);
		} catch (VertexNotFoundException | IllegalArgumentException e) {
			LOGGER.catching(e);
			return;
		}

		// go through every out-going edge
		for (var edge : outEdges) {
			V dst;
			try {
				dst = G.getOpposite(edge, v);
			} catch (VertexNotFoundException | EdgeNotFoundException | IllegalArgumentException e) {
				LOGGER.catching(e);
				continue;
			}

			var distToDst = distToSrc + edge.getWeight().doubleValue();
			if (updateState(v, dst, distToDst)) {
				PQ.add(new Pair<>(dst, distToDst));
			}
		}
	}

	// Helper method for updating class's state, return true if found a shorter
	// distance
	private boolean updateState(V src, V dst, double distance) {
		var distanceUpdated = false;

		if (distances.get(dst) > distance) {
			distances.put(dst, distance);
			pathCounts.put(dst, 0);
			predecessors.put(dst, new LinkedList<>());
			distanceUpdated = true;
		}

		// count number of shortest path and populate predecessors
		if (distances.get(dst) == distance) {
			pathCounts.put(dst, pathCounts.get(dst) + pathCounts.get(src));
			predecessors.get(dst).add(src);
		}

		return distanceUpdated;
	}

	// Helper method for initializing class's state
	private void resetState(V source) {
		visited.clear();
		visitOrder.clear();
		distances.clear();
		predecessors.clear();
		pathCounts.clear();

		for (var v : G.getVertices()) {
			distances.put(v, Double.MAX_VALUE);
			predecessors.put(v, new LinkedList<>());
			pathCounts.put(v, 0);
		}

		distances.put(source, 0.0);
		pathCounts.put(source, 1);
	}

}
