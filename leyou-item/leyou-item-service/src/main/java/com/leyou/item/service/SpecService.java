package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return this.specGroupMapper.select(record);
    }

    public List<SpecParam> queryParamsByGid(Long gid, Long cid, Boolean generic, Boolean searching) {

        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setSearching(searching);
        record.setGeneric(generic);
        return this.specParamMapper.select(record);
    }

    public List<SpecGroup> queryGroupWithParamByCid(Long cid) {

        List<SpecGroup> specGroups = this.queryGroupsByCid(cid);
        specGroups.forEach(group -> {
            group.setParams(this.queryParamsByGid(group.getId(), null, null, null));
        });
        return specGroups;
    }
}
