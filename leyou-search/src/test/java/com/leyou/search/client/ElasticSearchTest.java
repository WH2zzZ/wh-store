package com.leyou.search.client;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBo;
import com.leyou.search.GoodsRepositroy;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsRepositroy goodsRepositroy;

    @Test
    public void testCreate(){
        this.elasticsearchTemplate.createIndex(Goods.class);
        this.elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void importIndex(){

        Integer page = 1;
        Integer rows = 50;

        do{
            PageResult<SpuBo> pageResult = this.goodsClient.querySpuBo(null, true, page, rows);
            List<SpuBo> items = pageResult.getItems();
            List<Goods> goodsList = new ArrayList<>();
            items.forEach(spu->{
                try {
                    Goods goods = this.searchService.buildGoods(spu);
                    goodsList.add(goods);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            this.goodsRepositroy.saveAll(goodsList);
            page++;
            rows = items.size();
        } while (rows == 50);


    }
}
