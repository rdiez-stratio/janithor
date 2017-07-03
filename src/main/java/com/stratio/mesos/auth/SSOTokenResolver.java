package com.stratio.mesos.auth;

import com.stratio.mesos.http.HTTPUtils;
import com.stratio.mesos.http.CookieInterceptor;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SSOTokenResolver {
    private Logger log = LoggerFactory.getLogger(SSOTokenResolver.class);

    private OkHttpClient.Builder clientBuilder;
    private OkHttpClient clientHttp;

    private String baseUrl;
    private String marathonUser;
    private String marathonSecret;
    private String token;

    private RedirectionInterceptor redirectionInterceptor;
    private CookieInterceptor cookieInterceptor;

    public SSOTokenResolver(String baseUrl, String user, String password) {
        this.baseUrl = baseUrl;
        this.marathonUser = user;
        this.marathonSecret = password;

        this.redirectionInterceptor = new RedirectionInterceptor();
        this.cookieInterceptor = new CookieInterceptor();
        this.clientBuilder = new OkHttpClient.Builder();
        this.clientBuilder = HTTPUtils.getUnsafeOkHttpClient();
        this.clientBuilder.addNetworkInterceptor(this.redirectionInterceptor);
        this.clientBuilder.addNetworkInterceptor(this.cookieInterceptor);
        this.clientHttp = clientBuilder.build();
    }

    /**
     * Performs authentication and returns success or failure
     * @return true if token could be obtained, false otherwise
     */
    public boolean authenticate() {
        String[] bodyParams;

        log.info("1. GO TO " + baseUrl);
        Response response1_1 = getRequest(baseUrl);
        log.info(response1_1.toString());

        String callBackLocation = redirectionInterceptor.getLocationHistory().get(1);
        redirectionInterceptor.clearLocationHistory();

        log.info("2. REDIRECT TO : " + callBackLocation);
        Response response1_2 = getRequest(callBackLocation);
        log.info(response1_2.toString());

        String JSESSIONIDCookie = cookieInterceptor.getCookies().get(0);
        cookieInterceptor.clearCookies();

        log.info("2. COOKIE : " + JSESSIONIDCookie);

        String callBackLocation2 = redirectionInterceptor.getLocationHistory().get(1);
        redirectionInterceptor.clearLocationHistory();

        log.info("3. REDIRECT TO : " + callBackLocation2 + " with JSESSIONID");
        Response response1_3 = getRequest(callBackLocation2, JSESSIONIDCookie);
        log.info(response1_3.toString());

        try {
            bodyParams = parseBodyParams(response1_3.body().string());
        } catch (IOException e) {
            log.error("Unable to parse required body params to perform the authentication. Aborting", e);
            return false;
        }

        String lastRedirection = callBackLocation2;
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("lt", bodyParams[0]) // lt
                    .add("_eventId", bodyParams[1]) // event
                    .add("execution", bodyParams[2]) // execution
                    .add("submit", "LOGIN")
                    .add("username", this.marathonUser)
                    .add("password", this.marathonSecret)
                    .build();

            Request request = new Request.Builder()
                    .url(lastRedirection)
                    .addHeader("Cookie", JSESSIONIDCookie)
                    .post(formBody)
                    .build();
            Response response2_1 = clientHttp.newCall(request).execute();
            log.info(response2_1.toString());
        } catch (IOException e) {
            log.error("Unable to perform login. Aborting", e);
        }

        String CASPRIVACY =  cookieInterceptor.getCookies().get(0);
        String TGC = cookieInterceptor.getCookies().get(1);
        token = cookieInterceptor.getCookies().get(2);

        log.info("JSESSIONID: " + JSESSIONIDCookie);
        log.info("CASPRIVACY: " + CASPRIVACY);
        log.info("TGC: " + TGC);
        log.info("Oauth Token obtained: " + token);

        return true;
    }

    /**
     * Returns the dc/os token as obtained from the cookie (TGC=ysc12874bbklhjs...;cookie=)
     * @return
     */
    public String getRawToken() {
        return token;
    }

    /**
     * Returns a ready to use dc/os token
     * @return
     */
    public String getToken() {
        return getRawToken().split(";")[0].substring(4);
    }

    /**
     * Extracts HTML parameters lt, execution and event required to perform SSO request
     * @param htmlDocument callback location content after sso redirection
     * @return {lt, execution, event}
     */
    private String[] parseBodyParams(String htmlDocument) {
        String[] bodyParams = new String[3]; // lt, execution, event
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlDocument);
        Elements elements = doc.select("input[type=hidden]");
        for (Element element : elements){
            if (element.attr("name").equals("lt"))
                bodyParams[0] = element.attr("value");
            else if (element.attr("name").equals("execution"))
                bodyParams[1] = element.attr("value");
            else if (element.attr("name").equals("_eventId"))
                bodyParams[2] = element.attr("value");

        }
        return bodyParams;
    }

    /**
     * HTTP request execution based on url
     * @param url url to go to
     * @return Server response or null if error
     */
    private Response getRequest(String url) {
        return getRequest(url, null);
    }

    /**
     * HTTP request execution based on url
     * @param url url to go to
     * @param cookie to travel as a header
     * @return Server response or null if error
     */
    private Response getRequest(String url, String cookie) {
        Request request;
        try {
            if (cookie!=null) {
                request = new Request.Builder().addHeader("Cookie", cookie).url(url).build();
            } else {
                request = new Request.Builder().url(url).build();
            }
            return clientHttp.newCall(request).execute();
        } catch (IOException e) {
            log.error("Unable to get request for url " + url + " and cookie " + cookie, e);
            return null;
        }
    }
}



