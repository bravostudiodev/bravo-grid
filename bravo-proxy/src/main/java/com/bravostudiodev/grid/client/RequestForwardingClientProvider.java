package com.bravostudiodev.grid.client;

/**
 * @author IgorV
 *         Date: 13.2.2017
 */
public class RequestForwardingClientProvider {
    public RequestForwardingClient provide(String host, int port) {
        return new RequestForwardingClient(host, port);
    }
}
