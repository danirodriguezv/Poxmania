package poxmania;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;



@Controller
public class PoxManiaController implements CommandLineRunner{
	
	@Autowired   
	private ProductRepository repository;					//Tablas en la base de datos MySQL
	@Autowired
	private RequestRepository requestRepository;
	@Autowired
	private ItemRepository itemRepository;
	
	
	
	@RequestMapping("/")
	public ModelAndView index() {
		return new ModelAndView("index").addObject("products", repository.findAll()); //Pagina principal
	}
	@RequestMapping("/showOneRequest")
	public ModelAndView showOneRequest(@RequestParam long idRequest, HttpSession session) {
		if(!session.isNew()){
			if(((boolean)session.getAttribute("admin"))){
				Request request=requestRepository.findById(idRequest);
				return new ModelAndView("showItemsRequest").addObject("request",request)
						.addObject("items",request.getItems());
		}
		else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		}else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
	}
	
	
	@RequestMapping("/requestReady")
	public ModelAndView requestReady(@RequestParam long idRequest, HttpSession session) {
		if(!session.isNew()){
			if(((boolean)session.getAttribute("admin"))){
				Request request=requestRepository.findById(idRequest);
				request.setReady(true);
				requestRepository.save(request);
				return new ModelAndView("showRequest").addObject("readys", requestRepository.findByReady(true))
						.addObject("notReadys", requestRepository.findByReady(false));
		}
		else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		}else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
	}
	
	@RequestMapping(value = "/basket")
	public ModelAndView basket(HttpSession session){
		if(session.isNew()){
			List<Item> products= new CopyOnWriteArrayList<>();
			session.setAttribute("currentProducts", products);
			return new ModelAndView("shoppingCart").addObject("productsToBuy",products)  //Mostramos los productos añadidos al carrito
					.addObject("totalPrice",calculateTotalPrice(products));
		}else{
		List<Item> products =(List<Item>) session.getAttribute("currentProducts");
		return new ModelAndView("shoppingCart").addObject("productsToBuy",products)
				.addObject("totalPrice",calculateTotalPrice(products));
		}
	}
	 
	@RequestMapping(value = "/addBasket")
	public ModelAndView addBasket(@RequestParam long idProduct, HttpSession session){  //metodo para añadir al carrito productos
		if(session.isNew()){
			List<Item> products = new CopyOnWriteArrayList<>();
			Item item= new Item(repository.findById(idProduct));
			products.add(item);
			session.setAttribute("currentProducts", products);
			return new ModelAndView("shoppingCart").addObject("productsToBuy", session.getAttribute("currentProducts"))
					.addObject("totalPrice",calculateTotalPrice(products));
		}
		else{
			List<Item> products =(List<Item>) session.getAttribute("currentProducts");
			Iterator<Item> it = products.iterator();
			Product productToAdd=repository.findById(idProduct);
			boolean found=false;
			while ((it.hasNext()) && (!found)) {
				productToAdd=it.next().getProduct();
				if (productToAdd.getId() == idProduct) {
					found = true;
				}
			}
			if(!found){
				Item item= new Item(repository.findById(idProduct));
				products.add(item);
				session.setAttribute("currentProducts", products);
			}
			return new ModelAndView("shoppingCart").addObject("productsToBuy", session.getAttribute("currentProducts"))
					.addObject("totalPrice",calculateTotalPrice(products));
	}
	}
	
	
	@RequestMapping(value="/deleteBasket") 			//Borrar del carrito un producto
	public ModelAndView deleteBasket(@RequestParam long idProduct,
			HttpSession session) {
		List<Item> products = (List<Item>) session.getAttribute("currentProducts");
		Iterator<Item> it = products.iterator();
		boolean found = false;
		Product productToRemove=new Product();
		int count=0;
		while ((it.hasNext()) && (!found)) {
			productToRemove=it.next().getProduct();
			if (productToRemove.getId() == idProduct) {
				found = true;
				products.remove(count);
			}
			count++;
		}
		session.setAttribute("currentProducts", products);
		return new ModelAndView("shoppingCart").addObject("productsToBuy",
				session.getAttribute("currentProducts"))
				.addObject("totalPrice",calculateTotalPrice(products));

	}
	
	private long calculateTotalPrice(List<Item> products){ //Metodo para calcular el precio total de los productos del carrito
		Iterator<Item> it = products.iterator();
		Item product;
		long totalPrice=0;
		while (it.hasNext()) {
			product=it.next();
		totalPrice+=product.getProduct().getPrice() * product.getQuantity();
		}
		return totalPrice;
	}
	
