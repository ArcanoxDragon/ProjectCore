package com.gmail.trentech.pjc.utils;

public class InvalidItemTypeException extends Exception {

	private static final long serialVersionUID = 1997753363232807009L;

	public InvalidItemTypeException() {
		super("Not a valid ItemType");
	}

	public InvalidItemTypeException(String message) {
		super(message);
	}
}
