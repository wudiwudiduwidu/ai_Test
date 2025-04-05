<script setup lang="ts">
import { defineProps, ref, withDefaults } from "vue";

interface Props {
  onFileSelected: (file: File) => void;
}

const props = withDefaults(defineProps<Props>(), {});

const selectedFile = ref<File | null>(null);

const onFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0];
    props.onFileSelected(selectedFile.value);
  }
};
</script>

<template>
  <div>
    <input type="file" @change="onFileChange" />
  </div>
</template>

<style scoped></style>
