package io.keam;

import io.keam.models.Todo;
import io.keam.models.User;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.persistence.LockModeType;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.*;

/**
 * Generates Todos Rest Endpoints
 */
@Path("/todos")
@RolesAllowed({"User"})
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodosResource {

    private static final Logger LOG = Logger.getLogger(TodosResource.class);

    @Claim(standard = Claims.upn)
    String username;

    @GET
    public Uni<List<Todo>> list(@QueryParam("sort") @DefaultValue("desc") String sortOrder,
                                @QueryParam("page") @DefaultValue("0") int pageIndex,
                                @QueryParam("size") @DefaultValue("20") int pageSize) {
        if (LOG.isDebugEnabled()) {
            LOG.debugv("Username: {0}", username);
        }
        return User.findAllTodosForUsername(username, pageIndex, pageSize, sortOrder);
    }

    @GET
    @Path("/{id}")
    public Uni<Todo> get(Long id) {
        return Todo.<Todo> findById(id).chain(todo -> {
            if (!isOwnedByUser(todo)) {
                return Uni.createFrom().nullItem();
            }
            return Uni.createFrom().item(todo);
        });
    }

    @POST
    @ReactiveTransactional
    public Uni<Response> create(Todo todo) {
        if (todo == null) {
            throw new BadRequestException();
        }
        return User.findByUsername(username).chain(optionalUser -> {
            User user = optionalUser.orElseThrow(BadRequestException::new);
            user.addTodo(todo);
            return todo.persist().<Response>replaceWith(Response.ok().status(NO_CONTENT)::build);
        });
    }

    @PUT
    @Path("/{id}")
    @ReactiveTransactional
    public Uni<Response> update(Long id, Todo todo) {
        if (todo == null) {
            throw new WebApplicationException("Invalid request.", BAD_REQUEST);
        }
        return Todo.<Todo> findById(id).map(entity -> {
            if (!isOwnedByUser(entity)) {
                return Response.ok().status(NOT_FOUND).build();
            }
            entity.setTitle(todo.getTitle());
            entity.setDone(todo.isDone());
            return Response.ok(entity).build();
        });
    }

    @DELETE
    @Path("/{id}")
    @ReactiveTransactional
    public Uni<Response> delete(Long id) {
        return Todo.<Todo>findById(id).<Response>chain(entity -> {
            if (!isOwnedByUser(entity)) {
                return Uni.createFrom().item(Response.ok().status(NOT_FOUND).build());
            }
            return entity.delete().<Response>replaceWith(Response.ok().status(NO_CONTENT)::build);
        });
    }

    private boolean isOwnedByUser(Todo todo) {
        return todo != null && todo.isOwnedByUsername(username);
    }
}