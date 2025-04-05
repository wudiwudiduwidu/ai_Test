<template>
  <div id="homePage">
    <div style="text-align: center; padding: 14px">
      <img src="../../public/title.png" alt="标题" />
    </div>
    <div class="searchBar">
      <div @click="toCreateTest" style="padding: 14px">
        <a-button
          style="width: 275px; background-color: #88619a; color: white"
          :size="'medium'"
        >
          <a-dropdown trigger="hover">
            <p>开始创建属于你的应用</p>
            <template #content>
              <p>创建属于你的应用答题，借助AI,快捷灵活</p>
            </template>
            <template #icon>
              <icon-down />
            </template> </a-dropdown
        ></a-button>
      </div>

      <div>
        <a-input-search
          :style="{ width: '320px' }"
          placeholder="快速发现答题应用"
          button-text="搜索"
          size="large"
          search-button
        />
      </div>
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
      @page-change="onPageChange"
    >
      <template #item="{ item }">
        <div @click="select(item.id)">
          <AppCard :app="item" :app-id="appId" />
        </div>
      </template>
    </a-list>
    <div style="text-align: center" @click="startTest">
      <a-button
        shape="round"
        style="
          width: 600px;
          height: 60px;
          background-color: #88619a;
          color: white;
          font-size: 20px;
          font-weight: bold;
        "
      >
        开始测试
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { DownOutlined } from "@ant-design/icons-vue";
import { ref, watchEffect } from "vue";
import AppCard from "@/components/AppCard.vue";
import API from "@/api";
import { listAppVoByPageUsingPost } from "@/api/appController";
import message from "@arco-design/web-vue/es/message";
import { REVIEW_STATUS_ENUM } from "@/constant/app";
import { SizeType } from "@arco-design/web-vue/es/card/card-meta";
import { useRouter } from "vue-router";
import InstroduceView from "@/components/InstroduceView.vue";
const activeKey = ref(["1"]);
// 初始化搜索条件（不应该被修改）
const initSearchParams = {
  current: 1,
  pageSize: 12,
};

const searchParams = ref<API.AppQueryRequest>({
  ...initSearchParams,
});
const dataList = ref<API.AppVO[]>([]);
const total = ref<number>(0);
const size = ref<SizeType>("large");
/**
 * 加载数据
 */
const appId = ref(0);
const loadData = async () => {
  const params = {
    reviewStatus: REVIEW_STATUS_ENUM.PASS,
    ...searchParams.value,
  };
  const res = await listAppVoByPageUsingPost(params);
  if (res.data.code === 0) {
    dataList.value = res.data.data?.records || [];
    total.value = res.data.data?.total || 0;
    appId.value = dataList.value[0].id as number;
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

/**
 * 当分页变化时，改变搜索条件，触发数据加载
 * @param page
 */
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

// const startTest = () => {
//   router.push({});
// };
/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
const router = useRouter();
const startTest = () => {
  router.push(`/app/detail/${appId.value}`);
};
const select = (id: number) => {
  appId.value = id;
  console.log(appId.value);
};
watchEffect(() => {
  loadData();
});
const toCreateTest = () => {
  router.push("/add/app");
};
</script>

<style scoped>
#homePage {
}

.searchBar {
  width: 100vh;
  margin: 0 auto;
  padding: 20px;
  text-align: center;
}

.list-demo-action-layout .image-area {
  width: 183px;
  height: 119px;
  overflow: hidden;
  border-radius: 2px;
}

.list-demo-action-layout .list-demo-item {
  padding: 20px 0;
  border-bottom: 1px solid var(--color-fill-3);
}

.list-demo-action-layout .image-area img {
  width: 100%;
}

.list-demo-action-layout .arco-list-item-action .arco-icon {
  margin: 0 4px;
}
</style>
