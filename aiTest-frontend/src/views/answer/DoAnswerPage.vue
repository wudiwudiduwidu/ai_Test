<template>
  <div id="doAnswerPage" v-if="questionContent.length !== 0">
    <div
      style="
        width: 100%;
        height: 150px;
        background-color: #f9f9f9;
        align-items: center;
      "
    >
      <h1 style="text-align: center">{{ app.appName }}</h1>
      <p style="text-align: center">{{ app.appDesc }}</p>
      <div
        style="padding: 14px; margin-left: 25px; width: 700px; display: flex"
      >
        <div>
          {{ answeredQuestions ? answeredQuestions : 1 }}/{{ totalQuestions }}
        </div>
        <div class="progress-container">
          <div
            class="progress-bar"
            :style="{ width: progressPercentage + '%' }"
          ></div>
        </div>
      </div>
    </div>
    <a-card :bordered="false">
      <h2 style="margin-bottom: 16px">
        {{ currentQuestion?.title }}
      </h2>
      <!--      :options="questionOptions"-->
      <a-radio-group
        v-model="currentAnswer"
        @change="doRadioChange"
        direction="vertical"
      >
        <a-radio
          v-for="(item, index) in questionOptions"
          :key="index"
          :value="item.value"
          style="padding: 5px"
        >
          <div class="border">
            <div
              style="height: 70px; width: 5px; background-color: cornflowerblue"
            ></div>
            <div class="question">{{ item.label }}</div>
          </div>
        </a-radio>
      </a-radio-group>

      <div style="margin-top: 24px">
        <a-space size="large">
          <a-button
            type="primary"
            circle
            v-if="current < questionContent.length"
            :disabled="!currentAnswer"
            @click="current += 1"
          >
            下一题
          </a-button>
          <a-button
            type="primary"
            v-if="current === questionContent.length"
            :loading="submitting"
            circle
            :disabled="!currentAnswer"
            @click="doSubmit"
          >
            {{ submitting ? "评分中" : "查看结果" }}
          </a-button>
          <a-button v-if="current > 1" circle @click="current -= 1">
            上一题
          </a-button>
        </a-space>
      </div>
    </a-card>
  </div>
  <!--这里由于需要走缓存，第一次与redis连接会比较慢，所以搞个过渡-->
  <div v-else>
    <a-spin tips="加载中" />
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  defineProps,
  reactive,
  ref,
  watchEffect,
  withDefaults,
} from "vue";
import API from "@/api";
import { useRouter } from "vue-router";
import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import message from "@arco-design/web-vue/es/message";
import { getAppVoByIdUsingGet } from "@/api/appController";
import myAxios from "@/request";

interface Props {
  appId: string;
}
const totalQuestions = computed(() => {
  return questionContent.value.length;
});
const answeredQuestions = computed(() => {
  return answerList.filter((item) => item).length;
});
const progressPercentage = computed(() => {
  return ((answeredQuestions.value / totalQuestions.value) * 100).toFixed(2);
});
const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const router = useRouter();

const app = ref<API.AppVO>({});
// 题目内容结构（理解为题目列表）
const questionContent = ref<API.QuestionContentDTO[]>([]);

// 当前题目的序号（从 1 开始）
const current = ref(1);
// 当前题目
const currentQuestion = ref<API.QuestionContentDTO>({});
// 当前题目选项
const questionOptions = computed(() => {
  return currentQuestion.value?.options
    ? currentQuestion.value.options.map((option) => {
        return {
          label: `${option.key}. ${option.value}`,
          value: option.key,
        };
      })
    : [];
});
// 当前答案
const currentAnswer = ref<string>();
// 回答列表
const answerList = reactive<string[]>([]);
// 是否正在提交结果
const submitting = ref(false);

/**
 * 加载数据
 */
// 唯一 id
const id = ref<number>();

// 生成唯一 id
const generateId = async () => {
  let res: any = await myAxios.get("/api/userAnswer/generate/id");
  if (res.data.code === 0) {
    id.value = res.data.data as any;
  } else {
    message.error("系统错误，" + res.data.message);
  }
};

const loadData = async () => {
  if (!props.appId) {
    return;
  }
  // 生成唯一 id
  await generateId();
  // 获取 app
  let res: any = await getAppVoByIdUsingGet({
    id: props.appId as any,
  });
  if (res.data.code === 0) {
    app.value = res.data.data as any;
  } else {
    message.error("获取应用失败，" + res.data.message);
  }
  // 获取题目
  res = await listQuestionVoByPageUsingPost({
    appId: props.appId as any,
    current: 1,
    pageSize: 1,
    sortField: "createTime",
    sortOrder: "descend",
  });
  if (res.data.code === 0 && res.data.data?.records) {
    questionContent.value = res.data.data.records[0].questionContent;
  } else {
    message.error("获取题目失败，" + res.data.message);
  }
};

// 获取旧数据
watchEffect(() => {
  loadData();
});

// 改变 current 题号后，会自动更新当前题目和答案
watchEffect(() => {
  currentQuestion.value = questionContent.value[current.value - 1];
  currentAnswer.value = answerList[current.value - 1];
});

/**
 * 选中选项后，保存选项记录
 * @param value
 */
const doRadioChange = (value: string) => {
  answerList[current.value - 1] = value;
};

/**
 * 提交
 */
const doSubmit = async () => {
  if (!props.appId || !answerList) {
    return;
  }
  submitting.value = true;
  const res = await myAxios.post("/api/userAnswer/add", {
    appId: props.appId as any,
    choices: answerList,
    id: id.value,
  });
  if (res.data.code === 0 && res.data.data) {
    router.push(`/answer/result/${res.data.data}`);
  } else {
    message.error("提交答案失败，" + res.data.message);
  }
  submitting.value = false;
};
</script>
<style>
.question {
  margin-left: 5px;
  color: #747474;
  font-size: 20px;
}
.border {
  display: flex;
  align-items: center;
  width: 700px;
  height: 70px;
  border: 2px solid cornflowerblue;
  cursor: pointer;
}
.progress-container {
  width: 700px;
  height: 10px;
  margin-left: 5px;
  overflow: hidden;
  background-color: #e0e0e0;
  border-radius: 10px;
}

.progress-bar {
  height: 100%;
  background-color: #007bff;
  transition: width 0.3s ease-in-out;
}
</style>
