<template>

  <a-layout class="custom-layout">
    <a-layout-content
      :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <p>
        <a-form layout="inline" :model="param">
          <a-form-item>
            <a-input v-model:value="param.loginName" placeholder="LoginName">
            </a-input>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery({page: 1, size: pagination.pageSize})">
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
      <a-table class="custom-table"
        :columns="columns"
        :row-key="record => record.id"
        :data-source="users"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
      >
        <template v-slot:action="{ text, record }">
          <a-space size="small">
            <a-button type="primary" @click="resetPassword(record)">
              Reset Password
            </a-button>
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
    title="User form"
    v-model:visible="modalVisible"
    :confirm-loading="modalLoading"
    @ok="handleModalOk"
  >
    <a-form :model="user" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="LoginName">
        <a-input v-model:value="user.loginName" :disabled="!!user.id"/>
      </a-form-item>
      <a-form-item label="Name">
        <a-input v-model:value="user.name" />
      </a-form-item>
      <a-form-item label="Password" v-show="!user.id">
        <a-input v-model:value="user.password" type="password"/>
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal
    title="Reset Password"
    v-model:visible="resetModalVisible"
    :confirm-loading="resetModalLoading"
    @ok="handleResetModalOk"
  >
    <a-form :model="user" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="New Password">
        <a-input v-model:value="user.password" type="password"/>
      </a-form-item>
    </a-form>
  </a-modal>

</template>

<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import axios from 'axios';
  import { message } from 'ant-design-vue';
  import {Tool} from "@/util/tool";

  declare let hexMd5: any;
  declare let KEY: any;

  export default defineComponent({
    name: 'AdminUser',
    setup() {
      const param = ref();
      param.value = {};
      const users = ref();
      const pagination = ref({
        current: 1,
        pageSize: 10,
        total: 0
      });
      const loading = ref(false);

      const columns = [
        {
          title: 'loginName',
          dataIndex: 'loginName'
        },
        {
          title: 'Name',
          dataIndex: 'name'
        },
        {
          title: 'Password',
          dataIndex: 'password'
        },
        {
          title: 'Action',
          key: 'action',
          slots: { customRender: 'action' }
        }
      ];


      const handleQuery = (params: any) => {
        loading.value = true;
        users.value = [];
        axios.get("/user/list", {
          params: {
            page: params.page,
            size: params.size,
            loginName: param.value.loginName
          }
        }).then((response) => {
          loading.value = false;
          const data = response.data;
          if (data.success) {
            users.value = data.content.list;

            pagination.value.current = params.page;
            pagination.value.total = data.content.total;
          } else {
            message.error(data.message);
          }
        });
      };


      const handleTableChange = (pagination: any) => {
        console.log("看看自带的分页参数都有啥：" + pagination);
        handleQuery({
          page: pagination.current,
          size: pagination.pageSize
        });
      };

      const user = ref();
      const modalVisible = ref(false);
      const modalLoading = ref(false);
      const handleModalOk = () => {
        modalLoading.value = true;

        user.value.password = hexMd5(user.value.password + KEY);

        axios.post("/user/save", user.value).then((response) => {
          modalLoading.value = false;
          const data = response.data; // data = commonResp
          if (data.success) {
            modalVisible.value = false;

            handleQuery({
              page: pagination.value.current,
              size: pagination.value.pageSize,
            });
          } else {
            message.error(data.message);
          }
        });
      };


      const edit = (record: any) => {
        modalVisible.value = true;
        user.value = Tool.copy(record);
      };


      const add = () => {   //function to handle click event
        modalVisible.value = true;
        user.value = {};
      };

      const handleDelete = (id: number) => {
        axios.delete("/user/delete/" + id).then((response) => {
          const data = response.data; // data = commonResp
          if (data.success) {
            handleQuery({
              page: pagination.value.current,
              size: pagination.value.pageSize,
            });
          } else {
            message.error(data.message);
          }
        });
      };

      // -------- reset password ---------
      const resetModalVisible = ref(false);
      const resetModalLoading = ref(false);
      const handleResetModalOk = () => {
        resetModalLoading.value = true;

        user.value.password = hexMd5(user.value.password + KEY);

        axios.post("/user/reset-password", user.value).then((response) => {
          resetModalLoading.value = false;
          const data = response.data; // data = commonResp
          if (data.success) {
            resetModalVisible.value = false;

            handleQuery({
              page: pagination.value.current,
              size: pagination.value.pageSize,
            });
          } else {
            message.error(data.message);
          }
        });
      };

      const resetPassword = (record: any) => {
        resetModalVisible.value = true;
        user.value = Tool.copy(record);
        user.value.password = null;
      };

      onMounted(() => {
        handleQuery({
          page: 1,
          size: pagination.value.pageSize,
        });
      });

      return {
        param,
        users,
        pagination,
        columns,
        loading,
        handleTableChange,
        handleQuery,

        edit,
        add,

        user,
        modalVisible,
        modalLoading,
        handleModalOk,

        handleDelete,

        resetModalVisible,
        resetModalLoading,
        handleResetModalOk,
        resetPassword
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

<style>

.ant-btn-primary {
  background-color: green !important;
  border-color: black !important;
  color: beige !important;
  font-size: 16px !important;

  border-radius: 50px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

.ant-btn-danger {
  background-color: darkred !important;
  border-color: black !important;
  color: beige !important;
  font-size: 16px !important;

  border-radius: 10px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

.custom-table .ant-table-thead th {
  background-color: sandybrown;
  font-size: 16px ;
}

.custom-table .ant-table-tbody td {
  background-color: beige !important;
  border-right-color: black !important;
  border-bottom-color: black !important;
  font-size: 16px ;
}

.custom-table .ant-table-thead th {
  border-right-color: black !important;
  border-bottom-color: black !important;
}

.custom-table .ant-table-row:hover,
.custom-table .ant-table-row:hover td {
  background-color: lightskyblue !important;
}

p {
  background-color: beige !important;
}

.custom-layout {
  background-color: beige !important;
}

.ant-table-wrapper {
  background-color: beige !important;
}

.ant-layout-content {
  background-color: beige !important;
}

.ant-pagination {
  background-color: beige;
}

.tip.ant-alert-info {
  background-color: lightpink !important;
  border-color: black !important;
  font-size: 14px !important;
  font-weight: bold !important;
}


.ant-notification-notice {
  background-color: lightcyan !important;
  border-color: black !important;
  font-weight: bold !important;

  border-radius: 6px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2) !important;
}
.ant-notification-notice-icon {
  color: lightpink !important;
}

.ant-card {
  background-color: floralwhite !important;
  font-weight: bold !important;
  font-size: 20px !important;
  border-radius: 8px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3) !important;
}

.ant-form {
  background-color: beige !important;
}

.ant-modal-content {
  background-color: beige !important;
  font-size: 24px !important;
  font-weight: bold !important;
}

.ant-modal-header {
  background-color: beige !important;
}

.ant-modal-footer {
  background-color: beige !important;
}

button.ant-btn[type="button"] {
  border-color: black !important;
  font-size: 16px !important;
  border-radius: 30px !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

.ant-form,
.ant-form-item,
.ant-form-item-control {
  background-color: beige !important;
}

.ant-popover-inner-content {
  background-color: lightcyan !important;
  border-color: black !important;
  /*border-radius: 20px !important;*/
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

.ant-select-dropdown .ant-select-tree {
  background-color: beige !important;
  border-radius: 5px !important;
  border: 1px solid gray !important;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1) !important;
  font-size: 16px !important;
  font-family: 'Arial', sans-serif !important;
  color: #333 !important;
  padding: 10px !important;
}


</style>

