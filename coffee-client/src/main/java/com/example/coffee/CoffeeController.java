package com.example.coffee;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RefreshScope
@RequestMapping("/coffees")
@RestController
public class CoffeeController {

	@Autowired
	private RestTemplate restTemplate;

	@HystrixCommand(fallbackMethod = "getDefaultNames")
	@RequestMapping(path = "/names-hateoas", method = RequestMethod.GET)
	public Collection<String> getNamesHateoas() {
		ParameterizedTypeReference<Resources<Coffee>> typeReference = new ParameterizedTypeReference<Resources<Coffee>>() {
		};
		return restTemplate.exchange("http://coffee-server/coffees", HttpMethod.GET, null, typeReference).getBody().getContent()
			.stream()
			.map(Coffee::getName)
			.collect(Collectors.toList());
	}


	@Autowired
	private CoffeeClient coffeeClient;

	@HystrixCommand(fallbackMethod = "getDefaultNames")
	@RequestMapping(path = "/names-feign", method = RequestMethod.GET)
	public Collection<String> getNamesFeign() {
		return coffeeClient.getCoffees().getContent()
			.stream()
			.map(Coffee::getName)
			.collect(Collectors.toList());
	}

	public Collection<String> getDefaultNames() {
		return Stream.of("hello", "world").collect(Collectors.toList());
	}


	@Value("${coffee.favorite:nothing}")
	private String favoriteCoffee;

	@RequestMapping(path = "/favorite", method = RequestMethod.GET)
	public String getFavoriteCoffee() {
		return favoriteCoffee;
	}


	@Autowired
	private CoffeeOrder coffeeOrder;

	@RequestMapping(path = "/order", method = RequestMethod.POST)
	public void order(@RequestBody String name) {
		coffeeOrder.order(name);
	}

}
