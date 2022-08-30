package com.solgroup.core.service.wamintegration;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;


@Component
public class WamIntegrationErrorHandler extends DefaultResponseErrorHandler {

    private static final Logger LOG = LogManager.getLogger(WamIntegrationErrorHandler.class);

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        LOG.error("Wam http call response error code: "
                + response.getStatusCode().toString() + " and status message: " + response.getStatusText());
    }
}
