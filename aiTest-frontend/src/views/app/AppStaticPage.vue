<script setup lang="ts">
import myAxios from "@/request";
import VChart from "vue-echarts";
import "echarts";
import { computed, onMounted, ref, watch, watchEffect } from "vue";
import { Message } from "@arco-design/web-vue";
import message from "@arco-design/web-vue/es/message";
const appIdList = ref([]);
const answerCountList = ref([]);
const answerResultList = ref([]);
const appId = ref(1);
const userAnswerCount = computed(() => {
  return {
    tooltip: {
      trigger: "item",
    },
    legend: {
      top: "5%",
      left: "center",
    },
    series: [
      {
        name: "Access From",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: "#fff",
          borderWidth: 2,
        },
        label: {
          show: false,
          position: "center",
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 30,
            fontWeight: "bold",
          },
        },
        labelLine: {
          show: false,
        },
        data: answerResultList.value.map((item: any) => {
          return {
            value: item.resultCount,
            name: item.resultName,
          };
        }),
      },
    ],
  };
});
const appAnswerOption = computed(() => {
  return {
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
      },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: [
      {
        type: "category",
        data: appIdList.value,
        axisTick: {
          alignWithLabel: true,
        },
      },
    ],
    yAxis: [
      {
        type: "value",
      },
    ],
    series: [
      {
        name: "Direct",
        type: "bar",
        barWidth: "40%",
        data: answerCountList.value,
      },
    ],
  };
});
const localData = async () => {
  const res = await myAxios.get("/api/userAnswer/answer_count");
  if (res.data.code === 0) {
    appIdList.value = res.data.data.map((item: any) => item.appId);
    answerCountList.value = res.data.data.map((item: any) => item.answerCount);
  } else {
    Message.error("获取数据失败，" + res.data.message);
  }
  getResult();
};
const getResult = async () => {
  const answerResult = await myAxios.get(
    "/api/userAnswer/answer_result_count?appId=" + appId.value
  );
  if (answerResult.data.code === 0) {
    answerResultList.value = answerResult.data.data;
  } else {
    Message.error("appId不存在");
  }
};
onMounted(() => {
  localData();
});
watch(appId, () => {
  getResult();
});
</script>

<template>
  <div style="padding: 14px">
    <h3 style="text-align: center">app回答次数统计</h3>
    <v-chart :option="appAnswerOption" style="height: 300px" />
  </div>
  <div style="padding: 14px">
    <h3 style="text-align: center">app回答分布</h3>
    <div style="padding: 14px">
      <div class="searchBar">
        <a-input-search
          :style="{ width: '320px' }"
          placeholder="使用appId搜索对应的结果"
          button-text="搜索"
          size="large"
          search-button
          v-model="appId"
        />
      </div>
    </div>
    <v-chart :option="userAnswerCount" style="height: 400px" />
  </div>
</template>

<style scoped>
.searchBar {
  margin-bottom: 28px;
  text-align: center;
}
</style>
