package cn.bugstack.mall.ware.listener;

import cn.bugstack.common.to.OrderTO;
import cn.bugstack.common.to.mq.StockLockedTo;
import cn.bugstack.mall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/8 22:49
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Service
@Slf4j
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 1、库存自动解锁。
     *      下订单成功，库存解锁成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁。使用seata太慢了
     * 2、订单失败
     *      锁库存失败
     *
     *  只要库存库存消息失败。一定要告诉服务器解锁失败。
     * @param lockedTO
     * @param message
     */
    @RabbitHandler
    @SneakyThrows
    public void handlerStockLockedRelease(StockLockedTo lockedTO, Message message, Channel channel){

        try {
            // 单前消息是否被第二次以后（重新）派发过来了。
            Boolean redelivered = message.getMessageProperties().getRedelivered();
            wareSkuService.unLockStock(lockedTO);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("库存解锁失败：{}", e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @SneakyThrows
    @RabbitListener
    public void handlerOrderCloseRelease(OrderTO orderTo, Message message, Channel channel) {
        try {
            wareSkuService.unLockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("库存解锁失败：{}", e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
