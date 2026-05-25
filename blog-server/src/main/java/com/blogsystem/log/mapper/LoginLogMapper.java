package com.blogsystem.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blogsystem.log.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {}
