<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <div class="auth-title">
        <div class="brand-logo" style="margin: 0 auto">E</div>
        <h2>创建学习账号</h2>
        <p>开启你的智能单词记忆计划</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名，4-20位" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称，可选" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱，可选" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码，至少6位" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
          注册
        </el-button>
      </el-form>

      <div style="margin-top: 18px; text-align: center">
        <span class="muted">已有账号？</span>
        <el-button type="primary" link @click="$router.push('/login')">返回登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRouter } from 'vue-router';
import { registerApi } from '@/api/auth';

const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);
const form = reactive({ username: '', nickname: '', email: '', password: '' });

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度必须为4-20位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度必须为6-30位', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
};

async function submit() {
  await formRef.value?.validate();
  loading.value = true;
  try {
    await registerApi(form);
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  } finally {
    loading.value = false;
  }
}
</script>
