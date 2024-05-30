import request from '@/utils/request'

//使用
export function fetchGateway(params) {
  return request({
    url: '/api/transaction/gateway',
    method: 'post',
    data: params
  });
}