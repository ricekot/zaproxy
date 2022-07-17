package org.zaproxy.zap.model;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

public class SiteNodeQuery {
    private URI uri;
    private String httpMethod = "";
    private String requestBody = "";
    private String contentTypeHeader = "";

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod != null ? httpMethod : "";
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody != null ? requestBody : "";
    }

    public String getContentTypeHeader() {
        return contentTypeHeader;
    }

    public void setContentTypeHeader(String contentTypeHeader) {
        this.contentTypeHeader = contentTypeHeader != null ? contentTypeHeader : "";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SiteNodeQuery fromHttpMessage(HttpMessage msg) {
        if (msg == null) {
            return null;
        }
        return builder()
                .httpMethod(msg.getRequestHeader().getMethod())
                .uri(msg.getRequestHeader().getURI())
                .contentTypeHeader(msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE))
                .requestBody(msg.getRequestBody().toString())
                .build();
    }

    public static class Builder {
        private URI uri;
        private String httpMethod;
        private String requestBody;
        private String contentTypeHeader;

        protected Builder() {
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder contentTypeHeader(String contentTypeHeader) {
            this.contentTypeHeader = contentTypeHeader;
            return this;
        }

        public SiteNodeQuery build() {
            SiteNodeQuery query = new SiteNodeQuery();
            query.setUri(uri);
            query.setHttpMethod(httpMethod);
            query.setRequestBody(requestBody);
            query.setContentTypeHeader(contentTypeHeader);
            return query;
        }
    }
}
