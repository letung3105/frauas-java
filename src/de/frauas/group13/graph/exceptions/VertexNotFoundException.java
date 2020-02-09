package de.frauas.group13.graph.exceptions;

/**
 * Exception when an operation is performed on an vertex that does not exist in the graph
 *
 * @author Alexander Orquera Barrera
 */
public class VertexNotFoundException extends GraphException {

    private static final long serialVersionUID = -7739676880094649814L;

    public VertexNotFoundException() {
        super();
    }

    public VertexNotFoundException(String message) {
        super(message);
    }

    public VertexNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public VertexNotFoundException(Throwable cause) {
        super(cause);
    }

}