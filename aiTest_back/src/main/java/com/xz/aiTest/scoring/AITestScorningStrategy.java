package com.xz.aiTest.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xz.aiTest.manager.AiManager;
import com.xz.aiTest.model.dto.question.QuestionAnswerDTO;
import com.xz.aiTest.model.dto.question.QuestionContentDTO;
import com.xz.aiTest.model.entity.App;
import com.xz.aiTest.model.entity.Question;
import com.xz.aiTest.model.entity.UserAnswer;
import com.xz.aiTest.service.impl.QuestionServiceImpl;
import com.xz.aiTest.utils.ChatGLMUtils;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AITestScorningStrategy implements ScoringStrategy{
    /**
     * 缓存需求实战
     * 用户对于同一题的相同答案能得到相同结果，那么在ai评分之后可以缓存起来，加快访问速度
     *
     */

    //引入缓存
    private final Cache<String, String> answerCacheMap =
            Caffeine.newBuilder().initialCapacity(1024)
                    // 缓存30分钟移除
                    .expireAfterAccess(30L, TimeUnit.MINUTES)
                    .build();

    /**
     *
     * @param appId
     * @param choices
     * @return 缓存key
     */
    public String getCacheKey(Long appId,String choices){
    return DigestUtils.md5DigestAsHex((appId + choices).getBytes());
}
    @Resource
    private QuestionServiceImpl questionService;
    @Resource
    private AiManager aiManager;
    private static final String AI_TEST_SCORING_SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
            "1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 200 字）\n" +
            "2. 严格按照下面的 json 格式输出评价名称和评价描述\n" +
            "```\n" +
            "{\"resultName\": \"评价名称\", \"resultDesc\": \"评价描述\"}\n" +
            "```\n" +
            "3. 返回格式必须为 JSON 对象";

    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }
@Resource
private RedissonClient redissonClient;
    private static final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";
    @Override
    public UserAnswer doScore(List<String> choices, App app) {
        Long appId = app.getId();
        Question question = questionService.query().eq("appId", appId).one();
        //走缓存判断
        String cacheKey = getCacheKey(appId, JSONUtil.toJsonStr(choices));
        String cacheValue = answerCacheMap.getIfPresent(cacheKey);
        if (StringUtils.isNotBlank(cacheValue)) {
            UserAnswer userAnswer = JSONUtil.toBean(cacheValue, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(JSONUtil.toJsonStr(choices));
            return userAnswer;
        }
//这里说明缓存没有命中 那么避免缓存击穿（热Key问题） 需要分布式锁 也就是第一个来去做缓存,其他等待，然后走缓存
        // redisson有阻塞作用
        RLock lock = redissonClient.getLock(AI_ANSWER_LOCK + cacheKey);

        try {
            //等待3s 20s自动释放
            boolean res = lock.tryLock(3,20, TimeUnit.SECONDS);
            if (res) {
//业务
                String questionContent = question.getQuestionContent();
                List<QuestionContentDTO> questionContentDTOList = JSONUtil.toList(questionContent, QuestionContentDTO.class);

                String userMessage = getAiTestScoringUserMessage(app, questionContentDTOList, choices);
                String s = aiManager.doAsyncRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, userMessage);

                int start = s.indexOf("{");
                int end = s.lastIndexOf("}");
                String answer = s.substring(start, end + 1);

                //直接拷贝属性
                UserAnswer userAnswer = JSONUtil.toBean(answer, UserAnswer.class);
               userAnswer.setAppId(appId);
                userAnswer.setAppId(null);
                userAnswer.setAppType(app.getAppType());
                userAnswer.setScoringStrategy(app.getScoringStrategy());
                userAnswer.setChoices(JSONUtil.toJsonStr(choices));

                answerCacheMap.put(cacheKey, JSONUtil.toJsonStr(userAnswer));
                return userAnswer;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // lock.isHeldByCurrentThread() 确保锁是当前线程上的。
            if(lock !=null && lock.isHeldByCurrentThread()){
                lock.unlock();
            }

        }
        return null;
    }
}
