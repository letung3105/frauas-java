package de.frauas.group13.graph.exceptions;

/**
 * Exception when the edge already exists in the graph
 *
 * @author Alejandro Guajardo Uribe
 */
public class DuplicateVertexException extends GraphException {

    private static final long serialVersionUID = -7337672013273846907L;

    public DuplicateVertexException() {
        super();
    }

    public DuplicateVertexException(String message) {
        super(message);
    }

    public DuplicateVertexException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateVertexException(Throwable cause) {
        super(cause);
    }

}