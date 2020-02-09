package de.frauas.group13;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import de.frauas.group13.graph.Graph;
import de.frauas.group13.graph.algorithms.BetweennessCentrality;
import de.frauas.group13.graph.algorithms.ConnectivityValidator;
import de.frauas.group13.graph.algorithms.DijkstraMultiSources;
import de.frauas.group13.graph.algorithms.ShortestPathsMultiSources;
import de.frauas.group13.graph.algorithms.ShortestPathsSingleSource;
import de.frauas.group13.utils.Edge;
import de.frauas.group13.utils.GraphReader;
import de.frauas.group13.utils.GraphWriter;
import de.frauas.group13.utils.Vertex;

/**
 * Program main entry point. This handle arguments parsing and thread setup
 * 
 * @author Alejandro Guajardo Uribe
 */
public class Main {
	@Parameter(required = true, description = "<INPUT>")
	private String input;

	@Parameter(names = { "-h", "--help" }, order = 0, help = true, description = "Show program usage")
	private boolean help = false;

	@Parameter(names = { "-n",
			"--nthreads" }, order = 1, description = "Number of threads used for computing betweenness and all pairs shortest paths")
	private int nthreads = 4;

	@Parameter(names = { "-s",
			"--shortest" }, arity = 2, order = 2, description = "Calculate shortest path between the two given vertices")
	private ArrayList<String> shortest = null;

	@Parameter(names = { "-b",
			"--betweenness" }, order = 3, description = "Calculate the betweenness centrality measure of the vertex")
	private String betweenness = null;

	@Parameter(names = { "-o",
			"--output" }, order = 4, description = "Calculate for every vertex its betweenness centrality measure and the shortest paths to every other vertices, then output the result to the given file *.graphml")
	private String output = null;

	private static final Logger LOGGER = LogManager.getRootLogger();

	public static void main(String[] argv) {
		var app = new Main();

		app.parseArgs(argv);
		app.run();
	}

	// parse command line arguments and handle errors when required arguments are
	// not given
	private void parseArgs(String[] argv) {
		var jc = JCommander.newBuilder().addObject(this).build();
		jc.setProgramName("java -jar NetworkAnalyzer.jar");

		try {
			jc.parse(argv);
		} catch (ParameterException e) {
			LOGGER.catching(Level.FATAL, e);
			jc.usage();
			System.exit(1);
			return;
		}

		if (help) {
			jc.usage();
			System.exit(0);
			return;
		}
	}

	// start program
	private void run() {
		// read graphs from file
		var parser = new GraphReader();
		List<Graph<Vertex, Edge>> graphs;
		try {
			graphs = parser.parse(input);
		} catch (Exception e) {
			errorExit(e);
			return;
		}

		if (graphs.size() > 0) {
			var G = graphs.get(0);

			if (shortest == null && betweenness == null || output != null) {
				findAll(G);
			} else {
				if (shortest != null) {
					findShortestPath(G);
				}

				if (betweenness != null) {
					findBetweenness(G);
				}
			}
		} else {
			LOGGER.warn(new IllegalArgumentException("File " + input + " does not contain graph data"));
			System.exit(0);
		}
	}

	// this is executed when no option is given
	private void findAll(Graph<Vertex, Edge> G) {
		var vertices = G.getVertices();

		var multiDijkstra = new DijkstraMultiSources<>(G);
		ShortestPathsMultiSources<Vertex, Edge> pathsFinder;
		BetweennessCentrality<Vertex, Edge> betweennessFinder;

		if (nthreads > 1) {
			// concurrent computation
			var executor = Executors.newFixedThreadPool(nthreads);
			try {
				// wait for tasks for computing Dijkstra states to finish before moving on to
				// the next steps
				LOGGER.info("Start running Dijkstra on every vertex...");
				waitTasks(multiDijkstra.compute(executor, nthreads, new LinkedList<>(vertices)));
			} catch (Exception e) {
				errorExit(e);
				return;
			}
			LOGGER.debug("Finished running Dijkstra");

			LOGGER.info("Start aggregating shortest paths...");
			pathsFinder = new ShortestPathsMultiSources<>(multiDijkstra);
			pathsFinder.computePaths(executor, nthreads);

			LOGGER.info("Start calculating betweenness...");
			betweennessFinder = new BetweennessCentrality<>(multiDijkstra);
			betweennessFinder.computeBetweenness(executor, nthreads);

			try {
				shutdownWait(executor);
			} catch (InterruptedException e) {
				errorExit(e);
				return;
			}
			LOGGER.debug("Finish aggegating shortest paths and calculating betweenness");

		} else {
			// sequential computation
			LOGGER.info("Running Dijkstra on every vertex...");
			multiDijkstra.compute(new LinkedList<>(vertices));

			LOGGER.info("Aggregating shortest paths...");
			pathsFinder = new ShortestPathsMultiSources<>(multiDijkstra);
			pathsFinder.computePaths();

			LOGGER.info("Calculating betweenness...");
			betweennessFinder = new BetweennessCentrality<>(multiDijkstra);
			betweennessFinder.computeBetweenness();
		}

		// find longest shortest-path
		var diameter = 0.0;
		var multiDijkstraStates = multiDijkstra.getStates();
		for (var v : multiDijkstraStates.keySet()) {
			for (var d : multiDijkstraStates.get(v).getDistances().values()) {
				if (d > diameter) {
					diameter = d;
				}
			}
		}
		printGraphSummary(G, diameter);

		for (var src : vertices) {
			Map<Vertex, List<Vertex>> paths;
			Map<Vertex, Double> distances;
			try {
				paths = pathsFinder.getPathsFrom(src);
				distances = pathsFinder.getDistancesFrom(src);
			} catch (Exception e) {
				errorExit(e);
				continue;
			}

			for (var dst : paths.keySet()) {
				printShortestPath(src, dst, paths.get(dst), distances.get(dst));
			}
		}

		for (var v : vertices) {
			printBetweenness(v, betweennessFinder.getMeasure(v));
		}

		if (output != null) {
			LOGGER.info("Writing results to " + output);
			var graphWriter = new GraphWriter(G, betweennessFinder, pathsFinder);
			graphWriter.setNamespaceInfo("JavaGroup13", "http://www.w3.org/2001/XMLSchema-instance",
					"myNamespaceXsiLocation-./outputGraph.xsd");
			try {
				graphWriter.write(output);
			} catch (Exception e) {
				errorExit(e);
			}
		}
	}

