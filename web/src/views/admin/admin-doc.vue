<template>
  <a-layout>
    <a-layout-content
      :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <a-row :gutter="24">
        <a-col :span="8">
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
          <a-table class="custom-table"
            v-if="level1.length > 0"
            :columns="columns"
            :row-key="record => record.id"
            :data-source="level1"
            :loading="loading"
            :pagination="false"
            size="small"
            :defaultExpandAllRows="true"
          >
            <template #name="{ text, record }">
              {{record.sort}} {{text}}
            </template>
            <template v-slot:action="{ text, record }">
              <a-space size="small">
                <a-button type="primary" @click="edit(record)" size="small">
                  Edit
                </a-button>
                <a-popconfirm
                  title="Permanent deletion, confirm?"
                  ok-text="Yes"
                  cancel-text="No"
                  @confirm="handleDelete(record.id)"
                >
                  <a-button type="danger" size="small">
                    Delete
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </a-col>
        <a-col :span="16">
          <p>
            <a-form layout="inline" :model="param">
              <a-form-item>
                <a-button type="primary" @click="handleSave()">
                  Save
                </a-button>
              </a-form-item>
            </a-form>
          </p>
          <a-form :model="doc" layout="vertical">
            <a-form-item>
              <a-input v-model:value="doc.name" placeholder="Name"/>
            </a-form-item>
            <a-form-item>
              <a-tree-select
                v-model:value="doc.parent"
                style="width: 100%"
                :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                :tree-data="treeSelectData"
                placeholder="Please Select Parent Document"
                tree-default-expand-all
                :replaceFields="{title: 'name', key: 'id', value: 'id'}"
              >
              </a-tree-select>
            </a-form-item>
            <a-form-item>
              <a-input v-model:value="doc.sort" placeholder="Order"/>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handlePreviewContent()">
                <EyeOutlined /> Content Preview
              </a-button>
            </a-form-item>
            <a-form-item>
              <div id="content"></div>
            </a-form-item>
          </a-form>
        </a-col>
      </a-row>

      <a-drawer width="900" placement="right" :closable="false" :visible="drawerVisible" @close="onDrawerClose">
        <div class="wangeditor" :innerHTML="previewHtml"></div>
      </a-drawer>

    </a-layout-content>
  </a-layout>


</template>

