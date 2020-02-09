# Network graph analyzer

This project is the final project for the course *Object Oriented Programming with Java - Advanced* provided by Prof. Müller-Bady at Frankfurt University of Applied Sciences.

# Usage

The source code can be compiled and exported to a *.jar with Eclipse IDE. Upon obtain the *.jar file, the program can be run via the command line.

```bash
Usage: java -jar NetworkAnalyzer.jar [options] <INPUT>
  Options:
    -h, --help
      Show program usage
    -n, --nthreads
      Number of threads used for computing betweenness and all pairs shortest 
      paths 
      Default: 4
    -s, --shortest
      Calculate shortest path between the two given vertices
    -b, --betweenness
      Calculate the betweenness centrality measure of the vertex
    -o, --output
      Calculate for every vertex its betweenness centrality measure and the 
      shortest paths to every other vertices, then output the result to the 
      given file *.graphml
```

