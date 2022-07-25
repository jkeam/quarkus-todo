package io.keam.models;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * Todo item
 */
@Entity
public class Todo extends PanacheEntity {
	@Column(length = 60, nullable = false)
    public String title;

	@Column(columnDefinition = "boolean default false")
	public Boolean done;

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
}