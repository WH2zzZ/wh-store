package com.leyou.search.pojo;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

public class SearchResult extends PageResult {

    private List<Brand> brands;
    private List<Map<String, Object>> categories;

    private List<Map<String, Object>> specs;

    public SearchResult(List<Brand> brands, List<Map<String, Object>> categories, List<Map<String, Object>> specs) {
        this.brands = brands;
        this.categories = categories;
        this.specs = specs;
    }

    public SearchResult(Long total, List items, List<Brand> brands, List<Map<String, Object>> categories, List<Map<String, Object>> specs) {
        super(total, items);
        this.brands = brands;
        this.categories = categories;
        this.specs = specs;
    }

    public SearchResult(Long total, Integer totalPage, List items, List<Brand> brands, List<Map<String, Object>> categories, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.brands = brands;
        this.categories = categories;
        this.specs = specs;
    }

    public List<Map<String, Object>> getSpecs() {
        return specs;
    }

    public void setSpecs(List<Map<String, Object>> specs) {
        this.specs = specs;
    }

    public List<Brand> getBrands() {
        return brands;
    }

    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }

    public List<Map<String, Object>> getCategories() {
        return categories;
    }

    public void setCategories(List<Map<String, Object>> categories) {
        this.categories = categories;
    }
}
