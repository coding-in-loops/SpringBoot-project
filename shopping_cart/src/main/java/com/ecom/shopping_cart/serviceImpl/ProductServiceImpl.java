package com.ecom.shopping_cart.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.shopping_cart.model.Product;
import com.ecom.shopping_cart.repository.ProductRepository;
import com.ecom.shopping_cart.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Boolean deleteProduct(int id) {
        Product product = productRepository.findById(id).orElse(null);
        if (!ObjectUtils.isEmpty(product)) {
            productRepository.delete(product);
            return true;
        }
        return false;
    }

    @Override
    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product updateProduct(Product product, MultipartFile file) {
        Product dbProduct = getProductById(product.getId());
        if (dbProduct == null) {
            throw new IllegalArgumentException("Product not found with id: " + product.getId());
        }

        String imageName = file.isEmpty() ? dbProduct.getImage() : file.getOriginalFilename();

        // Update product fields
        dbProduct.setTitle(product.getTitle());
        dbProduct.setCategory(product.getCategory());
        dbProduct.setDescription(product.getDescription());
        dbProduct.setPrice(product.getPrice());
        dbProduct.setStock(product.getStock());
        dbProduct.setImage(imageName);
        dbProduct.setDiscount(product.getDiscount());

        // Calculate discount price
        Double discount = product.getPrice() * (product.getDiscount() / 100.0);
        Double discountPrice = product.getPrice() - discount;
        dbProduct.setDiscountPrice(discountPrice);

        Product updatedProduct = productRepository.save(dbProduct);

        if (!file.isEmpty()) {
            try {
                File saveDir = new ClassPathResource("static/img/product_img").getFile();
                if (!saveDir.exists()) {
                    saveDir.mkdirs(); // Ensure directory exists
                }
                Path path = Paths.get(saveDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Image saved at: {}", path);
            } catch (IOException e) {
                logger.error("Error saving image: {}", e.getMessage());
            }
        }
        return updatedProduct;
    }

    @Override
    public List<Product> getAllActiveProducts(String category) {
        if (ObjectUtils.isEmpty(category)) {
            return productRepository.findByIsActiveTrue();
        } else {
            return productRepository.findByCategory(category);
        }
    }

    @Override
    public List<Product> searchProduct(String ch) {
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
    }
    
    @Override
	public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
	}


    @Override
    public Page<Product> getAllActiveProductsPagination(Integer pageNo, Integer pageSize,String category) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct = null;

		if (ObjectUtils.isEmpty(category)) {
			pageProduct = productRepository.findByIsActiveTrue(pageable);
		} else {
			pageProduct = productRepository.findByCategory(pageable, category);
		}
		return pageProduct;
    }
    
    @Override
    public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {
    	Page<Product> pageProduct = null;
		Pageable pageable = PageRequest.of(pageNo, pageSize);

		pageProduct = productRepository.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,
				ch, pageable);

//		if (ObjectUtils.isEmpty(category)) {
//			pageProduct = productRepository.findByIsActiveTrue(pageable);
//		} else {
//			pageProduct = productRepository.findByCategory(pageable, category);
//		}
		return pageProduct;
    }



	@Override
	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable=PageRequest.of(pageNo, pageSize);
		return productRepository.findAll(pageable);
	}
}
