package com.xz.aiTest.service;

import com.xz.aiTest.model.dto.question.AiGenerateQuestionRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;

public interface AiGenerate {
    String GenerateQuestion(String appAiSysMessageConfig,String appAiUserMessageConfig,int number);
    SseEmitter doSSEGenarate(AiGenerateQuestionRequest aiGenerateQuestionRequest, HttpServletRequest request);
}
