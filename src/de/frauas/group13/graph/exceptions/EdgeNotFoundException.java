package de.frauas.group13.graph.exceptions;

/**
 * Exception when an operation is performed on an edge that does not exist in the graph
 *
 * @author Alejandro Guajardo Uribe
 */
public class EdgeNotFoundException extends GraphException {

    private static final long serialVersionUID = -7739676880094649814L;

    public EdgeNotFoundException() {
        super();
    }

    public EdgeNotFoundException(String message) {
        super(message);
    }

    public EdgeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EdgeNotFoundException(Throwable cause) {
        super(cause);
    }

}
