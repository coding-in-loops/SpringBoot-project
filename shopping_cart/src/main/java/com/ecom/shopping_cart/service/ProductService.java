package com.ecom.shopping_cart.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.shopping_cart.model.Product;

@Service
public interface ProductService {

	public Product saveProduct(Product product);
	
	public List<Product> getAllProducts();
	
	public Boolean deleteProduct(int id);
	
	public Product getProductById(Integer id);
	
	public Product updateProduct(Product product,MultipartFile file);
	
	public List<Product> getAllActiveProducts(String category);

	public List<Product> searchProduct(String ch);
	
	public Page<Product> getAllActiveProductsPagination(Integer pageNo,Integer pageSize,String category);

	Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch);

	Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch);
	
	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize);
}