<script lang="ts">
  import { defineComponent, onMounted, ref, createVNode } from 'vue';
  import axios from 'axios';
  import {message, Modal} from 'ant-design-vue';
  import {Tool} from "@/util/tool";
  import {useRoute} from "vue-router";
  import ExclamationCircleOutlined from "@ant-design/icons-vue/ExclamationCircleOutlined";
  import E from 'wangeditor'

  export default defineComponent({
    name: 'AdminDoc',
    setup() {
      const route = useRoute();
      console.log("route: ", route);
      console.log("route.path：", route.path);
      console.log("route.query：", route.query);
      console.log("route.param：", route.params);
      console.log("route.fullPath：", route.fullPath);
      console.log("route.name：", route.name);
      console.log("route.meta：", route.meta);
      const param = ref();
      param.value = {};
      const docs = ref();
      const loading = ref(false);
      // Because the attribute status of the tree selection component changes with the currently edited node
      // a separate reactive variable is declared.
      const treeSelectData = ref();
      treeSelectData.value = [];

      const columns = [
        {
          title: 'Name',
          dataIndex: 'name',
          slots: { customRender: 'name' }
        },
        {
          title: 'Action',
          key: 'action',
          slots: { customRender: 'action' }
        }
      ];

      /**
       *
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
       * query
       **/
      const handleQuery = () => {
        loading.value = true;
        level1.value = [];
        axios.get("/doc/all/" + route.query.ebookId).then((response) => {
          loading.value = false;
          const data = response.data;
          if (data.success) {
            docs.value = data.content;
            console.log("original array", docs.value);

            level1.value = [];
            level1.value = Tool.array2Tree(docs.value, 0);
            console.log("tree structure", level1);

            // Initialization of the parent document dropdown, equivalent to clicking "Add New"
            treeSelectData.value = Tool.copy(level1.value) || [];

            treeSelectData.value.unshift({id: 0, name: 'None'});
          } else {
            message.error(data.message);
          }
        });
      };

      const doc = ref();
      doc.value = {
        ebookId: route.query.ebookId
      };
      const modalVisible = ref(false);
      const modalLoading = ref(false);
      const editor = new E('#content');
      editor.config.zIndex = 0;
      // Display the "Upload Image" button, convert and store as Base64
      // also support dragging and dropping images.
      editor.config.uploadImgShowBase64 = true;

      const handleSave = () => {
        modalLoading.value = true;
        doc.value.content = editor.txt.html();
        axios.post("/doc/save", doc.value).then((response) => {
          modalLoading.value = false;
          const data = response.data; // data = commonResp
          if (data.success) {
            // modalVisible.value = false;
            message.success("Save successful!");

            handleQuery();
          } else {
            message.error(data.message);
          }
        });
      };

      /**
       * disabled children
       */
      const setDisable = (treeSelectData: any, id: any) => {
        // console.log(treeSelectData, id);

        for (let i = 0; i < treeSelectData.length; i++) {
          const node = treeSelectData[i];
          if (node.id === id) {
            console.log("disabled", node);
            node.disabled = true;

            const children = node.children;
            if (Tool.isNotEmpty(children)) {
              for (let j = 0; j < children.length; j++) {
                setDisable(children, children[j].id)
              }
            }
          } else {
            const children = node.children;
            if (Tool.isNotEmpty(children)) {
              setDisable(children, id);
            }
          }
        }
      };

      const deleteIds: Array<string> = [];
      const deleteNames: Array<string> = [];

      const getDeleteIds = (treeSelectData: any, id: any) => {
        // Traverse the array
        // which means traversing nodes on a certain level
        for (let i = 0; i < treeSelectData.length; i++) {
          const node = treeSelectData[i];
          if (node.id === id) {
            // If the current node is the target node,
            console.log("delete", node);
            // add the target id to the result set 'ids'.
            deleteIds.push(id);
            deleteNames.push(node.name);

            // Iterate through all the child nodes,
            const children = node.children;
            if (Tool.isNotEmpty(children)) {
              for (let j = 0; j < children.length; j++) {
                getDeleteIds(children, children[j].id)
              }
            }
          } else {
            const children = node.children;
            if (Tool.isNotEmpty(children)) {
              getDeleteIds(children, id);
            }
          }
        }
      };

      /**
       * query
       **/
      const handleQueryContent = () => {
        axios.get("/doc/find-content/" + doc.value.id).then((response) => {
          const data = response.data;
          if (data.success) {
            editor.txt.html(data.content)
          } else {
            message.error(data.message);
          }
        });
      };

      /**
       * edit
       */
      const edit = (record: any) => {
        // clear
        editor.txt.html("");
        modalVisible.value = true;
        doc.value = Tool.copy(record);
        handleQueryContent();

        treeSelectData.value = Tool.copy(level1.value);
        setDisable(treeSelectData.value, record.id);

        treeSelectData.value.unshift({id: 0, name: 'None'});
      };

      /**
       * add
       */
      const add = () => {
        // clear
        editor.txt.html("");
        modalVisible.value = true;
        doc.value = {
          ebookId: route.query.ebookId
        };

        treeSelectData.value = Tool.copy(level1.value) || [];

        treeSelectData.value.unshift({id: 0, name: 'None'});
      };

      const handleDelete = (id: number) => {
        // console.log(level1, level1.value, id)
        deleteIds.length = 0;
        deleteNames.length = 0;
        getDeleteIds(level1.value, id);
        Modal.confirm({
          title: 'Important reminder',
          icon: createVNode(ExclamationCircleOutlined),
          content: 'Removing [' + deleteNames.join("，") + "], deletion is irreversible. Confirm deletion?",
          onOk() {
            // console.log(ids)
            axios.delete("/doc/delete/" + deleteIds.join(",")).then((response) => {
              const data = response.data; // data = commonResp
              if (data.success) {
                // Refresh List
                handleQuery();
              } else {
                message.error(data.message);
              }
            });
          },
        });
      };

      // ----------------preview--------------
      const drawerVisible = ref(false);
      const previewHtml = ref();
      const handlePreviewContent = () => {
        const html = editor.txt.html();
        previewHtml.value = html;
        drawerVisible.value = true;
      };
      const onDrawerClose = () => {
        drawerVisible.value = false;
      };

      onMounted(() => {
        handleQuery();

        editor.create();
      });

      return {
        param,
        // docs,
        level1,
        columns,
        loading,
        handleQuery,

        edit,
        add,

        doc,
        modalVisible,
        modalLoading,
        handleSave,

        handleDelete,

        treeSelectData,

        drawerVisible,
        previewHtml,
        handlePreviewContent,
        onDrawerClose,
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
