package com.leyou.user.service;

import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecClient;
import com.leyou.item.pojo.*;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecClient specClient;

    public Map<String, Object> buildItemMap(Long spuId){

        Map<String, Object> modelMap = new HashMap<>();

        // 查询spu
        Spu spu = this.goodsClient.querySpuById(spuId);
        // 查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetail(spuId);
        // 查询商品分类
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < cids.size(); i++ ) {
            Category category = new Category();
            category.setId(cids.get(i));
            category.setName(names.get(i));
            categories.add(category);
        }

        // 查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        // 查询特有的规格参数名
        Map<Long, String> paramMap = new HashMap<>();
        List<SpecParam> params = this.specClient.queryParamsByGid(null, spu.getCid3(), false, null);
        params.forEach(param -> {
            paramMap.put(param.getId(), param.getName());
        });

        // 查询skus
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spuId);
        // 查询规格参数组
        List<SpecGroup> groups = this.specClient.queryGroupByCid(spu.getCid3());

        // spu
        modelMap.put("spu", spu);
        // spuDetail
        modelMap.put("spuDetail", spuDetail);
        // categories
        modelMap.put("categories", categories);
        modelMap.put("brand", brand);
        modelMap.put("skus", skus);
        modelMap.put("paramMap", paramMap);
        modelMap.put("groups", groups);
        return modelMap;
    }
}
