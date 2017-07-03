package com.stratio.mesos.auth;

import okhttp3.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shernandez on 10/03/17.
 */
public class RedirectionInterceptor implements Interceptor {
    Logger LOG = LoggerFactory.getLogger(RedirectionInterceptor.class);

    List<String> locationHistory = new ArrayList<>();

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        locationHistory.add(chain.request().url().toString());

        return chain.proceed(chain.request());
    }

    public void clearLocationHistory(){
        locationHistory.clear();
    }

    public List<String> getLocationHistory(){
        return locationHistory;
    }
}
