<template>
  <div>
    <el-form :model="form" label-width="0">
      <el-form-item>
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          placeholder="请输入需要检测的文本内容..."
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit" :loading="loading" style="width: 100%">
          开始检测
        </el-button>
      </el-form-item>
    </el-form>

    <div v-if="result" style="margin-top: 20px">
      <el-alert
        :title="result.isSafe ? '检测通过：内容安全' : '检测未通过：存在违规风险'"
        :type="result.isSafe ? 'success' : 'error'"
        show-icon
        :closable="false"
      >
        <template #default>
          <div style="margin-top: 5px">
            <div v-if="!result.isSafe">
              <strong>违规类型：</strong>
              <el-tag type="danger" size="small" style="margin-right: 10px">{{ result.category }}</el-tag>
            </div>
            <div style="margin-top: 5px">
              <strong>详细原因：</strong> {{ result.reason }}
            </div>
          </div>
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['check-completed'])

const loading = ref(false)
const form = reactive({
  content: ''
})
const result = ref(null)

const onSubmit = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  loading.value = true
  result.value = null

  try {
    const user = JSON.parse(localStorage.getItem('user'))
    const response = await axios.post('/api/audit/check', {
      content: form.content,
      userId: user ? user.id : null,
      username: user ? user.username : null
    })
    result.value = response.data
    emit('check-completed')
    ElMessage.success('检测完成')
  } catch (error) {
    console.error(error)
    ElMessage.error('检测失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>
