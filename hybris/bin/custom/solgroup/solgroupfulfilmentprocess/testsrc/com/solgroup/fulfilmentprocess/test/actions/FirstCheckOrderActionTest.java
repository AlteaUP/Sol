package com.solgroup.fulfilmentprocess.test.actions;

import com.solgroup.fulfilmentprocess.CheckOrderService;
import com.solgroup.fulfilmentprocess.actions.order.OrderManualCheckedAction;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.task.RetryLaterException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.solgroup.fulfilmentprocess.actions.orderexport.FirstCheckOrderAction;

@UnitTest
public class FirstCheckOrderActionTest {

    private FirstCheckOrderAction action;
    @Mock
    private ModelService mockModelService;


    private OrderHistoryEntryModel historyLog;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        action = new FirstCheckOrderAction();
        action.setModelService(mockModelService);

        historyLog = new OrderHistoryEntryModel();
        BDDMockito.given(mockModelService.create(OrderHistoryEntryModel.class)).willReturn(historyLog);
    }

    @Test(expected=IllegalArgumentException.class)
    public void firstCheckOrderAction() throws Exception {
        final OrderProcessModel process = new OrderProcessModel();
        action.execute(process);
    }
}
