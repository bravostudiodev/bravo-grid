package com.bravostudiodev.grid.client;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author IgorV
 *         Date: 13.2.2017
 */
public class HttpClientProvider {

    public CloseableHttpClient provide() {
        return HttpClients.createDefault();
    }
}
