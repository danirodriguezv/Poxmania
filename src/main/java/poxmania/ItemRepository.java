package poxmania;

import org.springframework.data.repository.CrudRepository;  //Repositorio de la clase ITEM

public interface ItemRepository extends CrudRepository<Item,Long>{
	//Consultas a la base de datos
}
