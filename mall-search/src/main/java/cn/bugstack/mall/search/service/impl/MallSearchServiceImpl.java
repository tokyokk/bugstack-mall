package cn.bugstack.mall.search.service.impl;

import cn.bugstack.common.constant.CharacterConstant;
import cn.bugstack.common.to.es.SkuEsModel;
import cn.bugstack.mall.search.config.MallElasticSearchConfig;
import cn.bugstack.mall.search.constant.ProductConstant;
import cn.bugstack.mall.search.service.MallSearchService;
import cn.bugstack.mall.search.vo.SearchParamVO;
import cn.bugstack.mall.search.vo.SearchResponseVO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/5 13:45
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVO search(SearchParamVO searchParam) {
        // 构建查询结果
        SearchResponseVO result = null;
        // 1.构建DSL语句
        SearchRequest searchRequest = buildSearchRequest(searchParam);

        try {
            // 2. 执行DSL语句
            SearchResponse response = restHighLevelClient.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);

            // 3. 分析响应数据，封装结果
            result = buildSearchResult(response, searchParam);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 构建检索结果
     *
     * @param response    检索结果
     * @param searchParam 检索参数
     * @return SearchResponseVO
     */
    private SearchResponseVO buildSearchResult(SearchResponse response, SearchParamVO searchParam) {
        SearchResponseVO result = new SearchResponseVO();
        SearchHits hits = response.getHits();

        List<SkuEsModel> skuEsModels = new ArrayList<>();
        Optional.ofNullable(hits.getHits()).ifPresent(hitList -> {
            for (SearchHit hit : hitList) {
                String sourceData = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceData, SkuEsModel.class);
                if (!StringUtils.isEmpty(searchParam.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String title = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(title);
                }
                skuEsModels.add(skuEsModel);
            }
        });

        // 1.返回所有商品信息
        result.setProducts(skuEsModels);

        // 2.当前商品所涉及到的所有属性信息
        List<SearchResponseVO.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAggregation = response.getAggregations().get("attrAggregation");
        ParsedLongTerms attrIdAggregation = attrAggregation.getAggregations().get("attrIdAggregation");
        attrIdAggregation.getBuckets().forEach(bucket -> {
            SearchResponseVO.AttrVo attrVo = new SearchResponseVO.AttrVo();
            // 获取属性id
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
            // 获取属性名称 attrNameAggregation
            String attrName = Optional.ofNullable(bucket.getAggregations().get("attrNameAggregation"))
                    .filter(ParsedStringTerms.class::isInstance)
                    .map(ParsedStringTerms.class::cast)
                    .map(ParsedStringTerms::getBuckets)
                    .filter(buckets -> !buckets.isEmpty())
                    .map(buckets -> buckets.get(0).getKeyAsString())
                    .orElse(null);

            attrVo.setAttrName(attrName);
            // 获取属性值 attrValuesAggregation
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attrValuesAggregation")).getBuckets()
                    .stream()
                    .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                    .collect(Collectors.toList());
            attrVo.setAttrValues(attrValues);
            attrVos.add(attrVo);
        });
        result.setAttrs(attrVos);

        // 3.当前商品所涉及到的所有品牌信息
        List<SearchResponseVO.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAggregation = response.getAggregations().get("brandAggregation");
        brandAggregation.getBuckets().forEach(bucket -> {
            SearchResponseVO.BrandVo brandVo = new SearchResponseVO.BrandVo();
            // 获取品牌id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());

            // 获取品牌名称 brandNameAggregation
            ParsedStringTerms brandNameAggregation = bucket.getAggregations().get("brandNameAggregation");
            String brandName = Optional.ofNullable(brandNameAggregation.getBuckets())
                    .filter(buckets -> !buckets.isEmpty())
                    .map(buckets -> buckets.get(0).getKeyAsString())
                    .orElse(null);
            brandVo.setBrandName(brandName);

            // 获取品牌图片 brandImgAggregation
            ParsedStringTerms brandImgAggregation = bucket.getAggregations().get("brandImgAggregation");
            String brandImg = Optional.ofNullable(brandImgAggregation.getBuckets())
                    .filter(buckets -> !buckets.isEmpty())
                    .map(buckets -> buckets.get(0).getKeyAsString())
                    .orElse(null);
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        });

        result.setBrands(brandVos);

        // 4.当前商品所涉及到的所有分类信息
        List<SearchResponseVO.CatalogVo> catalogs = new ArrayList<>();
        ParsedLongTerms catalogAggregation = response.getAggregations().get("catalogAggregation");
        catalogAggregation.getBuckets().forEach(bucket -> {
            SearchResponseVO.CatalogVo catalogVo = new SearchResponseVO.CatalogVo();
            // 获取品牌id
            catalogVo.setCatalogId(Long.valueOf(bucket.getKeyAsString()));
            // 获取品牌名称 catalogNameAggregation
            ParsedStringTerms catalogNameAggregation = bucket.getAggregations().get("catalogNameAggregation");
            String catalogName = Optional.ofNullable(catalogNameAggregation.getBuckets())
                    .filter(buckets -> !buckets.isEmpty())
                    .map(buckets -> buckets.get(0).getKeyAsString())
                    .orElse(null);
            catalogVo.setCatalogName(catalogName);
            catalogs.add(catalogVo);
        });
        result.setCatalogs(catalogs);

        // 5.分页信息
        long total = Objects.requireNonNull(response.getHits().getTotalHits()).value;
        result.setTotal(total); // 总记录数
        int totalPages = (int) Math.ceil((double) total / ProductConstant.PRODUCT_PAGESIZE);
        result.setTotalPages(totalPages); // 总页数
        result.setPageNum(searchParam.getPageNum()); // 当前页码

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i < totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs); // 页码导航

        log.info("商品检索结果：{}", JSON.toJSONString(result));
        return result;
    }

    /**
     * 构建检索请求
     *
     * @param searchParam 检索参数
     * @return SearchRequest
     */
    private SearchRequest buildSearchRequest(SearchParamVO searchParam) {

        // 构建DSL语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /*
            一、查询：模糊匹配，过滤 (按照属性，分类，品牌，价格区间，库存)
         */
        // 1.构建bool-query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 1.1 must-模糊匹配
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }

        // 1.2 filter 按照三级分类id查询
        if (null != searchParam.getCatalog3Id()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }

        // 1.3 filter 按照品牌id查询
        if (null != searchParam.getBrandId() && !searchParam.getBrandId().isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }

        // 1.3 filter 按照属性查询
        if (!CollectionUtils.isEmpty(searchParam.getAttrs())) {
            for (String attrStr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                // attrs=1_5寸:8寸&attrs=2_16G:8G
                String[] s = attrStr.split(CharacterConstant.UNDERLINE);
                String attrId = s[0]; // 检索属性id
                String[] attrValues = s[1].split(CharacterConstant.COLON); // 检索属性值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 每一个必须都要生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }
        }

        // 1.4 filter 按照库存是否有进行查询
        if (null != searchParam.getHasStock()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }

        // 1.5 filter 按照价格区间进行查询
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            // 1_500_/_500_/500_
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] s = searchParam.getSkuPrice().split(CharacterConstant.UNDERLINE);

            if (s.length == 2) {
                // 区间
                rangeQueryBuilder.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (searchParam.getSkuPrice().startsWith(CharacterConstant.UNDERLINE)) {
                    rangeQueryBuilder.lte(s[0]);
                }
                if (searchParam.getSkuPrice().endsWith(CharacterConstant.UNDERLINE)) {
                    rangeQueryBuilder.gte(s[0]);
                }
            }
        }

        sourceBuilder.query(boolQueryBuilder);

        /*
            二、排序，分页，高亮
         */
        // 2.1 排序
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String[] s = searchParam.getSort().split(CharacterConstant.UNDERLINE);
            SortOrder sortOrder = s[1].equalsIgnoreCase(CharacterConstant.ASC) ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], sortOrder);
        }

        // 2.2 分页
        sourceBuilder.from((searchParam.getPageNum() - 1) * ProductConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(ProductConstant.PRODUCT_PAGESIZE);

        // 2.3 高亮
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        /*
            三、聚合分析
         */
        // 3.1 品牌聚合
        TermsAggregationBuilder brandAggregation = AggregationBuilders.terms("brandAggregation").field("brandId").size(50);
        // 3.1.2 品牌聚合子聚合
        brandAggregation.subAggregation(AggregationBuilders.terms("brandNameAggregation").field("brandName").size(1));
        brandAggregation.subAggregation(AggregationBuilders.terms("brandImgAggregation").field("brandImg").size(1));
        sourceBuilder.aggregation(brandAggregation);

        // 3.2 分类聚合
        TermsAggregationBuilder catalogAggregation = AggregationBuilders.terms("catalogAggregation").field("catalogId").size(20);
        // 3.2.2 分类聚合子聚合
        catalogAggregation.subAggregation(AggregationBuilders.terms("catalogNameAggregation").field("catalogName").size(1));
        sourceBuilder.aggregation(catalogAggregation);

        // 3.3 属性聚合
        NestedAggregationBuilder attrAggregation = AggregationBuilders.nested("attrAggregation", "attrs");
        // 3.3.1 属性聚合子聚合attrId
        TermsAggregationBuilder attrIdAggregation = AggregationBuilders.terms("attrIdAggregation").field("attrs.attrId");
        // 3.3.2 属性聚合子聚合attrId子聚合 attrName
        attrIdAggregation.subAggregation(AggregationBuilders.terms("attrNameAggregation").field("attrs.attrName").size(1));
        // 3.3.3 属性聚合子聚合attrId子聚合 attrValue
        attrIdAggregation.subAggregation(AggregationBuilders.terms("attrValueAggregation").field("attrs.attrValue").size(50));
        attrAggregation.subAggregation(attrIdAggregation);
        sourceBuilder.aggregation(attrAggregation);

        String dsl = sourceBuilder.toString();
        log.info("构建的DSL语句：{}", dsl);

        return new SearchRequest(new String[]{ProductConstant.PRODUCT_INDEX}, sourceBuilder);
    }
}
