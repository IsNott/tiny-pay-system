<template>
  <div id="app">
    <el-container class="header-container">
  <el-header>
    <div class="logo">
      Tiny pay
    </div>
  </el-header>
    <el-container class="body-container">
      <el-main><router-view></router-view></el-main>
    </el-container>
    </el-container>
    
  </div>
</template>


<script>
export default {
  name: 'App',
  components: {
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
    /* float: left; */
    line-height: 60px;
  }
.logo{
  width: 8%;
}


  .body-container{
    height: 100%;
  }

  .el-aside {
    background-color: #D3DCE6;
    color: #333;
    text-align: center;
    line-height: 200px;
    height: 100%;
  }
  .el-main {
    background-color: #E9EEF3;
    color: #333;
    text-align: center;
    line-height: 160px;
  }
</style>
