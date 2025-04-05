<template>
  <a-button type="outline" @click="handleClick">AI 生成题目</a-button>
  <a-drawer
    :width="340"
    :visible="visible"
    @ok="handleOk"
    @cancel="handleCancel"
    unmountOnClose
  >
    <template #title>AI 生成题目</template>
    <div>
      <a-form
        :model="form"
        label-align="left"
        auto-label-width
        @submit="handleSubmit"
      >
        <a-form-item label="应用 id">
          {{ appId }}
        </a-form-item>
        <a-form-item field="questionNumber" label="题目数量">
          <a-input-number
            min="0"
            max="20"
            v-model="form.questionNumber"
            placeholder="请输入题目数量"
          />
        </a-form-item>
        <a-form-item field="optionNumber" label="选项数量">
          <a-input-number
            min="0"
            max="6"
            v-model="form.optionNumber"
            placeholder="请输入选项数量"
          />
        </a-form-item>
        <a-form-item>
          <a-button
            :loading="submitting"
            type="primary"
            html-type="submit"
            style="width: 120px"
          >
            {{ submitting ? "生成中" : "实时生成" }}
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { defineProps, reactive, ref, withDefaults } from "vue";
import API from "@/api";
import message from "@arco-design/web-vue/es/message";

interface Props {
  appId: string;
  onSuccess?: (result: API.QuestionContentDTO) => void;
  //这里传进来表示状态一致
  submitting: boolean;
  //子传父 把数据传出去
  changeSubmit: (submitting: boolean) => void;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const form = reactive({
  optionNumber: 2,
  questionNumber: 10,
} as API.AiGenerateQuestionRequest);

const visible = ref(false);
const submitting = ref(props.submitting);
const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
};
const handleCancel = () => {
  visible.value = false;
};

/**
 * 提交
 */
const handleSubmit = async () => {
  if (!props.appId) {
    message.error("应用不存在");
    return;
  }
  submitting.value = true;
  visible.value = true;
  //这里调用后端sse实时通信接口进行流式返回数据
  // 记得携带cookie
  const eventSource = new EventSource(
    "http://8.148.226.174" +
      `/api/question/ai_generate/sse?appId=${props.appId}&optionNumber=${form.optionNumber}&questionNumber=${form.questionNumber}`,
    { withCredentials: true }
  );
  eventSource.onmessage = (event) => {
    console.log(event.data);
    props.onSuccess(JSON.parse(event.data));
  };
  eventSource.onerror = (event) => {
    if (event.eventPhase === EventSource.CLOSED) {
      console.log("连接关闭");
    }
    //统一关闭连接和开放按钮
    eventSource.close();
    submitting.value = false;
    props.changeSubmit(false);
    message.success("生成完毕,请检查是否符合要求");
  };
  eventSource.onopen = () => {
    console.log("开始连接");
    message.success("开始生成题目,请不要关闭当前页面");
    props.changeSubmit(true);
  };
};
</script>
