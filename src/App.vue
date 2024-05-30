<template>
  <div id="app">
    <el-container class="header-container">
    <el-header>
      <el-menu class="el-menu-demo" mode="horizontal" style="width:100%" @select="handleSelect">
        <el-menu-item index="0">TinyPay</el-menu-item>
        <el-menu-item index="1"><a href="https://github.com/IsNott/tiny-pay-system" target="_blank">GitHub</a></el-menu-item>
        <el-menu-item index="2" disabled>意见</el-menu-item>
        <el-menu-item index="3" disabled>交流</el-menu-item>
      </el-menu>
    </el-header>
    <el-container class="body-container">
      <el-aside width="200px">
        <el-menu style="height:100%" class="el-menu-vertical-demo" 
        @select="handleAsideMenuSelect"
        :collapse="false">
          <el-menu-item index="1">
            <template slot="title">
              <i class="el-icon-location"></i>
              <span slot="title">QR</span>
            </template>
          </el-menu-item>
          <el-menu-item index="2">
            <i class="el-icon-menu"></i>
            <span slot="title">H5</span>
          </el-menu-item>
          <el-menu-item index="3">
            <i class="el-icon-document"></i>
            <span slot="title">JSAPI</span>
          </el-menu-item>
          <el-menu-item index="4">
            <i class="el-icon-setting"></i>
            <span slot="title">APP</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main><router-view></router-view></el-main>
    </el-container>
    </el-container>
    
  </div>
</template>


<script>
import SvgIcon from './components/common/SvgIcon.vue'
export default {
  name: 'App',
  components: {
    SvgIcon
  },
  methods:{
    handleSelect(index, indexPath) {
      switch(index){
        case "0" :{
          if(this.$route.path!='/Home'){
            this.$router.push({
                path:'/Home',
            })
          }
        }
      }
    },
    handleAsideMenuSelect(index,indexPath){
      switch(index){
        case "1" :{
          if(this.$route.path!='/qr'){
            this.$router.push({
                path:'/qr',
            })
          }
        }
      }
    }
  }
}
</script>

<style>
html,body,#app{
 		height: 100%;
 		width: 100%;
    overflow-x:hidden;
    overflow-y:hidden;
	}
.header-container{
  width: 100%;
  height: 100%;
}
.el-header{
    background-color: #ffffff;
    color: #333;
    text-align: center;
    display: flex;
    flex-direction: row;
    line-height: 60px;
  }
  .logo{
    width: 8%;
  }

  .body-container{
    height: 100%;
  }

  .el-aside {
    text-align: center;
    line-height: 200px;
    height: 100%;
  }
  .el-main {
    /*
    text-align: center;
    line-height: 160px;
     */
  }
</style>
