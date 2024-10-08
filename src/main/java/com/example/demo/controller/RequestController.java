package com.example.demo.controller;

import com.example.demo.config.ConfigServerProperties;
import com.example.demo.config.ConfigurationProperties;
import com.example.demo.exception.MyException;
import com.example.demo.model.Address;
import com.example.demo.model.User;
import com.example.demo.model.dbentities.Book;
import com.example.demo.service.IService;
import com.example.demo.service.ServiceImplementation;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/home")
@Slf4j
public class RequestController {
    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    @Autowired
    RestTemplate restTemplate;
    static List<User> userlist ;
    static {
        userlist = List.of(
          new User("H1",23,"M",new Address("","","","")),
                new User("H2",45,"M",new Address("","","","")),
                new User("H3",67,"M",new Address("","","",""))
        );
    }

    ConfigurationProperties props;
    ConfigServerProperties serverProps;

    ServiceImplementation serviceImplementation;

    IService service;
    public RequestController(ConfigurationProperties props,ConfigServerProperties serverProps,IService service,
                             ServiceImplementation serviceImplementation){
        this.props = props;
        this.serverProps=serverProps;
        this.service = service;
        this.serviceImplementation = serviceImplementation;
    }

    @Value("${spring.application.name:nothing}")
    public String profileName;

    @GetMapping("/dbexample")
    public ResponseEntity<List<Book>> getBookInfo(){
        logger.info("Entering get Book Info controller");
        List<Book> list=serviceImplementation.getBookInformation();
        logger.info("Exiting get Book Info controller {}",list);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/webclientexample")
    public Mono<ResponseEntity<List<User>>> getUsersInformation(){
        logger.info("Entering Controller");
        Mono<ResponseEntity<List<User>>> response= service.getUserInfo().map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
        logger.info("Existing Controller");
        return response;
    }

    @GetMapping("/greeting")
    public String homeController(){
        return "Hello, This is my First Controller After a Long Time"+props.toString();
    }

    @GetMapping("/properties")
    public String getConfigServerProps()  {
        return serverProps.toString();
    }

    @RequestMapping(value = "/NewHome",method = RequestMethod.GET)
    public String home(){
        return "From New Home";
    }

    @PostMapping("/post")
    public String postExample(@RequestBody String str){
        System.out.println(str);
        return str;
    }

    @PostMapping("/postWithRequestParms")
    public String postWithRequestParms(@RequestBody String str){
        System.out.println(str);
        return str;
    }

    @GetMapping(path="/getUserDetails/{name}")
    public User getUserDetails(@PathVariable("name") String name){
        log.info("Method Entry {}",name);

        User u1=userlist.stream().filter(x->x.getName().equalsIgnoreCase(name)).findFirst().orElse(null);

        logger.info("Method End {}",u1);
        return u1;
    }

    @GetMapping(path="/exception")
    public User exceptionThrower(){
        log.info("Method Entry");
        throw new RuntimeException();


    }

    @GetMapping(path="/myException")
    public User myCustomExceptionThrower() throws MyException {
        log.info("Method Entry");
        throw new MyException("My Custom Exception with message");
    }

    @GetMapping(path="/arthexception")
    public User arthmaticException() throws ClassNotFoundException {
        log.info("Arthimetic Exception Method Entry");
        throw new ClassNotFoundException("CLass Not FOund Exception");
    }

    @PostConstruct
    public void init(){
        System.out.println("In rest controller post construct method");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("In rest controller pre destroy method");
    }


    @GetMapping("/restTemplate")
    public User getUserDetails(){

   /* ResponseEntity<User> response=        restTemplate.getForEntity("http://localhost:8080/home/getUserDetails/H1", User.class);*/
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<User> response=restTemplate.exchange("http://microservice-a/home/getUserDetails/H1",HttpMethod.GET,entity, User.class);
    return response.hasBody() ? response.getBody(): null;

    }



}
