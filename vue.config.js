const { defineConfig } = require('@vue/cli-service')
const path = require('path');
module.exports = defineConfig({
  transpileDependencies: true,
  configureWebpack:{
    resolve:{
      alias: {
        // 为src目录设置别名
        "~": path.resolve(__dirname,'src'),
        'src': path.resolve(__dirname, './src'),
        'components': path.resolve(__dirname, './src/components'),
        'api': path.resolve(__dirname, './src/api'),
        'utils': path.resolve(__dirname, './src/utils'),
        'store': path.resolve(__dirname, './src/store'),
        'router': path.resolve(__dirname, './src/router'),
        'views': path.resolve(__dirname,'./src/views')
      }
    }
  },
  devServer: {
    open: true,
    host: 'localhost',
    port: 8000,
    https: false,
    client:{
      overlay: false
    },
    proxy: {
        '/api': {
            target: process.env.VUE_APP_BASE_URL,
            ws: true,
            changOrigin: true, //允许跨域
            pathRewrite: {
                '^/api': ''
            }
        }
    }
}
})
