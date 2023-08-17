<template>
  <a-layout-footer v-show="shouldShow" style="text-align: center">
    Knowledge Base<span v-show="user.id">, welcome: {{user.name}}</span>
  </a-layout-footer>
</template>

<script lang="ts">
import { defineComponent, computed, onMounted, ref } from 'vue';
import store from "@/store";
import { Tool } from "@/util/tool";
import { notification } from 'ant-design-vue';

export default defineComponent({
  name: 'the-footer',
  setup() {
    const user = computed(() => store.state.user);
    const shouldShow = ref(false);

    let websocket: any;
    let token: any;
    const onOpen = () => {
      console.log('WebSocket connection successful, status code:', websocket.readyState);
    };
    const onMessage = (event: any) => {
      console.log('WebSocket received message: ', event.data);
      notification['info']({
        message: 'Received message',
        description: event.data,
      });
    };
    const onError = () => {
      console.log('WebSocket connection error, status code:', websocket.readyState);
    };
    const onClose = () => {
      console.log('WebSocket connection closed, status code:', websocket.readyState);
    };
    const initWebSocket = () => {
      websocket.onopen = onOpen;
      websocket.onmessage = onMessage;
      websocket.onerror = onError;
      websocket.onclose = onClose;
    };

    onMounted(() => {
      // WebSocket
      if ('WebSocket' in window) {
        token = Tool.uuid(10);
        websocket = new WebSocket(process.env.VUE_APP_WS_SERVER + '/ws/' + token);
        initWebSocket();
      } else {
        alert('The current browser is not supported');
      }
    });

    return {
      user,
      shouldShow
    };
  }
});
</script>
