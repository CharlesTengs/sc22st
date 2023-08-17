<template>
  <a-layout id="components-layout-demo-top-side-2">
    <a-layout-content class="app-content">
      <router-view/>
    </a-layout-content>
    <the-header></the-header> <!-- This will display as a sidebar -->
    <the-footer></the-footer>
  </a-layout>
</template>




<script lang="ts">
  import { defineComponent } from 'vue';
  import TheHeader from '@/components/the-header.vue';
  import TheFooter from '@/components/the-footer.vue';

  export default defineComponent({
    name: 'app',
    components: {
      TheHeader,
      TheFooter,
    },
  });
</script>
