package express.middleware;

import express.filter.Filter;
import express.http.HttpRequestHandler;
import express.http.request.Request;
import express.http.response.Response;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpProxy implements HttpRequestHandler, Filter {
    public final String context;
    public final String address;
    public HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public HttpProxy(String context, String address) {
        this.context = context;
        this.address = address;
    }

    @Override
    public void handle(Request request, Response response) {
        StringBuilder path = new StringBuilder(request.getPath());
        if (path.toString().startsWith(context)) {
            try {
                if (path.indexOf(context) == 0) path = new StringBuilder(path.substring(context.length()));
                boolean isFirst = true;
                for(String key : request.getQueries().keySet()){
                    path.append(isFirst ? "?" : "&");
                    if(isFirst) isFirst = false;
                    path.append(key);
                    if(request.getQueries().get(key) != null) path.append("=").append(request.getQueries().get(key));
                }
                URI uri = new URI(address + path);
                HttpRequest.Builder builder = HttpRequest.newBuilder(uri);
                switch (request.getMethod()){
                    case "HEAD" -> builder.HEAD();
                    case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofInputStream(request::getBody));
                    case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofInputStream(request::getBody));
                    case "DELETE" -> builder.DELETE();
                    default -> builder.GET();
                }
                for(String key : request.getHeaders().keySet()){
                    for(String key2 : request.getHeaders().get(key)){
                        try {
                            builder.header(key, key2);
                        } catch (Exception ignored){}
                    }
                }

                HttpResponse<byte[]> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
                byte[] bytes = resp.body();
                for(String key : resp.headers().map().keySet()){
                    response.setHeader(key, resp.headers().map().get(key).getFirst());
                }
                response.setStatus(resp.statusCode());
                if(resp.headers().map().containsKey("Content-Type"))
                    response.sendBytes(bytes, resp.headers().firstValue("Content-Type").get());
                else response.sendBytes(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return "http-proxy";
    }
}
