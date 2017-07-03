package com.stratio.mesos.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Path;

/**
 * Created by alonso on 23/06/17.
 */
public interface ExhibitorInterface {
    @DELETE("/exhibitor/v1/explorer/znode/{service}")
    Call<ResponseBody> delete(
            @Path("service") String service
    );
}
