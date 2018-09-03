package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryByPageAndSort(Integer pageNum, Integer pageSize, Boolean desc, String sortBy, String key) {

        // 设置分页
        PageHelper.startPage(pageNum, pageSize);

        Example example = new Example(Brand.class);
        // 设置查询条件
        if(StringUtils.isNotBlank(key)){
            example.createCriteria().andLike("name", "%" + key + "%").orEqualTo("letter", key);
        }

        // 设置排序
        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }
        // 获取分页对象
        List<Brand> brandList = this.brandMapper.selectByExample(example);
        PageInfo<Brand> pageInfo = new PageInfo<>(brandList);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), pageInfo.getList());
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {

        this.brandMapper.insertSelective(brand);
        cids.forEach(cid->{
            this.brandMapper.insertCategoryBrand(brand.getId(), cid);
        });
    }

    public List<Brand> queryByCid(Long cid) {

        return this.brandMapper.selectBrandsByCid(cid);
    }

    public Brand queryById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }

    public List<Brand> queryByIds(List<Long> ids) {
        return this.brandMapper.selectByIdList(ids);
    }
}
