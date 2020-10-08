package org.gluu.oxauth.audit.debug.wrapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by eugeniuparvan on 5/10/17.
 */
public class RequestWrapper extends HttpServletRequestWrapper {

    private byte[] content;

    private final Map<String, String[]> parameterMap;

    private final HttpServletRequest delegate;


    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     * @throws IllegalArgumentException if the request is null
     */
    public RequestWrapper(HttpServletRequest request) {
        super(request);
        this.delegate = request;
        if (isFormPost()) {
            this.parameterMap = request.getParameterMap() != null ? new HashMap<String, String[]>(request.getParameterMap()) : Collections.<String, String[]>emptyMap();
        } else {
            this.parameterMap = Collections.emptyMap();
        }
    }

    public Map<String, String[]> getParams() {
        if (ArrayUtils.isEmpty(content) || this.parameterMap.isEmpty()) {
            return delegate.getParameterMap();
        }
        return this.parameterMap;
    }

    public String getContent() {
        try {
            if (this.parameterMap.isEmpty()) {
                if (ArrayUtils.isEmpty(content))
                    content = IOUtils.toByteArray(delegate.getInputStream());
                else
                    content = IOUtils.toByteArray(new LoggingServletInputStream(content));
            } else {
                content = getContentFromParameterMap(this.parameterMap);
            }
            String requestEncoding = delegate.getCharacterEncoding();
            String normalizedContent = StringUtils.normalizeSpace(new String(content, requestEncoding != null ? requestEncoding : StandardCharsets.UTF_8.name()));
            return StringUtils.isBlank(normalizedContent) ? null : normalizedContent;
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }


    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<String, String>(0);
        Enumeration<String> headerNames = getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName != null) {
                headers.put(headerName, getHeader(headerName));
            }
        }
        return headers;
    }

    public Map<String, String> getParameters() {
        Map<String, String> params = new HashMap<String, String>();
        for (Map.Entry<String, String[]> entry : getParams().entrySet()) {
            String[] values = entry.getValue();
            params.put(entry.getKey(), values.length > 0 ? values[0] : null);
        }
        return params;
    }

    public boolean isFormPost() {
        String contentType = getContentType();
        return (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED) && HttpMethod.POST.equalsIgnoreCase(getMethod()));
    }

    private byte[] getContentFromParameterMap(Map<String, String[]> parameterMap) {
        StringBuilder sb = new StringBuilder();
        String ampersand = "&";
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] value = entry.getValue();
            sb.append(entry.getKey() + "=" + (value.length == 1 ? value[0] : Arrays.toString(value)) + ampersand);
        }
        String params = sb.toString();
        return params.substring(0, params.length() - 1).getBytes();
    }

    private class LoggingServletInputStream extends ServletInputStream {

        private final InputStream is;

        private LoggingServletInputStream(byte[] content) {
            this.is = new ByteArrayInputStream(content);
        }

        @Override
        public boolean isFinished() {
            return true;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            return this.is.read();
        }

        @Override
        public void close() throws IOException {
            super.close();
            is.close();
        }
    }
}
