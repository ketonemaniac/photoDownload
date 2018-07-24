package net.ketone.photodownload;

import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

@Service
public class CookieStore implements ClientRequestFilter {

    private ArrayList<Object> cookies;

    @PostConstruct
    public void authenticate() {

        cookies = new ArrayList<>();

        Client firstTouch = ClientBuilder.newClient();
        Response firstResp = firstTouch.target("http://cffc.dyndns.org:8008/piwigo/")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE)
                .request()
                .get();
        Map<String, NewCookie> cr = firstResp.getCookies();
        System.out.println("Response=" + firstResp.getStatus() + " Cookies=" + cr.size());
        for (NewCookie cookie : cr.values()) {
            System.out.println("cookie=" + cookie.toString());
            cookies.add(cookie.toCookie());
        }

    }

    public void filter(ClientRequestContext requestContext) throws IOException {
        if (cookies != null) {
            requestContext.getHeaders().put("Cookie", cookies);
        }
    }
}