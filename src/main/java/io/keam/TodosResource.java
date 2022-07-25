package io.keam;

import io.keam.models.Todo;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * Generates Todos Rest Endpoints
 */
@Path("/todos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodosResource {
    @GET
    public List<Todo> list() {
        return Todo.listAll();
    }

    @GET
    @Path("/{id}")
    public Todo get(Long id) {
        return Todo.findById(id);
    }

    @POST
    @Transactional
    public Response create(Todo todo) {
        if (todo == null) {
            throw new BadRequestException();
        }
        todo.persist();
        return Response.created(URI.create("/persons/" + todo.getId())).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Todo update(Long id, Todo todo) {
        Todo entity = Todo.findById(id);
        if(entity == null) {
            throw new NotFoundException();
        }

        entity.setDone(todo.isDone());
        entity.setTitle(todo.getTitle());
        return entity;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(Long id) {
        Todo entity = Todo.findById(id);
        if(entity == null) {
            throw new NotFoundException();
        }
        entity.delete();
    }

}