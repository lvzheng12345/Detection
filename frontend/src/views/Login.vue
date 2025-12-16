<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-header">
        <h1 class="app-title">DeepAudit</h1>
        <p class="app-subtitle">智能内容审核平台</p>
      </div>
      
      <el-card class="login-card">
        <h2 class="login-title">欢迎回来</h2>
        <el-form :model="form" size="large" class="login-form">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="onLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="onLogin" style="width: 100%">
              立即登录
            </el-button>
          </el-form-item>
          <div class="form-footer">
            <span class="no-account">还没有账号？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const onLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const response = await axios.post('/api/user/login', form)
    const user = response.data
    localStorage.setItem('user', JSON.stringify(user))
    ElMessage.success('登录成功')
    if (user.role === 'ADMIN') {
      router.push('/admin')
    } else {
      router.push('/user')
    }
  } catch (error) {
    ElMessage.error(error.response?.data || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
  background-size: cover;
  background-position: center;
}

.login-content {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  color: #fff;
}

.app-title {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.app-subtitle {
  font-size: 16px;
  margin-top: 10px;
  opacity: 0.9;
}

.login-card {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.login-title {
  text-align: center;
  margin: 10px 0 30px;
  color: #303133;
  font-weight: 600;
}

.login-form {
  padding: 0 10px;
}

.form-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}

.no-account {
  color: #606266;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
