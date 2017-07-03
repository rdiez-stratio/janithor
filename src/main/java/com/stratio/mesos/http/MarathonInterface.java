package com.stratio.mesos.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Created by jmartinez on 2/02/17.
 */

public interface MarathonInterface {

    @DELETE("v2/apps/{serviceName}")
    Call<ResponseBody> destroy(@Path("serviceName") String serviceName);

    @DELETE("v2/apps/{serviceName}")
    Call<ResponseBody> destroy(@Header("Cookie") String dcosCookie, @Path("serviceName") String serviceName);

}
