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
        Uni<Todo> todoUni = Todo.findById(id);
        return todoUni.chain(todo -> {
            if (todo != null && todo.isOwnedByUsername(username)) {
                return Uni.createFrom().item(todo);
            }
            return Uni.createFrom().nullItem();
        });
    }

    @POST
    public Uni<Response> create(Todo todo) {
        if (todo == null) {
            throw new BadRequestException();
        }
        Uni<Optional<User>> userUni = User.findByUsername(username);
        return userUni.chain(optionalUser -> {
            User user = optionalUser.orElseThrow(BadRequestException::new);
            user.addTodo(todo);
            return Panache.withTransaction(todo::persist)
                    .replaceWith(Uni.createFrom().item(Response.ok(todo).status(Response.Status.CREATED).build()));
        });
    }

    @PUT
    @Path("/{id}")
    @ReactiveTransactional
    public Uni<Response> update(Long id, Todo todo) {
        if (todo == null) {
            throw new WebApplicationException("Invalid request.", BAD_REQUEST);
        }

//        return Panache.withTransaction(() -> Todo.<Todo> findById(id, LockModeType.PESSIMISTIC_WRITE)
//                // If entity exists then update it
//                .onItem().ifNotNull().invoke(entity -> {
//                    entity.setTitle(todo.getTitle());
//                    System.out.println("here");
//                })
//                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
//                // If entity not found return the appropriate response
//                .onItem().ifNull().continueWith(() -> Response.ok().status(NOT_FOUND).build() )
//        );

//       return Panache
//                .withTransaction(() -> Todo.<Todo> findById(id)
//                    .onItem().ifNotNull().invoke(entity -> entity.setTitle(todo.getTitle()))
//                )
//                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
//                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);


//        return Uni.createFrom().item(() -> {
            return Todo.<Todo> findById(id).onItem().transform(entity -> {
                if (entity == null || !entity.isOwnedByUsername(username)) {
                    return Response.ok().status(NOT_FOUND).build();
                }
                entity.setTitle(todo.getTitle());
                entity.setDone(todo.isDone());
                return Response.ok(entity).build();
            });
//        });


//        return Panache.withTransaction(() -> Todo.<Todo> findById(id)
//                .chain(entity -> {
//                    if (entity == null) {
//                        return Uni.createFrom().item(null);
//                    }
//                    entity.setTitle(todo.getTitle());
//                    entity.setDone(todo.isDone());
//                    return Uni.createFrom().item(entity);
//                })
//                .map(entity -> {
//                    if (entity == null) {
//                        return Response.ok().status(NOT_FOUND).build();
//                    }
//                    return Response.ok(entity).build();
//                })
//        );
    }

    @DELETE
    @Path("/{id}")
    @ReactiveTransactional
    public Uni<Response> delete(Long id) {
        /*
        return Todo.<Todo> findById(id).onItem().transform(entity -> {
            if (entity == null || !entity.isOwnedByUsername(username)) {
                return Response.ok().status(NOT_FOUND).build();
            }
            return entity.delete().replaceWith(Response.ok().status(NO_CONTENT).build());
        });
        */
        return Panache.withTransaction(() -> Todo.<Todo> findById(id)
                .chain(entity -> {
                    if (entity == null || !entity.isOwnedByUsername(username)) {
                        return Uni.createFrom().item(Response.ok().status(NOT_FOUND).build());
                    }
                    Response response = Response.ok().status(NO_CONTENT).build();
                    return entity.delete().chain(() -> Uni.createFrom().item(response));
                })
        );
    }
}