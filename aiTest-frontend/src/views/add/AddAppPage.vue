<template>
  <div id="addAppPage">
    <div style="display: flex; justify-content: space-between; width: 1100px">
      <div>
        <h2 style="margin-bottom: 32px">创建应用</h2>
        <a-form
          :model="form"
          :style="{ width: '480px' }"
          label-align="left"
          auto-label-width
          @submit="handleSubmit"
        >
          <a-form-item field="appName" label="应用名称">
            <a-input v-model="form.appName" placeholder="请输入应用名称" />
          </a-form-item>
          <a-form-item field="appDesc" label="应用描述">
            <a-input v-model="form.appDesc" placeholder="请输入应用描述" />
          </a-form-item>
          <a-form-item field="appIcon" label="应用图标">
            <div
              style="
                display: flex;
                justify-content: space-between;
                width: 300px;
                align-items: center;
              "
            >
              <div
                v-if="url"
                style="
                  display: flex;
                  justify-content: space-between;
                  width: 100px;
                "
              >
                <div>已选择图片</div>
                <div @click="del">
                  <icon-close-circle-fill size="15" />
                </div>
              </div>
              <div v-else>
                <OSSupload :onFileSelected="handleFile"></OSSupload>
              </div>
              <div>
                <a-button status="normal" type="primary" @click="toRecommend"
                  >图片推荐</a-button
                >
              </div>
            </div>
          </a-form-item>

          <a-form-item field="appType" label="应用类型">
            <a-select
              v-model="form.appType"
              :style="{ width: '320px' }"
              placeholder="请选择应用类型"
            >
              <a-option
                v-for="(value, key) of APP_TYPE_MAP"
                :value="Number(key)"
                :label="value"
              />
            </a-select>
          </a-form-item>
          <a-form-item field="scoringStrategy" label="评分策略">
            <a-select
              v-model="form.scoringStrategy"
              :style="{ width: '320px' }"
              placeholder="请选择评分策略"
            >
              <a-option
                v-for="(value, key) of APP_SCORING_STRATEGY_MAP"
                :value="Number(key)"
                :label="value"
              />
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit" style="width: 120px">
              提交
            </a-button>
          </a-form-item>
        </a-form>
      </div>
      <div>
        <h3>用户须知</h3>
        <p>推荐使用测评类+AI形式进行命题，方便快捷</p>
        <p>
          如果使用自定义评分策略，需自定义评分结果，用户答题之后基于固定算法得出结果
        </p>
        <p>创建应用之后需要通过<b>审核</b>，才可发布</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineProps, onMounted, ref, withDefaults } from "vue";
import API from "@/api";
import message from "@arco-design/web-vue/es/message";
import { useRoute, useRouter } from "vue-router";
import {
  addAppUsingPost,
  editAppUsingPost,
  getAppVoByIdUsingGet,
} from "@/api/appController";
import { APP_SCORING_STRATEGY_MAP, APP_TYPE_MAP } from "@/constant/app";
import OSSupload from "@/components/OSSupload.vue";
import myAxios from "@/request";
import { Message } from "@arco-design/web-vue";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => {
    return "";
  },
});

const router = useRouter();
const formData = new FormData();
const handleFile = async (data: File) => {
  formData.append("file", data);
};
const form = ref({
  appDesc: "",
  appIcon: "",
  appName: "",
  appType: 0,
  scoringStrategy: 0,
} as API.AppAddRequest);

const oldApp = ref<API.AppVO>();
const toRecommend = () => {
  router.push({
    path: "/app/recommend",
  });
};

/**
 * 加载数据
 */
const route = useRoute();
const url = computed(() => {
  return route.query.url;
});
const loadData = async () => {
  if (!props.id) {
    return;
  }
  const res = await getAppVoByIdUsingGet({
    id: props.id as any,
  });
  if (res.data.code === 0 && res.data.data) {
    oldApp.value = res.data.data;
    form.value = res.data.data;
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
  if (url.value) {
    form.value.appIcon = url.value as string;
  }
};

// 获取旧数据
onMounted(() => {
  loadData();
});
const del = () => {
  form.value.appIcon = "";
  router.replace({ path: route.path, query: {} });
};
/**
 * 提交
 */
const handleSubmit = async () => {
  let res: any;
  // 如果是修改
  if (props.id) {
    res = await editAppUsingPost({
      id: props.id as any,
      ...form.value,
    });
  } else {
    // 创建
    res = await addAppUsingPost(form.value);
  }
  if (res.data.code !== 0) {
    message.error("操作失败，" + res.data.message);
    return;
  }
  //如果没有选择推荐图片，则自主上传
  if (!url.value) {
    formData.append("id", res.data.data);
    console.log(res.data.data);
    res = await myAxios.post(`/api/upload/appPicture`, formData);
    if (res.data.code !== 0) {
      Message.error("上传应用图片失败 ", res.data.message);
      return;
    }
  }
  message.success("操作成功，即将跳转到应用详情页");
  setTimeout(() => {
    router.push(`/app/detail/${props.id || res.data.data}`);
  }, 3000);
};
const typeFlag = ref(true);
</script>
