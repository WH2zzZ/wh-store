package com.leyou.listener;

import com.leyou.user.service.GoodsHtmlService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ItemListener {

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value="leyou.goods.create.html", durable = "true"),
            exchange = @Exchange(value = "leyou.item.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void createHtmlListen(Long id){
        if (id == null) {
            return;
        }
        this.goodsHtmlService.CreateItemHtml(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value="leyou.goods.delete.html", durable = "true"),
            exchange = @Exchange(value = "leyou.item.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void deleteHtmlListen(Long id){
        if (id == null) {
            return;
        }
        File file = new File("C:\\hm33\\tools\\nginx-1.14.0\\html\\item\\" + id + ".html");
        file.deleteOnExit();
    }

}
