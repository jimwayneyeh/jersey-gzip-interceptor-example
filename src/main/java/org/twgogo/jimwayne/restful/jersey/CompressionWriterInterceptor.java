package org.twgogo.jimwayne.restful.jersey;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressionWriterInterceptor implements WriterInterceptor {
  private static final Logger log = LoggerFactory.getLogger(CompressionWriterInterceptor.class);
  private HttpHeaders httpHeaders;
  
  public CompressionWriterInterceptor (@Context @NotNull HttpHeaders httpHeaders) {
    this.httpHeaders = httpHeaders;
  }

  @Override
  public void aroundWriteTo (WriterInterceptorContext context)
      throws IOException, WebApplicationException {
    List<String> encodings = this.httpHeaders.getRequestHeader(HttpHeaders.ACCEPT_ENCODING);
    
    try {
      final OutputStream outputStream = context.getOutputStream();
      
      // Compress the response using 'gzip'.
      if(encodings.contains("gzip")) {
        log.trace("Compress the response using gzip.");
        context.getHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");
        context.setOutputStream(new GZIPOutputStream(outputStream));
      }
    } finally {
      log.trace("Proceed the response.");
      context.proceed();
    }
  }
}