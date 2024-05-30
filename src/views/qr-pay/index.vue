<template>
  <div>
    <transaction-component
    v-show="!qrPayParam"
    v-on:returnParam="handleReturn"
    :payment-type="paymenType"/>
  </div>
</template>
<script>
import TransactionComponent from '~/components/transaction/TransactionComponent.vue';
export default {
  name: 'qr',
  components: { TransactionComponent },
  data(){
    return{
      paymenType: 'qr',
      qrPayParam: ''
    }
  },
  methods:{
    handleReturn(param){
      console.log(param);
      qrPayParam = param
    }
  }
}
</script>

<style scoped>

</style>