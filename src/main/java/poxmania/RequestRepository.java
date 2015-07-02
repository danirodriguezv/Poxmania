package poxmania;



import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface RequestRepository extends CrudRepository<Request,Long>{ //Repositorio de la clase REQUEST
	List<Request> findByReady(boolean ready);  //Consultas a la base de datos
	Request findById(long id);
	
}
