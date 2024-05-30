<template>
  <div>
    <p>创建交易</p>
    <el-form ref="form" :model="form" label-width="80px">
    <el-form-item label="商品名称">
      <el-input v-model="form.subjectName"></el-input>
    </el-form-item>
    <el-form-item label="商品金额">
      <el-input placeholder="请输入以‘元’为单位的数字" v-model="form.amount"></el-input>
    </el-form-item>
    <el-form-item label="选择厂家">
      <el-select v-model="form.paymentCode" placeholder="请选择">
        <el-option label="支付宝" value="alipay"></el-option>
        <el-option label="微信" disabled value="wx"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="业务方式">
      <el-input placeholder="请输入" disabled v-model="form.paymentType"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">立即创建</el-button>
      <el-button>取消</el-button>
    </el-form-item>
  </el-form>
  </div>
</template>

<script>
import { fetchGateway } from '@/api/transaction/api';
export default {
  name: 'TransactionComponent',
  components: {  },
  props:['paymentType'],
   data() {
    return {
      form: {
        subjectName: '',
        amount: '',
        paymentCode:'',
        paymentType:this.paymentType,
      }
    }
  },
  methods:{
    onSubmit(){
      fetchGateway(this.form).then(res=>{
        console.log(res);
        this.$emit('returnParam',res.data)
      })
    }
  },
 
}
</script>

<style>

</style>