/*
 * mdc
 */
package com.solgroup.facades.process.email.context;

import com.solgroup.core.model.NewProductsNotificationProcessModel;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.util.Config;
import de.hybris.platform.variants.model.GenericVariantProductModel;
import org.apache.commons.lang.StringUtils;

import java.util.List;


/**
 * Velocity context for a New Product Notification email.
 */
public class NewProductsNotificationEmailContext extends AbstractEmailContext<NewProductsNotificationProcessModel>
{

	private static final String FALLBACK_TO_EMAIL = Config.getParameter("newproduct.to.fallback.email");

	private static final String NEW_PRODUCTS_LIST = "newProducts";

	private static final String NEW_VARIANTS_LIST = "newVariants";

	@Override
	public void init(NewProductsNotificationProcessModel businessProcessModel, EmailPageModel emailPageModel) {
		super.init(businessProcessModel, emailPageModel);

		List<ProductModel> newProducts = businessProcessModel.getNewProducts();

		List<GenericVariantProductModel> newVariants = businessProcessModel.getNewVariants();

		EmployeeModel employee = businessProcessModel.getEmployee();

		//Retrieve emailTo from the Employee set in the CronJobModel
		if (employee != null) {
			if (employee.getDefaultShipmentAddress() != null && StringUtils.isNotBlank(employee.getDefaultShipmentAddress().getEmail())) {
				put(EMAIL, employee.getDefaultShipmentAddress().getEmail());
			}else {
				//Fallback email
				put(EMAIL, FALLBACK_TO_EMAIL);
			}
			put(DISPLAY_NAME, employee.getDisplayName());
		}

		put(NEW_PRODUCTS_LIST, newProducts);
		put(NEW_VARIANTS_LIST, newVariants);

	}

	@Override
	protected BaseSiteModel getSite(NewProductsNotificationProcessModel businessProcessModel) {
		return businessProcessModel.getSite();
	}

	@Override
	protected CustomerModel getCustomer(NewProductsNotificationProcessModel businessProcessModel) {
		return null;
	}

	@Override
	protected LanguageModel getEmailLanguage(NewProductsNotificationProcessModel businessProcessModel) {
		return businessProcessModel.getLanguageModel() != null ? businessProcessModel.getLanguageModel() : super.getEmailLanguage();
	}

}
