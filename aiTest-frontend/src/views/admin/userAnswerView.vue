<script setup lang="ts">
import { useRoute } from "vue-router";
import { ref } from "vue";

const route = useRoute();
const userAnswer = ref(JSON.parse(route.query.userAnswer as string));
</script>

<template>
  <h3>该用户选择</h3>

  <a-breadcrumb>
    <div v-for="(item, index) in userAnswer" :key="index">
      <a-breadcrumb-item>{{ index + 1 }}.{{ item }}</a-breadcrumb-item>
    </div>
  </a-breadcrumb>
</template>

<style scoped></style>
