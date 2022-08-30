package com.solgroup.service.utils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.solgroup.constants.SolgroupwebservicesConstants;
import com.solgroup.core.ws.services.stock.StocksResponse;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.servicelayer.config.ConfigurationService;

public class SolgroupWebServiceUtils {

    private static final Logger LOG = Logger.getLogger(SolgroupWebServiceUtils.class);

//    private static final String PO_USR = "app.core.services.po.usr";
//    private static final String PO_PWD = "app.core.services.po.pwd";
//
//    private static final String PO_PROXY_HOST = "app.core.services.po.proxy.host";
//    private static final String PO_PROXY_PORT = "app.core.services.po.proxy.port";
//
//    private static final String PO_TIMEOUT_SOAP = "app.core.services.po.soap.timeout";

//    public static final String[] ConsignmentState = { "ACCEPTED", "PLANNED", "SHIPPED", "DELIVERED", "CANCELLED",
//            "DELETED" };

    //public static final String[] ConsignmentStateFinalArray = { "DELIVERED", "CANCELLED", "DELETED" };

//    public static final String[] OrderStateFinalArray = { "DELIVERED", "CANCELLED" };

    //public static final String[] ConsignmentStateNotFinalArray = { "ACCEPTED", "PLANNED", "SHIPPED" };

//    public enum ConsignmentStateFinal {
//        DELIVERED, CANCELLED, DELETED
//    }

//    public enum ConsignmentStateNotFinal {
//        ACCEPTED, PLANNED, SHIPPED
//    }

//    public enum subOrderProcessStatus {
//        BACKOFFICE_UPDATE, LEGACY_UPDATE, ORDER_FREEZED
//    }

//    public static final String[] OrderState = { "CREATED", "IN_PROGRESS", "PARTIALLY_DELIVERED", "DELIVERED",
//            "CANCELLED" };
//
//    public static final String[] OrderStateFinal = { "DELIVERED", "CANCELLED" };

    /**
     * This method filter a collection and return the first result founded
     *
     * @param col
     * @param filter
     * @param <T>
     * @return
     */
    public static <T> T findByProperty(Collection<T> col, Predicate<T> filter) {
        return col.stream().filter(filter).findFirst().orElse(null);
    }

    /**
     * This method filter a collection and return a list of type founded
     *
     * @param col
     * @param filter
     * @param <T>
     * @return
     */
    public static <T> List<T> findAnyByProperty(List<T> col, Predicate<T> filter) {
        return col.stream().filter(filter).collect(Collectors.toCollection(ArrayList::new));
    }

    public static <E extends Enum<E>> boolean contains(Class<E> _enumClass, String value) {
        try {
            return EnumSet.allOf(_enumClass).contains(Enum.valueOf(_enumClass, value));
        } catch (Exception e) {
            return false;
        }
    }

    public static void logXmlData(Object data, Logger log) {
        try {
            String callIdPrefix =
                    "REMOTE_SYSTEM_CALL_" + data.getClass().getSimpleName() + "_" + System.currentTimeMillis();
            log.info(callIdPrefix + " :" + getStringFromXml(data));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static String getStringFromXml(Object obj) throws JAXBException {

        final JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
        final Marshaller m = jaxbContext.createMarshaller();

        JAXBElement jaxbElement = new JAXBElement(new QName(obj.getClass().getSimpleName()), obj.getClass(), obj);
        StringWriter writer = new StringWriter();

        m.marshal(jaxbElement, writer);
        return writer.toString();

    }

    public static void configureClient(Client client) {
        if (client != null) {
            final HTTPConduit conduit = (HTTPConduit) client.getConduit();
            final HTTPClientPolicy policy = new HTTPClientPolicy();

            final String proxyHost = getConfigurationService().getConfiguration().getString(SolgroupwebservicesConstants.PROPERTY_NAME_PO_PROXY_HOST);
            final String proxyPort = getConfigurationService().getConfiguration().getString(SolgroupwebservicesConstants.PROPERTY_NAME_PO_PROXY_PORT);
            final long timeout = getConfigurationService().getConfiguration().getLong(SolgroupwebservicesConstants.PROPERTY_NAME_PO_TIMEOUT_SOAP);

            policy.setReceiveTimeout(timeout);
            policy.setConnectionTimeout(timeout);
            // policy.setAutoRedirect(true);

            if (!StringUtils.isBlank(proxyHost) && !StringUtils.isBlank(proxyPort)) {
                try {
                    policy.setProxyServerPort(Integer.valueOf(proxyPort).intValue());
                    policy.setProxyServer(proxyHost);
                } catch (Exception e) {
                    LOG.error("Error reading proxy configuration", e);
                }
            }
            conduit.setClient(policy);
        }
    }

    public static Object createSoapClient(String url, Class classType) {

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(classType);
        factory.setAddress(url);
        factory.setUsername(getAuthUsr());
        factory.setPassword(getAuthPwd());

        return factory.create();
    }

//    public static String generateSolgroupProductCode(final CMSSiteModel cmsSite, final String erpCode) {
//        String productHybrisCode = cmsSite.getUid() + "_" + erpCode;
//        return productHybrisCode;
//    }

    private static String getAuthPwd() {
        return getConfigurationService().getConfiguration().getString(SolgroupwebservicesConstants.PROPERTY_NAME_PO_PWD);
    }

    private static String getAuthUsr() {
        return getConfigurationService().getConfiguration().getString(SolgroupwebservicesConstants.PROPERTY_NAME_PO_USR);
    }

    private static ConfigurationService getConfigurationService() {
        ConfigurationService configurationService = (ConfigurationService) Registry.getApplicationContext()
                .getBean("configurationService");
        return configurationService;
    }

}
