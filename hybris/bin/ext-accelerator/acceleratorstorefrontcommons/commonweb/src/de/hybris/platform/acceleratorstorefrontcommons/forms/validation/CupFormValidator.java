package de.hybris.platform.acceleratorstorefrontcommons.forms.validation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.solgroup.core.constants.SolgroupCoreConstants;


public class CupFormValidator implements Validator {

	private static final String INVALID_CUP_ERROR_KEY = "cart.cup.notvalid";
	
	
	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		if(target instanceof String && StringUtils.isEmpty(target.toString())) {
			return;
		}
		
		String cup = target.toString();
		
		boolean isAlphaNumeric = StringUtils.isAlphanumeric(cup);
		boolean rightLength = StringUtils.length(cup) == SolgroupCoreConstants.CUP_LENGTH;
		
		if(!isAlphaNumeric || !rightLength) {
			errors.reject(SolgroupCoreConstants.ORDERENTRY_CUP, INVALID_CUP_ERROR_KEY);
		}
		
	}

}
