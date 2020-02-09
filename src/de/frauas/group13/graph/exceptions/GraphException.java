package de.frauas.group13.graph.exceptions;

/**
 * General graph exception
 *
 * @author Alexander Orquera Barrera
 */
public class GraphException extends Exception {

    private static final long serialVersionUID = 6009400633383461286L;

    public GraphException() {
        super();
    }

    public GraphException(String message) {
        super(message);
    }

    public GraphException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphException(Throwable cause) {
        super(cause);
    }

}
