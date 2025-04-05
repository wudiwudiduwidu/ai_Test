package com.xz.aiTest.service.impl;


import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.xz.aiTest.common.ErrorCode;
import com.xz.aiTest.exception.BusinessException;
import com.xz.aiTest.exception.ThrowUtils;
import com.xz.aiTest.manager.AiManager;
import com.xz.aiTest.model.dto.question.AiGenerateQuestionRequest;
import com.xz.aiTest.model.entity.App;
import com.xz.aiTest.model.entity.User;
import com.xz.aiTest.model.enums.AppTypeEnum;
import com.xz.aiTest.model.enums.UserRoleEnum;
import com.xz.aiTest.service.AiGenerate;
import com.xz.aiTest.utils.ChatGLMUtils;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.service.v4.model.*;
import com.alibaba.fastjson.JSONArray;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
@Service
public class AiGenerateImpl implements AiGenerate {
    @Resource
    private UserServiceImpl userService;
    @Resource
    private Scheduler vipScheduler;
    private static final String GENERATE_QUESTION_SYSTEM_MESSAGE = "你是一位严谨的出题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "应用类别，\n" +
            "要生成的题目数，\n" +
            "每个题目的选项数\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来出题：\n" +
            "1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
            "2. 严格按照下面的 json 格式输出题目和选项\n" +
            "```\n" +
            "[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
            "```\n" +
            "title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
            "3. 检查题目是否包含序号，若包含序号则去除序号\n" +
            "4. 返回的题目列表格式必须为 JSON 数组";
    @Resource
    private ClientV4 client;
    @Resource
    private AppServiceImpl appService;
    @Resource
    private AiManager aiManager;
    @Value("${ai.key}")
    private String key;
    //自动关联上下文进行生成题目，为了防止生成重复题目
    private String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber) {
        String userMessage = app.getAppName() + "\n" +
                app.getAppDesc() + "\n" +
                AppTypeEnum.getEnumByValue(app.getAppType()).getText() + "类" + "\n" +
                questionNumber + "\n" +
                optionNumber;
        return userMessage;
    }

    /**
     *
     * @param number 题目数量
     * @return
     */
    @Override
    public String GenerateQuestion( String appAiSysMessageConfig,String appAiUserMessageConfig,int number) {
        //经过测试，需要每 20 道题一组，ChatGLM 拒绝一次性输出大量题目
        List<ChatMessage> assistantChatMessageList = new ArrayList<>();
        if(number >20)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最大生成题目数量为20");
        }
        while (number >= 10) {
            invokeAiLoop(appAiSysMessageConfig, appAiUserMessageConfig, assistantChatMessageList);
            number = number - 10;
        }
        if (number > 0) {//说明number < 10
            invokeAiLoop(appAiSysMessageConfig, appAiUserMessageConfig, assistantChatMessageList);
        }
        //将结果合并成一个 List 返回
        JSONArray result = new JSONArray();
        for (ChatMessage message : assistantChatMessageList) {
            JSONArray jsonArray = JSONArray.parseArray(String.valueOf(message.getContent()));
            result.addAll(jsonArray);
        }
        return JSON.toJSONString(result);
    }

    @Override
    public SseEmitter doSSEGenarate(AiGenerateQuestionRequest aiGenerateQuestionRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(aiGenerateQuestionRequest == null, ErrorCode.PARAMS_ERROR);
        User user = userService.getLoginUser(request);
        // 获取参数
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = Math.min(aiGenerateQuestionRequest.getQuestionNumber(),20);
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
        int number = aiGenerateQuestionRequest.getQuestionNumber();
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 封装 Prompt
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);
        //建立sse链接
        //后面没带参数，表示不限制连接时间
        SseEmitter sseEmitter = new SseEmitter();
        // AI 生成
        Flowable<ModelData> flowable = aiManager.doStreamRequest(GENERATE_QUESTION_SYSTEM_MESSAGE, userMessage);
        //由于涉及并发 这里采用原子类
        AtomicInteger count = new AtomicInteger(0);
        //拼接成一个字符串
        StringBuilder aQuestion = new StringBuilder();
        Scheduler schedule = Schedulers.io();
        //线程池隔离业务
        if(UserRoleEnum.ADMIN.getValue().equals(user.getUserRole())){
            schedule = vipScheduler;
        }

        //处理订阅消息
        flowable.observeOn(schedule)
                .map(data -> data.getChoices().get(0).getDelta().getContent())
                // 将空白字符移除
                .map(message -> message.replaceAll("\\s", ""))
                // 将字符串转换成分流的字符
                .flatMap(s -> {
                    List<Character> chars = new ArrayList<>();
                    for (char ch : s.toCharArray()) {
                        chars.add(ch);
                    }
                    return Flowable.fromIterable(chars);
                })
                // 左右括号匹配
                .doOnNext(c -> {
                    try {
                        if (c == '{') count.getAndAdd(1);
                        if (count.get() > 0) aQuestion.append(c);
                        if (c == '}') {
                            count.getAndAdd(-1);
                            if (count.get() == 0) {
                                sseEmitter.send(JSONUtil.toJsonStr(aQuestion));
                                log.info("question: {}", aQuestion);
                                // 重置
                                aQuestion.setLength(0);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error in doOnNext: ", e);
                        throw e; // 重新抛出异常以便 doOnError 处理
                    }
                })
                .doOnError(e -> log.error("ai SSE过程错误: ", e))
                .doOnComplete(sseEmitter::complete)
                .subscribe();
        return  sseEmitter;
    }

    private void invokeAiLoop(String appAiSysMessageConfig, String appAiUserMessageConfig, List<ChatMessage> assistantChatMessages) {
        //每次需要重建request，因为一次调用后sokcet通道已经被关闭，复用老的 request 会复用老链接会报错
       ChatCompletionRequest chatCompletionRequest = ChatGLMUtils.getChat(appAiSysMessageConfig, appAiUserMessageConfig, false, 0.5f,assistantChatMessages);
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);

  String s =invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();

        int start = s.indexOf("[");
        int end = s.lastIndexOf("]");
        String content = s.substring(start, end + 1);

        //保存上下文
        assistantChatMessages.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), content));
    }


}
