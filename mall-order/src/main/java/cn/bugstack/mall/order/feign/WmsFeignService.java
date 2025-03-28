package cn.bugstack.mall.order.feign;

import cn.bugstack.common.utils.R;
import cn.bugstack.mall.order.vo.WareSkuLockVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/24 21:50
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient("mall-ware")
public interface WmsFeignService {

    /**
     * 查询sku是否有库存
     */
    @PostMapping("/ware/waresku/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds);

    /**
     * 获取运费信息
     */
    @GetMapping("/ware/wareinfo/fare")
    public R getFare(@RequestParam("addrId") Long addrId);

    @RequestMapping("/ware/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVO skuLockVO);
}
