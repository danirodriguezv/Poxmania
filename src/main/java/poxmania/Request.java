package poxmania;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;


@Entity
public class Request {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	private String name;
	private String surnames;
	private String direction;
	private boolean ready;
	
	//Relacion entre la tabla de ITEM y REQUEST
	@OneToMany(fetch = FetchType.LAZY, cascade={CascadeType.ALL})
	private List<Item> items;
	
	public Request(){
	items=new CopyOnWriteArrayList<Item>();
	}
	
	public Request(String name,String surnames,String direction,List<Item> items){
		this.name = name;
		this.surnames = surnames;
		this.direction = direction;
		this.items = items;
		this.ready = false;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;

	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getSurnames() {
		return surnames;
	}

	public void setSurnames(String surnames) {
		this.surnames = surnames;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
