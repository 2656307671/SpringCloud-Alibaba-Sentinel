package com.szh.springcloud.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.szh.springcloud.entity.CommonResult;

/**
 *
 */
public class CustomerBlockHandler {

    public static CommonResult handleException2(BlockException blockException) {
        return new CommonResult(2020,"自定义的限流处理信息......CustomerBlockHandler");
    }
}