	@RequestMapping(value = "/modifyQuantity")  //Metodo para modificar la cantidad de productos seleccionados en el carrito
	public ModelAndView modifyQuantity(@RequestParam long idProduct, @RequestParam String quantity, HttpSession session){
		int newQuantity= Integer.parseInt(quantity);
		List<Item> products =(List<Item>) session.getAttribute("currentProducts");
		Iterator<Item> it = products.iterator();
		Product productToModify=repository.findById(idProduct);
		int count=0;
		boolean found=false;
		while ((it.hasNext()) && (!found)) {
			productToModify=it.next().getProduct();
			if (productToModify.getId() == idProduct) {
				found = true;
				products.get(count).setQuantity(newQuantity);
			}
			count++;
		}
		return new ModelAndView("shoppingCart").addObject("productsToBuy",session.getAttribute("currentProducts"))
												.addObject("totalPrice",calculateTotalPrice(products));
		
	}

	//Metodos de las busquedas tanto en la pagina principal como en la pagina del administrador
	
	@RequestMapping(value = "/search-product")
	public ModelAndView search(String product){
		return new ModelAndView("index").addObject("products",repository.findByNameIgnoreCase(product));
	}
	
	@RequestMapping(value = "/search-price")
	public ModelAndView search_price(long min,long max){
		return new ModelAndView("index").addObject("products",repository.findByPriceBetween(min,max));
	}
	
	@RequestMapping(value="/search_category_pe")
	public ModelAndView search_category_pe(){
		return new ModelAndView("index").addObject("products",repository.findByCategoryIgnoreCase("Pequeños electrodomésticos"));
	}
	
	@RequestMapping(value="/search_category_t")
	public ModelAndView search_category_t(){
		return new ModelAndView("index").addObject("products",repository.findByCategoryIgnoreCase("Televisiones"));
	}
	
	@RequestMapping(value="/search_category_i")
	public ModelAndView search_category_i(){
		return new ModelAndView("index").addObject("products",repository.findByCategoryIgnoreCase("Informática"));
	}
	
	@RequestMapping(value="/search_category_v")
	public ModelAndView search_category_v(){
		return new ModelAndView("index").addObject("products",repository.findByCategoryIgnoreCase("Videoconsolas"));
	}
	
	//Busquedas del administrador
	@RequestMapping(value= "/search-productAD")
	public ModelAndView searchAD(String product){
		return new ModelAndView("adminProducts").addObject("products",repository.findByNameIgnoreCase(product));
	}
	
	@RequestMapping(value="/search-priceAD")
	public ModelAndView search_priceAD(long min,long max){
		return new ModelAndView("adminProducts").addObject("products",repository.findByPriceBetween(min,max));
	}
	
	@RequestMapping(value= "/search_category_peAD")
	public ModelAndView search_category_peAD(){
		return new ModelAndView("adminProducts").addObject("products",repository.findByCategoryIgnoreCase("Pequeños electrodomésticos"));
	}
	
	@RequestMapping(value="/search_category_tAD")
	public ModelAndView search_category_tAD(){
		return new ModelAndView("adminProducts").addObject("products",repository.findByCategoryIgnoreCase("Televisiones"));
	}
	
	@RequestMapping(value="/search_category_iAD")
	public ModelAndView search_category_iAD(){
		return new ModelAndView("adminProducts").addObject("products",repository.findByCategoryIgnoreCase("Informática"));
	}
	
	@RequestMapping(value="/search_category_vAD")
	public ModelAndView search_category_vAD(){
		return new ModelAndView("adminProducts").addObject("products",repository.findByCategoryIgnoreCase("Videoconsolas"));
	}
	
	@RequestMapping(value="/adminProduct")
	public ModelAndView adminProduct(HttpSession session){  //Metodo para mostrar la base de datos desde el administrador y poder modificarla
		if(!session.isNew()){
			if(((boolean)session.getAttribute("admin"))){ //Controlamos que no se puedan saltar la seguridad de la pagina web
				return new ModelAndView("adminProducts").addObject("products", repository.findAll());	
		}
		else{
			session.invalidate();
			return new ModelAndView("redirect:/");
		}
		}else{
			session.invalidate();
			return new ModelAndView("redirect:/");
		}
	}
	
	@RequestMapping(value="/addProd")
	public ModelAndView addProd(HttpSession session){  //Metodo para ir a la pagina para añadir un producto
		if(!session.isNew()){
			if(((boolean)session.getAttribute("admin"))){
				return new ModelAndView("addProduct");
		}
		else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		}else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
	}
	
	@RequestMapping(value="/log")  //Metodo para ir a la pagina de logeo
	public ModelAndView log(){
		return new ModelAndView("login");
	}
	
	
	@RequestMapping(value="/validated", method = RequestMethod.POST) //Metodo para comprobar la autentificacion del administrador
	public ModelAndView validated(String username,String password, HttpSession session){
		if((username.equals("admin")) && (password.equals("1234"))){
			session.setAttribute("admin", true);
			return new ModelAndView("adminInterface");
		}
		else{
			return new ModelAndView("novalidate");
		}
		}
	
	
	@RequestMapping(value="/backAdmin") //Metodo para volver al panel del administrador
	public ModelAndView backAdmin(HttpSession session){
		if(!session.isNew()){
			if(((boolean)session.getAttribute("admin"))){
				return new ModelAndView("adminInterface");
		}
		else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		}else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		
	}
	
