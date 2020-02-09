package de.frauas.group13.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.exceptions.DuplicateEdgeException;
import de.frauas.group13.graph.exceptions.DuplicateVertexException;
import de.frauas.group13.graph.exceptions.EdgeNotFoundException;
import de.frauas.group13.graph.exceptions.GraphException;
import de.frauas.group13.graph.exceptions.VertexNotFoundException;
import de.frauas.group13.graph.utils.Pair;

/**
 * An implementation of a the graph data structure using an adjacency list with
 * all edges are of the same type.
 * <p>
 * Queries for the edges that connect the vertices will be computationally
 * expensive when given a dense graph due to the nature of the sequential access
 * on the adjacency list.
 *
 * @param <V> data type of vertices
 * @param <E> data type of edges
 * @author Tung Vo Le
 * @see de.frauas.group13.graph.AbstractGraph
 */
public class SparseGraph<V, E> extends AbstractGraph<V, E> {

	protected Map<E, Pair<V, V>> edges;
	protected Map<V, List<E>> vertices;

	private static final Logger LOGGER = LogManager.getRootLogger();

	public SparseGraph() {
		super();
		edges = new HashMap<>();
		vertices = new HashMap<>();
	}

	public SparseGraph(EdgeType edgeType) {
		super(edgeType);
		edges = new HashMap<>();
		vertices = new HashMap<>();
	}

	/**
	 * Get the number of vertices exist in the graph.
	 *
	 * @return number of vertices
	 */
	@Override
	public int countVertices() {
		return vertices.size();
	}

	/**
	 * Get the number of edges in the graph.
	 *
	 * @return number of edges
	 */
	@Override
	public int countEdges() {
		return edges.size();
	}

	/**
	 * Get a collection of the vertices in the graph.
	 *
	 * @return vertices
	 */
	@Override
	public Set<V> getVertices() {
		return new HashSet<>(vertices.keySet());
	}

	/**
	 * Get a collection of the edges in the graphs.
	 *
	 * @return edges
	 */
	@Override
	public Set<E> getEdges() {
		return new HashSet<>(edges.keySet());
	}

	/**
	 * Check if the graph contains the given vertex.
	 *
	 * @param v the vertex
	 * @return whether the vertex is contained
	 * @throws IllegalArgumentException if <code>v</code> is null
	 */
	@Override
	public boolean hasVertex(V v) throws IllegalArgumentException {
		if (v == null) {
			throw LOGGER.throwing(new IllegalArgumentException("vertex should not be null"));
		}

		return vertices.containsKey(v);
	}

	/**
	 * Check if the graph contains the given edge.
	 *
	 * @param e the edge.
	 * @return whether the edge is contained
	 * @throws IllegalArgumentException if <code>e</code> is null
	 */
	@Override
	public boolean hasEdge(E e) throws IllegalArgumentException {
		if (e == null) {
			throw LOGGER.throwing(new IllegalArgumentException("edge should not be null"));
		}

		return edges.containsKey(e);
	}

	/**
	 * Add a new vertex to the graph
	 *
	 * @param v new vertex
	 * @throws DuplicateVertexException if the graph already contains <code>v</code>
	 * @throws IllegalArgumentException if <code>v</code> is null
	 */
	@Override
	public void addVertex(V v) throws DuplicateVertexException, IllegalArgumentException {
		if (v == null) {
			throw LOGGER.throwing(new IllegalArgumentException("added vertex should not be null"));
		}

		if (hasVertex(v)) {
			throw LOGGER.throwing(new DuplicateVertexException(v + " already exists in the graph"));
		}

		vertices.put(v, new LinkedList<>());
	}

	/**
	 * Add a new edge to the graph.
	 *
	 * @param e  new edge
	 * @param v1 the source vertex of the edge
	 * @param v2 the destination vertex of the edge
	 * @throws DuplicateEdgeException   if the graph already contains <code>e</code>
	 * @throws VertexNotFoundException  if the graph does not contain
	 *                                  <code>v1</code> or <code>v2</code>
	 * @throws IllegalArgumentException if <code>e</code> is null or <code>v1</code>
	 *                                  is null or <code>v2</code> is null
	 */
	@Override
	public void addEdge(E e, V v1, V v2)
			throws VertexNotFoundException, DuplicateEdgeException, IllegalArgumentException {
		if (v1 == null || v1 == null) {
			throw LOGGER.throwing(new IllegalArgumentException("connected vertices should not be null"));
		}

		if (e == null) {
			throw LOGGER.throwing(new IllegalArgumentException("added edge should not be null"));
		}

		if (!hasVertex(v1)) {
			throw LOGGER.throwing(new VertexNotFoundException(v1 + " does not exist in the graph"));
		}

		if (!hasVertex(v2)) {
			throw LOGGER.throwing(new VertexNotFoundException(v2 + " does not exist in the graph"));
		}

		if (hasEdge(e)) {
			throw LOGGER.throwing(new DuplicateEdgeException(e + " already exists in the graph"));
		}

		edges.put(e, new Pair<>(v1, v2));
		vertices.get(v1).add(e);
		if (edgeType == EdgeType.UNDIRECTED) {
			vertices.get(v2).add(e);
		}
	}

