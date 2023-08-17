<template>
  <a-layout>
    <a-layout-sider width="200" style="border-right: 4px solid darkseagreen">
      <a-menu
        mode="inline"
        :style="{ height: '100%', borderRight: 0, background: 'darkseagreen'}"
        @click="handleClick"
        :openKeys="openKeys"
      >
        <a-menu-item key="welcome" style="background-color: beige; border-right: 4px solid darkseagreen">
          <MailOutlined />
          <span>Welcome</span>
        </a-menu-item>
        <a-sub-menu v-for="item in level1" :key="item.id" :disabled="true">
          <template v-slot:title>
            <span><user-outlined />{{item.name}}</span>
          </template>
          <a-menu-item v-for="child in item.children" :key="child.id" style="background-color: beige;border-right: 1px solid darkseagreen">
            <MailOutlined /><span>{{child.name}}</span>
          </a-menu-item>
        </a-sub-menu>
        <a-menu-item key="tip" :disabled="true">
          <span>Set in category manager</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout-content
    :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
  >
      <div class="welcome" v-show="isShowWelcome">
        <the-welcome></the-welcome>
      </div>


      <a-list
          v-show="!isShowWelcome"
          item-layout="vertical"
          size="large"
          :grid="{ gutter: 20, column: 3 }"
          :data-source="ebooks"
      >
        <template #renderItem="{ item }">
          <a-list-item key="item.name">
            <a-card>
<!--              <template #cover>-->
<!--                <div style="height: 70px; background-color: #f0f0f0;"></div>-->
<!--              </template>-->
              <template #cover>
                <svg width="100%" height="70" xmlns="http://www.w3.org/2000/svg">
                  <!-- Gradient Background -->
                  <defs>
                    <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" style="stop-color:#83a4d4;stop-opacity:1" />
                      <stop offset="100%" style="stop-color:#b6fbff;stop-opacity:1" />
                    </linearGradient>
                  </defs>
                  <rect width="100%" height="70" fill="url(#gradient)" />

                  <!-- Add two decorative lines -->
                  <line x1="10%" y1="10%" x2="90%" y2="10%" stroke="#fff" stroke-width="2"/>
                  <line x1="10%" y1="75%" x2="90%" y2="75%" stroke="#fff" stroke-width="2"/>

                  <!-- Text "COVER" -->
                  <text x="50%" y="50%" font-family="Arial" font-size="30" fill="#fff" text-anchor="middle" dy=".3em">
                    COVER
                  </text>

                  <!-- Two small dot decorations -->
                  <circle cx="10%" cy="35%" r="3" fill="#fff" />
                  <circle cx="90%" cy="35%" r="3" fill="#fff" />
                </svg>
              </template>



              <template #title>
                <router-link :to="'/doc?ebookId=' + item.id">
                  {{ item.name }}
                </router-link>
              </template>

              <template #extra>
                    <span style="margin-right: 12px;">
                        <component v-bind:is="'FileOutlined'" style="margin-right: 8px" />
                        {{ item.docCount }}
                    </span>
                <span style="margin-right: 12px;">
                        <component v-bind:is="'UserOutlined'" style="margin-right: 8px" />
                        {{ item.viewCount }}
                    </span>
                <span>
                        <component v-bind:is="'LikeOutlined'" style="margin-right: 8px" />
                        {{ item.voteCount }}
                    </span>
              </template>

              <p>{{ item.description }}</p>
            </a-card>
          </a-list-item>
        </template>
      </a-list>


  </a-layout-content>
  </a-layout>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, reactive, toRef } from 'vue';
import axios from 'axios';
import { message } from 'ant-design-vue';
import {Tool} from "@/util/tool";
import TheWelcome from '@/components/the-welcome.vue';


export default defineComponent({
  name: 'Home',
  components: {
    TheWelcome
  },
  setup() {
    const ebooks = ref();
    // const ebooks1 = reactive({books: []});

    const openKeys =  ref();

    const level1 =  ref();
    let categorys: any;

    const handleQueryCategory = () => {
      axios.get("/category/all").then((response) => {
        const data = response.data;
        if (data.success) {
          categorys = data.content;
          console.log("original array:", categorys);

          openKeys.value = [];
          for (let i = 0; i < categorys.length; i++) {
            openKeys.value.push(categorys[i].id)
          }

          level1.value = [];
          level1.value = Tool.array2Tree(categorys, 0);
          console.log("tree structure:", level1.value);
        } else {
          message.error(data.message);
        }
      });
    };

    const isShowWelcome = ref(true);
    let categoryId2 = 0;

    const handleQueryEbook = () => {
      axios.get("/ebook/list", {
        params: {
          page: 1,
          size: 1000,
          categoryId2: categoryId2
        }
      }).then((response) => {
        const data = response.data;
        ebooks.value = data.content.list;
      });
    };

    const handleClick = (value: any) => {
      if (value.key === 'welcome') {
        isShowWelcome.value = true;
      } else {
        categoryId2 = value.key;
        isShowWelcome.value = false;
        handleQueryEbook();
      }
    };

    onMounted(() => {
      handleQueryCategory();
    });

    return {
      ebooks,

      pagination: {
        onChange: (page: any) => {
          console.log(page);
        },
        pageSize: 3,
      },

      handleClick,
      level1,

      isShowWelcome,

      openKeys
    }
  }
});
</script>

<style scoped>
  .ant-avatar {
    width: 50px;
    height: 50px;
    line-height: 50px;
    border-radius: 8%;
    margin: 5px 0;
  }

  /*::v-deep  .a-layout-sider .ant-menu-item-selected {*/
  /*  background-color: indianred !important;*/
  /*}*/

</style>
