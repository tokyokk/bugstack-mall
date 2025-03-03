package cn.bugstack.mall.search;

import cn.bugstack.mall.search.config.MallElasticSearchConfig;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class MallSearchApplicationTests {

    /*
        1. 方便检索 {
            skuId:1
            spuId:11
            skuTitle:华为
            skuPrice:1000
            saleCount:100
            attrs:[
                {"attrId":1,"attrName":"颜色","attrValue":"黑色"},
            ]
        }

        冗余：100万 * 2kb。= 200M = 200M * 0.01 = 2G

        2.分别存储

        sku索引 {
            skuId:1
            spuId:11
            xxx
        }

        attr 索引 {
            spuId:11
            attrs:[
                {尺寸：5寸}
                {颜色：黑色}
                {CPU：澎湃OS}
                {分辨率：高清}
            ]
        }

        搜索：小米 ：粮食，手机，电器
        10000个数据，4000个spu
        分布：4000个spu对应的所有可能属性：
        esClient：spuId：【4000个spuId】4000*8 = 32000byte = 32000/1024 = 31.25kb 单次请求
        32kb * 10000人 = 32000 * 10000 = 32000000byte = 32000000/1024/1024 = 30G  所以不推荐！！！
    */


    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 测试存储数据到es   保存/更新 都可以
     */
    @Test
    public void indexData() {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        //indexRequest.source("userName","zhangsan","age",30,"sex","男");
        User user = new User();
        user.setUserName("zhangsan");
        user.setAge(18);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);
        try {
            IndexResponse index = restHighLevelClient.index(indexRequest, MallElasticSearchConfig.COMMON_OPTIONS);
            System.out.println(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试搜索数据 以及构建条件
     */
    public void searchData() {
        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();

        // 2. 指定索引
        searchRequest.indices("users");
        // 3. 指定DSL，检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 3.1. 构造检索条件
        sourceBuilder.query(QueryBuilders.matchQuery("userName", "zhangsan"));

        // 3.2. 聚合条件 按照年龄进行聚合
        TermsAggregationBuilder ageAggregation = AggregationBuilders.terms("ageAggregation").field("age").size(10);
        sourceBuilder.aggregation(ageAggregation);
        // 3.3. 聚合条件 计算平均工资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);


        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        System.out.println("检索条件：" + sourceBuilder.toString());
        searchRequest.source(sourceBuilder);

        try {
            // 4. 执行检索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
            // 5. 分析结果
            System.out.println(searchResponse.toString());
            //Map map = JSON.parseObject(response.toString(), Map.class);
            // 5.1 获取所有查到的数据
            SearchHits hits = searchResponse.getHits(); // 获取所有查询到的数据
            SearchHit[] searchHits = hits.getHits(); // 获取所有数据
            for (SearchHit searchHit : searchHits) {
                String index = searchHit.getIndex(); // 索引
                String id = searchHit.getId(); // id
                String type = searchHit.getType(); // type
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap(); // 数据
                String sourceAsString = searchHit.getSourceAsString(); // 数据
                JSON.parseObject(sourceAsString, User.class);
                System.out.println("index:" + index + ",id:" + id + ",type:" + type + ",sourceAsMap:" + sourceAsMap + ",sourceAsString:" + sourceAsString);
            }

            // 5.2 获取聚合结果
            Aggregations aggregations = searchResponse.getAggregations();
            //for (Aggregation aggregation : aggregations.asList()) {
            //    System.out.println("当前聚合名称：" + aggregation.getName());
            //}

            // 5.2.1 获取年龄分组结果
            Terms ageAgg = aggregations.get("ageAggregation");
            for (Terms.Bucket bucket : ageAgg.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();
                System.out.println("年龄：" + keyAsString + "，人数：" + bucket.getDocCount());
            }

            // 5.3 获取平均薪资
            Avg balanceAvg1 = aggregations.get("balanceAvg");
            System.out.println("平均薪资：" + balanceAvg1.getValueAsString());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Data
    static class User {
        private String userName;
        private Integer age;
        private String gender;
        private BigDecimal balance;
    }

    @Test
    public void contextLoads() {
        System.out.println(restHighLevelClient);
    }



}
