import { createApp } from "vue";

import { createPinia } from "pinia";
import "@arco-design/web-vue/dist/arco.css";
import router from "./router";
import "@/access";
import * as echarts from "echarts/core";
import { TooltipComponent, GridComponent } from "echarts/components";
import { BarChart } from "echarts/charts";
import { CanvasRenderer } from "echarts/renderers";
import "echarts";
import ArcoVue from "@arco-design/web-vue";
// 额外引入图标库
import ArcoVueIcon from "@arco-design/web-vue/es/icon";
import App from "./App.vue";
import "@arco-design/web-vue/dist/arco.css";
echarts.use([TooltipComponent, GridComponent, BarChart, CanvasRenderer]);
const pinia = createPinia();

createApp(App)
  .use(ArcoVue)
  .use(pinia)
  .use(router)
  .use(ArcoVueIcon)
  .mount("#app");
