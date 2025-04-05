package com.xz.aiTest.model.dto.userAnswer;

import lombok.Data;

@Data
public class AppAnswerResultCountDTO {
    // 想象成一个饼图，一部分对应一个结果 是INFP 还是？
    // 结果名称
    private String resultName;
    // 对应个数
    private String resultCount;
}

