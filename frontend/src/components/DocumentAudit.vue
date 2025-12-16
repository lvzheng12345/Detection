<template>
  <div class="document-audit-container">
    <el-upload
      class="upload-demo"
      drag
      action="#"
      :http-request="uploadFile"
      :before-upload="beforeUpload"
      :show-file-list="false"
      accept=".txt,.pdf,.doc,.docx"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        Drop file here or <em>click to upload</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持 .txt, .pdf, .doc, .docx 文件，大小不超过 10MB
        </div>
      </template>
    </el-upload>

    <div v-if="loading" class="loading-area">
      <el-skeleton :rows="5" animated />
      <p>正在分析文档，请稍候...</p>
    </div>

    <div v-if="result" class="result-area">
      <el-alert
        v-if="result.success && (!result.issues || result.issues.length === 0)"
        title="文档审核通过"
        type="success"
        description="未发现违规或敏感内容。"
        show-icon
        :closable="false"
      />

      <div v-else-if="result.success && result.issues && result.issues.length > 0">
        <el-alert
          title="发现潜在问题"
          type="warning"
          :description="`共发现 ${result.issues.length} 处问题，请参考以下建议进行修改。`"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
        />

        <el-card v-for="(issue, index) in result.issues" :key="index" class="issue-card">
          <template #header>
            <div class="card-header">
              <span class="issue-badge">问题 #{{ index + 1 }}</span>
              <el-tag type="danger" effect="plain">{{ issue.reason }}</el-tag>
            </div>
          </template>
          <div class="issue-content">
            <div class="original-text">
              <h4>原文片段:</h4>
              <p>{{ issue.original }}</p>
            </div>
            <div class="suggestion-text">
              <h4>修改建议:</h4>
              <p>{{ issue.suggestion }}</p>
            </div>
          </div>
        </el-card>
      </div>

      <el-alert
        v-else
        title="审核失败"
        type="error"
        :description="result.message"
        show-icon
        :closable="false"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const result = ref(null)

const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
  }
  return isLt10M
}

const uploadFile = async (options) => {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)

  loading.value = true
  result.value = null

  try {
    const response = await axios.post('http://localhost:8080/api/audit/document', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    result.value = response.data
  } catch (error) {
    console.error('Upload error:', error)
    result.value = {
      success: false,
      message: error.response?.data?.message || '上传或分析过程中发生错误'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.document-audit-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.upload-demo {
  margin-bottom: 30px;
}

.loading-area {
  margin-top: 20px;
  text-align: center;
}

.result-area {
  margin-top: 20px;
}

.issue-card {
  margin-bottom: 15px;
  border-left: 4px solid #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.issue-badge {
  font-weight: bold;
  color: var(--el-text-color-regular);
}

.issue-content h4 {
  margin: 10px 0 5px;
  color: var(--el-text-color-primary);
}

.original-text p {
  background-color: var(--el-color-danger-light-9);
  padding: 10px;
  border-radius: 4px;
  color: var(--el-color-danger);
}

.suggestion-text p {
  background-color: var(--el-color-success-light-9);
  padding: 10px;
  border-radius: 4px;
  color: var(--el-color-success);
}
</style>
