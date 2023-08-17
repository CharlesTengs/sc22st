<template>
  <a-layout>
    <a-layout-content
      :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <p>
        <a-form layout="inline" :model="param">
          <a-form-item>
            <a-input class="custom-input" v-model:value="param.name" placeholder="Name">
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
        :data-source="ebooks"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
      >
        <template #cover="{ text: cover }">
          <img v-if="cover" :src="cover" alt="avatar" />
        </template>
        <template v-slot:category="{ text, record }">
          <span>{{ getCategoryName(record.category1Id) }} / {{ getCategoryName(record.category2Id) }}</span>
        </template>
        <template v-slot:action="{ text, record }">
          <a-space size="small">
            <router-link :to="'/admin/doc?ebookId=' + record.id">
              <a-button type="primary">
                Document Management
              </a-button>
            </router-link>
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
    title="E-book Form"
    v-model:visible="modalVisible"
    :confirm-loading="modalLoading"
    @ok="handleModalOk"
  >
    <a-form :model="ebook" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="Cover">
        <a-input v-model:value="ebook.cover" />
        <a-upload
                v-model:file-list="fileList"
                name="avatar"
                list-type="picture-card"
                class="avatar-uploader"
                :show-upload-list="false"
                :action="SERVER + '/ebook/upload/avatar'"
                :before-upload="beforeUpload"
                @change="handleChange"
        >
          <img v-if="imageUrl" :src="imageUrl" alt="avatar" />
          <div v-else>
            <loading-outlined v-if="coverLoading"></loading-outlined>
            <plus-outlined v-else></plus-outlined>
            <div class="ant-upload-text">Upload</div>
          </div>
        </a-upload>
      </a-form-item>
      <a-form-item label="Name">
        <a-input v-model:value="ebook.name" />
      </a-form-item>
      <a-form-item label="Category">
        <a-cascader
          v-model:value="categoryIds"
          :field-names="{ label: 'name', value: 'id', children: 'children' }"
          :options="level1"
        />
      </a-form-item>
      <a-form-item label="Description">
        <a-input v-model:value="ebook.description" type="textarea" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import axios from 'axios';
  import { message } from 'ant-design-vue';
  import {Tool} from "@/util/tool";

  function getBase64(img: Blob, callback: (base64Url: string) => void) {
    const reader = new FileReader();
    reader.addEventListener('load', () => callback(reader.result as string));
    reader.readAsDataURL(img);
  }

  export default defineComponent({
    name: 'AdminEbook',
    setup() {
      const param = ref();
      param.value = {};
      const ebooks = ref();
      const pagination = ref({
        current: 1,
        pageSize: 4,
        total: 0
      });
      const loading = ref(false);

      const columns = [
        {
          title: 'Cover',
          dataIndex: 'cover',
          slots: { customRender: 'cover' }
        },
        {
          title: 'Name',
          dataIndex: 'name'
        },
        {
          title: 'Category',
          slots: { customRender: 'category' }
        },
        {
          title: 'DocCount',
          dataIndex: 'docCount'
        },
        {
          title: 'ViewCount',
          dataIndex: 'viewCount'
        },
        {
          title: 'VoteCount',
          dataIndex: 'voteCount'
        },
        {
          title: 'Action',
          key: 'action',
          slots: { customRender: 'action' }
        }
      ];


      const handleQuery = (params: any) => {
        loading.value = true;
        ebooks.value = [];
        axios.get("/ebook/list", {
          params: {
            page: params.page,
            size: params.size,
            name: param.value.name
          }
        }).then((response) => {
          loading.value = false;
          const data = response.data;
          if (data.success) {
            ebooks.value = data.content.list;
            // Reset Pagination Button
            pagination.value.current = params.page;
            pagination.value.total = data.content.total;
          } else {
            message.error(data.message);
          }
        });
      };


      const handleTableChange = (pagination: any) => {
        console.log("pagination param" + pagination);
        handleQuery({
          page: pagination.current,
          size: pagination.pageSize
        });
      };


      const categoryIds = ref();
      const ebook = ref();
      const modalVisible = ref(false);
      const modalLoading = ref(false);
      const handleModalOk = () => {
        modalLoading.value = true;
        ebook.value.category1Id = categoryIds.value[0];
        ebook.value.category2Id = categoryIds.value[1];
        axios.post("/ebook/save", ebook.value).then((response) => {
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
        ebook.value = Tool.copy(record);
        categoryIds.value = [ebook.value.category1Id, ebook.value.category2Id]
      };

      const add = () => {  //function to handle click event
        modalVisible.value = true;
        ebook.value = {};
      };

      const handleDelete = (id: number) => {
        axios.delete("/ebook/delete/" + id).then((response) => {
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

      const level1 =  ref();
      let categorys: any;

      const handleQueryCategory = () => {
        loading.value = true;
        axios.get("/category/all").then((response) => {
          loading.value = false;
          const data = response.data;
          if (data.success) {
            categorys = data.content;
            console.log("original array:", categorys);

            level1.value = [];
            level1.value = Tool.array2Tree(categorys, 0);
            console.log("tree structure:", level1.value);

            handleQuery({
              page: 1,
              size: pagination.value.pageSize,
            });
          } else {
            message.error(data.message);
          }
        });
      };

      const getCategoryName = (cid: number) => {
        // console.log(cid)
        let result = "";
        categorys.forEach((item: any) => {
          if (item.id === cid) {
            // return item.name;
            result = item.name;
          }
        });
        return result;
      };

      const SERVER = process.env.VUE_APP_SERVER;
      const fileList = ref([]);
      const coverLoading = ref<boolean>(false);
      const imageUrl = ref<string>('');

      const handleChange = (info: any) => {
        if (info.file.status === 'upcoverLoading') {
          coverLoading.value = true;
          return;
        }
        if (info.file.status === 'done') {
          // Get this url from response in real world.
          getBase64(info.file.originFileObj, (base64Url: string) => {
            imageUrl.value = base64Url;
            coverLoading.value = false;
          });

          ebook.value.cover = SERVER + "/file/" + info.file.name;
        }
        if (info.file.status === 'error') {
          coverLoading.value = false;
          message.error('upload error');
        }
      };

      const beforeUpload = (file: any) => {
        const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
        if (!isJpgOrPng) {
          message.error('You can only upload JPG file!');
        }
        const isLt2M = file.size / 1024 / 1024 < 2;
        if (!isLt2M) {
          message.error('Image must smaller than 2MB!');
        }
        return isJpgOrPng && isLt2M;
      };

      onMounted(() => {
        handleQueryCategory();
      });

      return {
        param,
        ebooks,
        pagination,
        columns,
        loading,
        handleTableChange,
        handleQuery,
        getCategoryName,

        edit,
        add,

        ebook,
        modalVisible,
        modalLoading,
        handleModalOk,
        categoryIds,
        level1,

        handleDelete,

        fileList,
        coverLoading,
        imageUrl,
        handleChange,
        beforeUpload,
        SERVER
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
