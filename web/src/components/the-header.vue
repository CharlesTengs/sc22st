<template>


  <a-layout-sider width="80" class="custom-sider">
    <div class="logo">Menu</div>
    <a-menu
        class="custom-menu"
        theme="dark"
        mode="vertical"
        :style="{ lineHeight: '64px', height: '100%' }"
    >


      <a-menu-item key="/">
        <router-link to="/">Home</router-link>
      </a-menu-item>

      <!-- Figure part 2, code of adding menu, written in the-header.vue -->
      <a-menu-item key="/admin/ebook" :style="user.id? {} : {display:'none'}">
        <router-link to="/admin/ebook">E-book</router-link>
      </a-menu-item>
      <a-menu-item key="/admin/category" :style="user.id? {} : {display:'none'}">
        <router-link to="/admin/category">Category</router-link>
      </a-menu-item>
      <a-menu-item key="/admin/user" :style="user.id? {} : {display:'none'}">
        <router-link to="/admin/user">User</router-link>
      </a-menu-item>

      <a-menu-item key="login" v-show="!user.id" @click="showLoginModal">
        Login
      </a-menu-item>

      <a-menu-item key="logout" v-show="user.id">
        <a-popconfirm
            title="Confirm logout?"
            ok-text="Yes"
            cancel-text="No"
            @confirm="logout"
        >
          Logout
        </a-popconfirm>
      </a-menu-item>

      <a-menu-item key="hello" v-show="user.id">
        Hi: {{user.name}}
      </a-menu-item>


    </a-menu>

  </a-layout-sider>

    <a-modal
      title="Log In"
      v-model:visible="loginModalVisible"
      :confirm-loading="loginModalLoading"
      @ok="login"
    >
      <a-form :model="loginUser" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="LoginName">
          <a-input v-model:value="loginUser.loginName" />
        </a-form-item>
        <a-form-item label="Password">
          <a-input v-model:value="loginUser.password" type="password" />
        </a-form-item>
      </a-form>
    </a-modal>



</template>

<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import axios from 'axios';
  import { message } from 'ant-design-vue';
  import store from "@/store";

  declare let hexMd5: any;
  declare let KEY: any;

  export default defineComponent({
    name: 'the-header',
    setup () {
      // Save after logging in
      const user = computed(() => store.state.user);

      const loginUser = ref({
        loginName: "test",
        password: "test"
      });
      const loginModalVisible = ref(false);
      const loginModalLoading = ref(false);
      const showLoginModal = () => {
        loginModalVisible.value = true;
      };

      // login
      const login = () => {
        console.log("Start login");
        loginModalLoading.value = true;
        loginUser.value.password = hexMd5(loginUser.value.password + KEY);
        axios.post('/user/login', loginUser.value).then((response) => {
          loginModalLoading.value = false;
          const data = response.data;
          if (data.success) {
            loginModalVisible.value = false;
            message.success("Login successful!");

            store.commit("setUser", data.content);
          } else {
            message.error(data.message);
          }
        });
      };

      // 退出登录
      const logout = () => {
        console.log("Logout initiated");
        axios.get('/user/logout/' + user.value.token).then((response) => {
          const data = response.data;
          if (data.success) {
            message.success("Logout successful!");
            store.commit("setUser", {});
          } else {
            message.error(data.message);
          }
        });
      };

      return {
        loginModalVisible,
        loginModalLoading,
        showLoginModal,
        loginUser,
        login,
        user,
        logout
      }
    }
  });
</script>

<style>
  .logo {
    width: 120px;
    height: 31px;
    /*background: rgba(255, 255, 255, 0.2);*/
    /*margin: 16px 28px 16px 0;*/
    float: left;
    color: white;
    font-size: 18px;
  }


  /* Custom */
  .custom-menu {
    background-color:darkgreen !important;
    font-size: 16px !important;
  }

  .custom-menu .ant-menu-item-selected {
    background-color:darkseagreen !important;
  }

  .header {
    background-color: darkgreen !important;

  }

  .logo {
    font-size: 18px !important;
  }

  .logo {
    width: 100%;
    height: 60px;
    color: white;
    font-size: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  /* Custom */
  .custom-menu {
    background-color: darkgreen !important;
    font-size: 16px !important;
  }

  .custom-menu .ant-menu-item-selected {
    background-color: darkseagreen !important;
  }

  .custom-sider {
    background-color: darkgreen !important;
    height: 100vh;
    position: fixed;
    right: 0;
    top: 0;
  }


</style>
