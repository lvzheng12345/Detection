<template>
  <div class="settings-container">
    <el-row :gutter="24">
      <!-- Left Column: Profile & Account -->
      <el-col :span="16">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>个人资料</span>
            </div>
          </template>
          <el-form :model="profileForm" label-width="100px">
            <el-form-item label="头像">
              <div class="avatar-wrapper">
                <el-avatar :size="60" :src="avatarUrl" icon="UserFilled" />
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :http-request="uploadAvatar"
                >
                  <el-button type="primary" link>更换头像</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="未绑定邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="未绑定手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="settings-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>安全设置</span>
            </div>
          </template>
          <el-form :model="passwordForm" label-width="100px">
            <el-form-item label="旧密码">
              <el-input v-model="passwordForm.oldPass" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPass" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="passwordForm.confirmPass" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" plain @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- Right Column: Preferences & System -->
      <el-col :span="8">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>偏好设置</span>
            </div>
          </template>
          <div class="preference-item">
            <span>邮件通知</span>
            <el-switch v-model="preferences.emailNotify" />
          </div>
          <div class="preference-item">
            <span>每日报表推送</span>
            <el-switch v-model="preferences.dailyReport" />
          </div>
          <div class="preference-item">
            <span>深色模式</span>
            <el-switch v-model="preferences.darkMode" @change="toggleDarkMode" />
          </div>
        </el-card>

        <el-card class="settings-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>API 凭证</span>
            </div>
          </template>
          <div class="api-key-section">
            <p>您的 API User ID</p>
            <div class="key-display">
              <code>{{ userId }}</code>
              <el-button type="primary" link size="small" @click="copyId">复制</el-button>
            </div>
            <p class="hint">在调用 API 时请携带此 ID 以记录历史。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const emit = defineEmits(['profile-updated'])

const userId = ref('')
const avatarUrl = ref('')
const profileForm = reactive({
  username: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPass: '',
  newPass: '',
  confirmPass: ''
})

const preferences = reactive({
  emailNotify: true,
  dailyReport: false,
  darkMode: false
})

onMounted(() => {
  const user = JSON.parse(localStorage.getItem('user'))
  if (user) {
    userId.value = user.id
    profileForm.username = user.username
    profileForm.email = user.email || ''
    profileForm.phone = user.phone || ''
    avatarUrl.value = user.avatarUrl || '' // Assuming backend returns full URL or handle relative
  }
  
  // Load Dark Mode preference
  const isDark = localStorage.getItem('darkMode') === 'true'
  preferences.darkMode = isDark
  if (isDark) {
    document.documentElement.classList.add('dark')
  }
})

const toggleDarkMode = (val) => {
  if (val) {
    document.documentElement.classList.add('dark')
    localStorage.setItem('darkMode', 'true')
  } else {
    document.documentElement.classList.remove('dark')
    localStorage.setItem('darkMode', 'false')
  }
}

const uploadAvatar = async (options) => {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', userId.value)

  try {
    const response = await axios.post('http://localhost:8080/api/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    // Assuming backend returns the relative path
    // In dev, we might need to prepend host if not proxying, but let's assume simple case
    // Note: Serving static files from Spring Boot requires config. 
    // For this demo, we'll just update the UI state.
    avatarUrl.value = 'http://localhost:8080' + response.data
    
    // Update local storage
    const user = JSON.parse(localStorage.getItem('user'))
    user.avatarUrl = avatarUrl.value
    localStorage.setItem('user', JSON.stringify(user))
    
    ElMessage.success('头像上传成功')
    emit('profile-updated')
  } catch (error) {
    ElMessage.error('头像上传失败')
  }
}

const saveProfile = async () => {
  try {
    await axios.post('http://localhost:8080/api/user/update-profile', {
      userId: userId.value,
      email: profileForm.email,
      phone: profileForm.phone
    })
    
    // Update local storage
    const user = JSON.parse(localStorage.getItem('user'))
    user.email = profileForm.email
    user.phone = profileForm.phone
    localStorage.setItem('user', JSON.stringify(user))
    
    ElMessage.success('个人资料已更新')
    emit('profile-updated')
  } catch (error) {
    ElMessage.error(error.response?.data || '更新失败')
  }
}

const changePassword = async () => {
  if (passwordForm.newPass !== passwordForm.confirmPass) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (!passwordForm.oldPass || !passwordForm.newPass) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  
  try {
    await axios.post('http://localhost:8080/api/user/change-password', {
      userId: userId.value,
      oldPassword: passwordForm.oldPass,
      newPassword: passwordForm.newPass
    })
    ElMessage.success('密码修改成功，请重新登录')
    // Optional: logout user
  } catch (error) {
    ElMessage.error(error.response?.data || '密码修改失败')
  }
}

const copyId = () => {
  navigator.clipboard.writeText(userId.value.toString())
  ElMessage.success('ID 已复制到剪贴板')
}
</script>

<style scoped>
.settings-container {
  max-width: 1200px;
  margin: 0 auto;
}

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
}

.preference-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-light);
}

.preference-item:last-child {
  border-bottom: none;
}

.api-key-section p {
  margin: 5px 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.key-display {
  background: var(--el-fill-color-light);
  padding: 10px;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0;
  border: 1px solid var(--el-border-color-lighter);
}

.key-display code {
  font-family: monospace;
  font-weight: bold;
  color: var(--el-color-primary);
}

.hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