	@RequestMapping(value="/newProduct") //Metodo para añadir el producto a la base de datos
	public ModelAndView newProduct(Product product, HttpSession session){
		if(!session.isNew()){
			if(((boolean)session.getAttribute("admin"))){
				repository.save(product);
				return new ModelAndView("validProduct");
		}
		else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		}else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		
		
	}
	
	
	@RequestMapping(value="/mostrar") //Metodo para mostrar un producto y los atributos correspondientes
	public ModelAndView mostrar(@RequestParam long idProduct) {
		Product product = repository.findById(idProduct);
		return new ModelAndView("show").addObject("product", product);
	}
	
	@RequestMapping(value="/mostraradmin")  //Metodo para mostrar un producto desde la pagina del administrador
	public ModelAndView mostraradmin(@RequestParam long idProduct, HttpSession session) {
		if(!session.isNew()){
			if(((boolean)session.getAttribute("admin"))){
				Product product = repository.findById(idProduct);
				return new ModelAndView("showProAd").addObject("product", product);
		}
		else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		}else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		
	}
	
	@RequestMapping(value="/delet") //Metodo para borrar un producto de la base de datos con su correspondiente comprobacion por seguridad
	public ModelAndView delet(@RequestParam long idProduct,HttpSession session) {
		if(!session.isNew()){
			if(((boolean)session.getAttribute("admin"))){
				repository.delete(idProduct);
				return new ModelAndView("adminProducts").addObject("products", repository.findAll());
		}
		else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		}else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
	}
	
	@RequestMapping(value="/edit")  //Metodo para ir a la pagina de editar un producto desde la pagina con el logeo del administrador
	public ModelAndView edit(Product product){
		repository.save(product);
		
		return new ModelAndView("adminProducts").addObject("products",repository.findAll());
		
	}
	
	
	@RequestMapping(value="/modify")  //Metodo para modificar un producto desde el administrador
	public ModelAndView modify(@RequestParam long idProduct){
		Product product = repository.findById(idProduct);
		return new ModelAndView("showModify").addObject("product",product);
	}
	
	@RequestMapping(value="/finish") //Metodo para guardar los productos del carrito en la base de datos de los pedidos
	public ModelAndView finish(@RequestParam String name,String surnames,String dir, HttpSession session){
		List<Item> products =(List<Item>) session.getAttribute("currentProducts");
		Request request = new Request(name,surnames,dir,products);
		requestRepository.save(request);
		session.invalidate();
		return new ModelAndView("sentFinish").addObject("name",name)
											 .addObject("surnames", surnames)
											 .addObject("dir", dir);
	}
	
	@RequestMapping(value="/showAllRequest") //Metodo para mostrar todos los pedidos de la base de datos
	public ModelAndView showRequest(HttpSession session){
		if(!session.isNew()){
			if(((boolean)session.getAttribute("admin"))){
				return new ModelAndView("showRequest").addObject("readys", requestRepository.findByReady(true))
						.addObject("notReadys", requestRepository.findByReady(false));
		}
		else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
		}else{
			session.invalidate();
			return new ModelAndView("redirect:/log");
		}
	}
	
	@RequestMapping(value="/sendProduct")  //Metodo para enviar el producto donde aparecera una pantalla para recibir el nombre, apellidos y direccion de envio
	public ModelAndView sendProduct(HttpSession session){
		List<Item> products =(List<Item>) session.getAttribute("currentProducts");
		return new ModelAndView("finishShopping").addObject("productsToBuy", products)
				.addObject("totalPrice", calculateTotalPrice(products));
	}
	
	@RequestMapping(value="/disconect") //Metodo para desconectar de la sesion del administrador y anular la sesion creada
	public ModelAndView disconect(HttpSession session){
		session.setAttribute("admin", false);
		session.invalidate();
		return new ModelAndView("index").addObject("products", repository.findAll());
		
	}

	
	@Override
	public void run(String... arg0) throws Exception {  //Metodo que añadira estos productos a la base de datos para probar la funcionalidad implementada
		// TODO Auto-generated method stub
		/*repository.save(new Product("Lavadora","Pequeños electrodomésticos","Venta de lavadora en muy buen estado",425));
		repository.save(new Product("Play Station 3","Videoconsolas","Venta con dos juegos",300));
		repository.save(new Product("Microondas","Pequeños electrodomésticos","Venta en perfecto estado",120));
		repository.save(new Product("XBOX 360","Videoconsolas","Venta con cinco juegos actuales",75));
		repository.save(new Product("Play Station 2","Videoconsolas","Venta con juegos piratas",180));
		repository.save(new Product("TV LG 2000 23''","Televisiones","Venta con mando incluido",278));
		repository.save(new Product("Samsung SmartTV 32''","Televisiones","Venta con gafas 3D",365));
		repository.save(new Product("Usb 500gb","Informática","Bonito bonito",500));*/
		
		
	}
	



}