package com.leyou.search.client;

import com.leyou.item.pojo.SpuDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GoodsClientTest {

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void test(){
        SpuDetail spuDetail = this.goodsClient.querySpuDetail(2l);
        System.out.println(spuDetail);
    }

}