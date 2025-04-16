package cn.bugstack.mall.order.service;

import cn.bugstack.mall.order.vo.OrderConfirmVO;
import cn.bugstack.mall.order.vo.OrderSubmitVo;
import cn.bugstack.mall.order.vo.PayVo;
import cn.bugstack.mall.order.vo.SubmitOrderResponseVO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:32:12
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVO confirmOrder();

    SubmitOrderResponseVO submitOrder(OrderSubmitVo orderSubmitVo);

    OrderEntity getOrderStatusByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    /**
     * 获取当前订单的支付信息
     *
     * @param orderSn 订单号
     * @return 订单支付信息
     */
    PayVo getOrderPay(String orderSn);
}

