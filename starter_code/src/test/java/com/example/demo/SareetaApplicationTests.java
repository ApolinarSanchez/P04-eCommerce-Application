package com.example.demo;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.AddItemRequest;
import com.example.demo.model.requests.LoginRequest;
import com.example.demo.model.requests.CreateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class SareetaApplicationTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JacksonTester<CreateUserRequest> jsonCreateUserRequest;

	@Autowired
	JacksonTester<LoginRequest> jsonCreateLoginRequest;

	@Autowired
	JacksonTester<AddItemRequest> jsonAddItemRequest;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CartRepository cartRepository;

	private String token = null;


	@Test
	public void testCreateUserAndLogin() throws Exception {
		createUser();
		login();
	}
	@Test
	public void testGetUserById() throws Exception {
		createUser();
		String token = login();
		MvcResult mvcResult = mvc.perform(
				get(new URI("/api/user/id/1"))
				.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is("test")))
				.andReturn();

	}

	@Test
	public void testGetUserByUsername() throws Exception {
		createUser();
		String token = login();
		MvcResult mvcResult = mvc.perform(
				get(new URI("/api/user/test"))
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is("test")))
				.andReturn();

	}

	@Test
	public void testGetAllItems() throws Exception {
		createUser();
		String token = login();

		mvc.perform(
				get(new URI("/api/item"))
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[1].name", is("Square Widget")))
				.andReturn();
	}

	@Test
	public void testGetItemById() throws Exception {
		createUser();
		String token = login();

		mvc.perform(
				get(new URI("/api/item/1"))
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Round Widget")))
				.andReturn();
	}

	@Test
	public void testGetItemByName() throws Exception {
		createUser();
		String token = login();
		mvc.perform(
				get(new URI("/api/item/name/Round%20Widget"))
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is(1)))
				.andReturn();
	}

	@Test
	public void testAddItemsToCart() throws Exception {
		createUser();
		String token = login();
		addItemsToCart();

	}

	@Test
	public void testRemoveItemsFromCart() throws Exception {
		createUser();
		String token = login();
		AddItemRequest item = new AddItemRequest();
		item.setUsername("test");
		item.setItemId(1L);
		item.setQuantity(3);

		mvc.perform(
				post(new URI("/api/cart/removeFromCart"))
						.header("Authorization", token)
						.content(jsonAddItemRequest.write(item).getJson())
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.accept(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(2)));
	}

	public void addItemsToCart() throws Exception {
		createUser();
		String token = login();

		AddItemRequest item = new AddItemRequest();
		item.setUsername("test");
		item.setItemId(1L);
		item.setQuantity(5);
		mvc.perform(
				post(new URI("/api/cart/addToCart"))
						.header("Authorization", token)
						.content(jsonAddItemRequest.write(item).getJson())
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.accept(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(5)));



	}

	@Test
	public void testSubmitOrder() throws Exception {
		createUser();
		String token = login();
		mvc.perform(
				post(new URI("/api/order/submit/test"))
				.header("Authorization", token))
				.andExpect(status().isOk());
	}

	@Test
	public void testGetHistory() throws Exception {
		createUser();
		String token = login();
		mvc.perform(
				get(new URI("/api/order/history/test"))
						.header("Authorization", token))
				.andExpect(status().isOk());
	}

	public void createUser() throws Exception {
		CreateUserRequest createUserRequest = getCreateUserRequest();
		User user = userRepository.findByUsername("test");

		if(user == null) {
			mvc.perform(
					post(new URI("/api/user/create"))
							.content(jsonCreateUserRequest.write(createUserRequest).getJson())
							.contentType(MediaType.APPLICATION_JSON_UTF8)
							.accept(MediaType.APPLICATION_JSON_UTF8))
					.andExpect(status().isOk());
		}

	}

	private String login() throws Exception {
		if(this.token == null) {
			LoginRequest apolinar = new LoginRequest();
			apolinar.setUsername("test");
			apolinar.setPassword("pass123");

			MvcResult mvcResult = mvc.perform(
					post(new URI("/login"))
							.content(jsonCreateLoginRequest.write(apolinar).getJson())
							.contentType(MediaType.APPLICATION_JSON_UTF8)
							.accept(MediaType.APPLICATION_JSON_UTF8))
					.andExpect(status().isOk())
					.andReturn();

			return mvcResult.getResponse().getHeader("Authorization");
		} else return this.token;

	}

	private CreateUserRequest getCreateUserRequest() {
		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setUsername("test");
		createUserRequest.setPassword("pass123");
		createUserRequest.setConfirmPassword("pass123");
		return createUserRequest;
	}
}
