/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parosproxy.paros.model;

import java.lang.ref.SoftReference;
import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

public class HttpMessageCachedData {

    private final String method;
    private final URI uri;
    private final String reason;
    private final int statusCode;
    private final int rtt;
    private final long timeSentMillis;
    private final int requestHeaderLength;
    private final int requestBodyLength;
    private final String requestContentType;
    private final int responseHeaderLength;
    private final int responseBodyLength;
    private boolean note;
    private SoftReference<String> srRequestBody;

    public HttpMessageCachedData(HttpMessage msg) {
        this.method = msg.getRequestHeader().getMethod();
        this.uri = msg.getRequestHeader().getURI();
        this.statusCode = msg.getResponseHeader().getStatusCode();
        this.reason = msg.getResponseHeader().getReasonPhrase();
        this.rtt = msg.getTimeElapsedMillis();
        this.note = msg.getNote() != null && msg.getNote().length() > 0;
        this.timeSentMillis = msg.getTimeSentMillis();
        this.requestHeaderLength = msg.getRequestHeader().toString().length();
        this.requestBodyLength = msg.getRequestBody().length();
        this.requestContentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        this.responseHeaderLength = msg.getResponseHeader().toString().length();
        this.responseBodyLength = msg.getResponseBody().length();
        this.srRequestBody = new SoftReference<>(msg.getRequestBody().toString());
    }

    public String getMethod() {
        return method;
    }

    public URI getUri() {
        return uri;
    }

    public String getReason() {
        return reason;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getRtt() {
        return rtt;
    }

    public boolean hasNote() {
        return note;
    }

    public void setNote(boolean note) {
        this.note = note;
    }

    public long getTimeSentMillis() {
        return timeSentMillis;
    }

    public long getTimeReceivedMillis() {
        return timeSentMillis + rtt;
    }

    public int getRequestHeaderLength() {
        return requestHeaderLength;
    }

    public int getRequestBodyLength() {
        return requestBodyLength;
    }

    public String getRequestBodyContentType() {
        return requestContentType;
    }

    public int getResponseHeaderLength() {
        return responseHeaderLength;
    }

    public int getResponseBodyLength() {
        return responseBodyLength;
    }

    public String getRequestBody() {
        return srRequestBody.get();
    }

    public void setRequestBody(String requestBody) {
        this.srRequestBody.clear();
        this.srRequestBody = new SoftReference<>(requestBody);
    }
}
