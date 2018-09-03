package com.leyou.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

@Service
public class GoodsHtmlService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private GoodsService goodsService;

    public void CreateItemHtml(Long spuId){

        try {
            // 获取数据模型
            Map<String, Object> itemMap = this.goodsService.buildItemMap(spuId);

            // 上下文对象， 可以方法数据模型
            Context context = new Context();
            context.setVariables(itemMap);

            // 初始化文件流
            File file = new File("C:\\hm33\\tools\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
            PrintWriter writer = new PrintWriter(file);

            this.templateEngine.process("item", context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
