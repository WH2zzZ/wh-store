package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandListByPageAndSort(
            @RequestParam(value = "page", defaultValue = "1")Integer pageNum,
            @RequestParam(value = "rows", defaultValue = "5")Integer pageSize,
            @RequestParam(value = "desc", defaultValue = "false")Boolean desc,
            @RequestParam(value = "sortBy", required = false)String sortBy,
            @RequestParam(value = "key", required = false)String key
    ){
        PageResult<Brand> pageResult = this.brandService.queryByPageAndSort(pageNum, pageSize, desc, sortBy, key);
        if (CollectionUtils.isEmpty(pageResult.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pageResult);
    }

    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids")List<Long> cids){

        this.brandService.saveBrand(brand, cids);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("cid/{cid}")
    public ResponseEntity queryByCid(@PathVariable("cid")Long cid){

        List<Brand> brands = this.brandService.queryByCid(cid);
        if (CollectionUtils.isEmpty(brands)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brands);
    }

    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id")Long id){
        Brand brand = this.brandService.queryById(id);
        if (brand == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brand);
    }

    @GetMapping("query")
    public ResponseEntity<List<Brand>> queryBrandsByIds(@RequestParam("ids")List<Long> ids){
        List<Brand> brands = this.brandService.queryByIds(ids);
        if (CollectionUtils.isEmpty(brands)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brands);
    }

}
