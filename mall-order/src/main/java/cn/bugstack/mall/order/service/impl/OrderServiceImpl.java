package cn.bugstack.mall.order.service.impl;

import cn.bugstack.common.vo.MemberResponseVO;
import cn.bugstack.mall.order.feign.CartFeignService;
import cn.bugstack.mall.order.feign.MemberFeignService;
import cn.bugstack.mall.order.interceptor.LoginUserInterceptor;
import cn.bugstack.mall.order.vo.MemberAddressVO;
import cn.bugstack.mall.order.vo.OrderConfirmVO;
import cn.bugstack.mall.order.vo.OrderItemVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.order.dao.OrderDao;
import cn.bugstack.mall.order.entity.OrderEntity;
import cn.bugstack.mall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final MemberFeignService memberFeignService;
    private final CartFeignService cartFeignService;

    public OrderServiceImpl(MemberFeignService memberFeignService, CartFeignService cartFeignService) {
        this.memberFeignService = memberFeignService;
        this.cartFeignService = cartFeignService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVO confirmOrder() {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();

        // 1.远程查询所有的收获地址列表
        List<MemberAddressVO> addressList = memberFeignService.getAddress(memberResponseVO.getId());
        orderConfirmVO.setAddress(addressList);

        // 2.远程查询购物车所有选中的购物项
        List<OrderItemVO> items = cartFeignService.getCurrentUserCartItems();
        orderConfirmVO.setItems(items);

        // 3.查询用户积分
        Integer integration = memberResponseVO.getIntegration();
        orderConfirmVO.setIntegration(integration);

        // 4.其他数据自动计算

        // 5.订单防重令牌

        return  orderConfirmVO;
    }

}