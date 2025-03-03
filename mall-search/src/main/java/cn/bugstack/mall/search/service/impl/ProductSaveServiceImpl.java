package cn.bugstack.mall.search.service.impl;

import cn.bugstack.common.to.es.SkuEsModel;
import cn.bugstack.mall.search.config.MallElasticSearchConfig;
import cn.bugstack.mall.search.constant.ProductConstant;
import cn.bugstack.mall.search.service.ProductSaveService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/3 15:39
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 1.给es中建立索引，product，建立映射关系
        // 2. 保存数据到es
        //BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(ProductConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            indexRequest.source(JSON.toJSONString(skuEsModel), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        BulkResponse resp = restHighLevelClient.bulk(bulkRequest, MallElasticSearchConfig.COMMON_OPTIONS);

        boolean flag = resp.hasFailures(); // 是否有错误

        List<String> skuErrorIds = Arrays.stream(resp.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("商品上架成功：{},返回数据：{}", JSON.toJSONString(skuErrorIds), resp.toString());

        return flag;
    }
}
