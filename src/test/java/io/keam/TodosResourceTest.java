package io.keam;

import io.keam.models.Todo;
import io.keam.models.User;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestHTTPEndpoint(TodosResource.class)
public class TodosResourceTest {

    private static User user;
    private static Todo todo;

    @InjectMock
    Session session;

    @BeforeEach
    public void setup() {
        User newUser = new User();
        newUser.setPassword("password");
        newUser.setUsername("testuser");
        newUser.addTodo(new Todo("Walk Dog"));
        user = newUser;
        todo = newUser.getTodos().get(0);
        user.setId(1L);
        todo.setId(1L);
    }

    private void mockTodoSave() {
        Query mockQuery = Mockito.mock(Query.class);
        Mockito.doNothing().when(session).persist(Mockito.any(Todo.class));
        Mockito.when(session.createQuery(Mockito.anyString())).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(0L);
    }

    private void mockTodoDelete() {
        Query mockQuery = Mockito.mock(Query.class);
        Mockito.doNothing().when(session).remove(Mockito.any(Todo.class));
        Mockito.when(session.createQuery(Mockito.anyString())).thenReturn(mockQuery);
        Mockito.when(mockQuery.getSingleResult()).thenReturn(0l);
    }

    private void mockUserGet() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByUsername("testuser")).thenReturn(Optional.of(user));
    }

    private void mockTodoGet() {
        PanacheMock.mock(Todo.class);
        Mockito.when(Todo.findById(1L)).thenReturn(todo);
    }

    private void mockTodosGet() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findAllTodosForUsername("testuser", 0, 20, "desc")).thenReturn(List.of(todo));
    }

    private void verifyTodoSave() {
        Mockito.verify(session, Mockito.times(1)).persist(Mockito.any(Todo.class));
        Mockito.reset(session);
    }

    private void verifyTodoDelete() {
        Mockito.verify(session, Mockito.times(1)).remove(Mockito.any(Todo.class));
        Mockito.reset(session);
    }

    private void verifyUserGet() {
        PanacheMock.verify(User.class, Mockito.times(1)).findByUsername("testuser");
        PanacheMock.reset();
    }

    private void verifyTodosGet() {
        PanacheMock.verify(User.class, Mockito.times(1)).findAllTodosForUsername("testuser", 0, 20, "desc");
        PanacheMock.reset();
    }

    private void verifyTodoGet() {
        PanacheMock.verify(Todo.class, Mockito.times(1)).findById(1L);
        PanacheMock.reset();
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "testuser")
    })
    public void testList() {
        mockTodosGet();
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
                            hasEntry("title", todo.getTitle())
                        )
                    )
                );
        verifyTodosGet();
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
        mockTodoGet();
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .when()
                .get("/1")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        "title", is(todo.getTitle()),
                        "done", is(todo.isDone())
                );
        verifyTodoGet();
    }

    @Test
    @TestTransaction
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "testuser")
    })
    public void testCreate() {
        mockUserGet();
        mockTodoSave();
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
        verifyUserGet();
        verifyTodoSave();
    }

    @Test
    @TestTransaction
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "testuser")
    })
    public void testUpdate() {
        mockTodoGet();
        Todo newTodo = new Todo("Do not brush teeth");
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(newTodo)
                .when()
                    .put("/1")
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body(
                            "title", is(newTodo.getTitle()),
                            "done", is(newTodo.isDone())
                    );
        verifyTodoGet();
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "testuser")
    })
    public void testDelete() {
        mockTodoGet();
        mockTodoDelete();
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .when()
                    .delete("/1")
                .then()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        verifyTodoGet();
        verifyTodoDelete();
    }
}