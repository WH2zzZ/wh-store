package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBo;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {

    @GetMapping("spu/page")
    public PageResult<SpuBo> querySpuBo(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    );

    @GetMapping("spu/detail/{spuId}")
    public SpuDetail querySpuDetail(@PathVariable("spuId") Long spuId);

    @GetMapping("sku/list")
    public List<Sku> querySkusBySpuId(@RequestParam("id") Long id);

    @GetMapping("spu/{id}")
    public Spu querySpuById(@PathVariable("id")Long id);

    @GetMapping("sku/{id}")
    public Sku querySkuById(@PathVariable("id")Long id);
}
