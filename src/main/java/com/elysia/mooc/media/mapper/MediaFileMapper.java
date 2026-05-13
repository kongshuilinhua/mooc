package com.elysia.mooc.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.media.domain.po.MediaFilePO;
import org.apache.ibatis.annotations.Mapper;

/** 媒资文件数据库访问接口。 */
@Mapper
public interface MediaFileMapper extends BaseMapper<MediaFilePO> {
}
