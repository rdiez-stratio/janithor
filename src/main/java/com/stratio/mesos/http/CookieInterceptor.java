package com.stratio.mesos.http;

import okhttp3.Interceptor;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shernandez on 9/03/17.
 */
public class CookieInterceptor implements Interceptor {
    Logger LOG = LoggerFactory.getLogger(CookieInterceptor.class);

    private List<String> cookieHistory = new ArrayList<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        if (!response.headers("Set-Cookie").isEmpty()) {
            for (String header : response.headers("Set-Cookie")) {
                cookieHistory.add(header.toString());
            }
        }

        return response;
    }

    public List<String> getCookies(){
        return cookieHistory;
    }

    public void clearCookies(){
        cookieHistory.clear();
    }
}
