package com.xz.aiTest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xz.aiTest.model.dto.userAnswer.AppAnswerCountDTO;
import com.xz.aiTest.model.entity.User;

import java.util.List;

/**
 * 用户数据库操作
 *

 */
public interface UserMapper extends BaseMapper<User> {
List<AppAnswerCountDTO> getRankForApp();
}




