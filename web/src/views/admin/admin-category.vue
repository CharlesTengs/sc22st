<template>
  <a-layout>
    <a-layout-content
      :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <p>
        <a-form layout="inline" :model="param">
          <a-form-item>
            <a-button type="primary" @click="handleQuery()">
              Query
            </a-button>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="add()">
              Add
            </a-button>
          </a-form-item>
        </a-form>
      </p>
      <p>
        <a-alert
                class="tip"
                message="Tip: The categories here will be displayed in the sidebar menu on the homepage."
                type="info"
                closable
        />
      </p>
      <a-table class="custom-table"
        v-if="level1.length > 0"
        :columns="columns"
        :row-key="record => record.id"
        :data-source="level1"
        :loading="loading"
        :pagination="false"
        :defaultExpandAllRows="true"
      >
        <template #cover="{ text: cover }">
          <img v-if="cover" :src="cover" alt="avatar" />
        </template>
        <template v-slot:action="{ text, record }">
          <a-space size="small">
            <a-button type="primary" @click="edit(record)">
              Edit
            </a-button>
            <a-popconfirm
              title="Permanent deletion, confirm?"
              ok-text="Yes"
              cancel-text="No"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="danger">
                Delete
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-layout-content>
  </a-layout>

  <a-modal
    title="Category Form"
    v-model:visible="modalVisible"
    :confirm-loading="modalLoading"
    @ok="handleModalOk"
  >
    <a-form :model="category" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="Name">
        <a-input v-model:value="category.name" />
      </a-form-item>
      <a-form-item label="Parent Category">
        <a-select
          v-model:value="category.parent"
          ref="select"
        >
          <a-select-option :value="0">
            None
          </a-select-option>
          <a-select-option v-for="c in level1" :key="c.id" :value="c.id" :disabled="category.id === c.id">
            {{c.name}}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="Order">
        <a-input v-model:value="category.sort" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import axios from 'axios';
  import { message } from 'ant-design-vue';
  import {Tool} from "@/util/tool";

  export default defineComponent({
    name: 'AdminCategory',
    setup() {
      const param = ref();
      param.value = {};
      const categorys = ref();
      const loading = ref(false);

      const columns = [
        {
          title: 'Name',
          dataIndex: 'name'
        },
        // {
        //   title: 'parent category',
        //   key: 'parent',
        //   dataIndex: 'parent'
        // },
        {
          title: 'Order',
          dataIndex: 'sort'
        },
        {
          title: 'Action',
          key: 'action',
          slots: { customRender: 'action' }
        }
      ];

      /**
       * Primary category tree, where the 'children' attribute represents secondary categories.
       * [{
       *   id: "",
       *   name: "",
       *   children: [{
       *     id: "",
       *     name: "",
       *   }]
       * }]
       */
      const level1 = ref();
      level1.value = [];

      /**
       * data query
       **/
      const handleQuery = () => {
        loading.value = true;
        // If existing data is not cleared, then after editing, saving, and reloading the data
        // if click on edit again, the list will still display the data from before the edit
        level1.value = [];
        axios.get("/category/all").then((response) => {
          loading.value = false;
          const data = response.data;
          if (data.success) {
            categorys.value = data.content;
            console.log("original array:", categorys.value);

            level1.value = [];
            level1.value = Tool.array2Tree(categorys.value, 0);
            console.log("tress structure:", level1);
          } else {
            message.error(data.message);
          }
        });
      };

      // -------- modal ---------
      const category = ref({});
      const modalVisible = ref(false);
      const modalLoading = ref(false);
      const handleModalOk = () => {
        modalLoading.value = true;
        axios.post("/category/save", category.value).then((response) => {
          modalLoading.value = false;
          const data = response.data; // data = commonResp
          if (data.success) {
            modalVisible.value = false;

            // query again
            handleQuery();
          } else {
            message.error(data.message);
          }
        });
      };

      /**
       * edit
       */
      const edit = (record: any) => {
        modalVisible.value = true;
        category.value = Tool.copy(record);
      };

      /**
       * add
       */
      const add = () => {
        modalVisible.value = true;
        category.value = {};
      };

      const handleDelete = (id: number) => {
        axios.delete("/category/delete/" + id).then((response) => {
          const data = response.data; // data = commonResp
          if (data.success) {
            // query again
            handleQuery();
          } else {
            message.error(data.message);
          }
        });
      };

      onMounted(() => {
        handleQuery();
      });

      return {
        param,
        // categorys,
        level1,
        columns,
        loading,
        handleQuery,

        edit,
        add,

        category,
        modalVisible,
        modalLoading,
        handleModalOk,

        handleDelete
      }
    }
  });
</script>

<style scoped>
  img {
    width: 50px;
    height: 50px;
  }
</style>
