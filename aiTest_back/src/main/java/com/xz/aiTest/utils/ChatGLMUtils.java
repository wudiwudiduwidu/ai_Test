package com.xz.aiTest.utils;

import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;

import java.util.ArrayList;
import java.util.List;

public class ChatGLMUtils {
    public static List<ChatMessage> getMessages(String SystemMessage, String UserMessage)
    {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.USER.value(), UserMessage);
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), SystemMessage);
        messages.add(chatMessage);
        messages.add(systemMessage);
        return messages;
    }

    public static ChatCompletionRequest getChat(String SystemMessage, String UserMessage,Boolean stream,Float temperature){
        //添加前置条件和提问信息
        List<ChatMessage> messages = getMessages(SystemMessage, UserMessage);
        // String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        return ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4Airx)
                .stream(stream)
                //随机数 容易理解点讲，就是回答风格的抽象程度 越大越抽象
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                //.sensitiveWordCheck() 敏感词检测
                //.requestId(requestId)
                .build();
    }
    public static ChatCompletionRequest getChat(String SystemMessage, String UserMessage,Boolean stream,Float temperature,List<ChatMessage> assistantMessages){
        //添加前置条件和提问信息
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.USER.value(), UserMessage);
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), SystemMessage);
        messages.add(chatMessage);
        messages.add(systemMessage);
        messages.addAll(assistantMessages);
        // String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        return ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4Airx)
                .stream(stream)
                //随机数 容易理解点讲，就是回答风格的抽象程度 越大越抽象
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                //.sensitiveWordCheck() 敏感词检测
                //.requestId(requestId)
                .build();
    }

}
