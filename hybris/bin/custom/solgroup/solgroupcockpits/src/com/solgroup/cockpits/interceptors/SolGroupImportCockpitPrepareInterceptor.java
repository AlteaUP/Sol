/*
 * Put your copyright text here
 */
package com.solgroup.cockpits.interceptors;

import de.hybris.platform.impex.model.cronjob.ImpExImportCronJobModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author fmilazzo
 * @see SAP Hana: unique constraint violated: Table(MEDIAS), Index(CODEVERSIONIDX_30)
 *
 */
public class SolGroupImportCockpitPrepareInterceptor implements PrepareInterceptor<ImpExImportCronJobModel> {

    private ModelService modelService;

    private final Logger LOG = Logger.getLogger(SolGroupImportCockpitPrepareInterceptor.class);

    private static final String PROCESS_1 = "1_import-topBarCategory";
    private static final String PROCESS_2 = "2-1_import-variantCategories";
    private static final String PROCESS_3 = "2-2_import-variantCategories";
    private static final String PROCESS_4 = "3-1_import-classificationSystem";
    private static final String PROCESS_5 = "3-2_import-classificationSystem";
    private static final String PROCESS_6 = "3-3_import-classificationSystem";
    private static final String PROCESS_7 = "3-4_import-classificationSystem";
    private static final String PROCESS_8 = "3-5_import-classificationSystem";
    private static final String PROCESS_9 = "4_import-product";
    private static final String PROCESS_10 = "5_import-genericVariantProduct";
    private static final String PROCESS_11 = "6_import-media";
    private static final String PROCESS_12 = "7_import-updateProductMedia";
    private static final String PROCESS_13 = "8_import-productAttributes";
    private static final String PROCESS_14 = "import-techAttachmentsSOL";
    private static final String PROCESS_15 = "import-techAttachments";
    private static final String PROCESS_16 = "import-techAttachmentsToProduct";
    private static final String PROCESS_17 = "import-techAttachmentsMedia";
    private static final String PROCESS_18 = "import-techAttachmentsTechnical";
    private static final String PROCESS_19 = "import-techAttachmentsMedical";
    private static final String PROCESS_20 = "import-techMedicalAttachmentsToProduct";

    private static final String[] PROCESSES = { PROCESS_1, PROCESS_2, PROCESS_3, PROCESS_4, PROCESS_5, PROCESS_6, PROCESS_7, PROCESS_8,
            PROCESS_9, PROCESS_10, PROCESS_11, PROCESS_12, PROCESS_13, PROCESS_14, PROCESS_15, PROCESS_16, PROCESS_17, PROCESS_18, PROCESS_19, PROCESS_20 };

    @Override
    public void onPrepare(final ImpExImportCronJobModel impexImportCronJob, final InterceptorContext context) throws InterceptorException {
        String newProcessName = null;

        if (!getModelService().isNew(impexImportCronJob)) {
            final String processName = impexImportCronJob.getCode();
            for (final String process : PROCESSES) {
                if (processName.contains(process)) {
                    newProcessName = process.concat("-" + getSysdate());
                }
            }

            LOG.info("Renamed '" + processName + "' in '" + newProcessName + "'");

            impexImportCronJob.setCode(newProcessName);

        }
    }

    private String getSysdate() {
        final Date sysdate = new Date();
        final DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
        return dateFormat.format(sysdate.getTime());
    }

    protected ModelService getModelService() {
        return modelService;
    }

    @Required
    public void setModelService(final ModelService modelService) {
        this.modelService = modelService;
    }

}