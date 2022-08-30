package de.hybris.platform.acceleratorstorefrontcommons.forms.validation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.solgroup.core.constants.SolgroupCoreConstants;





public class CigFormValidator implements Validator {

	private static final String INVALID_CIG_ERROR_KEY = "cart.cig.notvalid";
	
	
	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		if(target instanceof String && StringUtils.isEmpty(target.toString())) {
			return;
		}
		
		String cig = target.toString();
		
		boolean isAlphaNumeric = StringUtils.isAlphanumeric(cig);
		boolean rightLength = StringUtils.length(cig) == SolgroupCoreConstants.CIG_LENGTH;
		
		if(!isAlphaNumeric || !rightLength) {
			errors.reject(SolgroupCoreConstants.ORDERENTRY_CIG, INVALID_CIG_ERROR_KEY);
		}
		
	}

}
