package de.frauas.group13.graph;

import de.frauas.group13.graph.exceptions.DuplicateEdgeException;
import de.frauas.group13.graph.exceptions.DuplicateVertexException;
import de.frauas.group13.graph.exceptions.EdgeNotFoundException;
import de.frauas.group13.graph.exceptions.VertexNotFoundException;
import de.frauas.group13.graph.utils.Pair;

import java.util.Collection;

/**
 * Includes all the expected functionality of a given graph. Every
 * implementation of the graph data structure should implement this to ensure
 * the consistency of the package.
 *
 * @param <V> data type of vertices
 * @param <E> data type of edges
 * @author Tung Vo Le
 */
public interface Graph<V, E> {

	/**
	 * Get the default edge type of the graph.
	 *
	 * @return edge type
	 * @see de.frauas.group13.graph.EdgeType
	 */
	EdgeType getEdgeType();

	/**
	 * Get the number of vertices exist in the graph.
	 *
	 * @return number of vertices
	 */
	int countVertices();

	/**
	 * Get the number of edges in the graph.
	 *
	 * @return number of edges
	 */
	int countEdges();

	/**
	 * Get a collection of the vertices in the graph.
	 *
	 * @return vertices
	 */
	Collection<V> getVertices();

	/**
	 * Get a collection of the edges in the graphs.
	 *
	 * @return edges
	 */
	Collection<E> getEdges();

	/**
	 * Check if the graph contains the given vertex.
	 *
	 * @param v the vertex
	 * @return whether the <code>v</code> is contained
	 * @throws IllegalArgumentException if <code>v</code> is null
	 */
	boolean hasVertex(V v) throws IllegalArgumentException;

	/**
	 * Check if the graph contains the given edge.
	 *
	 * @param e the edge.
	 * @return whether the <code>e</code> is contained
	 * @throws IllegalArgumentException if <code>e</code> is null
	 */
	boolean hasEdge(E e) throws IllegalArgumentException;

	/**
	 * Add a new vertex to the graph
	 *
	 * @param v new vertex
	 * @throws DuplicateVertexException if the graph already contains <code>v</code>
	 * @throws IllegalArgumentException if <code>v</code> is null
	 * 
	 */
	void addVertex(V v) throws DuplicateVertexException, IllegalArgumentException;

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
	void addEdge(E e, V v1, V v2) throws DuplicateEdgeException, VertexNotFoundException, IllegalArgumentException;

	/**
	 * The the pair of vertices connected by the given edge
	 *
	 * @return vertices pair
	 * @throws EdgeNotFoundException    if the graph does not contain <code>e</code>
	 * @throws IllegalArgumentException if <code>e</code> is null
	 */
	Pair<V, V> getEndpoints(E e) throws EdgeNotFoundException, IllegalArgumentException;

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
	V getOpposite(E e, V v) throws VertexNotFoundException, EdgeNotFoundException, IllegalArgumentException;

	/**
	 * Get all edges that have the given vertex as its source vertex.
	 *
	 * @param v the source vertex
	 * @return the out edges
	 * @throws VertexNotFoundException  if the graph does not contain <code>v</code>
	 * @throws IllegalArgumentException if <code>v</code> is null
	 */
	Collection<E> getOutEdges(V v) throws VertexNotFoundException, IllegalArgumentException;

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
	Collection<V> getSuccessors(V v) throws VertexNotFoundException, IllegalArgumentException;

}
