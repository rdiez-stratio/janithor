package com.stratio.mesos.api;

import com.stratio.mesos.auth.SSOTokenResolver;
import com.stratio.mesos.http.HTTPUtils;
import com.stratio.mesos.http.MarathonInterface;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

/**
 * Created by alonso on 29/06/17.
 */
public class MarathonApi {
    private static final Logger LOG = LoggerFactory.getLogger(MarathonApi.class);

    private MarathonInterface marathonInterface;

    public MarathonApi(String mesosMasterUrl) {
        this.marathonInterface = HTTPUtils.buildBasicInterface(mesosMasterUrl, MarathonInterface.class);
    }

    public MarathonApi(String accessToken, String mesosMasterUrl) {
        this.marathonInterface = HTTPUtils.buildTokenBasedInterface(accessToken, mesosMasterUrl, MarathonInterface.class);
    }

    public MarathonApi(String principal, String secret, String mesosMasterUrl) {
        this.marathonInterface = HTTPUtils.buildSecretBasedInterface(principal, secret, mesosMasterUrl, MarathonInterface.class);
    }

    public boolean destroy(String serviceName) {
        Call<ResponseBody> mesosCall;
        try {
            mesosCall = marathonInterface.destroy(serviceName);
            Response<ResponseBody> response = mesosCall.execute();
            return (response.code() == HTTPUtils.HTTP_OK_CODE);
        } catch (IOException e) {
            LOG.info("Marathon failure with message " + e.getMessage());
            return false;
        }
    }

    public static String obtainToken(String user, String password, String ssoUrl) {
        SSOTokenResolver authenticator = new SSOTokenResolver(
                ssoUrl,
                user,
                password
        );
        boolean authenticated = authenticator.authenticate();

        if (authenticated) {
            String token = authenticator.getToken();
            System.out.println("Token is " + token);
            return token;
        } else {
            return null;
        }
    }
}
