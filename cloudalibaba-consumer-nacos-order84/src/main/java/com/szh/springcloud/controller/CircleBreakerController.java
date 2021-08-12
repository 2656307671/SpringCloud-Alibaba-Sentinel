package com.szh.springcloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.szh.springcloud.entity.CommonResult;
import com.szh.springcloud.entity.Payment;
import com.szh.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 *
 */
@RestController
@Slf4j
public class CircleBreakerController {

    //======================Ribbon======================

    public static final String SERVICE_URL = "http://nacos-payment-provider";

    @Resource
    private RestTemplate restTemplate;

    @RequestMapping(value = "/consumer/fallback/{id}")
//  无配置
//  @SentinelResource(value = "fallback")
//  fallback负责业务异常
//  @SentinelResource(value = "fallback",
//                    fallback = "handlerFallback")
//  blockHandler负责在sentinel里面配置的降级限流
//  @SentinelResource(value = "fallback",
//                    blockHandler = "blockHandler")
//  fallback + blockHandler
    @SentinelResource(value = "fallback",
                      fallback = "handlerFallback",
                      blockHandler = "blockHandler")
    public CommonResult<Payment> fallback(@PathVariable("id") Long id) {
        CommonResult<Payment> result = restTemplate.getForObject(SERVICE_URL + "/paymentSQL/" + id,CommonResult.class,id);
        if (id == 4) {
            throw new IllegalArgumentException("IllegalArgumentException, 非法参数异常....");
        } else if (result.getData() == null) {
            throw new NullPointerException("NullPointerException, 该ID没有对应记录, 空指针异常");
        }
        return result;
    }
    //该方法只针对fallback
    public CommonResult handlerFallback(@PathVariable("id") Long id,Throwable e) {
        Payment payment = new Payment(id,null);
        return new CommonResult(444,"兜底异常handlerFallback, exception内容  " + e.getMessage(),payment);
    }
    //该方法只针对blockHandler
    public CommonResult blockHandler(@PathVariable("id") Long id,BlockException blockException) {
        Payment payment = new Payment(id,"null");
        return new CommonResult<>(445,"blockHandler-sentinel限流,无此流水: blockException  " + blockException.getMessage(),payment);
    }

    //======================OpenFeign======================
    @Resource
    private PaymentService paymentService;

    @GetMapping(value = "/consumer/openfeign/{id}")
    public CommonResult<Payment> paymentSQL(@PathVariable("id") Long id) {
        if(id == 4) {
            throw new RuntimeException("没有该id");
        }
        return paymentService.paymentSQL(id);
    }

}
