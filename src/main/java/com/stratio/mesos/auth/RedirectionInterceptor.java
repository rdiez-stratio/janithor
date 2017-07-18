package com.stratio.mesos.auth;

import okhttp3.Interceptor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shernandez on 10/03/17.
 */
public class RedirectionInterceptor implements Interceptor {
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
