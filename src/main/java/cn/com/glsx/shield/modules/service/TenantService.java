package cn.com.glsx.shield.modules.service;

import cn.com.glsx.shield.modules.entity.Tenant;
import cn.com.glsx.shield.modules.mapper.TenantMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: taoyr
 **/
@Service
public class TenantService {

    @Resource
    private TenantMapper tenantMapper;

    public Tenant getTenantById(Long tenantId) {
        return tenantMapper.selectById(tenantId);
    }

}
