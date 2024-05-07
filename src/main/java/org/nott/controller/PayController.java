package org.nott.controller;

import jakarta.annotation.Resource;
import org.nott.common.R;
import org.nott.common.ThreadPoolContext;
import org.nott.entity.PayOrderInfo;
import org.nott.service.OrderService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pay")
public class PayController {

    @Resource
    private OrderService orderService;

    @Resource
    private ThreadPoolContext threadPoolContext;

    @RequestMapping(path = "createOrder",method = RequestMethod.POST)
    public R<?> createPayOrder(){
        return R.okData(orderService.initializeOrder());
    }

    @RequestMapping(path = "h5Pay",method = RequestMethod.POST)
    public R<?> h5Pay(){

        return R.okData(null);
    }

    @RequestMapping(path = "refund",method = RequestMethod.POST)
    public R<?> refund(){

        return R.okData(null);
    }

    @RequestMapping(path = "notify")
    public R<?> notifyByOutSystem(){
        // 处理回调信息

        // 异步处理（更新内部交易记录）
        ThreadPoolTaskExecutor executor = threadPoolContext.threadPoolTaskExecutor();
        executor.execute(()->{
            //...
        });
        return R.ok();
    }
}
