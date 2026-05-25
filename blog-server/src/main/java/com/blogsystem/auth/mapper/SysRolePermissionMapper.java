package com.blogsystem.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blogsystem.auth.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    @Select("<script>" +
            "SELECT p.perm_code FROM sys_permission p " +
            "JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id IN <foreach collection='list' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<String> getPermissionCodesByRoleIds(List<Long> roleIds);
}