	// this is executed when "-s <source> <destination>" is given
	private void findShortestPath(Graph<Vertex, Edge> G) {
		var src = new Vertex(shortest.get(0));
		var dst = new Vertex(shortest.get(1));
		var pathsFinder = new ShortestPathsSingleSource<>(G);

		LOGGER.info("Running Dijkstra on " + src + "...");

		try {
			pathsFinder.compute(src);
			printShortestPath(src, dst, pathsFinder.getPathTo(dst), pathsFinder.getDistanceTo(dst));
		} catch (Exception e) {
			errorExit(e);
		}
	}

	// this is executed when "-b <vertex>" is given
	private void findBetweenness(Graph<Vertex, Edge> G) {
		var v = new Vertex(betweenness);

		LOGGER.info("Calculating betweenness measure for " + v + "...");

		var betweennessFinder = new BetweennessCentrality<>(G);
		if (nthreads > 1) {
			var executor = Executors.newFixedThreadPool(nthreads);

			try {
				waitTasks(betweennessFinder.compute(executor, nthreads, new LinkedList<>(G.getVertices())));
				betweennessFinder.computeBetweenness(executor, nthreads);
				shutdownWait(executor);
			} catch (Exception e) {
				errorExit(e);
				return;
			}
		} else {
			try {
				betweennessFinder.compute(new LinkedList<>(G.getVertices()));
				betweennessFinder.computeBetweenness();
			} catch (Exception e) {
				errorExit(e);
				return;
			}
		}

		printBetweenness(v, betweennessFinder.getMeasure(v));
	}

	// shuts down the executor and blocks main thread until for all sent task to
	// finish before terminate
	private void shutdownWait(ExecutorService executor) throws InterruptedException {
		if (executor != null) {
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		}
	}

	// blocks main thread until of given tasks finish without shutting down the the
	// executor
	private void waitTasks(Collection<Future<?>> tasks) {
		for (var t : tasks) {
			try {
				t.get();
				LOGGER.debug("Finish wating task " + t);
			} catch (InterruptedException | ExecutionException e) {
				errorExit(e);
			}
		}
	}

	private void printGraphSummary(Graph<Vertex, Edge> G, double diameter) {
		var verticesStr = G.getVertices().stream().map(Vertex::getName).collect(Collectors.joining(", "));
		var edgesStr = G.getEdges().stream().map(Edge::getName).collect(Collectors.joining(", "));

		LOGGER.info("Number of vertices " + G.countVertices());
		LOGGER.info("Number of edges " + G.countEdges());
		LOGGER.info("Vertices ids: " + verticesStr);
		LOGGER.info("Edges ids: " + edgesStr);
		if (new ConnectivityValidator<>(G).validate()) {
			LOGGER.info("Graph is connected");
		} else {
			LOGGER.info("Graph is not connected");
		}
		LOGGER.info("Diameter: " + diameter);
	}

	private void printShortestPath(Vertex src, Vertex dst, List<Vertex> path, double distance) {
		var pathStr = path.stream().map(Vertex::getName).collect(Collectors.joining(" --> "));
		LOGGER.info("Source: " + src + " | Destination: " + dst + "\n\tPath: " + pathStr + "\n\tDistance: " + distance);
	}

	private void printBetweenness(Vertex v, double betweennessVal) {
		LOGGER.info(v + " betweenness: " + betweennessVal);
	}

	private void errorExit(Exception e) {
		LOGGER.catching(Level.FATAL, e);
		System.exit(1);
	}
}