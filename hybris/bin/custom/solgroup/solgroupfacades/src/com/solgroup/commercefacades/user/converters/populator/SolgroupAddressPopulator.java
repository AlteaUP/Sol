package com.solgroup.commercefacades.user.converters.populator;

import org.apache.commons.lang.StringUtils;

import de.hybris.platform.commercefacades.user.converters.populator.AddressPopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;

public class SolgroupAddressPopulator extends AddressPopulator {

    private final String NULL = "null";

    @Override
    protected void populateAddressFields(final AddressModel source, final AddressData target) {
        if (source.getTitle() != null) {
            target.setTitle(NULL.equals(source.getTitle().getName()) ? StringUtils.EMPTY : source.getTitle().getName());
            target.setTitleCode(
                    NULL.equals(source.getTitle().getCode()) ? StringUtils.EMPTY : source.getTitle().getCode());
        }
        target.setFirstName(NULL.equals(source.getFirstname()) ? StringUtils.EMPTY : source.getFirstname());
        target.setLastName(NULL.equals(source.getLastname()) ? StringUtils.EMPTY : source.getLastname());
        target.setCompanyName(NULL.equals(source.getCompany()) ? StringUtils.EMPTY : source.getCompany());
        target.setLine1(NULL.equals(source.getLine1()) ? StringUtils.EMPTY : source.getLine1());
        target.setLine2(NULL.equals(source.getLine2()) ? StringUtils.EMPTY : source.getLine2());
        target.setTown(NULL.equals(source.getTown()) ? StringUtils.EMPTY : source.getTown());
        target.setPostalCode(NULL.equals(source.getPostalcode()) ? StringUtils.EMPTY : source.getPostalcode());
        target.setPhone(NULL.equals(source.getPhone1()) ? StringUtils.EMPTY : source.getPhone1());
        target.setEmail(NULL.equals(source.getEmail()) ? StringUtils.EMPTY : source.getEmail());
        target.setFax(NULL.equals(source.getFax()) ? StringUtils.EMPTY : source.getFax());

    }

}
