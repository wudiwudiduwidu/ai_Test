package com.xz.aiTest.model.dto.userAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建用户答案请求
 *

 */
@Data
public class UserAnswerAddRequest implements Serializable {
    /**
     * 唯一Id
     * 预先生成Id 用作幂等处理
     */
    private Long Id;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 用户答案（JSON 数组）
     */
    private List<String> choices;

    private static final long serialVersionUID = 1L;
}
