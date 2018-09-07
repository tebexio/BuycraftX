package net.buycraft.plugin.sponge.httplistener;

import com.google.common.base.Charsets;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;

public class Handler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = this.handleRequest(httpExchange);

        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.flush();
        os.close();
        httpExchange.close();
    }


    private String handleRequest(HttpExchange ex) {
        if (ex.getRequestURI().toString().equalsIgnoreCase("/ping")) {
            return "Connection Established";
        } else {
            try {
                String body = IOUtils.toString(ex.getRequestBody(), Charsets.UTF_8);
                String hash = ex.getRequestHeaders().get("X-Signature").get(0);


                System.out.println(hash);

                return hash;
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
    }
}
