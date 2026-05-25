package com.blogsystem.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blogsystem.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {}
