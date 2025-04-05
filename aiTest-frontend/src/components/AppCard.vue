<template>
  <a-card
    class="appCard"
    hoverable
    :bordered="false"
    :class="{ Border: app.id === props.appId }"
  >
    <template #actions>
      <!--      <span class="icon-hover"> <IconThumbUp /> </span>-->
      <span class="icon-hover"> <IconShareInternal /> </span>
    </template>
    <template #cover>
      <div class="isSelect" v-if="app.id === props.appId">
        <CheckOutlined style="font-size: 25px; color: white"></CheckOutlined>
      </div>
      <div
        :style="{
          height: '204px',
          overflow: 'hidden',
        }"
      >
        <img
          :style="{ width: '100%', transform: 'translateY(-20px)' }"
          :alt="app.appName"
          :src="app.appIcon"
        />
      </div>
    </template>
    <a-card-meta :title="app.appName" :description="app.appDesc">
      <template #avatar>
        <div
          :style="{ display: 'flex', alignItems: 'center', color: '#1D2129' }"
        >
          <a-avatar
            :size="24"
            :image-url="app.user?.userAvatar"
            :style="{ marginRight: '8px' }"
          />
          <a-typography-text
            >{{ app.user?.userName ?? "无名" }}
          </a-typography-text>
        </div>
      </template>
    </a-card-meta>
  </a-card>
</template>

<script setup lang="ts">
import { CheckOutlined } from "@ant-design/icons-vue";
import { IconShareInternal } from "@arco-design/web-vue/es/icon";
import API from "@/api";
import { defineProps, withDefaults } from "vue";
import { useRouter } from "vue-router";

interface Props {
  app: API.AppVO;
  appId: number;
}

const props = withDefaults(defineProps<Props>(), {
  app: () => {
    return {};
  },
  appId: 0,
});

const router = useRouter();
</script>
<style scoped>
.appCard {
  cursor: pointer;
}

.icon-hover {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  transition: all 0.1s;
}

.icon-hover:hover {
  background-color: rgb(var(--gray-2));
}
.isSelect {
  width: 30px;
  height: 30px;
  padding: 2px;
  background-color: #88619a;
  border-bottom-right-radius: 15px;
}
.Border {
  border: 4px solid #88619a;
  border-radius: 5px;
}
</style>
