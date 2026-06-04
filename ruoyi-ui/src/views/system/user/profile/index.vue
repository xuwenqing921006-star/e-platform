<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>修改密码</span>
      </div>
      <resetPwd />
    </el-card>
  </div>
</template>

<script>
import resetPwd from "./resetPwd"

export default {
  name: "Profile",
  components: { resetPwd }
}
</script>
