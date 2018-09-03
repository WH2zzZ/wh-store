package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        return this.categoryMapper.select(category);
    }

    public List<String> queryNamesByCids(List<Long> ids){

        List<String> names = new ArrayList<>();
        ids.forEach(id->{
            Category category = this.categoryMapper.selectByPrimaryKey(id);
            names.add(category.getName());
        });
        return names;
    }
}
