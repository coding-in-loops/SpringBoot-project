package com.ecom.shopping_cart.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.ecom.shopping_cart.model.OrderRequest;
import com.ecom.shopping_cart.model.ProductOrder;

@Service
public interface OrderService {

	public void saveOrder(Integer userId,OrderRequest orderRequest);
	
	public List<ProductOrder> getOrderByUser(Integer userId);
	
	public ProductOrder updateOrderStatus(Integer id,String status);
	
	public List<ProductOrder> getAllOrders();
	
	public ProductOrder getOrderByOrderId(String orderId);
	
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo,Integer pageSize);
}
