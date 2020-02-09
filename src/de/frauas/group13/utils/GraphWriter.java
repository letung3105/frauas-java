package de.frauas.group13.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.frauas.group13.graph.EdgeType;
import de.frauas.group13.graph.Graph;
import de.frauas.group13.graph.algorithms.BetweennessCentrality;
import de.frauas.group13.graph.algorithms.ShortestPathsMultiSources;
import de.frauas.group13.graph.utils.Pair;

/**
 * <p>
 * Export all computation of the inputed *.graphml file to another *.graphml
 * file with all basic properties of a graph, betweenness centrality measure of
 * all given vertices, shortest paths and its distance from each vertex to all
 * the others.
 * </p>
 * 
 * @author Hieu Truong Minh
 */

public class GraphWriter {

	private static final Logger LOGGER = LogManager.getRootLogger();

	private Graph<Vertex, Edge> G;
	private BetweennessCentrality<Vertex, Edge> betweenness;
	private ShortestPathsMultiSources<Vertex, Edge> paths;
	private String namespaceInfo;
	private String namespaceXsiInfo;
	private String namespaceXsiLocInfo;
	private XMLEventFactory eventFactory;

	public GraphWriter(Graph<Vertex, Edge> G, BetweennessCentrality<Vertex, Edge> betweenness,
			ShortestPathsMultiSources<Vertex, Edge> paths) {
		if (G == null) {
			throw LOGGER.throwing(new IllegalArgumentException("graph should not be null"));
		}
		if (betweenness == null) {
			throw LOGGER.throwing(new IllegalArgumentException("betweenness state should not be null"));
		}
		if (paths == null) {
			throw LOGGER.throwing(new IllegalArgumentException("paths state should not be null"));
		}

		this.G = G;
		this.betweenness = betweenness;
		this.paths = paths;
		eventFactory = XMLEventFactory.newInstance();
	}

	/**
	 * Set the name space information for the XML file
	 * 
	 * @param namespaceInfo      name space name
	 * @param namespaceXsiInfo   XSI name space
	 * @param nmesapceXsiLocInfo XSI name space location
	 */
	public void setNamespaceInfo(String namespaceInfo, String namespaceXsiInfo, String namespaceXsiLocInfo) {
		this.namespaceInfo = namespaceInfo;
		this.namespaceXsiInfo = namespaceXsiInfo;
		this.namespaceXsiLocInfo = namespaceXsiLocInfo;
	}

	/**
	 * Write the the computation results and graph data to the given output file
	 * 
	 * @param filePath file location on the system
	 * @throws XMLStreamException    if error occurs while writing file
	 * @throws FileNotFoundException if <code>filePath</code> does not exist
	 */
	public void write(String filePath) throws XMLStreamException, FileNotFoundException {
		// get graph data
		var xmlOutputFactory = XMLOutputFactory.newInstance();
		var xmlEventWriter = xmlOutputFactory.createXMLEventWriter(new FileOutputStream(filePath), "UTF-8");

		configStartDocument(xmlEventWriter);

		for (var vertex : G.getVertices()) {
			createVertex(xmlEventWriter, vertex);
		}

		for (var edge : G.getEdges()) {
			createEdge(xmlEventWriter, edge);
		}

		configEndDocument(xmlEventWriter);

	}

