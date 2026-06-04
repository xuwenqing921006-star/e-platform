<template>
  <div class="login">
    <section class="login-brand">
      <div class="brand-inner">
        <div class="brand-icon">
          <i class="el-icon-office-building"></i>
        </div>
        <h1>{{ title }}</h1>
        <p class="brand-lead">面向公众号 H5 的金融服务内容发布管理后台</p>
        <p class="brand-desc">统一管理服务指引、政策宣传、助企产品与乡村振兴服务信息。</p>
      </div>
    </section>

    <section class="login-panel">
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
        <h2 class="form-title">登录后台</h2>
        <p class="form-subtitle">请输入账号和密码继续操作</p>
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            auto-complete="off"
            placeholder="请输入账号"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            auto-complete="off"
            placeholder="请输入密码"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <el-checkbox v-model="loginForm.rememberMe" class="remember-check">记住密码</el-checkbox>
        <el-button
          :loading="loading"
          type="primary"
          class="login-button"
          @click.native.prevent="handleLogin"
        >
          <span v-if="!loading">登录</span>
          <span v-else>登录中...</span>
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<script>
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      loginForm: {
        username: "",
        password: "",
        rememberMe: false
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ]
      },
      loading: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCookie()
  },
  methods: {
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login {
  display: flex;
  min-height: 100vh;
  background: #f4f7fc;
}

.login-brand {
  width: 52.7%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  background: linear-gradient(180deg, #174ba8 0%, #155bd8 100%);
  color: #fff;
}

.brand-inner {
  width: min(680px, 72%);
  margin: 0 auto;
}

.brand-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 56px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-size: 34px;
}

.brand-inner h1 {
  margin: 0 0 28px;
  font-size: 48px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: 0;
}

.brand-lead {
  margin: 0 0 24px;
  color: rgba(255, 255, 255, 0.88);
  font-size: 21px;
  font-weight: 700;
  line-height: 1.5;
}

.brand-desc {
  margin: 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 17px;
  line-height: 1.9;
}

.login-panel {
  flex: 1;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.login-form {
  width: 392px;
  padding: 34px 38px 28px;
  border: 1px solid #e1e7f0;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 22px 54px rgba(30, 65, 120, 0.13);
  z-index: 1;

  ::v-deep .el-form-item {
    margin-bottom: 18px;
  }

  ::v-deep .el-form-item__label {
    padding-bottom: 10px;
    color: #17233d;
    font-size: 15px;
    font-weight: 700;
    line-height: 1.2;
  }

  .el-input {
    height: 42px;
  }

  ::v-deep .el-input__inner {
    height: 42px;
    border-color: #dce5f2;
    border-radius: 6px;
    color: #17233d;
    line-height: 42px;
  }

  ::v-deep .el-input__inner::placeholder {
    color: #9aa7b7;
  }
}

.form-title {
  margin: 0 0 14px;
  color: #17233d;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
}

.form-subtitle {
  margin: 0 0 24px;
  color: #7a8799;
  font-size: 15px;
}

.remember-check {
  margin: 2px 0 22px;
  color: #7b8799;
  font-weight: 600;
}

.login-button {
  width: 100%;
  height: 44px;
  border-color: #1e5bd7;
  border-radius: 7px;
  background: #1e5bd7;
  font-size: 17px;
  font-weight: 700;
}

@media (max-width: 960px) {
  .login {
    display: block;
  }

  .login-brand {
    width: 100%;
    min-height: 320px;
    padding: 56px 24px;
  }

  .brand-inner {
    width: min(520px, 100%);
  }

  .brand-icon {
    margin-bottom: 32px;
  }

  .brand-inner h1 {
    font-size: 38px;
  }

  .brand-lead {
    font-size: 18px;
  }

  .login-panel {
    min-height: auto;
    padding: 36px 20px;
  }

  .login-form {
    width: min(392px, 100%);
  }
}
</style>
