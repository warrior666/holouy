package org.holouy.skp.onto.exception;

public class ModelException extends Exception {
	private static final long serialVersionUID = 1L;

	public ModelException(String message) {
		super(message);
	}

	public ModelException(String format, Object... arguments) {
		super(String.format(format, arguments));
	}
}
