<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item key="0" :style="{ padding: 0, marginRight: '38px' }">
          <div class="titleBar">
            <img
              class="logo"
              src="https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/%E7%94%9F%E6%88%90%E7%89%B9%E5%AE%9A%E9%A3%8E%E6%A0%BC%E5%9B%BE%E7%89%87.png"
            />
            <div class="title">趣云答题平台</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRoutes" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="100px">
      <div v-if="loginUserStore.loginUser.id">
        <div>
          <a-popover position="bottom">
            <a-avatar
              :style="{ backgroundColor: '#3370ff', 'margin-right': '16px' }"
              :image-url="loginUserStore.loginUser.userAvatar"
            >
              {{ loginUserStore.loginUser.userName ?? "无名" }}
            </a-avatar>

            <template #content>
              <div style="justify-items: center">
                <div style="padding: 2px">
                  <a-button style="display: block" @click="handleClick"
                    >修改个人信息
                  </a-button>
                </div>
                <div style="padding: 2px">
                  <a-button style="display: block" @click="userLogout"
                    >退出登录</a-button
                  >
                </div>
              </div>
            </template>
          </a-popover>
          <a-drawer
            :width="400"
            :visible="visible"
            @ok="handleOk"
            @cancel="handleCancel"
            unmountOnClose
          >
            <template #title> 修改你的个人信息</template>
            <div>
              <a-form :model="userImformation">
                <a-form-item label="名字">
                  <a-input v-model="userImformation.userName" />
                </a-form-item>
                <a-form-item label="个人描述">
                  <a-input v-model="userImformation.userProfile" />
                </a-form-item>
                <a-form-item label="上传用户头像">
                  <div>
                    <input type="file" @change="onFileChange" />
                  </div>
                </a-form-item>
              </a-form>
            </div>
          </a-drawer>
        </div>
      </div>
      <div v-else>
        <a-button type="primary" href="/user/login">登录</a-button>
      </div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useLoginUserStore } from "@/store/userStore";
import checkAccess from "@/access/checkAccess";
import myAxios from "@/request";
import { Message } from "@arco-design/web-vue";

const loginUserStore = useLoginUserStore();

const router = useRouter();
// 当前选中的菜单项
const selectedKeys = ref(["/"]);
// 路由跳转时，自动更新选中的菜单项
router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
});
const userImformation = ref({
  userName: "",
  userProfile: "",
});
// 展示在菜单栏的路由数组
const visibleRoutes = computed(() => {
  return routes.filter((item) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (!checkAccess(loginUserStore.loginUser, item.meta?.access as string)) {
      return false;
    }
    return true;
  });
});

// 点击菜单跳转到对应页面
const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};
const visible = ref(false);

const handleClick = () => {
  visible.value = true;
};
const selectedFile = ref<File | null>(null);

const onFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0];
  }
};
const handleOk = async () => {
  let res = await myAxios.post(
    "/api/user/update/imformation",
    userImformation.value
  );
  if (res.data.code !== 0) {
    Message.error("修改失败");
  }
  if (selectedFile.value) {
    const formData = new FormData();
    formData.append("file", selectedFile.value as any);
    res = await myAxios.post("/api/upload/uploadAvatar", formData);
    if (res.data.code !== 0) {
      Message.error("上传头像失败");
      visible.value = false;
      return;
    }
  }
  Message.success("修改成功");
  visible.value = false;
};
const handleCancel = () => {
  visible.value = false;
};
const userLogout = async () => {
  const res = await myAxios.post("/api/user/logout");
  if (res.data.code === 0) {
    Message.success("退出成功");
    await router.push("/user/login");
  } else {
    Message.error("退出失败 ", res.data.message);
  }
};
const toHome = () => {
  router.push("/");
};
</script>

<style scoped>
#globalHeader {
}

.titleBar {
  display: flex;
  align-items: center;
}

.title {
  margin-left: 16px;
  color: black;
}

.logo {
  height: 48px;
}
</style>
