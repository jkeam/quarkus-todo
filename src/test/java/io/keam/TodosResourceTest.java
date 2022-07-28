package io.keam;

import io.keam.models.Todo;
import io.keam.models.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestHTTPEndpoint(TodosResource.class)
public class TodosResourceTest implements QuarkusTestBeforeEachCallback {

    private String username = "testuser";

    @Transactional
    @Override
    public void beforeEach(QuarkusTestMethodContext context) {
        User.deleteAll();
        createUser();
        System.out.println("Executing " + context.getTestMethod());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "User")
    public void testList() {
        RestAssured
            .given()
                .contentType(ContentType.JSON)
            .when()
                .get("/todos")
            .then()
                .statusCode(200)
                .body(is("[]"));
    }

    @Test
    public void testInvalidUserList() {
        RestAssured.when().get("/todos").then().statusCode(401);
    }

    @Test
    public void testGet() {

    }

    @Test
    @TestTransaction
    @TestSecurity(user = "testuser", roles = "User")
    public void testCreate() {
        Todo todo = buildTodo();
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(todo)
                .when()
                    .post("/todos")
                .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .body(
                            "id", notNullValue(),
                            "title", is(todo.getTitle()),
                            "done", is(todo.isDone())
                    );
    }

    @Test
    public void testUpdate() {

    }

    @Test
    public void testDelete() {

    }

    private User createUser() {
        User user = new User();
        user.setPassword("password");
        user.setUsername(username);
        user.persistAndFlush();
        return user;
    }

    private Todo buildTodo() {
        Todo todo = new Todo();
        todo.setTitle("Brush Teeth");
        todo.setDone(false);
        return todo;
    }
}