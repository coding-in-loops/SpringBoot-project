package com.ecom.shopping_cart.serviceImpl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ecom.shopping_cart.model.Cart;
import com.ecom.shopping_cart.model.OrderAddress;
import com.ecom.shopping_cart.model.OrderRequest;
import com.ecom.shopping_cart.model.ProductOrder;
import com.ecom.shopping_cart.repository.CartRepository;
import com.ecom.shopping_cart.repository.ProductOrderRepository;
import com.ecom.shopping_cart.service.OrderService;
import com.ecom.utils.CommonUtil;
import com.ecom.utils.OrderStatus;

import jakarta.mail.MessagingException;

@Service
public class OrderServiceImpl implements OrderService{

	@Autowired
	ProductOrderRepository orderRepository; 
	
	@Autowired
	CartRepository cartRepository;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Override
	public void saveOrder(Integer userId,OrderRequest orderRequest) {
		List<Cart> carts= cartRepository.findByUserId(userId);
		for(Cart cart:carts) {
			ProductOrder order=new ProductOrder();
			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());
			order.setProduct(cart.getProduct());
			order.setPrice(cart.getProduct().getDiscountPrice());
			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());
			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());
			
			OrderAddress orderAddress=new OrderAddress();
			orderAddress.setFirstName(orderRequest.getFirstName());
			orderAddress.setLastName(orderRequest.getLastName());
			orderAddress.setEmail(orderRequest.getEmail());
			orderAddress.setMobileNumber(orderRequest.getMobileNumber());
			orderAddress.setAddress(orderRequest.getAddress());
			orderAddress.setCity(orderRequest.getCity());
			orderAddress.setState(orderRequest.getState());
			orderAddress.setPincode(orderRequest.getPincode());
			
			order.setOrderAddress(orderAddress);
			
			ProductOrder saveOrder=orderRepository.save(order);
			
			
			try {
				commonUtil.sendMailForProductOrder(saveOrder, "Success!");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<ProductOrder> getOrderByUser(Integer userId) {
		List<ProductOrder> orders=orderRepository.findByUserId(userId);
		return orders;
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
		Optional<ProductOrder> findById = orderRepository.findById(id);
		if(findById.isPresent()) {
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder=orderRepository.save(productOrder);
			return updateOrder;
		}
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public ProductOrder getOrderByOrderId(String orderId) {
		return orderRepository.findByOrderId(orderId);
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		PageRequest pageable=PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(pageable);
	}

	

}
