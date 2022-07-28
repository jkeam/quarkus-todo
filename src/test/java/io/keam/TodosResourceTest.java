package io.keam;

import io.keam.models.Todo;
import io.keam.models.User;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestHTTPEndpoint(TodosResource.class)
public class TodosResourceTest {

    private static User user;
    private static Todo todo;

    @Transactional
    @BeforeAll
    public static void setup() {
        User newUser = new User();
        newUser.setPassword("password");
        newUser.setUsername("testuser");
        newUser.addTodo(new Todo("Walk Dog"));
        newUser.persistAndFlush();

        user = newUser;
        todo = user.getTodos().get(0);
    }

    @Test
    @TestTransaction
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "testuser")
    })
    public void testList() {
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .when()
                .get()
            .then()
                .statusCode(HttpStatus.SC_OK)
                .body("",
                    hasItem(
                        allOf(
                            hasEntry("title", todo.getTitle().toString())
                        )
                    )
                );

    }

    @Test
    public void testInvalidUserList() {
        RestAssured.when().get("").then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "testuser")
    })
    public void testGet() {
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .when()
                .get(String.format("/%d", todo.getId()))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        "id", notNullValue(),
                        "title", is(todo.getTitle()),
                        "done", is(todo.isDone())
                );
    }

    @Test
    @TestTransaction
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "testuser")
    })
    public void testCreate() {
//        PanacheMock.mock(User.class);
//        user.persistAndFlush();
//        Mockito.when(User.findByUsername("testuser")).thenReturn(Optional.of(user));
        Todo todo = new Todo("Brush Teeth");
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(todo)
                .when()
                    .post("")
                .then()
                    .statusCode(HttpStatus.SC_CREATED);
//        PanacheMock.verify(User.class, Mockito.times(1)).findByUsername("testuser");
    }

    @Test
    @TestTransaction
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "testuser")
    })
    public void testUpdate() {
        Todo newTodo = new Todo("Do not brush teeth");
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(newTodo)
                .when()
                    .put(String.format("/%d", todo.getId()))
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body(
                            "id", notNullValue(),
                            "title", is(newTodo.getTitle()),
                            "done", is(newTodo.isDone())
                    );
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "testuser")
    })
    public void testDelete() {
        QuarkusTransaction.begin();
        Todo newTodo = new Todo("To delete");
        newTodo.setUser(user);
        newTodo.persistAndFlush();
        QuarkusTransaction.commit();
//        Todo savedTodo = user.getTodos().stream().filter(td -> !td.getId().equals(todo.getId())).findFirst().get();
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .when()
                    .delete(String.format("/%d", newTodo.getId()))
                .then()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}