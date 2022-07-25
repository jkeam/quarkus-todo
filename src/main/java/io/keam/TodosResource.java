package io.keam;

import io.keam.models.Todo;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;

/**
 * Generates Todos Rest Endpoints
 */
public interface TodosResource extends PanacheEntityResource<Todo, Long> {
}