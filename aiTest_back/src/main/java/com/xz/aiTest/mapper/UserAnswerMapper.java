package com.xz.aiTest.mapper;

import com.xz.aiTest.model.dto.userAnswer.AppAnswerResultCountDTO;
import com.xz.aiTest.model.entity.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
* @createDate 2024-05-09 20:41:03
* @Entity com.xz.aiTest.model.entity.UserAnswer
*/
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {
List<AppAnswerResultCountDTO> getAppResultStatic(Long appId);
}




