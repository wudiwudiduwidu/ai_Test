package com.xz.aiTest.manager;

import cn.hutool.json.JSONUtil;
import com.xz.aiTest.common.BaseResponse;
import com.xz.aiTest.common.ErrorCode;
import com.xz.aiTest.common.ResultUtils;
import com.xz.aiTest.exception.ThrowUtils;
import com.xz.aiTest.model.dto.question.AiGenerateQuestionRequest;
import com.xz.aiTest.model.dto.question.QuestionContentDTO;
import com.xz.aiTest.model.entity.App;
import com.xz.aiTest.model.enums.AppTypeEnum;
import com.xz.aiTest.service.AppService;
import com.xz.aiTest.service.impl.AppServiceImpl;
import com.xz.aiTest.utils.ChatGLMUtils;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
@Component
public class AiManager {
    private static final Float DEFAULET_FLOAT = 0.5f;
    private static final Float STABLE_FLOAT = 0.05f;
    private static final Float UNSTABLE_FLOAT = 0.99f;
    @Resource
    private ClientV4 client;
    public Flowable<ModelData> doStreamRequest(String SystemMessage, String UserMessage)
    {
        ChatCompletionRequest chatCompletionRequest = ChatGLMUtils.getChat(SystemMessage, UserMessage,Boolean.TRUE,DEFAULET_FLOAT);
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        //返回流式对象
        return invokeModelApiResp.getFlowable();
    }
    public String doUnStableRequest(String SystemMessage, String UserMessage)
    {
        return doRequest(SystemMessage, UserMessage,Boolean.FALSE,UNSTABLE_FLOAT);
    }
    public String doStableRequest(String SystemMessage, String UserMessage)
    {
        return doRequest(SystemMessage, UserMessage,Boolean.FALSE,STABLE_FLOAT);
    }
    public String doAsyncRequest(String SystemMessage, String UserMessage)
    {
        return doRequest(SystemMessage, UserMessage,Boolean.FALSE,DEFAULET_FLOAT);
    }
public String doContextRequest(String SystemMessage, String UserMessage,List<ChatMessage> assitantMessageList)
{
    //添加前置条件和提问信息
    List<ChatMessage> messages = new ArrayList<>();
    ChatMessage systemMessage = new ChatMessage(ChatMessageRole.USER.value(), UserMessage);
    ChatMessage chatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), SystemMessage);
    messages.add(chatMessage);
    messages.add(systemMessage);
    //把之前的上下文传递进去
    messages.addAll(assitantMessageList);
    // String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
            .model(Constants.ModelChatGLM4Airx)
            .stream(false)
            .invokeMethod(Constants.invokeMethod)
            .messages(messages)
            //.sensitiveWordCheck() 敏感词检测
            //.requestId(requestId)
            .build();
    ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
    return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
}
    public String doRequest(String SystemMessage, String UserMessage,Boolean stream,Float temperature)
    {
       ChatCompletionRequest chatCompletionRequest = ChatGLMUtils.getChat(SystemMessage, UserMessage,stream,temperature);
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
    }


}
