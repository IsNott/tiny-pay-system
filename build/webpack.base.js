'use strict'
const path = require('path')
const utils = require('./utils')
const config = require('../config')

function resolve(dir) {
  return path.join(__dirname, '..', dir)
}



module.exports = {
  context: path.resolve(__dirname, '../'),
  entry: {
    app: ['babel-polyfill', './src/main.js']
  },
  output: {
    path: config.build.assetsRoot,
    filename: '[name].js',
    publicPath: process.env.NODE_ENV === 'production' || process.env.NODE_ENV === 'testdev'
      ? config.build.assetsPublicPath
      : config.dev.assetsPublicPath
  },
  resolve: {
    extensions: ['.js', '.vue', '.json'],
    alias: {
      'vue$': 'vue/dist/vue.esm.js',
      '@': resolve('src'),
      'static': path.resolve(__dirname, '../static'),
      'assets': path.resolve(__dirname, '../src/assets'),
    }
  },
  module: {
    rules: [
        {
            test: /\.svg$/,
            loader: 'svg-sprite-loader',
            include: [resolve('src/assets/icons')],
            options: {
                symbolId: 'icon-[name]'//去掉svg这个图片加载不出来
            }
        },

        {
            test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
            loader: 'url-loader',
            //这个不处理svg图片，因为我们独立拆开让svg-sprite-loader来处理了
            exclude: [resolve('src/assets/icons')],

            options: {
                limit: 10000,
                name: utils.assetsPath('img/[name].[hash:7].[ext]')
            }
        },

    ]
  },
  node: {
    // prevent webpack from injecting useless setImmediate polyfill because Vue
    // source contains it (although only uses it if it's native).
    setImmediate: false,
    // prevent webpack from injecting mocks to Node native modules
    // that does not make sense for the client
    dgram: 'empty',
    fs: 'empty',
    net: 'empty',
    tls: 'empty',
    child_process: 'empty'
  }
}