	private void configStartDocument(XMLEventWriter xmlEventWriter) throws XMLStreamException {
		var end = eventFactory.createDTD("\n");
		xmlEventWriter.add(eventFactory.createStartDocument());
		xmlEventWriter.add(end);

		// xml schema reference
		xmlEventWriter.add(eventFactory.createStartElement("", namespaceInfo, "graphml"));
		xmlEventWriter.add(eventFactory.createNamespace(namespaceInfo));
		xmlEventWriter.add(eventFactory.createNamespace("xsi", namespaceXsiInfo));
		xmlEventWriter.add(eventFactory.createNamespace("schemalocation", namespaceXsiLocInfo));
		xmlEventWriter.add(end);

		// v_id key definition
		configStartElement(xmlEventWriter, 1, "key", Arrays.asList("id", "for", "attr.name", "attr.type"),
				Arrays.asList("v_id", "node", "id", "double"));
		configEndElement(xmlEventWriter, 1, "key");

		// e_id key definition
		configStartElement(xmlEventWriter, 1, "key", Arrays.asList("id", "for", "attr.name", "attr.type"),
				Arrays.asList("e_id", "edge", "id", "double"));
		configEndElement(xmlEventWriter, 1, "key");

		// e_weight key definition
		configStartElement(xmlEventWriter, 1, "key", Arrays.asList("id", "for", "attr.name", "attr.type"),
				Arrays.asList("e_weight", "edge", "weight", "double"));
		configEndElement(xmlEventWriter, 1, "key");

		// betweenness key definition
		configStartElement(xmlEventWriter, 1, "key", Arrays.asList("id", "for", "attr.name", "attr.type"),
				Arrays.asList("betweenness", "node", "betweenness", "double"));
		configEndElement(xmlEventWriter, 1, "key");

		// shortest_paths key definition
		configStartElement(xmlEventWriter, 1, "key", Arrays.asList("id", "for"),
				Arrays.asList("shortest_paths", "node"));
		indent(xmlEventWriter, 2);
		xmlEventWriter.add(eventFactory.createStartElement("", "", "desc"));
		xmlEventWriter.add(eventFactory.createCharacters("All the shortest paths with this vertex as source"));
		xmlEventWriter.add(eventFactory.createEndElement("", "", "desc"));
		xmlEventWriter.add(end);
		configEndElement(xmlEventWriter, 1, "key");

		// configuration of graph
		indent(xmlEventWriter, 1);
		xmlEventWriter.add(eventFactory.createStartElement("", "", "graph"));
		xmlEventWriter.add(eventFactory.createAttribute("id", "G"));

		var edgeType = G.getEdgeType();
		if (edgeType == EdgeType.DIRECTED) {
			xmlEventWriter.add(eventFactory.createAttribute("edgedefault", "directed"));
		} else if (edgeType == EdgeType.UNDIRECTED) {
			xmlEventWriter.add(eventFactory.createAttribute("edgedefault", "undirected"));
		}
		xmlEventWriter.add(end);
	}

	private void configEndDocument(XMLEventWriter xmlEventWriter) throws XMLStreamException {
		var end = eventFactory.createDTD("\n");
		indent(xmlEventWriter, 1);
		xmlEventWriter.add(eventFactory.createEndElement("", "", "graph"));
		xmlEventWriter.add(end);
		xmlEventWriter.add(eventFactory.createEndElement("", "", "graphml"));
		xmlEventWriter.add(end);
		xmlEventWriter.add(eventFactory.createEndDocument());
		xmlEventWriter.close();
	}

	private void createVertex(XMLEventWriter eventWriter, Vertex vertex) throws XMLStreamException {
		configStartElement(eventWriter, 2, "node", "id", vertex.getName());

		writeData(eventWriter, 3, "v_id", vertex.getId());
		writeData(eventWriter, 3, "betweenness", String.valueOf(betweenness.getMeasure(vertex)));

		configStartElement(eventWriter, 3, "data", "key", "shortest_paths");

		Map<Vertex, List<Vertex>> shortestPaths;
		Map<Vertex, Double> shortestDistances;
		try {
			shortestPaths = paths.getPathsFrom(vertex);
			shortestDistances = paths.getDistancesFrom(vertex);
		} catch (Exception e) {
			LOGGER.catching(Level.WARN, e);
			return;
		}

		// loop through all vertices to get shortest path and its distance from the
		// current node to all the others
		for (Vertex targetVertex : shortestPaths.keySet()) {
			// path and distance strings
			var pathStr = shortestPaths.get(targetVertex).stream().map(Vertex::getName)
					.collect(Collectors.joining("-"));
			var distance = String.valueOf(shortestDistances.get(targetVertex));

			configStartElement(eventWriter, 4, "target", "v_id", targetVertex.getName());

			writeShortestPath(eventWriter, 5, "path", pathStr);
			writeShortestPath(eventWriter, 5, "distance", distance);

			configEndElement(eventWriter, 4, "target");
		}

		configEndElement(eventWriter, 3, "data");
		configEndElement(eventWriter, 2, "node");
	}