	/**
	 * The the pair of vertices connected by the given edge
	 *
	 * @return vertices pair
	 * @throws EdgeNotFoundException    if the graph does not contain <code>e</code>
	 * @throws IllegalArgumentException if <code>e</code> is null
	 */
	@Override
	public Pair<V, V> getEndpoints(E e) throws EdgeNotFoundException, IllegalArgumentException {
		if (e == null) {
			throw LOGGER.throwing(new IllegalArgumentException("edge should not be null"));
		}

		if (!hasEdge(e)) {
			throw LOGGER.throwing(new EdgeNotFoundException(e + " does not exist in the graph"));
		}

		return edges.get(e);
	}

	/**
	 * Get the opposite end-point of a vertex on an edge in the given graph.
	 *
	 * @param e an edge in the graph
	 * @param v a vertex on the given edge
	 * @return the opposite end-point
	 * @throws VertexNotFoundException  if the graph does not contain the
	 *                                  <code>v</code>
	 * @throws EdgeNotFoundException    if the graph does not contain the given
	 *                                  <code>e</code>
	 * @throws IllegalArgumentException if <code>e</code> is null or <code>v</code>
	 *                                  or <code>e</code> does not connect
	 *                                  <code>v</code>
	 */
	@Override
	public V getOpposite(E e, V v) throws VertexNotFoundException, EdgeNotFoundException, IllegalArgumentException {
		if (v == null) {
			throw LOGGER.throwing(new IllegalArgumentException("vertex should not be null"));
		}

		if (e == null) {
			throw LOGGER.throwing(new IllegalArgumentException("edge should not be null"));
		}

		if (!hasEdge(e)) {
			throw LOGGER.throwing(new EdgeNotFoundException(e + " does not exist in the graph"));
		}

		if (!hasVertex(v)) {
			throw LOGGER.throwing(new VertexNotFoundException(v + " does not exist in the graph"));
		}

		var endpoints = getEndpoints(e);
		if (!endpoints.getFirst().equals(v) && !endpoints.getSecond().equals(v)) {
			throw LOGGER.throwing(new IllegalArgumentException(e + " does not connect " + v));
		}

		if (endpoints.getFirst().equals(v)) {
			return endpoints.getSecond();
		}

		return endpoints.getFirst();
	}

	/**
	 * Get all edges that have the given vertex as its source vertex.
	 *
	 * @param v the source vertex
	 * @return the out edges
	 * @throws VertexNotFoundException  if the graph does not contain <code>v</code>
	 * @throws IllegalArgumentException if <code>v</code> is null
	 */
	@Override
	public Collection<E> getOutEdges(V v) throws VertexNotFoundException, IllegalArgumentException {
		if (v == null) {
			throw LOGGER.throwing(new IllegalArgumentException("vertex should not be null"));
		}

		if (!hasVertex(v)) {
			throw LOGGER.throwing(new VertexNotFoundException(v + " does not exist in the graph"));
		}

		return new HashSet<>(vertices.get(v));
	}

	/**
	 * Get all the vertices that are successors to the given vertex. The vertex V is
	 * a successor to a vertex U if there exists an edge that has V as the
	 * destination vertex and U as the source vertex.
	 *
	 * @param v the predecessor
	 * @return the successors
	 * @throws VertexNotFoundException  if the graph does not contain <code>v</code>
	 * @throws IllegalArgumentException if <code>v</code> is null
	 */
	@Override
	public Set<V> getSuccessors(V v) throws VertexNotFoundException, IllegalArgumentException {
		if (v == null) {
			throw LOGGER.throwing(new IllegalArgumentException("vertex should not be null"));
		}

		if (!hasVertex(v)) {
			throw LOGGER.throwing(new VertexNotFoundException(v + " does not exist in the graph"));
		}

		var successors = new HashSet<V>();
		for (var edge : vertices.get(v)) {
			V successor;
			try {
				successor = getOpposite(edge, v);
			} catch (GraphException | IllegalArgumentException e) {
				LOGGER.catching(e);
				continue;
			}

			successors.add(successor);
		}

		return new HashSet<>(successors);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (obj.getClass() != SparseGraph.class) {
			return false;
		}

		var other = (SparseGraph<?, ?>) obj;
		return vertices.equals(other.vertices) && edges.equals(other.edges);
	}

}
