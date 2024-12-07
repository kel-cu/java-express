package express.http.request;

import com.sun.net.httpserver.Headers;
import express.http.Cookie;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
final class RequestUtils {

    private RequestUtils() {}

    /**
     * Extract the cookies from the 'Cookie' header.
     *
     * @param headers The Headers
     * @return An HashMap with the cookie name as key and the complete cookie as value.
     */
    static HashMap<String, Cookie> parseCookies(Headers headers) {
        HashMap<String, Cookie> cookieList = new HashMap<>();
        List<String> headerCookies = headers.get("Cookie");

        if (headerCookies == null || headerCookies.isEmpty()) {
            return cookieList;
        }

        char[] chars = headerCookies.get(0).toCharArray();
        StringBuilder key = new StringBuilder();
        StringBuilder val = new StringBuilder();
        boolean swap = false;

        for (char c : chars) {
            if (c == '=') {
                swap = true;
            } else if (c == ';') {
                String rkey = key.toString().trim();
                cookieList.put(rkey, new Cookie(rkey, val.toString()));

                key.setLength(0);
                val.setLength(0);
                swap = false;
            } else if (swap) {
                val.append(c);
            } else {
                key.append(c);
            }
        }

        if (key.length() > 0 && val.length() > 0) {
            String rkey = key.toString().trim();
            cookieList.put(rkey, new Cookie(rkey, val.toString()));
        }

        return cookieList;
    }

    /**
     * Method to extract the query's from an url.
     *
     * @param rawQuery The raw query
     * @return An list with key-values which are encoded in UTF8.
     */
    static HashMap<String, String> parseRawQuery(String rawQuery) {
        HashMap<String, String> queries = new HashMap<>();
        if(rawQuery == null) return queries;
        String[] values = rawQuery.split("&");
        for(String arg : values){
            if(!arg.isEmpty()) {
                String key = null;
                String value = null;
                if(arg.contains("=")){
                    String[] data = arg.split("=");
                    key = URLDecoder.decode(data[0], StandardCharsets.UTF_8);
                    value = (data[1] != null && !data[1].isBlank()) ? URLDecoder.decode(data[1], StandardCharsets.UTF_8) : null;
                } else key = arg;
                queries.put(key, value);
            }
        }
        return queries;
    }
}
