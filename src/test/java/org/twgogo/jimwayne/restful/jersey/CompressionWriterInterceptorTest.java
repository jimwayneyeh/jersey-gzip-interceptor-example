package org.twgogo.jimwayne.restful.jersey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.twgogo.jimwayne.restful.MyResource;
import org.twgogo.jimwayne.restful.jersey.CompressionWriterInterceptor;

public class CompressionWriterInterceptorTest extends JerseyTest {
  @Override
  protected Application configure() {
      return new ResourceConfig(
          CompressionWriterInterceptor.class,
          MyResource.class);
  }
  
  @Test
  public void testGzipEncoding () throws IOException {
    Response response = target("myresource").request().acceptEncoding("gzip").get(Response.class);
    
    // Validate the basic information from the response.
    Assert.assertEquals(200, response.getStatus());
    Assert.assertTrue(response.getHeaderString(HttpHeaders.CONTENT_ENCODING).contains("gzip"));
    
    // Validate the content of the response.
    try (InputStream stream = (InputStream) response.getEntity()) {
      byte[] responseBody = IOUtils.toByteArray(stream);
      // The result should not be the same since the response should be compressed by gzip.
      Assert.assertNotEquals("Got it!", new String(responseBody));
      // The result should be the same after we de-compress the response body.
      Assert.assertEquals("Got it!", new String(decompress(responseBody)));
    }
  }
  
  private byte[] decompress (byte[] compressedBytes) {
    if (compressedBytes == null || compressedBytes.length == 0) {
        return new byte[0];
    }
    
    try (ByteArrayInputStream in = new ByteArrayInputStream(compressedBytes);
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        try (GZIPInputStream gunzip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[256];
            int n;
            while ((n = gunzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        }
        
        out.flush();
        return out.toByteArray();
    } catch (IOException e) {
        return compressedBytes;
    }
  }
}
