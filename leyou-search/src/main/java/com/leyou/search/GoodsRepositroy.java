package com.leyou.search;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepositroy extends ElasticsearchRepository<Goods, Long> {
}
