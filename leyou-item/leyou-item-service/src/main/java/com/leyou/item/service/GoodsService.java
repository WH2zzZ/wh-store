package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<SpuBo> querySpuBo(String key, Boolean saleable, Integer page, Integer rows) {

        // 准备设置查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 根据关键字模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 查询是否上下架的商品
        if (saleable != null ) {
            criteria.andEqualTo("saleable", saleable);
        }

        // 设置分页
        PageHelper.startPage(page, rows);

        // 执行查询
        Page<Spu> pageInfo = (Page<Spu>)this.spuMapper.selectByExample(example);

        // 获取spubo
        List<SpuBo> spuBoList = pageInfo.getResult().stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            // copyspu中的属性值给spubo对象
            BeanUtils.copyProperties(spu, spuBo);

            // 查询整个分类路径
            List<String> names = this.categoryService.queryNamesByCids(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "-"));

            // 获取品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());

            return spuBo;
        }).collect(Collectors.toList());

        return new PageResult<>(pageInfo.getTotal(), spuBoList);
    }

    @Transactional
    public void saveGoods(SpuBo spuBo) {

        // 保存spu信息
        Spu spu = new Spu();
        BeanUtils.copyProperties(spuBo, spu);
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(true);
        this.spuMapper.insert(spu);

        // 获取spuid，保存spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        this.spuDetailMapper.insertSelective(spuDetail);
        saveSkuAndStock(spuBo, spu.getId());

        this.sendMessage(spu.getId(), "insert");
    }

    private void saveSkuAndStock(SpuBo spuBo, Long spuId) {
        // 保存skus
        List<Sku> skus = spuBo.getSkus();
        skus.forEach(sku -> {
            sku.setSpuId(spuId);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            // 保存库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    @Transactional
    public void updateGoods(SpuBo spuBo) {

        // 根据spuId查询skus
        List<Sku> skus = this.querySkusBySpuId(spuBo.getId());
        List<Object> skuIds = skus.stream().map(sku->sku.getId()).collect(Collectors.toList());

        // 先删除stock
        Example example = new Example(Stock.class);
        example.createCriteria().andIn("skuId", skuIds);
        this.stockMapper.deleteByExample(example);

        // 删除sku
        Example example1 = new Example(Sku.class);
        example1.createCriteria().andIn("id", skuIds);
        this.skuMapper.deleteByExample(example1);

        // 新增stock 新增sku
        this.saveSkuAndStock(spuBo, spuBo.getId());

        // 更新spudetail
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        // 更新spu
        spuBo.setSaleable(null);
        spuBo.setValid(null);
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        this.sendMessage(spuBo.getId(), "update");
    }

    private void sendMessage(Long id, String type){
        try {
            this.amqpTemplate.convertAndSend("leyou.item.exchange", "item." + type, id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    public SpuDetail queryspuDetail(Long spuId) {

        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }

    public List<Sku> querySkusBySpuId(Long id) {
        Sku record = new Sku();
        record.setSpuId(id);
        List<Sku> skuList = this.skuMapper.select(record);
        for (Sku sku : skuList) {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        }
        return skuList;
    }

    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    public Sku querySkuById(Long id) {
        return this.skuMapper.selectByPrimaryKey(id);
    }
}
