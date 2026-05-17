package com.elysia.mooc.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.recommend.domain.po.UserRecommendSnapshotPO;
import org.apache.ibatis.annotations.Mapper;

/** 用户推荐快照 Mapper。 */
@Mapper
public interface UserRecommendSnapshotMapper extends BaseMapper<UserRecommendSnapshotPO> {
}
