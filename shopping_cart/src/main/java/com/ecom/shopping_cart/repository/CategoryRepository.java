package com.ecom.shopping_cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.shopping_cart.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Integer>{

	public Boolean existsByName(String name);
	
	List<Category> findByIsActiveTrue();
}
