<script setup lang="ts">
import { useRoute } from "vue-router";
import myAxios from "@/request";
import { Message } from "@arco-design/web-vue";
import { ref, watchEffect } from "vue";

const route = useRoute();
const id = route.query.id as any;
const question = ref({});
const questionContent = ref([]);
const localdata = async () => {
  const res = await myAxios.get("/api/question/get/vo?id=" + id);
  if (res.data.code === 0) {
    question.value = res.data.data;
    questionContent.value = question.value.questionContent as any;
    console.log(questionContent.value);
  } else {
    Message.error("获取题目信息失败：" + res.data.message);
  }
};
watchEffect(() => {
  localdata();
});
</script>

<template>
  <h2 style="text-align: center">用户上传题目信息</h2>
  <div v-for="(item, index) in questionContent" :key="index">
    <div style="padding: 14px; background-color: #f0f0f0">
      <div>{{ item.title }}</div>
      <div v-for="(option, j) in item.options" :key="j">
        <div style="padding: 7px; background-color: white; margin-top: 10px">
          <div>选项：{{ option.key }}</div>
          <div>选项结果：{{ option.result }}</div>
          <div>选项分数：{{ option.score }}</div>
          <div>选项值：{{ option.value }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
