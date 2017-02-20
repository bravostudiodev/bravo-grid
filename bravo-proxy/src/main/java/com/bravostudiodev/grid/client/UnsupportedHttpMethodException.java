package com.bravostudiodev.grid.client;

/**
 * @author IgorV
 *         Date: 13.2.2017
 */
public class UnsupportedHttpMethodException extends RuntimeException {
    public UnsupportedHttpMethodException(String method) {
        super(String.format("Method %s is not supported", method));
    }
}
