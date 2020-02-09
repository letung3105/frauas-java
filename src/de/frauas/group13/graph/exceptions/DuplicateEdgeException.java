package de.frauas.group13.graph.exceptions;

/**
 * Exception when the edge already exists in the graph
 *
 * @author Alejandro Guajardo Uribe
 */
public class DuplicateEdgeException extends GraphException {

    private static final long serialVersionUID = 8126937885767053528L;

    public DuplicateEdgeException() {
        super();
    }

    public DuplicateEdgeException(String message) {
        super(message);
    }

    public DuplicateEdgeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateEdgeException(Throwable cause) {
        super(cause);
    }

}