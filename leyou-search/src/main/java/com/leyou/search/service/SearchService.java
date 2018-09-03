package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.GoodsRepositroy;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecClient specClient;

    @Autowired
    private GoodsRepositroy goodsRepositroy;

    @Autowired
    private ElasticsearchTemplate template;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();
        // 查询商品分类
        List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        // 查询该spu下的skus
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        // 查询所有搜索的规格参数
        List<SpecParam> params = this.specClient.queryParamsByGid(null, spu.getCid3(), null, true);
        // 查询spudetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetail(spu.getId());

        // 构建价格的集合
        List<Long> prices = new ArrayList<>();
        // 构建skuMap集合
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        // 遍历skus
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            map.put("price", sku.getPrice());
            skuMapList.add(map);
        });

        // 获取spuDetail中的通用规格参数值
        Map<String, Object> genericSpec = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        // 获取spuDetail中的特殊规格参数值
        Map<String, List<Object>> specialSpec = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, Object>>() {
        });
        // 搜索规格参数及参数值集合
        Map<String, Object> searchingMap = new HashMap<>();
        params.forEach(param -> {
            if (param.getGeneric()) {
                String value = genericSpec.get(param.getId().toString()).toString();
                if (param.getNumeric()) {
                    value = chooseSegment(value, param);
                }
                searchingMap.put(param.getName(), value);
            } else {
                List<Object> value = specialSpec.get(param.getId().toString());
                searchingMap.put(param.getName(), value);
            }
        });

        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setBrandId(spu.getBrandId());
        goods.setSubTitle(spu.getSubTitle());
        // 商品标题，分类，品牌
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " ") + " " + brand.getName());
        // sku中所有价格的集合，方便以价格进行搜索
        goods.setPrice(prices);
        // 结果集中一个spu中的所有sku
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        // 搜索的规格参数
        goods.setSpecs(searchingMap);
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {

        String key = request.getKey();

        // 判断搜索关键字，如果为空，直接返回
        if (StringUtils.isBlank(key)) {
            // 返回默认值
            return null;
        }
        // 构建自定义搜索构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 设置查询条件
        MatchQueryBuilder basicQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
        // 添加过滤
        addFilterQuery(queryBuilder, basicQuery, request.getFilter());

        // 结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));

        // 分页
        Integer page = request.getPage();
        Integer size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 进行聚合
        String categoryAggName = "category";
        String brandAggName = "brand";
        // 根据分类聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        // 根据品牌聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        // 执行搜索获取结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) this.goodsRepositroy.search(queryBuilder.build());

        // 解析聚合
        List<Map<String, Object>> categories = getCategoryAgg(goodsPage.getAggregation(categoryAggName));
        List<Brand> brands = getBrandAgg(goodsPage.getAggregation(brandAggName));

        // 判断分类是否为1，如果为1的话，才去做规格参数的聚合
        List<Map<String, Object>> specs = new ArrayList<>();
        if (categories.size() == 1) {
            specs = getSpecs((Long) categories.get(0).get("id"), basicQuery);
        }

        return new SearchResult(goodsPage.getTotalElements(), goodsPage.getTotalPages(), goodsPage.getContent(), brands, categories, specs);
    }

    private void addFilterQuery(NativeSearchQueryBuilder queryBuilder, MatchQueryBuilder basicQuery, Map<String,Object> filter) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 添加基础查询条件
        boolQueryBuilder.must(basicQuery);

        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey();
            if(StringUtils.equals("品牌", key)){
                key = "brandId";
            } else if(StringUtils.equals("分类", key)) {
                key = "cid3";
            } else {
                key = "specs." + entry.getKey() + ".keyword";
            }
            filterQueryBuilder.must(QueryBuilders.termQuery(key, entry.getValue()));
        }
        boolQueryBuilder.filter(filterQueryBuilder);
        queryBuilder.withQuery(boolQueryBuilder);
    }


    private List<Map<String,Object>> getSpecs(Long cid, MatchQueryBuilder basicQuery) {
        // 获取要聚合的规格参数
        List<SpecParam> params = this.specClient.queryParamsByGid(null, cid, null, true);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        // 遍历结果集，添加过滤聚合
        params.forEach(param->{
            String key = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(key).field("specs." + key + ".keyword"));
        });

        // 执行聚合查询，获取结果集
        Map<String, Aggregation> aggregationMap = this.template.query(queryBuilder.build(), SearchResponse::getAggregations).asMap();

        // 解析聚合结果集
        List<Map<String, Object>> specs = new ArrayList<>();
        for (Map.Entry<String, Aggregation> entry: aggregationMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            StringTerms agg = (StringTerms)entry.getValue();
            List<Object> objs = new ArrayList<>();
            agg.getBuckets().forEach(bucket -> {
                objs.add(bucket.getKeyAsString());
            });
            map.put("k", entry.getKey());
            map.put("options", objs);
            specs.add(map);
        }
        return specs;
    }

    /**
     * 解析品牌的聚合
     *
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAgg(Aggregation aggregation) {
        List<Long> bids = new ArrayList<>();
        // 解析聚合，获取所有的品牌id
        LongTerms brandAgg = (LongTerms) aggregation;
        brandAgg.getBuckets().forEach(bucket -> {
            bids.add(bucket.getKeyAsNumber().longValue());
        });
        return this.brandClient.queryBrandsByIds(bids);
    }

    /**
     * 解析分类聚合
     *
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAgg(Aggregation aggregation) {
        List<Long> cids = new ArrayList<>();
        LongTerms brandAgg = (LongTerms) aggregation;
        brandAgg.getBuckets().forEach(bucket -> {
            cids.add(bucket.getKeyAsNumber().longValue());
        });
        List<Map<String, Object>> categories = new ArrayList<>();
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        for (int i = 0; i < cids.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cids.get(i));
            map.put("name", names.get(i));
            categories.add(map);
        }
        return categories;
    }

    public void saveIndex(Long id) throws IOException {

        Spu spu = this.goodsClient.querySpuById(id);

        Goods goods = this.buildGoods(spu);

        this.goodsRepositroy.save(goods);
    }

    public void deleteIndex(Long id) {

        this.goodsRepositroy.deleteById(id);
    }
}
