package io.keam.models;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Todo item
 */
@Entity
@Table(name = "todos")
public class Todo extends PanacheEntity {
	@Column(length = 60, nullable = false)
    public String title;

	@Column(columnDefinition = "boolean default false")
	public Boolean done;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private User user;

	/**
	 * Check if username owns this todo.
	 * @param username The username to check
	 * @return boolean
	 */
	public boolean isOwnedByUsername(String username) {
		return (username != null && username.equalsIgnoreCase(user.getUsername()));
	}

    public Todo() {
    }

	public Todo(String title) {
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean isDone() {
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Todo)) return false;
		Todo that = (Todo) o;
		return new EqualsBuilder().append(this.id, that.getId()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}
}