package de.frauas.group13.graph.exceptions;

/**
 * Exception when trying to find a path between two vertices that do not have a path between them
 *
 * @author Alexander Orquera Barrera
 */
public class PathNotFoundException extends GraphException {

    private static final long serialVersionUID = 7366782049096184503L;

    public PathNotFoundException() {
        super();
    }

    public PathNotFoundException(String message) {
        super(message);
    }

    public PathNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PathNotFoundException(Throwable cause) {
        super(cause);
    }

}
