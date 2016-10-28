/*
 * Copyright (c) 2013-2016.  Urban Airship and Contributors
 */

package com.urbanairship.api.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.io.BaseEncoding;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.filter.FilterContext;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * The UrbanAirshipClient class handles HTTP requests to the Urban Airship API.
 */
public class UrbanAirshipClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(UrbanAirshipClient.class);

    private final String appKey;
    private final String appSecret;
    private final URI baseUri;
    private final AsyncHttpClient client;

    private UrbanAirshipClient(Builder builder) {
        this.appKey = builder.key;
        this.appSecret = builder.secret;
        this.baseUri = URI.create(builder.baseUri);

        AsyncHttpClientConfig.Builder clientConfigBuilder = builder.clientConfigBuilder;
        clientConfigBuilder.setUserAgent(getUserAgent());
        clientConfigBuilder.addResponseFilter(new RequestRetryFilter(builder.maxRetries, Optional.fromNullable(builder.retryPredicate)));

        Optional<ProxyServer> proxyServer = convertProxyInfo(Optional.fromNullable(builder.proxyInfo));
        if (proxyServer.isPresent()) {
            clientConfigBuilder.setProxyServer(proxyServer.get());
        }

        this.client = new AsyncHttpClient(clientConfigBuilder.build());
    }

    /**
     * UrbanAirshipClient builder
     * @return Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Get the app key.
     *
     * @return The app key.
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * Get the app secret.
     *
     * @return The app secret.
     */
    public String getAppSecret() {
        return appSecret;
    }

    /**
     * Get the base URI.
     *
     * @return The base URI.
     */
    public URI getBaseUri() {
        return baseUri;
    }

    /**
     * Get the underlying HTTP client.
     *
     * @return The HTTP client.
     */
    public AsyncHttpClient getClient() {
        return client;
    }

    /**
     * Command for executing Urban Airship requests asynchronously with a ResponseCallback.
     *
     * @param request An Urban Airship request object.
     * @param callback A ResponseCallback instance.
     * @return A client response future.
     */
    public <T> Future<Response> executeAsync(final Request<T> request, final ResponseCallback callback) throws IOException {
        AsyncHttpClient.BoundRequestBuilder requestBuilder;
        String uri;

        try {
            uri = request.getUri(baseUri).toString();
        } catch (URISyntaxException e) {
            log.error("Failed to generate a request URI from base URI " + baseUri.toString(), e);
            throw new RuntimeException(e);
        }

        switch (request.getHttpMethod()) {
            case GET:
                requestBuilder = client.prepareGet(uri);
                break;
            case PUT:
                requestBuilder = client.preparePut(uri);
                break;
            case POST:
                requestBuilder = client.preparePost(uri);
                break;
            case DELETE:
                requestBuilder = client.prepareDelete(uri);
                break;
            default:
                requestBuilder = client.prepareGet(uri);
                break;
        }

        // Headers
        Map<String, String> requestHeaders = request.getRequestHeaders();
        if (requestHeaders != null) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        // Auth
        requestBuilder.setHeader(
            "Authorization",
            "Basic " + BaseEncoding.base64().encode((appKey + ":" + appSecret).getBytes())
        );


        // Body
        String body = request.getRequestBody();
        ContentType contentType = request.getContentType();
        if (body != null && contentType != null) {
            requestBuilder.setBody(body.getBytes(contentType.getCharset()));
        }

        log.debug(String.format("Executing Urban Airship request to %s with body %s.", uri, request.getRequestBody()));
        ResponseAsyncHandler<T> handler = new ResponseAsyncHandler<>(Optional.fromNullable(callback), request.getResponseParser());
        return requestBuilder.execute(handler);
    }

    /**
     * Command for executing Urban Airship requests asynchronously without a ResponseCallback.
     *
     * @param request An Urban Airship request object.
     * @return A client response future.
     * @throws IOException
     */
    public <T> Future<Response> executeAsync(final Request<T> request) throws IOException {
        return executeAsync(request, null);
    }

    /**
     * Command for executing Urban Airship requests synchronously with a ResponseCallback.
     *
     * @param request An Urban Airship request object.
     * @param callback A ResponseCallback instance.
     * @throws IOException
     */
    public <T> Response execute(Request<T> request, ResponseCallback callback) throws IOException {
        try {
            return executeAsync(request, callback).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while retrieving response from future", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to retrieve response from future", e);
        }
    }

    /**
     * Command for executing Urban Airship requests synchronously without a ResponseCallback.
     *
     * @param request An Urban Airship request object.
     * @return A client response.
     */
    public <T> Response execute(Request<T> request) throws IOException {
        return execute(request, null);
    }

    /**
     * Close the underlying HTTP client's thread pool.
     */
    @Override
    public void close() {
        log.info("Closing client");
        client.close();
    }

    /**
     * Retrieve the client user agent.
     *
     * @return The user agent.
     */
    @VisibleForTesting
    public String getUserAgent() {
        String userAgent = "UNKNOWN";
        InputStream stream = getClass().getResourceAsStream("/client.properties");

        if (stream != null) {
            Properties props = new Properties();
            try {
                props.load(stream);
                stream.close();
                userAgent = "UAJavaLib/" + props.get("client.version");
            } catch (IOException e) {
                log.error("Failed to retrieve client user agent due to IOException - setting to \"UNKNOWN\"", e);
            }
        }
        return userAgent;
    }

    /**
     * Convert the ProxyInfo wrapper into a ProxyServer instance.
     *
     * @param proxyInfo An optional ProxyInfo instance.
     * @return An optional ProxyServer instance.
     */
    private Optional<ProxyServer> convertProxyInfo(Optional<ProxyInfo> proxyInfo) {
        if (proxyInfo.isPresent()) {
            ProxyServer.Protocol protocol = ProxyServer.Protocol.HTTPS;
            for (ProxyServer.Protocol proto : ProxyServer.Protocol.values()) {
                if (proxyInfo.get().getProtocol().equals(proto.getProtocol())) {
                    protocol = proto;
                }
            }

            ProxyServer proxyServer = new ProxyServer(
                protocol,
                proxyInfo.get().getHost(),
                proxyInfo.get().getPort(),
                proxyInfo.get().getPrincipal(),
                proxyInfo.get().getPassword()
            );
            return Optional.of(proxyServer);
        }
        return Optional.absent();
    }


    /* Object methods */

    @Override
    public String toString() {
        return "UrbanAirshipClient{" +
            "appKey='" + appKey + '\'' +
            ", appSecret='" + appSecret + '\'' +
            ", baseUri=" + baseUri +
            ", client=" + client +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UrbanAirshipClient)) return false;

        UrbanAirshipClient that = (UrbanAirshipClient) o;

        if (appKey != null ? !appKey.equals(that.appKey) : that.appKey != null) return false;
        if (appSecret != null ? !appSecret.equals(that.appSecret) : that.appSecret != null) return false;
        if (baseUri != null ? !baseUri.equals(that.baseUri) : that.baseUri != null) return false;
        if (client != null ? !client.equals(that.client) : that.client != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appKey != null ? appKey.hashCode() : 0;
        result = 31 * result + (appSecret != null ? appSecret.hashCode() : 0);
        result = 31 * result + (baseUri != null ? baseUri.hashCode() : 0);
        result = 31 * result + (client != null ? client.hashCode() : 0);
        return result;
    }

    /* Builder for newAPIClient */

    public static class Builder {

        private String key;
        private String secret;
        private String baseUri;
        private Integer maxRetries = 10;
        private AsyncHttpClientConfig.Builder clientConfigBuilder = new AsyncHttpClientConfig.Builder();
        private ProxyInfo proxyInfo = null;
        private Predicate<FilterContext> retryPredicate = null;

        private Builder() {
            baseUri = "https://go.urbanairship.com";
        }

        /**
         * Set the app key.
         * @param key String app key
         * @return Builder
         */
        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        /**
         * Set the app secret.
         * @param appSecret String app secret
         * @return Builder
         */
        public Builder setSecret(String appSecret) {
            this.secret = appSecret;
            return this;
        }

        /**
         * Set the base URI -- defaults to "https://go.urbanairship.com"
         * @param URI String base URI
         * @return Builder
         */
        public Builder setBaseUri(String URI) {
            this.baseUri = URI;
            return this;
        }

        /**
         * Set the maximum for non-POST request retries on 5xxs -- defaults to 10.
         *
         * @param maxRetries The maximum.
         * @return Builder
         */
        public Builder setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Set the client config builder -- defaults to a new builder. Available for custom settings.
         *
         * @param builder The client config builder.
         * @return Builder
         */
        public Builder setClientConfigBuilder(AsyncHttpClientConfig.Builder builder) {
            this.clientConfigBuilder = builder;
            return this;
        }

        /**
         * Set the proxy info.
         *
         * @param proxyInfo The proxy info.
         * @return Builder
         */
        public Builder setProxyInfo(ProxyInfo proxyInfo) {
            this.proxyInfo = proxyInfo;
            return this;
        }

        /**
         * Set an optional predicate for allowing request retries on 5xxs.
         *
         * @param retryPredicate The retry predicate.
         * @return Builder
         */
        public Builder setRetryPredicate(Predicate<FilterContext> retryPredicate) {
            this.retryPredicate = retryPredicate;
            return this;
        }


        /**
         * Build an UrbanAirshipClient object.  Will fail if any of the following
         * preconditions are not met.
         * <pre>
         * 1. App key must be set.
         * 2. App secret must be set.
         * 3. The base URI has been overridden but not set.
         * 4. Max for non-POST 5xx retries must be set, already defaults to 10.
         * 5. HTTP client config builder must be set, already defaults to a new builder.
         * </pre>
         *
         * @return UrbanAirshipClient
         */
        public UrbanAirshipClient build() {
            Preconditions.checkNotNull(key, "app key needed to build APIClient");
            Preconditions.checkNotNull(secret, "app secret needed to build APIClient");
            Preconditions.checkNotNull(baseUri, "base URI needed to build APIClient");
            Preconditions.checkNotNull(maxRetries, "max non-POST retries needed to build APIClient");
            Preconditions.checkNotNull(clientConfigBuilder, "Async HTTP client config builder needed to build APIClient");

            return new UrbanAirshipClient(this);
        }
    }
}