	private void createEdge(XMLEventWriter eventWriter, Edge edge) throws XMLStreamException {

		Pair<Vertex, Vertex> endpoints;
		try {
			endpoints = G.getEndpoints(edge);
		} catch (Exception e) {
			LOGGER.catching(Level.WARN, e);
			return;
		}

		configStartElement(eventWriter, 2, "edge", Arrays.asList("source", "target"),
				Arrays.asList(endpoints.getFirst().getName(), endpoints.getSecond().getName()));

		writeData(eventWriter, 3, "e_id", edge.getId());
		writeData(eventWriter, 3, "e_weight", String.valueOf(edge.getWeight()));

		configEndElement(eventWriter, 2, "edge");
	}

	private void indent(XMLEventWriter eventWriter, int numberIndent) throws XMLStreamException {
		var tab = eventFactory.createDTD("\t");
		for (int i = 0; i < numberIndent; i++) {
			eventWriter.add(tab);
		}
	}

	private void writeData(XMLEventWriter eventWriter, int numberIndent, String attributeValue, String data)
			throws XMLStreamException {
		XMLEvent end = eventFactory.createDTD("\n");

		indent(eventWriter, numberIndent);
		eventWriter.add(eventFactory.createStartElement("", "", "data"));
		eventWriter.add(eventFactory.createAttribute("key", attributeValue));
		eventWriter.add(eventFactory.createCharacters(data));
		eventWriter.add(eventFactory.createEndElement("", "", "data"));
		eventWriter.add(end);
	}

	private void writeShortestPath(XMLEventWriter eventWriter, int numberIndent, String tagName, String data)
			throws XMLStreamException {
		var end = eventFactory.createDTD("\n");
		indent(eventWriter, numberIndent);
		eventWriter.add(eventFactory.createStartElement("", "", tagName));
		eventWriter.add(eventFactory.createCharacters(data));
		eventWriter.add(eventFactory.createEndElement("", "", tagName));
		eventWriter.add(end);
	}

	private void configStartElement(XMLEventWriter eventWriter, int numberIndent, String tagName, String key,
			String val) throws XMLStreamException {

		var end = eventFactory.createDTD("\n");

		indent(eventWriter, numberIndent);
		eventWriter.add(eventFactory.createStartElement("", "", tagName));

		eventWriter.add(eventFactory.createAttribute(key, val));

		eventWriter.add(end);
	}

	private void configStartElement(XMLEventWriter eventWriter, int numberIndent, String tagName,
			List<String> attributes, List<String> values) throws XMLStreamException, IllegalArgumentException {
		if (attributes.size() != values.size()) {
			LOGGER.throwing(new IllegalArgumentException("attributes keys size and values size mismatch"));
		}

		var end = eventFactory.createDTD("\n");

		indent(eventWriter, numberIndent);
		eventWriter.add(eventFactory.createStartElement("", "", tagName));

		for (int i = 0; i < attributes.size(); i++) {
			eventWriter.add(eventFactory.createAttribute(attributes.get(i), values.get(i)));
		}

		eventWriter.add(end);
	}

	private void configEndElement(XMLEventWriter eventWriter, int numberIndent, String tagName)
			throws XMLStreamException {
		var end = eventFactory.createDTD("\n");

		indent(eventWriter, numberIndent);
		eventWriter.add(eventFactory.createEndElement("", "", tagName));
		eventWriter.add(end);
	}
}
