package de.hybris.platform.acceleratorstorefrontcommons.forms.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.solgroup.core.constants.SolgroupCoreConstants;


public class OrderDateFormValidator implements Validator {

	private static final String INVALID_ORDER_DATE_ERROR_KEY = "cart.ordedate.notvalid";
	
	
	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		if(target instanceof String && StringUtils.isEmpty(target.toString())) {
			return;
		}
		
		String orderDate = target.toString();
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA_FORMAT);
		try {
			simpleDateFormat.parse(orderDate);
		} catch (ParseException e) {
			errors.reject(SolgroupCoreConstants.ORDERENTRY_ORDER_DATA, INVALID_ORDER_DATE_ERROR_KEY);
		}

	}

}
