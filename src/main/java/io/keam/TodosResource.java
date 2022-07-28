package io.keam;

import io.keam.models.Todo;
import io.keam.models.User;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
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
@RolesAllowed({ "User" })
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodosResource {

    private static final Logger LOG = Logger.getLogger(TodosResource.class);

//    @Inject
//    JsonWebToken jwt;

    @Claim(standard = Claims.upn)
    String username;

    @GET
    public List<Todo> list(@QueryParam("sort") @DefaultValue("desc") String sortOrder,
                           @QueryParam("page") @DefaultValue("0") int pageIndex,
                           @QueryParam("size") @DefaultValue("20") int pageSize) {
        if (LOG.isDebugEnabled()) {
            LOG.debugv("Username: {0}", username);
        }
        return User.findAllTodosForUsername(username, pageIndex, pageSize, sortOrder);
    }

    @GET
    @Path("/{id}")
    public Todo get(Long id) {
        Todo todo = Todo.findById(id);
        if (todo != null && todo.isOwnedByUsername(username)) {
            return todo;
        }
        return null;
    }

    @POST
    @Transactional
    public Response create(Todo todo) {
        if (todo == null) {
            throw new BadRequestException();
        }
        User user = User.findByUsername(username);
        user.addTodo(todo);
        todo.persist();
        return Response.created(URI.create("/todos/" + todo.getId())).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Todo update(Long id, Todo todo) {
        Todo entity = Todo.findById(id);
        if (entity == null || !entity.isOwnedByUsername(username)) {
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
        if (entity == null || !entity.isOwnedByUsername(username)) {
            throw new NotFoundException();
        }
        entity.delete();
    }
}