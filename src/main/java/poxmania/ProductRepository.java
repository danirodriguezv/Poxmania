package poxmania;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product,Long>{ //Repositorio de la clase PRODUCTO
	List<Product> findByNameIgnoreCase(String name);			//Consultas a la base de datos
	List<Product> findByPriceBetween(long min, long max);
	List<Product> findByCategoryIgnoreCase(String category);
	Product findById(long id);
}
