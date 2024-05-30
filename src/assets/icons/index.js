import Vue from 'vue'
import SvgIcon from '@/components/common/SvgIcon'// svg组件

// register globally
Vue.component('svg-icon', SvgIcon)

const req = require.context('./', false, /\.svg$/)
const requireAll = requireContext => requireContext.keys().map(requireContext)
requireAll(req)

