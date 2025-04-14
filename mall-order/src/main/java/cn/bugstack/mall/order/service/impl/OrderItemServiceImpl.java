package cn.bugstack.mall.order.service.impl;

import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;
import cn.bugstack.mall.order.dao.OrderItemDao;
import cn.bugstack.mall.order.entity.OrderEntity;
import cn.bugstack.mall.order.entity.OrderItemEntity;
import cn.bugstack.mall.order.entity.OrderReturnReasonEntity;
import cn.bugstack.mall.order.service.OrderItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RabbitListener(queues = {"mall.order.release.queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(final Map<String, Object> params) {
        final IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /*
     *  queues：声明需要监听的队列
     *  org.springframework.amqp.core.Message
     *  参数可以写一下：
     *      1、Message message：原生消息详细信息：消息头+消息体
     *      2、T<但是发送的消息的类型>
     *      3、Channel channel：当前传输数据的通道
     *
     *  Queue：可以很多人都来监听，只要接收到消息，就删除消息，只能有一个人接收到消息
     *      场景：
     *          1.订单服务启动了多个，同一个消息，只能有一个客户端收到！注意点：如果使用单元测试会被单元测试的启动类接收到几个消息不是丢失！
     *          2.只有一个消息处理完，我们才可以接收到下一个消息
     */
    // @RabbitListener(queues = {"mall.order.release.queue"})
    @RabbitHandler
    public void receiveMessage(final Message message, final OrderReturnReasonEntity content, final Channel channel) throws InterruptedException {
        System.out.println("接收到的消息为：" + message + "，内容为：" + content);
        final byte[] body = message.getBody(); // 消息内容
        final MessageProperties messageProperties = message.getMessageProperties(); // 消息头属性信息
        // Thread.sleep(3000);
        System.out.println("消息处理完成....{}" + content);
        // channel内按顺序自增的
        // 签收货物，非批量签收
        try {
            if (message.getMessageProperties().getDeliveryTag() % 2 == 0) {
                // 收货
                channel.basicAck(messageProperties.getDeliveryTag(), false);
                log.info("消息签收成功：{}", messageProperties.getDeliveryTag());
            } else {
                // 退货，
                // void basicNack(long deliveryTag, boolean multiple, boolean requeue)
                // 参数：1、消息的tag；2、是否批量签收；3、是否重新入队列requeue=true发回服务器并且重新入队，requeue=false丢弃
                channel.basicNack(messageProperties.getDeliveryTag(), false, true);
                // 拒收货物
                // channel.basicReject(messageProperties.getDeliveryTag(), true);
            }
        } catch (final IOException e) {
            log.error("消息签收失败：{}", e);
        }
    }

    @RabbitHandler
    public void receiveMessage2(final OrderEntity content) throws InterruptedException {
        System.out.println("消息处理完成....{}" + content);
    }

}