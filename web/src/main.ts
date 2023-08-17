import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/antd.css';
import * as Icons from '@ant-design/icons-vue';
import axios from 'axios';
import {Tool} from "@/util/tool";
import { message } from 'ant-design-vue';
// import 'ant-design-vue/dist/antd.dark.css';  // dark theme

axios.defaults.baseURL = process.env.VUE_APP_SERVER;

/**
 * Axios Interceptors
 */
axios.interceptors.request.use(function (config) {
  console.log('Request Parameters:', config);
  const token = store.state.user.token;
  if (Tool.isNotEmpty(token)) {
    config.headers.token = token;
    console.log("Adding Token to Request Headers:", token);
  }
  return config;
}, error => {
  return Promise.reject(error);
});
axios.interceptors.response.use(function (response) {
  console.log('Response Result:', response);
  return response;
}, error => {
  console.log('Error Response:', error);
  const response = error.response;
  const status = response.status;
  if (status === 401) {
    console.log("Not Logged In, Redirect to Homepage");
    store.commit("setUser", {});
    message.error("Not Logged In or Session Expired");
    router.push('/');
  }
  return Promise.reject(error);
});

const app = createApp(App);
app.use(store).use(router).use(Antd).mount('#app');

// Using Icons Globally
const icons: any = Icons;
for (const i in icons) {
  app.component(i, icons[i]);
}

console.log('Environment:', process.env.NODE_ENV);
console.log('Server Side:', process.env.VUE_APP_SERVER);
