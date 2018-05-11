package com.demo.filter;

import javax.servlet.*;
import java.io.IOException;

public class CharacterFilter implements Filter {

    String encodeRequest;
    String encodeResponse;

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        req.setCharacterEncoding(encodeRequest);
        resp.setCharacterEncoding(encodeResponse);
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {
        encodeRequest = config.getInitParameter("encodeRequest");
        encodeResponse = config.getInitParameter("encodeResponse");
    }

}
