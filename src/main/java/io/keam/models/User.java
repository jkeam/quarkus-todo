package io.keam.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.keam.utils.ModelUtils;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.panache.common.Page;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Entity
@Table(name = "users", indexes = { @Index(name = "usernameIndex", columnList = "username", unique = true) })
public class User extends PanacheEntity {
    @Column(length = 128, nullable = false)
    public String username;

    @JsonIgnore
    @Column(length = 32, nullable = false)
    public String password;

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Todo> todos = new ArrayList<>();

    /**
     * Find user by username
     * @param username The username of the user
     * @return user
     */
    public static Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(find("lower(username)", username.toLowerCase(Locale.US)).firstResult());
    }

    /**
     * Find all todos for a username.
     * @param username The username that owns the todos
     * @param pageIndex The page number they want
     * @param pageSize The size of the page
     * @param sortOrder The order to apply the sort
     * @return list of todos
     */
    public static List<Todo> findAllTodosForUsername(String username, int pageIndex, int pageSize, String sortOrder) {
        Optional<User> userOptional = findByUsername(username);
        if (userOptional.isEmpty()) {
            return new ArrayList<>();
        }

        User user = userOptional.get();
        return Todo.find("user_id", ModelUtils.createSort(sortOrder), user.getId())
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        if (this.username != null) {
            this.username = this.username.toLowerCase(Locale.US);
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void addTodo(Todo todo) {
        todos.add(todo);
        todo.setUser(this);
    }

    public void removeTodo(Todo todo) {
        todos.remove(todo);
        todo.setUser(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        return new EqualsBuilder().append(this.id, that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }
}
