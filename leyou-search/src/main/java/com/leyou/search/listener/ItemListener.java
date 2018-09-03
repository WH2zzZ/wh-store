package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ItemListener {

    @Autowired
    private SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value="leyou.search.create.index", durable = "true"),
            exchange = @Exchange(value = "leyou.item.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void listenCreate(Long id) throws IOException {

        if (id == null) {
            return ;
        }
        this.searchService.saveIndex(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.search.delete.index", durable = "true"),
            exchange = @Exchange(value = "leyou.item.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void listenDelete(Long id){
        if (id == null) {
            return ;
        }
        this.searchService.deleteIndex(id);
    }
}
