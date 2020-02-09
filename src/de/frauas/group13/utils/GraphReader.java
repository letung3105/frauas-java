package de.frauas.group13.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.frauas.group13.graph.EdgeType;
import de.frauas.group13.graph.Graph;
import de.frauas.group13.graph.SparseGraph;
import de.frauas.group13.graph.utils.Pair;

/**
 * This implements a graphml parser with a predefined format using Java
 * DOMParser
 * 
 * @author Alexander Orquera Barrera
 *
 */
public class GraphReader {

	private static final Logger LOGGER = LogManager.getRootLogger();

	/**
	 * Get a collection of graphs defined in the data given by the input file
	 * 
	 * @param path file's location on the system
	 * @return collection of graphs
	 * @throws IOException if there IO error or parse error occurs
	 */
	public List<Graph<Vertex, Edge>> parse(String path) throws IOException {
		var inputFile = new File(path);
		var factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder;
		Document doc;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(inputFile);
		} catch (Exception e) {
			throw LOGGER.throwing(new IOException("Could not parse graphml", e));
		}

		return parseGraphs(doc);
	}

	private List<Graph<Vertex, Edge>> parseGraphs(Document xmlDoc) {
		var graphs = new ArrayList<Graph<Vertex, Edge>>();
		var xmlGraphNodes = xmlDoc.getElementsByTagName("graph");

		for (int i = 0; i < xmlGraphNodes.getLength(); i++) {
			var xmlGraphNode = xmlGraphNodes.item(i);

			if (xmlGraphNode.getNodeType() == Node.ELEMENT_NODE) {
				var xmlGraphElem = (Element) xmlGraphNode;

				// check if graph is undirected
				var edgeType = EdgeType.UNDIRECTED;
				if (xmlGraphElem.getAttribute("edgedefault").equals("directed")) {
					edgeType = EdgeType.DIRECTED;
				}

				var G = new SparseGraph<Vertex, Edge>(edgeType);

				// adding parsed vertices to graph
				var vertices = parseVertices(xmlGraphElem);
				for (var vertex : vertices.values()) {
					try {
						G.addVertex(vertex);
					} catch (Exception e) {
						LOGGER.catching(Level.WARN, e);
					}
				}

				// adding parsed edges to graph
				var edges = this.parseEdges(xmlGraphElem);
				for (var edge : edges.keySet()) {
					var srcId = edges.get(edge).getFirst();
					var dstId = edges.get(edge).getSecond();
					if (vertices.containsKey(srcId) && vertices.containsKey(dstId)) {
						try {
							G.addEdge(edge, vertices.get(srcId), vertices.get(dstId));
						} catch (Exception e) {
							LOGGER.catching(Level.WARN, e);
						}
					}
				}

				graphs.add(G);
			}
		}

		return graphs;
	}

	private Map<String, Vertex> parseVertices(Element xmlGraphElem) {
		var vertices = new HashMap<String, Vertex>();
		var dataKeys = new HashSet<String>();
		dataKeys.add("v_id");

		// go through every <node></node>
		var xmlVertexNodes = xmlGraphElem.getElementsByTagName("node");
		for (int i = 0; i < xmlVertexNodes.getLength(); i++) {
			var xmlVertexNode = xmlVertexNodes.item(i);

			if (xmlVertexNode.getNodeType() == Node.ELEMENT_NODE) {
				var xmlVertexElem = (Element) xmlVertexNode;
				var vertexData = parseData(xmlVertexElem, dataKeys);

				if (vertexData.size() == dataKeys.size()) {
					vertices.put(xmlVertexElem.getAttribute("id"), new Vertex(vertexData.get("v_id")));
				}
			}
		}

		return vertices;
	}

	private Map<Edge, Pair<String, String>> parseEdges(Element xmlGraphElem) {
		var edges = new HashMap<Edge, Pair<String, String>>();
		var dataKeys = new HashSet<String>();
		dataKeys.add("e_id");
		dataKeys.add("e_weight");

		// go through every tag with name = "edge"
		var xmlEdgeNodes = xmlGraphElem.getElementsByTagName("edge");
		for (int i = 0; i < xmlEdgeNodes.getLength(); i++) {
			var xmlEdgeNode = xmlEdgeNodes.item(i);

			if (xmlEdgeNode.getNodeType() == Node.ELEMENT_NODE) {
				var xmlEdgeElem = (Element) xmlEdgeNode;
				var edgeData = parseData(xmlEdgeElem, dataKeys);

				if (edgeData.size() == dataKeys.size()) {
					var srcId = xmlEdgeElem.getAttribute("source");
					var dstId = xmlEdgeElem.getAttribute("target");
					edges.put(new Edge(edgeData.get("e_id"), Double.parseDouble(edgeData.get("e_weight"))),
							new Pair<>(srcId, dstId));

				}
			}
		}

		return edges;
	}

	private Map<String, String> parseData(Element root, Set<String> keys) {
		var keysValue = new HashMap<String, String>();

		// go through every tag with name = "data"
		var xmlKeyNodes = root.getElementsByTagName("data");
		for (int i = 0; i < xmlKeyNodes.getLength(); i++) {
			var xmlKeyNode = xmlKeyNodes.item(i);

			if (xmlKeyNode.getNodeType() == Node.ELEMENT_NODE) {
				var xmlKeyElem = (Element) xmlKeyNode;

				var key = xmlKeyElem.getAttribute("key");
				if (keys.contains(key)) {
					keysValue.put(key, xmlKeyElem.getTextContent());
				}
			}
		}

		return keysValue;
	}
}
