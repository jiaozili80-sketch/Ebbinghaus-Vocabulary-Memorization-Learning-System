<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <div class="auth-title">
        <div class="brand-logo" style="margin: 0 auto">E</div>
        <h2>欢迎回来</h2>
        <p>登录后继续你的艾宾浩斯复习计划</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>

      <div style="margin-top: 18px; text-align: center">
        <span class="muted">还没有账号？</span>
        <el-button type="primary" link @click="$router.push('/register')">立即注册</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const form = reactive({ username: '', password: '' });

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function submit() {
  await formRef.value?.validate();
  loading.value = true;
  try {
    await userStore.login(form.username, form.password);
    ElMessage.success('登录成功');
    router.push('/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>
