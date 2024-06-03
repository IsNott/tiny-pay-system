<template>
  <el-card class="box-card">
  <div>
    <transaction-component
    v-show="!qrPayParam"
    v-on:returnParam="handleReturn"
    :payment-type="paymenType"/>
    <div class="pay-qr" v-show="qrPayParam">
        <div style="width:50%">
          <el-descriptions title="订单描述" direction="horizontal" :column="3">
    <el-descriptions-item label="订单号">{{ qrPayParam.orderNo}}</el-descriptions-item>
    <el-descriptions-item label="商品名称">{{ qrPayParam.subjectName }}</el-descriptions-item>
    <el-descriptions-item label="金额" :span="2">{{ qrPayParam.amount }}</el-descriptions-item>
  </el-descriptions>
        </div>
      <vue-qr style="width:10%;margin-top:10px;margin-bottom:10px;" :text="qrPayParam.qrUrl" logoMargin="6" :size="400"></vue-qr>
      <div style="margin-top:10px;margin-bottom:10px;">
        <el-button @click="handleCancel" type="info" size="small">取消</el-button>
      <el-button type="primary" size="small">已完成？</el-button>
      </div>
    </div>
  </div>
</el-card>
</template>
<script>
import VueQr from 'vue-qr'
import TransactionComponent from '~/components/transaction/TransactionComponent.vue';
export default {
  name: 'qr',
  components: { TransactionComponent ,VueQr },
  data(){
    return{
      paymenType: 'qr',
      qrPayParam: '',
    }
  },
  methods:{
    handleReturn(param){
      console.log(param);
      qrPayParam = param
    },
    handleCancel(){
      this.qrPayParam = ''
    }
  }
}
</script>

<style scoped>
.pay-qr{
  display: flex;
  flex-direction: column;
  text-align: center;
  align-items: center;
}
</style>