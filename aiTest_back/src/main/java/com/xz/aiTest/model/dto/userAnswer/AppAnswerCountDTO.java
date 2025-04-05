package com.xz.aiTest.model.dto.userAnswer;

import lombok.Data;

/**
 * 用来统计每个app回答次数 根据此进行排名
 */
@Data
public class AppAnswerCountDTO {

    private Long appId;
    private Long answerCount;
}
