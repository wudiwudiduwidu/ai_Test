<script setup lang="ts">
import AppCard from "@/components/AppCard.vue";
import { ref, watchEffect } from "vue";
import myAxios from "@/request";
import { Message } from "@arco-design/web-vue";
import { useRoute, useRouter } from "vue-router";
const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  current: 1,
  pageSize: 10,
});
watchEffect(async () => {
  const res = await myAxios.get("/api/app/picture");
  if (res.data.code === 0) {
    dataList.value = res.data.data;
    total.value = dataList.value.length;
  } else {
    Message.error("获取数据失败，" + res.data.message);
  }
});
const router = useRouter();
const route = useRoute();
const select = (url: string) => {
  router.push({
    path: "/add/app" as any,
    query: {
      url: url,
    },
  });
};
</script>

<template>
  <div id="app">
    <h2 style="text-align: center">app图片推荐(点击即可选择)</h2>
  </div>
  <a-list
    class="list-demo-action-layout"
    :grid-props="{ gutter: [20, 20], sm: 24, md: 12, lg: 8, xl: 6 }"
    :bordered="false"
    :data="dataList"
    :pagination-props="{
      pageSize: searchParams.pageSize,
      current: searchParams.current,
      total,
    }"
  >
    <template #item="{ item }">
      <div style="display: flex; cursor: pointer" @click="select(item.url)">
        <div style="width: 20px; height: 300px"></div>
        <a-card hoverable :style="{ width: '360px' }" :bordered="false">
          <template #cover>
            <div
              :style="{
                height: '300px',
                overflow: 'hidden',
              }"
            >
              <img :style="{ width: '100%' }" alt="dessert" :src="item.url" />
            </div>
          </template>
          <a-card-meta title="">
            <template #description>
              {{ item.title }}
            </template>
          </a-card-meta>
        </a-card>
      </div>
    </template>
  </a-list>
</template>

<style scoped>
#app {
}
</style>
