package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>,SelectByIdListMapper<Brand, Long> {

    @Insert("insert into tb_category_brand (category_id, brand_id) values (#{cid}, #{bid})")
    void insertCategoryBrand(@Param("bid")Long id, @Param("cid") Long cid);

    @Select("select * from tb_brand b INNER JOIN tb_category_brand cb on b.id=cb.brand_id where cb.category_id = #{cid}")
    List<Brand> selectBrandsByCid(Long cid);
}
