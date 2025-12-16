<template>
  <div class="audit-rules-container">
    <div class="header-actions">
      <el-button type="primary" @click="dialogVisible = true">添加敏感词</el-button>
    </div>

    <el-table :data="rules" style="width: 100%; margin-top: 20px;" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="word" label="敏感词" />
      <el-table-column prop="category" label="类别" width="120">
        <template #default="scope">
          <el-tag>{{ scope.row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="等级" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.level === 'High' ? 'danger' : 'warning'">{{ scope.row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button size="small" type="danger" @click="deleteRule(scope.$index, scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="添加敏感词" width="30%">
      <el-form :model="form">
        <el-form-item label="敏感词">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="form.category" placeholder="选择类别">
            <el-option label="政治敏感" value="Politics" />
            <el-option label="暴力恐怖" value="Violence" />
            <el-option label="色情低俗" value="Porn" />
            <el-option label="辱骂谩骂" value="Abuse" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level" placeholder="选择等级">
            <el-option label="高 (直接拦截)" value="High" />
            <el-option label="中 (人工审核)" value="Medium" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addRule">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const dialogVisible = ref(false)
const rules = ref([])

const form = reactive({
  word: '',
  category: '',
  level: ''
})

const fetchRules = async () => {
    try {
        const res = await axios.get('/api/rules/all')
        rules.value = res.data
    } catch (e) {
        console.error(e)
    }
}

onMounted(() => {
    fetchRules()
})

const addRule = async () => {
  if (!form.word || !form.category) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
      await axios.post('/api/rules/add', form)
      ElMessage.success('添加成功')
      dialogVisible.value = false
      form.word = ''
      form.category = ''
      form.level = ''
      fetchRules()
  } catch (e) {
      ElMessage.error('添加失败')
  }
}

const deleteRule = async (index, row) => {
  try {
      await axios.delete(`/api/rules/delete/${row.id}`)
      ElMessage.success('删除成功')
      fetchRules()
  } catch (e) {
      ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.audit-rules-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
</style>
