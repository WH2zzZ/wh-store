package com.leyou.controller;


import com.leyou.user.service.GoodsHtmlService;
import com.leyou.user.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @GetMapping("{id}.html")
    public String toItem(@PathVariable("id")Long id, Model model){
        // 数据模型名称， 数据模型对应的值
        Map<String, Object> modelMap = this.goodsService.buildItemMap(id);
        model.addAllAttributes(modelMap);

        this.goodsHtmlService.CreateItemHtml(id);
        return "item";
    }
}
