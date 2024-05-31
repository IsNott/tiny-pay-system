<template>
  <div>
    <transaction-component :paymentType="'h5'"/>
  </div>
</template>

<script>
import TransactionComponent from '@/components/transaction/TransactionComponent.vue'
export default {
  name: 'h5',
  components: { TransactionComponent },
  data(){
    return{

    }
  }

}
</script>

<style>

</style>