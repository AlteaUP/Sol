package de.hybris.platform.acceleratorstorefrontcommons.forms.validation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.solgroup.core.constants.SolgroupCoreConstants;

public class PurchaseOrderNumberFormValidator implements Validator {

	private static final String INVALID_PO_ERROR_KEY = "cart.po.notvalid";
	
	
	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		if(target instanceof String && StringUtils.isEmpty(target.toString())) {
			return;
		}
		
		String purchaseOrderNumber = target.toString();
		
		boolean isAlphaNumeric = StringUtils.isAlphanumeric(purchaseOrderNumber);
		boolean rightLength = StringUtils.length(purchaseOrderNumber) <= SolgroupCoreConstants.PURCHASE_ORDER_NUMBER_MAX_LENGTH;
		
		if(!isAlphaNumeric || !rightLength) {
			errors.reject(SolgroupCoreConstants.ORDERENTRY_PURCHASE_ORDER_NUMER, INVALID_PO_ERROR_KEY);
		}
		
	}

}
